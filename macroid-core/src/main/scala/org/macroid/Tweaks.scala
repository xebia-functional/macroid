package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros
import android.view.{ ViewGroup, Gravity, View }
import ViewGroup.LayoutParams._
import android.widget.{ LinearLayout, TextView, FrameLayout }
import scala.reflect.macros.{ Context ⇒ MacroContext }
import org.macroid.Util.Thunk

trait Tweaks {
  import LayoutDsl._
  import TweakMacros._

  /** Set this view’s id */
  def id(id: Int): Tweak[View] = x ⇒ x.setId(id)

  /** Hide this view (uses View.GONE) */
  def hide: Tweak[View] = x ⇒ x.setVisibility(View.GONE)
  /** Show this view (uses View.VISIBLE) */
  def show: Tweak[View] = x ⇒ x.setVisibility(View.VISIBLE)

  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def layoutParams(params: Any*): Tweak[View] = macro layoutParamsImpl
  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def lp(params: Any*): Tweak[View] = macro layoutParamsImpl

  /** Use `LayoutParams` of the specified layout class */
  def layoutParamsOf[B <: ViewGroup](params: Any*): Tweak[View] = macro layoutParamsOfImpl[B]
  def lpOf[B <: ViewGroup](params: Any*): Tweak[View] = macro layoutParamsOfImpl[B]

  /** Set text */
  def text(text: CharSequence): Tweak[TextView] = x ⇒ x.setText(text)
  /** Set text */
  def text(text: Int): Tweak[TextView] = x ⇒ x.setText(text)

  /** Make this layout vertical */
  def vertical: Tweak[LinearLayout] = x ⇒ x.setOrientation(LinearLayout.VERTICAL)
  /** Make this layout horizontal */
  def horizontal: Tweak[LinearLayout] = x ⇒ x.setOrientation(LinearLayout.HORIZONTAL)

  /** Assign the view to the provided `var` */
  def wire[A <: View](v: A): Tweak[A] = macro wireImpl[A]

  /** Add views to the layout */
  def addViews(children: Seq[View]): Tweak[ViewGroup] = x ⇒ children.foreach(c ⇒ x.addView(c))

  object On extends Dynamic {
    /** Override the listener treating `f` as a by-name argument. */
    def applyDynamic[A <: View](event: String)(f: Any): Tweak[A] = macro onBlockImpl[A]
  }

  object FuncOn extends Dynamic {
    /** Override the listener with `f` */
    def applyDynamic[A <: View](event: String)(f: Any): Tweak[A] = macro onFuncImpl[A]
  }

  object ThunkOn extends Dynamic {
    /** Override the listener with `f()` */
    def applyDynamic[A <: View](event: String)(f: Thunk[Any]): Tweak[A] = macro onThunkImpl[A]
  }
}

object Tweaks extends Tweaks

object TweakMacros {
  import LayoutDsl._

  def wireImpl[A <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[A]): c.Expr[Tweak[A]] = {
    import c.universe._
    c.Expr[Tweak[A]](q"{ x: ${weakTypeOf[A]} ⇒ ${v.tree} = x }")
  }

  def layoutParams(c: MacroContext)(l: c.Type, params: Seq[c.Expr[Any]]) = {
    import c.universe._
    q"{ x: View ⇒ x.setLayoutParams(new ${l.typeSymbol.companionSymbol}.LayoutParams(..$params)) }"
  }

  def findLayoutParams(c: MacroContext)(layoutType: c.Type, params: Seq[c.Expr[Any]]): c.Expr[Tweak[View]] = {
    import c.universe._
    var tp = layoutType

    // go up the inheritance chain until we find a suitable LayoutParams class in the companion
    while (scala.util.Try {
      c.typeCheck(layoutParams(c)(tp, params))
    }.isFailure && tp.baseClasses.length > 2) {
      tp = tp.baseClasses(1).asType.toType
    }
    if (tp.baseClasses.length > 2) {
      c.info(c.enclosingPosition, s"Using $tp.LayoutParams", force = false)
      c.Expr[Tweak[View]](layoutParams(c)(tp, params))
    } else {
      c.abort(c.enclosingPosition, "Could not find the appropriate LayoutParams constructor")
    }
  }

  /* @xeno_by was quite impressed with this hack... */
  def findImmediateParentTree(c: MacroContext)(parent: PartialFunction[c.Tree, Boolean]) = {
    import c.universe._

    // a parent contains the current macro application
    def isParent(x: Tree) = parent.isDefinedAt(x) & x.find(_.pos == c.macroApplication.pos).isDefined

    // an immediate parent is a parent and contains no other parents
    c.enclosingMethod.find { x ⇒
      isParent(x) && x.children.forall(_.find(isParent(_)).isEmpty)
    }
  }

  def layoutParamsImpl(c: MacroContext)(params: c.Expr[Any]*): c.Expr[Tweak[View]] = {
    import c.universe._

    // this isDefined at l[SomeLayout] macro applications
    val L = newTermName("l")
    val lay: PartialFunction[Tree, Boolean] = { case Apply(TypeApply(Ident(L), _), _) ⇒ true }

    // find l[SomeLayout](...) application and get SomeLayout exactly
    findImmediateParentTree(c)(lay) flatMap {
      case x @ Apply(TypeApply(Ident(L), t), _) ⇒
        // avoid recursive type-checking
        val empty = Apply(TypeApply(Ident(L), t), List())
        Some(c.typeCheck(empty).tpe)
      case _ ⇒ None
    } map { x ⇒
      findLayoutParams(c)(x, params)
    } getOrElse {
      c.abort(c.enclosingPosition, "Could not find layout type")
    }
  }

  def layoutParamsOfImpl[B <: ViewGroup: c.WeakTypeTag](c: MacroContext)(params: c.Expr[Any]*): c.Expr[Tweak[View]] = {
    findLayoutParams(c)(c.weakTypeOf[B], params)
  }

  /* Another great hack. Credit goes to @pelotom for using this in Effectful */
  def fixTypeInference(c: MacroContext)(tp: c.Type) = {
    import c.universe._
    c.macroApplication.setType(c.typeCheck(q"val x: Tweak[$tp] = ???; x").tpe)
  }

  def onBase[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String]) = {
    import c.universe._

    // fight against variance (A may be inferred as View, since Tweak[View] <:< Tweak[A <: View])
    val TildeArrow = newTermName("~>").encodedName
    val UnicodeArrow = newTermName("⇝").encodedName
    val tweaking: PartialFunction[Tree, Boolean] = { case Apply(Select(_, TildeArrow | UnicodeArrow), _) ⇒ true }

    // find `widget ~> On....` application and get widget’s exact type
    val tp = findImmediateParentTree(c)(tweaking) flatMap {
      case Apply(Select(victim, _), _) ⇒ Some(c.typeCheck(victim).tpe)
      case _ ⇒ None
    } getOrElse {
      weakTypeOf[A]
    }
    fixTypeInference(c)(tp)

    // find the setter
    val Expr(Literal(Constant(eventName: String))) = event
    val setter = scala.util.Try {
      val s = tp.member(newTermName(s"setOn${eventName.capitalize}Listener")).asMethod
      assert(s != NoSymbol); s
    } getOrElse {
      c.abort(c.enclosingPosition, s"Could not find method setOn${eventName.capitalize}Listener in $tp. You may need to provide the type argument explicitly")
    }

    // find the method to override
    val listener = setter.paramss(0)(0).typeSignature
    val on = scala.util.Try {
      val x = listener.member(newTermName(s"on${eventName.capitalize}")).asMethod
      assert(x != NoSymbol); x
    } getOrElse {
      c.abort(c.enclosingPosition, s"Unsupported event listener class in $setter")
    }

    (setter, listener, on, tp)
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
      // by-name argument
      q"""
        { x: $tpe ⇒ x.$setter(new $listener {
          override def ${on.name}(..$params) = { ${f.tree} }
        })}
      """
    } else {
      // thunk
      q"""
        { x: $tpe ⇒ x.$setter(new $listener {
          override def ${on.name}(..$params) = $f()
        })}
      """
    }
  }

  def onBlockImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on, tp) = onBase[A](c)(event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType <:< on.returnType)
      c.Expr[Tweak[A]](getListener(c)(tp, setter, listener, on, f, 1))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type ${on.returnType}")
    }
  }

  def onFuncImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on, tp) = onBase[A](c)(event)
    scala.util.Try {
      c.Expr[Tweak[A]](c.typeCheck(getListener(c)(tp, setter, listener, on, f, 0)))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should have type signature ${on.typeSignature}")
    }
  }

  def onThunkImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Thunk[Any]]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on, tp) = onBase[A](c)(event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
      c.Expr[Tweak[A]](getListener(c)(tp, setter, listener, on, f, 2))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type Thunk[${on.returnType}]")
    }
  }
}
