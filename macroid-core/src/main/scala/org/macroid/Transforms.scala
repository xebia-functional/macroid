package org.macroid

import scala.language.dynamics
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

  def layoutParams[A <: View](params: Any*): ViewMutator[A] = macro layoutParamsImpl[A]
  def lp[A <: View](params: Any*): ViewMutator[A] = macro layoutParamsImpl[A]

  def text[A <: TextView](text: CharSequence): ViewMutator[A] = x ⇒ x.setText(text)
  def text[A <: TextView](text: Int): ViewMutator[A] = x ⇒ x.setText(text)

  def vertical[A <: LinearLayout]: ViewMutator[A] = x ⇒ x.setOrientation(LinearLayout.VERTICAL)
  def horizontal[A <: LinearLayout]: ViewMutator[A] = x ⇒ x.setOrientation(LinearLayout.HORIZONTAL)

  def wire[A <: View](v: A): ViewMutator[A] = macro wireImpl[A]

  def addViews[A <: ViewGroup](children: Seq[View]): ViewMutator[A] = x ⇒ children.foreach(c ⇒ x.addView(c))

  object On extends Dynamic {
    def applyDynamic[A <: View](event: String)(f: Any) = macro onImpl[A]
  }
}

object TransformMacros {
  import LayoutDsl._

  class Helper[CTX <: MacroContext](val c: CTX) extends QuasiquoteCompat {
    import c.universe._

    def createWire(tpe: Type, v: Tree) = q"""
      { x: $tpe ⇒ $v = x }
    """

    def emptyMutator(tpe: Type) = q"""
      { x: $tpe ⇒ () }
    """

    def layoutParams(tpe: Type, l: Symbol, params: Seq[c.Expr[Any]]) = q"""
      { x: $tpe ⇒ x.setLayoutParams(new $l.LayoutParams(..$params)) }
    """

    def getListener(tpe: Type, setter: MethodSymbol, listener: Type, on: MethodSymbol, f: c.Expr[Any], func: Boolean) = {
      val args = on.paramss(0).indices.map(i ⇒ newTermName(s"arg$i"))
      val params = args zip on.paramss(0) map { case (a, p) ⇒ q"val $a: ${p.typeSignature}" }
      if (func) {
        val appl = args.map(a ⇒ Ident(a))
        q"""
          { x: $tpe ⇒ x.$setter(new $listener {
            override def ${on.name}(..$params) = $f(..$appl)
          })}
        """
      } else {
        q"""
          { x: $tpe ⇒ x.$setter(new $listener {
            override def ${on.name}(..$params) = { ${f.tree} }
          })}
        """
      }
    }
  }

  def refuse[A <: View: c.WeakTypeTag](c: MacroContext)(helper: Helper[c.type], msg: String) = {
    c.error(c.enclosingPosition, msg)
    c.Expr[ViewMutator[A]](helper.emptyMutator(c.weakTypeOf[A]))
  }

  def wireImpl[A <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[A]): c.Expr[ViewMutator[A]] = {
    val helper = new Helper[c.type](c)
    val wire = helper.createWire(c.weakTypeOf[A], v.tree)
    c.Expr[ViewMutator[A]](wire)
  }

  def layoutParamsImpl[A <: View: c.WeakTypeTag](c: MacroContext)(params: c.Expr[Any]*): c.Expr[ViewMutator[A]] = {
    import c.universe._
    val helper = new Helper[c.type](c)

    // this isDefined at l[SomeLayout] macro applications
    val L = newTermName("l")
    val lay: PartialFunction[Tree, Boolean] = { case Apply(TypeApply(Ident(L), _), _) ⇒ true }

    // a parent layout contains the current macro application
    def isParent(x: Tree) = lay.isDefinedAt(x) && x.find(_.pos == c.macroApplication.pos).isDefined

    // an immediate parent layout is a parent and contains no other parents
    val parentLayoutType = c.enclosingMethod.find { x ⇒
      isParent(x) && x.children.find(isParent(_)).isEmpty
    } flatMap {
      case x @ Apply(TypeApply(Ident(L), t), _) ⇒
        // avoid recursive type-checking
        val empty = Apply(TypeApply(Ident(L), t), List())
        Some(c.typeCheck(empty).tpe)
      case _ ⇒ None
    }

    // go up the inheritance chain until we find a suitable LayoutParams class in the companion
    parentLayoutType map { x ⇒
      var tp = x
      while (scala.util.Try {
        c.typeCheck(helper.layoutParams(weakTypeOf[A], tp.typeSymbol.companionSymbol, params))
      }.isFailure && tp.baseClasses.length > 2) {
        tp = tp.baseClasses(1).asType.toType
      }
      if (tp.baseClasses.length > 2) {
        c.info(c.enclosingPosition, s"Using $tp.LayoutParams", force = true)
        c.Expr[ViewMutator[A]](helper.layoutParams(weakTypeOf[A], tp.typeSymbol.companionSymbol, params))
      } else {
        refuse[A](c)(helper, "Could not find the appropriate LayoutParams constructor")
      }
    } getOrElse {
      refuse[A](c)(helper, "Could not find layout type")
    }
  }

  def onImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[ViewMutator[A]] = {
    import c.universe._
    val helper = new Helper[c.type](c)

    val Expr(Literal(Constant(eventName: String))) = event
    val setter = weakTypeOf[A].member(newTermName(s"setOn${eventName.capitalize}Listener")).asMethod
    if (setter == NoSymbol) {
      refuse[A](c)(helper, s"Could not find method setOn${eventName.capitalize}Listener in ${weakTypeOf[A]}")
    } else {
      // find the method to override
      val listener = setter.paramss(0)(0).typeSignature
      val on = scala.util.Try {
        val x = listener.member(newTermName(s"on${eventName.capitalize}")).asMethod
        assert(x != NoSymbol); x
      } getOrElse {
        c.error(c.enclosingPosition, s"Unsupported event listener class in $setter")
        NoSymbol.asMethod
      }

      scala.util.Try {
        // try to use `f` as a function
        if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
        c.Expr[ViewMutator[A]](c.typeCheck(helper.getListener(weakTypeOf[A], setter, listener, on, f, func = true)))
      } orElse scala.util.Try {
        // try to use `f` as a lazy block
        if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType <:< on.returnType)
        c.info(c.enclosingPosition, "Using lazy block as event listener", force = true)
        c.Expr[ViewMutator[A]](c.typeCheck(helper.getListener(weakTypeOf[A], setter, listener, on, f, func = false)))
      } getOrElse {
        refuse[A](c)(helper, s"Event listener should be either a function of type ${on.typeSignature} or a lazy block. The return type should be ${on.returnType}")
      }
    }
  }
}
