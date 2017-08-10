package macroid

import scala.language.experimental.macros
import android.view.{View, ViewGroup}
import macrocompat.bundle
import scala.reflect.macros.blackbox

/** This trait contains basic building blocks used to define layouts: w, l and slot */
private[macroid] trait LayoutBuilding {

  /** Define a widget */
  def widget[W <: View](implicit ctx: ContextWrapper): Ui[W] =
    macro LayoutBuildingMacros.widgetImpl[W]

  /** Define a widget (an alias for `widget`) */
  def w[W <: View](implicit ctx: ContextWrapper): Ui[W] =
    macro LayoutBuildingMacros.widgetImpl[W]

  /** Define a widget, supplying additional arguments */
  def widget[W <: View](args: Any*)(implicit ctx: ContextWrapper): Ui[W] =
    macro LayoutBuildingMacros.widgetArgImpl[W]

  /** Define a widget, supplying additional arguments (an alias for `widget`) */
  def w[W <: View](args: Any*)(implicit ctx: ContextWrapper): Ui[W] =
    macro LayoutBuildingMacros.widgetArgImpl[W]

  /** Define a layout */
  def layout[L <: ViewGroup](children: Ui[View]*)(implicit ctx: ContextWrapper): Ui[L] =
    macro LayoutBuildingMacros.layoutUiImpl[L]

  /** Define a layout (an alias for `layout`) */
  def l[L <: ViewGroup](children: Ui[View]*)(implicit ctx: ContextWrapper): Ui[L] =
    macro LayoutBuildingMacros.layoutUiImpl[L]

  /** Define a slot */
  def slot[W <: View]: Option[W] = None
}

object LayoutBuilding extends LayoutBuilding

@bundle
class LayoutBuildingMacros(val c: blackbox.Context) {
  import c.universe._

  def widgetImpl[W <: View: c.WeakTypeTag](ctx: c.Expr[ContextWrapper]): Tree =
    q"_root_.macroid.Ui(new ${weakTypeOf[W]}($ctx.getOriginal))"

  def widgetArgImpl[W <: View: c.WeakTypeTag](args: c.Expr[Any]*)(
      ctx: c.Expr[ContextWrapper]): Tree =
    q"_root_.macroid.Ui(new ${weakTypeOf[W]}($ctx.getOriginal, ..$args))"

  def layoutUiImpl[L <: ViewGroup: c.WeakTypeTag](children: c.Expr[Ui[View]]*)(
      ctx: c.Expr[ContextWrapper]): Tree = {
    val untypechecked = children.map(ch ⇒ c.untypecheck(ch.tree))
    q"_root_.macroid.Ui.sequence(..$untypechecked).map(ch ⇒ new ${weakTypeOf[L]}($ctx.getOriginal) { ch.foreach(this.addView) })"
  }
}
