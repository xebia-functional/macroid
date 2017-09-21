package macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

@implicitNotFound(
  "Don't know how to snail ${W} with ${S}. Try importing an instance of CanSnail[${W}, ${S}, ...]. It is also possible that you are missing an implicit scala.concurrent.ExecutionContext")
trait CanSnail[W, -S, R] {
  def snail(w: W, s: S): Ui[Future[R]]
}

object CanSnail {
  implicit def `Widget is snailable with Snail`[W <: View](
      implicit ec: ExecutionContext): CanSnail[W, Snail[W], W] =
    new CanSnail[W, Snail[W], W] {
      def snail(w: W, s: Snail[W]) = s(w).withResultAsync(w)
    }

  implicit def `Widget is snailable with Future[Tweak]`[W <: View]: CanSnail[
    W,
    Future[Tweak[W]],
    W] =
    new CanSnail[W, Future[Tweak[W]], W] {
      def snail(w: W, ft: Future[Tweak[W]]) = Ui {
        ft.mapUi(t ⇒ t(w).withResult(w))
      }
    }

  implicit def `Widget is snailable with Option`[W <: View, S, R](
      implicit ec: ExecutionContext,
      canSnail: CanSnail[W, S, R]): CanSnail[W, Option[S], W] =
    new CanSnail[W, Option[S], W] {
      def snail(w: W, o: Option[S]) = o.fold(Ui(Future.successful(w))) { s ⇒
        canSnail.snail(w, s).withResultAsync(w)
      }
    }

  implicit def `Ui is snailable`[W, S, R](
      implicit ec: ExecutionContext,
      canSnail: CanSnail[W, S, R]): CanSnail[Ui[W], S, W] =
    new CanSnail[Ui[W], S, W] {
      def snail(ui: Ui[W], s: S) = ui flatMap { w ⇒
        canSnail.snail(w, s).withResultAsync(w)
      }
    }

  implicit def `Option is snailable`[W, S, R](
      implicit ec: ExecutionContext,
      canSnail: CanSnail[W, S, R]): CanSnail[Option[W], S, Option[W]] =
    new CanSnail[Option[W], S, Option[W]] {
      def snail(o: Option[W], s: S) = o.fold(Ui(Future.successful(o))) { w ⇒
        canSnail.snail(w, s).withResultAsync(o)
      }
    }

  implicit def `Widget is snailable with Future`[W <: View, S, R](
      implicit ec: ExecutionContext,
      canSnail: CanSnail[W, S, R]): CanSnail[W, Future[S], W] =
    new CanSnail[W, Future[S], W] {
      def snail(w: W, f: Future[S]) = Ui {
        f.flatMapUi(s ⇒ canSnail.snail(w, s).withResultAsync(w))
      }
    }

  implicit def `Future is snailable`[W, S, R](
      implicit ec: ExecutionContext,
      canSnail: CanSnail[W, S, R]): CanSnail[Future[W], S, W] =
    new CanSnail[Future[W], S, W] {
      def snail(f: Future[W], s: S) = Ui {
        f.flatMapUi(w ⇒ canSnail.snail(w, s).withResultAsync(w))
      }
    }

  implicit def `Widget is snailable with TraversableOnce`[W <: View, S, R](
      implicit canSnail: CanSnail[W, S, R]): CanSnail[W, TraversableOnce[S], W] =
    new CanSnail[W, TraversableOnce[S], W] {
      def snail(w: W, l: TraversableOnce[S]) =
        Ui(async {
          val it = l.toIterator
          while (it.hasNext) {
            // we can call Ui.get, since we are already inside the UI thread
            await(canSnail.snail(w, it.next()).get)
          }
          w
        }(UiThreadExecutionContext))
    }

  implicit def `TraversableOnce is snailable`[W, S, R, C[X] <: TraversableOnce[X]](
      implicit canSnail: CanSnail[W, S, R]): CanSnail[C[W], S, C[W]] =
    new CanSnail[C[W], S, C[W]] {
      def snail(l: C[W], s: S) =
        Ui(async {
          val it = l.toIterator
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
    def <~~[T, R](t: T)(implicit canSnail: CanSnail[W, T, R]): Ui[Future[R]] =
      canSnail.snail(w, t)
  }
}
