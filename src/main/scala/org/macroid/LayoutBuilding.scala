package org.macroid

import scala.language.experimental.macros
import android.view.{ ViewGroup, View }
import scala.reflect.macros.{ Context ⇒ MacroContext }
import org.macroid.util.Ui

/** This trait contains basic building blocks used to define layouts: w, l and slot */
private[macroid] trait LayoutBuilding {
  import LayoutBuildingMacros._

  /** Define a widget */
  def w[W <: View](implicit ctx: ActivityContext): Ui[W] = macro widgetImpl[W]
  /** Define a widget, supplying additional arguments */
  def w[W <: View](args: Any*)(implicit ctx: ActivityContext): Ui[W] = macro widgetArgImpl[W]

  /** Define a layout */
  def l[L <: ViewGroup](children: Ui[View]*)(implicit ctx: ActivityContext): Ui[L] = macro layoutUiImpl[L]

  /** Define a slot */
  def slot[W <: View]: Option[W] = None
}

object LayoutBuilding extends LayoutBuilding

object LayoutBuildingMacros {
  def widgetImpl[W <: View: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[ActivityContext]): c.Expr[W] = {
    import c.universe._
    c.Expr[W](q"org.macroid.util.Ui(new ${weakTypeOf[W]}($ctx.get))")
  }

  def widgetArgImpl[W <: View: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*)(ctx: c.Expr[ActivityContext]): c.Expr[W] = {
    import c.universe._
    c.Expr[W](q"org.macroid.util.Ui(new ${weakTypeOf[W]}($ctx.get, ..$args))")
  }

  def layoutImpl[L <: ViewGroup: c.WeakTypeTag](c: MacroContext)(children: c.Expr[View]*)(ctx: c.Expr[ActivityContext]): c.Expr[L] = {
    import c.universe._
    val additions = children.map(ch ⇒ c.resetLocalAttrs(q"this.addView($ch)"))
    c.Expr[L](q"org.macroid.util.Ui(new ${weakTypeOf[L]}($ctx.get) { ..$additions })")
  }

  def layoutUiImpl[L <: ViewGroup: c.WeakTypeTag](c: MacroContext)(children: c.Expr[Ui[View]]*)(ctx: c.Expr[ActivityContext]): c.Expr[L] = {
    import c.universe._
    val untypechecked = children.map(ch ⇒ c.resetLocalAttrs(ch.tree))
    c.Expr[L](q"org.macroid.util.Ui.sequence(..$untypechecked).map(ch ⇒ new ${weakTypeOf[L]}($ctx.get) { ch.foreach(this.addView) })")
  }
}
