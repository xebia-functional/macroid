package macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import scala.async.Async._
import scala.concurrent.{ ExecutionContext, Future }
import macroid.util.{ UiThreadExecutionContext, Ui }

@implicitNotFound("Don't know how to snail ${W} with ${S}. Try importing an instance of CanSnail[${W}, ${S}, ...]")
trait CanSnail[W, S, R] {
  def snail(w: W, s: S): Ui[Future[R]]
}

object CanSnail {
  import UiThreading._

  implicit def `Widget is snailable with Snail`[W <: View, S <: Snail[W]](implicit ec: ExecutionContext) =
    new CanSnail[W, S, W] {
      def snail(w: W, s: S) = Ui { s(w).map(_ ⇒ w) }
    }

  implicit def `Widget is snailable with Future[Tweak]`[W <: View, T <: Tweak[W]](implicit ec: ExecutionContext) =
    new CanSnail[W, Future[T], W] {
      def snail(w: W, ft: Future[T]) = Ui { ft mapUi { t ⇒ t(w); w } }
    }

  implicit def `Widget is snailable with Option`[W <: View, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[W, Option[S], W] {
      def snail(w: W, o: Option[S]) = o.fold(Ui(Future.successful(w))) { s ⇒
        canSnail.snail(w, s).map(f ⇒ f.map(_ ⇒ w))
      }
    }

  implicit def `Ui is snailable`[W, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[Ui[W], S, W] {
      def snail(ui: Ui[W], s: S) = ui flatMap { w ⇒ canSnail.snail(w, s).map(f ⇒ f.map(_ ⇒ w)) }
    }

  implicit def `Option is snailable`[W, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[Option[W], S, Option[W]] {
      def snail(o: Option[W], s: S) = o.fold(Ui(Future.successful(o))) { w ⇒
        canSnail.snail(w, s).map(f ⇒ f.map(_ ⇒ o))
      }
    }

  implicit def `Widget is snailable with Future`[W <: View, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[W, Future[S], W] {
      // we can call Ui.get, since we are already inside the UI thread
      def snail(w: W, f: Future[S]) = Ui(f.flatMapUi(s ⇒ canSnail.snail(w, s).get.map(_ ⇒ w)))
    }

  implicit def `Future is snailable`[W, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[Future[W], S, W] {
      // we can call Ui.get, since we are already inside the UI thread
      def snail(f: Future[W], s: S) = Ui(f.flatMapUi(w ⇒ canSnail.snail(w, s).get.map(_ ⇒ w)))
    }

  implicit def `Widget is snailable with List`[W <: View, S, R](implicit canSnail: CanSnail[W, S, R]) =
    new CanSnail[W, List[S], W] {
      def snail(w: W, l: List[S]) = Ui(async {
        val it = l.iterator
        while (it.hasNext) {
          // we can call Ui.get, since we are already inside the UI thread
          await(canSnail.snail(w, it.next()).get)
        }
        w
      }(UiThreadExecutionContext))
    }

  implicit def `List is snailable`[W, S, R](implicit canSnail: CanSnail[W, S, R]) =
    new CanSnail[List[W], S, List[W]] {
      def snail(l: List[W], s: S) = Ui(async {
        val it = l.iterator
        while (it.hasNext) {
          // we can call Ui.get, since we are already inside the UI thread
          await(canSnail.snail(it.next(), s).get)
        }
        l
      }(UiThreadExecutionContext))
    }
}

/** This trait defines the snailing operator (<~~) */
private[macroid] trait Snailing {
  /** Snailing operator */
  implicit class SnailingOps[W](w: W) {
    /** Apply a snail */
    def <~~[T, R](t: T)(implicit canSnail: CanSnail[W, T, R]): Ui[Future[R]] = canSnail.snail(w, t)
  }
}

object Snailing extends Snailing
