package org.macroid

import scala.language.experimental.macros
import android.view.{ ViewGroup, View }
import scala.reflect.macros.{ Context ⇒ MacroContext }

/** This trait contains basic building blocks used to define layouts: w, l and slot */
trait LayoutBuilding {
  import LayoutBuildingMacros._

  /** Define a widget */
  def w[A <: View](implicit ctx: ActivityContext) = macro widgetImpl[A]
  /** Define a widget, supplying additional arguments */
  def w[A <: View](args: Any*)(implicit ctx: ActivityContext) = macro widgetArgImpl[A]

  /** Define a layout */
  def l[A <: ViewGroup](children: View*)(implicit ctx: ActivityContext) = macro layoutImpl[A]

  /** Define a slot */
  def slot[A <: View]: Option[A] = None
}

object LayoutBuilding extends LayoutBuilding

object LayoutBuildingMacros {
  def widgetImpl[A <: View: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[ActivityContext]): c.Expr[A] = {
    import c.universe._
    c.Expr[A](q"new ${weakTypeOf[A]}($ctx.get)")
  }

  def widgetArgImpl[A <: View: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*)(ctx: c.Expr[ActivityContext]): c.Expr[A] = {
    import c.universe._
    c.Expr[A](q"new ${weakTypeOf[A]}($ctx.get, ..$args)")
  }

  def layoutImpl[A <: View: c.WeakTypeTag](c: MacroContext)(children: c.Expr[View]*)(ctx: c.Expr[ActivityContext]): c.Expr[A] = {
    import c.universe._
    val additions = children.map(ch ⇒ c.resetLocalAttrs(q"this.addView($ch)"))
    c.Expr[A](q"new ${weakTypeOf[A]}($ctx.get) { ..$additions }")
  }
}

/** This trait defines transformers and transforming operator (~~>) */
trait LayoutTransforming {
  /** A transformer is a partial mutating function that can be recursively applied to a layout */
  type Transformer = PartialFunction[View, Unit]

  // transforming layouts
  implicit class RichViewGroup[A <: ViewGroup](v: A) {
    /** Apply transformer. Always runs on UI thread */
    def ~~>(t: Transformer) = {
      def applyTo(v: View) {
        if (t.isDefinedAt(v)) t(v)
        v match {
          case Layout(children @ _*) ⇒ children.foreach(applyTo)
          case _ ⇒ ()
        }
      }
      UiThreading.runOnUiThread(applyTo(v))
      v
    }
  }
  object LayoutTransforming extends LayoutTransforming

  /** layout extractor */
  object Layout {
    def unapplySeq(v: View): Option[Seq[View]] = v match {
      case g: ViewGroup ⇒ Some((0 until g.getChildCount).map(i ⇒ g.getChildAt(i)))
      case _ ⇒ None
    }
  }
}