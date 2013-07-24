package org.macroid

import scala.language.experimental.macros
import android.view.{ ViewGroup, Gravity, View }
import ViewGroup.LayoutParams._
import android.widget.{ LinearLayout, TextView, FrameLayout }
import scala.reflect.macros.{ Context ⇒ MacroContext }

object Transforms {
  import LayoutDsl._
  import TransformMacros._

  def id[A <: View](id: Int): ViewMutator[A] = x ⇒ x.setId(id)

  def hide[A <: View]: ViewMutator[A] = x ⇒ x.setVisibility(View.GONE)
  def show[A <: View]: ViewMutator[A] = x ⇒ x.setVisibility(View.VISIBLE)

  def center[A <: View](h: Boolean = true, v: Boolean = true): ViewMutator[A] = { x ⇒
    val ch = if (h) Gravity.CENTER_HORIZONTAL else 0
    val cv = if (v) Gravity.CENTER_VERTICAL else 0
    x.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, ch | cv))
  }

  def text[A <: TextView](text: CharSequence): ViewMutator[A] = x ⇒ x.setText(text)
  def text[A <: TextView](text: Int): ViewMutator[A] = x ⇒ x.setText(text)

  def vertical[A <: LinearLayout]: ViewMutator[A] = x ⇒ x.setOrientation(LinearLayout.VERTICAL)
  def horizontal[A <: LinearLayout]: ViewMutator[A] = x ⇒ x.setOrientation(LinearLayout.HORIZONTAL)

  def wire[A <: View](v: A): ViewMutator[A] = macro wireImpl[A]
}

object TransformMacros {
  import LayoutDsl._

  class Helper[CTX <: MacroContext](val c: CTX) extends QuasiquoteCompat {
    import c.universe._

    def createWire(tpe: c.Type, v: c.Tree) = q"""
      { x: $tpe ⇒ $v = x }
    """
  }

  def wireImpl[A <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[A]): c.Expr[ViewMutator[A]] = {
    val helper = new Helper[c.type](c)
    val wire = helper.createWire(c.weakTypeOf[A], v.tree)
    c.Expr[ViewMutator[A]](wire)
  }
}
