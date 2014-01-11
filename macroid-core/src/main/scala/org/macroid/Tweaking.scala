package org.macroid

import scala.language.experimental.macros
import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import org.macroid.util.{ MacroUtils, Effector, Effectors }
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

@implicitNotFound("Could not infer the type of the widget being tweaked. Please provide it explicitly.") /** The type of the widget(s) being tweaked */
trait WidgetType {
  type W <: View
}

/** This trait defines tweaking operations */
trait Tweaking extends Effectors {
  /** Combining tweaks */
  implicit class TweakAddition[W <: View](t: Tweak[W]) {
    /** Combine (sequence) with another tweak */
    def +[V <: W](other: Tweak[V]) = Tweak[V] { x ⇒ t(x); other(x) }
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

  /** A widget can be tweaked with a Tweak */
  implicit def tweakableWithTweak[W <: View, T <: Tweak[W]] =
    new (W TweakableWith T) {
      def tweakWith(w: W, t: T) = UiThreading.fireUi(t(w))
    }

  /** A widget can be tweaked with an Effector */
  implicit def tweakableWithEffector[W <: View, F[+_], T](implicit effector: Effector[F], tweakableWith: W TweakableWith T) =
    new (W TweakableWith F[T]) {
      def tweakWith(w: W, f: F[T]) = effector.foreach(f)(t ⇒ tweakableWith.tweakWith(w, t))
    }

  /** Effector can be tweaked with something */
  implicit def effectorTweakable[W, F[+_], T](implicit effector: Effector[F], tweakableWith: W TweakableWith T) =
    new (F[W] TweakableWith T) {
      def tweakWith(f: F[W], t: T) = effector.foreach(f)(w ⇒ tweakableWith.tweakWith(w, t))
    }

  // format: ON

  /** A helper class to make tweaks */
  class TweakMaker[W <: View] {
    def ~(f: W ⇒ Unit) = Tweak(f)
    def doing(f: W ⇒ Unit) = Tweak(f)
  }

  /** Use this when you want to infer the type of the widget from the context */
  def tweak(implicit wtp: WidgetType) = new TweakMaker[wtp.W]

  /** Use this to provide widget type explicitly */
  def tweak[W <: View] = new TweakMaker[W]

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
    q"new org.macroid.WidgetType { type W = $tp }"
  }

  /** Get W from WidgetType */
  def readWidgetType(c: MacroContext)(wtp: c.Expr[WidgetType]) = {
    import c.universe._
    c.typeCheck(q"val x = $wtp; val y: x.W = ???; y").tpe
  }
}
