package org.macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import org.macroid.util.{ Effector, Effectors }

/** A Tweak is something that mutates a View */
case class Tweak[-A <: View](f: A ⇒ Unit) {
  def apply(v: A) = f(v)
}

object Tweak {
  /** A tweak that does nothing */
  def blank[A <: View] = Tweak[A](x ⇒ ())
}

@implicitNotFound("Don't know how to tweak ${V} with ${T}. Try importing an instance of TweakableWith[${V}, ${T}]")
trait TweakableWith[V, T] {
  def tweakWith(v: V, t: T): Unit
}

/** This trait defines tweaking operations */
trait Tweaking extends Effectors {
  /** Combining tweaks */
  implicit class TweakAddition[A <: View](t: Tweak[A]) {
    /** Combine (sequence) with another tweak */
    def +[B <: A](other: Tweak[B]) = Tweak[B] { x ⇒ t(x); other(x) }
  }

  /** An alias for Tweak.apply */
  def tweak[V <: View](f: V ⇒ Unit) = Tweak(f)

  // format: OFF

  /** Tweaking operator and its aliases */
  implicit class TweakingOps[V](v: V) {
    /** Apply a tweak on the UI thread */
    def ~>[T](t: T)(implicit tweakableWith: V TweakableWith T): V = { tweakableWith.tweakWith(v, t); v }
    /** Apply a tweak on the UI thread (unicode alias for `~>`) */
    def ⇝[T](t: T)(implicit tweakableWith: V TweakableWith T): V = { tweakableWith.tweakWith(v, t); v }
    /** Apply a tweak on the UI thread (plain text alias for `~>`) */
    def tweakWith[T](t: T)(implicit tweakableWith: V TweakableWith T): V = { tweakableWith.tweakWith(v, t); v }
  }

  /** A widget can be tweaked with a Tweak */
  implicit def tweakableWithTweak[V <: View, T <: Tweak[V]] =
    new (V TweakableWith T) {
      def tweakWith(v: V, t: T) = UiThreading.fireUi(t(v))
    }

  /** A widget can be tweaked with an Effector */
  implicit def tweakableWithEffector[V <: View, F[+_], T](implicit effector: Effector[F], tweakableWith: V TweakableWith T) =
    new (V TweakableWith F[T]) {
      def tweakWith(v: V, f: F[T]) = effector.foreach(f)(t ⇒ tweakableWith.tweakWith(v, t))
    }

  /** Effector can be tweaked with something */
  implicit def effectorTweakable[V, F[+_], T](implicit effector: Effector[F], tweakableWith: V TweakableWith T) =
    new (F[V] TweakableWith T) {
      def tweakWith(f: F[V], t: T) = effector.foreach(f)(v ⇒ tweakableWith.tweakWith(v, t))
    }

  // format: ON
}

object Tweaking extends Tweaking
