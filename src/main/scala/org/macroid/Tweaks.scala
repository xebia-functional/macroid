package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros
import android.view.{ ViewGroup, Gravity, View }
import ViewGroup.LayoutParams._
import android.widget.{ LinearLayout, TextView, FrameLayout }
import scala.reflect.macros.{ Context ⇒ MacroContext }
import org.macroid.Util.ByName

trait Tweaks {
  import LayoutDsl._
  import TweakMacros._

  /** Set this view’s id */
  def id[A <: View](id: Int): Tweak[A] = x ⇒ x.setId(id)

  /** Hide this view (uses View.GONE) */
  def hide[A <: View]: Tweak[A] = x ⇒ x.setVisibility(View.GONE)
  /** Show this view (uses View.VISIBLE) */
  def show[A <: View]: Tweak[A] = x ⇒ x.setVisibility(View.VISIBLE)

  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def layoutParams[A <: View](params: Any*): Tweak[A] = macro layoutParamsImpl[A]
  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def lp[A <: View](params: Any*): Tweak[A] = macro layoutParamsImpl[A]

  class LPOF[A <: View] {
    /** Use `LayoutParams` of the specified layout class */
    def of[B <: ViewGroup](params: Any*): Tweak[A] = macro layoutParamsOfImpl[A, B]
  }
  def layoutParams[A <: View] = new LPOF[A]
  def lp[A <: View] = new LPOF[A]

  /** Set text */
  def text[A <: TextView](text: CharSequence): Tweak[A] = x ⇒ x.setText(text)
  /** Set text */
  def text[A <: TextView](text: Int): Tweak[A] = x ⇒ x.setText(text)

  /** Make this layout vertical */
  def vertical[A <: LinearLayout]: Tweak[A] = x ⇒ x.setOrientation(LinearLayout.VERTICAL)
  /** Make this layout horizontal */
  def horizontal[A <: LinearLayout]: Tweak[A] = x ⇒ x.setOrientation(LinearLayout.HORIZONTAL)

  /** Assign the view to the provided `var` */
  def wire[A <: View](v: A): Tweak[A] = macro wireImpl[A]

  /** Add views to the layout */
  def addViews[A <: ViewGroup](children: Seq[View]): Tweak[A] = x ⇒ children.foreach(c ⇒ x.addView(c))

  object On extends Dynamic {
    /** Override the listener treating `f` as a by-name argument. */
    def applyDynamic[A <: View](event: String)(f: Any) = macro onBlockImpl[A]
  }

  object FuncOn extends Dynamic {
    /** Override the listener with `f` */
    def applyDynamic[A <: View](event: String)(f: Any) = macro onFuncImpl[A]
  }

  object ByNameOn extends Dynamic {
    /** Override the listener with `f()` */
    def applyDynamic[A <: View](event: String)(f: ByName[Any]) = macro onByNameImpl[A]
  }
}

object Tweaks extends Tweaks

object TweakMacros {
  import LayoutDsl._

  def wireImpl[A <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[A]): c.Expr[Tweak[A]] = {
    import c.universe._
    val wire = q"{ x: ${weakTypeOf[A]} ⇒ ${v.tree} = x }"
    c.Expr[Tweak[A]](wire)
  }

  def layoutParams(c: MacroContext)(tpe: c.Type, l: c.Type, params: Seq[c.Expr[Any]]) = {
    import c.universe._
    q"{ x: $tpe ⇒ x.setLayoutParams(new ${l.typeSymbol.companionSymbol}.LayoutParams(..$params)) }"
  }

  def findLayoutParams[A <: View: c.WeakTypeTag](c: MacroContext)(layoutType: c.Type, params: Seq[c.Expr[Any]]): c.Expr[Tweak[A]] = {
    import c.universe._
    var tp = layoutType

    // go up the inheritance chain until we find a suitable LayoutParams class in the companion
    while (scala.util.Try {
      c.typeCheck(layoutParams(c)(weakTypeOf[A], tp, params))
    }.isFailure && tp.baseClasses.length > 2) {
      tp = tp.baseClasses(1).asType.toType
    }
    if (tp.baseClasses.length > 2) {
      c.info(c.enclosingPosition, s"Using $tp.LayoutParams", force = true)
      c.Expr[Tweak[A]](layoutParams(c)(weakTypeOf[A], tp, params))
    } else {
      c.abort(c.enclosingPosition, "Could not find the appropriate LayoutParams constructor")
    }
  }

  def layoutParamsImpl[A <: View: c.WeakTypeTag](c: MacroContext)(params: c.Expr[Any]*): c.Expr[Tweak[A]] = {
    import c.universe._

    // this isDefined at l[SomeLayout] macro applications
    val L = newTermName("l")
    val lay: PartialFunction[Tree, Boolean] = { case Apply(TypeApply(Ident(L), _), _) ⇒ true }

    // a parent layout contains the current macro application
    def isParent(x: Tree) = lay.isDefinedAt(x) && x.find(_.pos == c.macroApplication.pos).isDefined

    // an immediate parent layout is a parent and contains no other parents
    c.enclosingMethod.find { x ⇒
      isParent(x) && x.children.forall(_.find(isParent(_)).isEmpty)
    } flatMap {
      case x @ Apply(TypeApply(Ident(L), t), _) ⇒
        // avoid recursive type-checking
        val empty = Apply(TypeApply(Ident(L), t), List())
        Some(c.typeCheck(empty).tpe)
      case _ ⇒ None
    } map { x ⇒
      findLayoutParams[A](c)(x, params)
    } getOrElse {
      c.abort(c.enclosingPosition, "Could not find layout type")
    }
  }

  def layoutParamsOfImpl[A <: View: c.WeakTypeTag, B <: ViewGroup: c.WeakTypeTag](c: MacroContext)(params: c.Expr[Any]*): c.Expr[Tweak[A]] = {
    findLayoutParams[A](c)(c.weakTypeOf[B], params)
  }

  def onBase[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String]) = {
    import c.universe._

    // find the setter
    val Expr(Literal(Constant(eventName: String))) = event
    val setter = weakTypeOf[A].member(newTermName(s"setOn${eventName.capitalize}Listener")).asMethod
    if (setter == NoSymbol) {
      c.abort(c.enclosingPosition, s"Could not find method setOn${eventName.capitalize}Listener in ${weakTypeOf[A]}")
    }

    // find the method to override
    val listener = setter.paramss(0)(0).typeSignature
    val on = scala.util.Try {
      val x = listener.member(newTermName(s"on${eventName.capitalize}")).asMethod
      assert(x != NoSymbol); x
    } getOrElse {
      c.abort(c.enclosingPosition, s"Unsupported event listener class in $setter")
    }

    (setter, listener, on)
  }

  def getListener(c: MacroContext)(tpe: c.Type, setter: c.universe.MethodSymbol, listener: c.Type, on: c.universe.MethodSymbol, f: c.Expr[Any], mode: Int) = {
    import c.universe._
    val args = on.paramss(0).indices.map(i ⇒ newTermName(s"arg$i"))
    val params = args zip on.paramss(0) map { case (a, p) ⇒ q"val $a: ${p.typeSignature}" }
    if (mode == 0) {
      // function
      val appl = args.map(a ⇒ Ident(a))
      q"""
        { x: $tpe ⇒ x.$setter(new $listener {
          override def ${on.name}(..$params) = $f(..$appl)
        })}
      """
    } else if (mode == 1) {
      // by-name block
      q"""
        { x: $tpe ⇒ x.$setter(new $listener {
          override def ${on.name}(..$params) = { ${f.tree} }
        })}
      """
    } else {
      // ByName
      q"""
        { x: $tpe ⇒ x.$setter(new $listener {
          override def ${on.name}(..$params) = $f()
        })}
      """
    }
  }

  def onBlockImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on) = onBase[A](c)(event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType <:< on.returnType)
      c.Expr[Tweak[A]](getListener(c)(c.weakTypeOf[A], setter, listener, on, f, 1))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type ${on.returnType}")
    }
  }

  def onFuncImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on) = onBase[A](c)(event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
      c.Expr[Tweak[A]](c.typeCheck(getListener(c)(weakTypeOf[A], setter, listener, on, f, 0)))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should have type signature ${on.typeSignature}")
    }
  }

  def onByNameImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[ByName[Any]]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on) = onBase[A](c)(event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
      c.Expr[Tweak[A]](getListener(c)(weakTypeOf[A], setter, listener, on, f, 2))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type ByName or Function0 and return ${on.returnType}")
    }
  }
}
