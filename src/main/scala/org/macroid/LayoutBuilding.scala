package org.macroid

import scala.language.experimental.macros
import android.view.{ ViewGroup, View }
import scala.reflect.macros.{ Context ⇒ MacroContext }

/** This trait contains basic building blocks used to define layouts: w, l and slot */
private[macroid] trait LayoutBuilding {
  import LayoutBuildingMacros._

  /** Define a widget */
  def w[W <: View](implicit ctx: ActivityContext): W = macro widgetImpl[W]
  /** Define a widget, supplying additional arguments */
  def w[W <: View](args: Any*)(implicit ctx: ActivityContext): W = macro widgetArgImpl[W]

  /** Define a layout */
  def l[L <: ViewGroup](children: View*)(implicit ctx: ActivityContext): L = macro layoutImpl[L]

  /** Define a slot */
  def slot[W <: View]: Option[W] = None
}

object LayoutBuilding extends LayoutBuilding

object LayoutBuildingMacros {
  def widgetImpl[W <: View: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[ActivityContext]): c.Expr[W] = {
    import c.universe._
    c.Expr[W](q"new ${weakTypeOf[W]}($ctx.get)")
  }

  def widgetArgImpl[W <: View: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*)(ctx: c.Expr[ActivityContext]): c.Expr[W] = {
    import c.universe._
    c.Expr[W](q"new ${weakTypeOf[W]}($ctx.get, ..$args)")
  }

  def layoutImpl[L <: ViewGroup: c.WeakTypeTag](c: MacroContext)(children: c.Expr[View]*)(ctx: c.Expr[ActivityContext]): c.Expr[L] = {
    import c.universe._
    val additions = children.map(ch ⇒ c.resetLocalAttrs(q"this.addView($ch)"))
    c.Expr[L](q"new ${weakTypeOf[L]}($ctx.get) { ..$additions }")
  }
}
