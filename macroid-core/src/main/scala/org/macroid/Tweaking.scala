package org.macroid

import scala.language.experimental.macros
import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import org.macroid.util.{ MacroUtils, Effector }
import scala.reflect.macros.{ Context ⇒ MacroContext }

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
  def tweakWith(w: W, t: T): Unit
}

object TweakableWith {
  // format: OFF

  implicit def `Widget is tweakable with Tweak`[W <: View, T <: Tweak[W]] =
    new (W TweakableWith T) {
      def tweakWith(w: W, t: T) = UiThreading.fireUi(t(w))
    }

  implicit def `Widget is tweakable with Effector`[W <: View, F[+_], T](implicit effector: Effector[F], tweakableWith: W TweakableWith T) =
    new (W TweakableWith F[T]) {
      def tweakWith(w: W, f: F[T]) = effector.foreach(f)(t ⇒ tweakableWith.tweakWith(w, t))
    }

  implicit def `Effector is tweakable`[W, F[+_], T](implicit effector: Effector[F], tweakableWith: W TweakableWith T) =
    new (F[W] TweakableWith T) {
      def tweakWith(f: F[W], t: T) = effector.foreach(f)(w ⇒ tweakableWith.tweakWith(w, t))
    }

  // format: ON
}

@implicitNotFound("Could not infer the type of the widget being tweaked. Please provide it explicitly.") /** The type of the widget(s) being tweaked */
trait WidgetType {
  type W <: View
}

/** This trait defines tweaking operations */
private[macroid] trait Tweaking {
  /** Combining tweaks */
  implicit class TweakAddition[W <: View](t: Tweak[W]) {
    /** Combine (sequence) with another tweak */
    def +[W1 <: W](other: Tweak[W1]) = Tweak[W1] { x ⇒ t(x); other(x) }
  }

  // format: OFF

  /** Tweaking operator and its aliases */
  implicit class TweakingOps[W](w: W) {
    /** Apply a tweak on the UI thread */
    def ~>[T](t: T)(implicit tweakableWith: W TweakableWith T): W = { tweakableWith.tweakWith(w, t); w }
    /** Apply a tweak on the UI thread (unicode alias for `~>`) */
    def ⇝[T](t: T)(implicit tweakableWith: W TweakableWith T): W = { tweakableWith.tweakWith(w, t); w }
    /** Apply a tweak on the UI thread (plain text alias for `~>`) */
    def tweakWith[T](t: T)(implicit tweakableWith: W TweakableWith T): W = { tweakableWith.tweakWith(w, t); w }
  }

  // format: ON

  @deprecated("To be replaced with http://github.com/dsl-paradise/dsl-paradise", "2.0") /** A helper class to make tweaks */
  class TweakMaker[W <: View] {
    def ~(f: W ⇒ Unit) = Tweak(f)
    def doing(f: W ⇒ Unit) = Tweak(f)
  }

  @deprecated("To be replaced with http://github.com/dsl-paradise/dsl-paradise", "2.0") /** Create a tweak, inferring the widget type from the context */
  def tweak(implicit wtp: WidgetType) = new TweakMaker[wtp.W]

  @deprecated("To be replaced with http://github.com/dsl-paradise/dsl-paradise", "2.0") /** Use to provide widget type explicitly */
  def W[X <: View] = new WidgetType { type W = X }

  /** Infer widget type */
  implicit def inferWidgetType: WidgetType = macro TweakingMacros.inferWidgetType
}

object Tweaking extends Tweaking

object TweakingMacros {
  def inferWidgetType(c: MacroContext) = {
    import c.universe._
    val tilde: PartialFunction[Tree, Type] = {
      // TODO: bring more sanity!
      case q"$x ~> $y" ⇒ c.typeCheck(x).tpe
      case q"$x ⇝ $y" ⇒ c.typeCheck(x).tpe
      case q"$x tweakWith $y" ⇒ c.typeCheck(x).tpe
    }
    val tp = MacroUtils.fromImmediateParentTree(c)(tilde).flatMap(t ⇒ MacroUtils.innerType(c)(t))
    c.Expr[WidgetType](writeWidgetType(c)(tp.getOrElse(typeOf[View])))
  }

  /** Create an instance of WidgetType */
  def writeWidgetType(c: MacroContext)(tp: c.Type) = {
    import c.universe._
    q"new ${typeOf[WidgetType]} { type W = $tp }"
  }

  /** Get W from WidgetType */
  def readWidgetType(c: MacroContext)(wtp: c.Expr[WidgetType]) = {
    import c.universe._
    c.typeCheck(q"val x = $wtp; val y: x.W = ???; y").tpe
  }
}
