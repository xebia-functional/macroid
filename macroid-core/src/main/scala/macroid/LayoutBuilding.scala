package macroid

import scala.language.experimental.macros
import android.view.{ ViewGroup, View }
import scala.reflect.macros.{ Context ⇒ MacroContext }

/** This trait contains basic building blocks used to define layouts: w, l and slot */
private[macroid] trait LayoutBuilding {
  import LayoutBuildingMacros._

  /** Define a widget */
  def widget[W <: View](implicit ctx: ContextWrapper): Ui[W] = macro widgetImpl[W]

  /** Define a widget (an alias for `widget`) */
  def w[W <: View](implicit ctx: ContextWrapper): Ui[W] = macro widgetImpl[W]

  /** Define a widget, supplying additional arguments */
  def widget[W <: View](args: Any*)(implicit ctx: ContextWrapper): Ui[W] = macro widgetArgImpl[W]

  /** Define a widget, supplying additional arguments (an alias for `widget`) */
  def w[W <: View](args: Any*)(implicit ctx: ContextWrapper): Ui[W] = macro widgetArgImpl[W]

  /** Define a layout */
  def layout[L <: ViewGroup](children: Ui[View]*)(implicit ctx: ContextWrapper): Ui[L] = macro layoutUiImpl[L]

  /** Define a layout (an alias for `layout`) */
  def l[L <: ViewGroup](children: Ui[View]*)(implicit ctx: ContextWrapper): Ui[L] = macro layoutUiImpl[L]

  /** Define a slot */
  def slot[W <: View]: Option[W] = None
}

object LayoutBuilding extends LayoutBuilding

object LayoutBuildingMacros {
  def widgetImpl[W <: View: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[ContextWrapper]): c.Expr[W] = {
    import c.universe._
    c.Expr[W](q"_root_.macroid.Ui(new ${weakTypeOf[W]}($ctx.getOriginal))")
  }

  def widgetArgImpl[W <: View: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*)(ctx: c.Expr[ContextWrapper]): c.Expr[W] = {
    import c.universe._
    c.Expr[W](q"_root_.macroid.Ui(new ${weakTypeOf[W]}($ctx.getOriginal, ..$args))")
  }

  def layoutUiImpl[L <: ViewGroup: c.WeakTypeTag](c: MacroContext)(children: c.Expr[Ui[View]]*)(ctx: c.Expr[ContextWrapper]): c.Expr[L] = {
    import c.universe._
    val untypechecked = children.map(ch ⇒ c.resetLocalAttrs(ch.tree))
    c.Expr[L](q"_root_.macroid.Ui.sequence(..$untypechecked).map(ch ⇒ new ${weakTypeOf[L]}($ctx.getOriginal) { ch.foreach(this.addView) })")
  }
}
