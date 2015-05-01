package macroid

import scala.language.higherKinds
import android.view.{ ViewGroup, View }
import scala.annotation.implicitNotFound
import macroid.util.Effector

@implicitNotFound("Don't know how to tweak ${W} with ${T}. Try importing an instance of CanTweak[${W}, ${T}, ...].") /** A typeclass for 'tweakable' relation */
trait CanTweak[W, T, R] {
  def tweak(w: W, t: T): Ui[R]
}

object CanTweak {
  implicit def `Widget is tweakable with Tweak`[W <: View, T <: Tweak[W]]: CanTweak[W, T, W] =
    new CanTweak[W, T, W] {
      def tweak(w: W, t: T) = t(w).withResult(w)
    }

  implicit def `Widget is tweakable with Snail`[W <: View, S <: Snail[W]]: CanTweak[W, S, W] =
    new CanTweak[W, S, W] {
      def tweak(w: W, s: S) = s(w).withResult(w)
    }

  implicit def `Layout is tweakable with Transformer`[L <: ViewGroup]: CanTweak[L, Transformer, L] =
    new CanTweak[L, Transformer, L] {
      def tweak(l: L, t: Transformer) = t(l).withResult(l)
    }

  implicit def `Widget is tweakable with Effector`[W <: View, F[+_], T, R](implicit effector: Effector[F], canTweak: CanTweak[W, T, R]): CanTweak[W, F[T], W] =
    new CanTweak[W, F[T], W] {
      def tweak(w: W, f: F[T]) = Ui { effector.foreach(f)(t ⇒ canTweak.tweak(w, t)); w }
    }

  implicit def `Effector is tweakable`[W, F[+_], T, R](implicit effector: Effector[F], canTweak: CanTweak[W, T, R]): CanTweak[F[W], T, F[W]] =
    new CanTweak[F[W], T, F[W]] {
      def tweak(f: F[W], t: T) = Ui { effector.foreach(f)(w ⇒ canTweak.tweak(w, t)); f }
    }

  implicit def `Ui is tweakable`[W, T, R](implicit canTweak: CanTweak[W, T, R]): CanTweak[Ui[W], T, W] =
    new CanTweak[Ui[W], T, W] {
      def tweak(ui: Ui[W], t: T) = ui flatMap { w ⇒ canTweak.tweak(w, t).withResult(w) }
    }
}

/** This trait defines the tweaking operator (<~) */
private[macroid] trait Tweaking {
  /** Tweaking operator */
  implicit class TweakingOps[W](w: W) {
    /** Apply a tweak */
    def <~[T, R](t: T)(implicit canTweak: CanTweak[W, T, R]): Ui[R] = canTweak.tweak(w, t)
  }
}
