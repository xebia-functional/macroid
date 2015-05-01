package macroid

import scala.concurrent.Future
import scala.language.higherKinds
import scala.language.existentials
import android.view.View
import scala.annotation.implicitNotFound

@implicitNotFound("Don't know how to excerpt from ${W} with ${E}. Try importing an instance of CanExcerpt[${W}, ${E}, ...].") /** A typeclass for 'excerptable' relation */
trait CanExcerpt[W, -E, R] {
  def excerpt(w: W, e: E): Ui[R]
}

object CanExcerpt {
  implicit def `Widget is excerptable with Excerpt`[W <: View, R]: CanExcerpt[W, Excerpt[W, R], R] =
    new CanExcerpt[W, Excerpt[W, R], R] {
      def excerpt(w: W, e: Excerpt[W, R]) = e(w)
    }

  implicit def `Widget is excerptable with Future[Excerpt]`[W <: View, R]: CanExcerpt[W, Future[Excerpt[W, R]], Future[R]] =
    new CanExcerpt[W, Future[Excerpt[W, R]], Future[R]] {
      def excerpt(w: W, ft: Future[Excerpt[W, R]]) = Ui {
        ft.mapUi(e ⇒ e(w))
      }
    }

  implicit def `Option is excerptable`[W, E, R](implicit canExcerpt: CanExcerpt[W, E, R]): CanExcerpt[Option[W], E, Option[R]] =
    new CanExcerpt[Option[W], E, Option[R]] {
      def excerpt(o: Option[W], e: E) = o.fold(Ui(Option.empty[R])) { w ⇒
        canExcerpt.excerpt(w, e).map(Some.apply)
      }
    }

  implicit def `Ui is excerptable`[W, E, R](implicit canExcerpt: CanExcerpt[W, E, R]): CanExcerpt[Ui[W], E, R] =
    new CanExcerpt[Ui[W], E, R] {
      def excerpt(ui: Ui[W], e: E) = ui flatMap { w ⇒ canExcerpt.excerpt(w, e) }
    }
}

/** This trait defines the excerpting operator (~>) */
private[macroid] trait Excerpting {
  /** Excerpting operator */
  implicit class ExcerptingOps[W](w: W) {
    /** Excerpt a value */
    def ~>[E, R](e: E)(implicit canExcerpt: CanExcerpt[W, E, R]): Ui[R] = canExcerpt.excerpt(w, e)
  }
}
