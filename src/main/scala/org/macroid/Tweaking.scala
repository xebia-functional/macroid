package org.macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import org.macroid.util.{ Ui, Effector }

/** A Tweak is something that mutates a widget */
case class Tweak[-W <: View](f: W ⇒ Unit) {
  def apply(w: W) = f(w)
}

object Tweak {
  /** A tweak that does nothing */
  def blank[W <: View] = Tweak[W](x ⇒ ())
}

@implicitNotFound("Don't know how to tweak ${W} with ${T}. Try importing an instance of TweakableWith[${W}, ${T}].") /** A typeclass for 'tweakable' relation */
trait TweakableWith[W, T] {
  /** Warning: this method expects to be called from the UI thread */
  def tweakWith(w: W, t: T): Unit
}

object TweakableWith {
  // format: OFF

  implicit def `Widget is tweakable with Tweak`[W <: View, T <: Tweak[W]] =
    new (W TweakableWith T) {
      def tweakWith(w: W, t: T) = t(w)
    }

  implicit def `Widget is tweakable with Effector`[W <: View, F[+_], T](implicit effector: Effector[F], tweakable: W TweakableWith T) =
    new (W TweakableWith F[T]) {
      def tweakWith(w: W, f: F[T]) = effector.foreach(f)(t ⇒ tweakable.tweakWith(w, t))
    }

  implicit def `Effector is tweakable`[W, F[+_], T](implicit effector: Effector[F], tweakable: W TweakableWith T) =
    new (F[W] TweakableWith T) {
      def tweakWith(f: F[W], t: T) = effector.foreach(f)(w ⇒ tweakable.tweakWith(w, t))
    }

  // format: ON
}

/** This trait defines tweaking operations */
private[macroid] trait Tweaking {
  /** Combining tweaks */
  implicit class TweakAddition[W <: View](t: Tweak[W]) {
    /** Combine (sequence) with another tweak */
    def +[W1 <: W](other: Tweak[W1]) = Tweak[W1] { x ⇒ t(x); other(x) }
  }

  // format: OFF

  /** Tweaking operator */
  implicit class UiTweakingOps[W](ui: Ui[W]) {
    /** Apply a tweak */
    def ~>[T](t: T)(implicit tweakable: W TweakableWith T): Ui[W] = ui map { w ⇒ tweakable.tweakWith(w, t); w }
  }

  /** Tweaking operator */
  implicit class TweakingOps[W](w: W) {
    /** Apply a tweak */
    def ~>[T](t: T)(implicit tweakable: W TweakableWith T): Ui[W] = Ui { tweakable.tweakWith(w, t); w }
  }

  // format: ON
}

object Tweaking extends Tweaking
