package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros
import android.view.{ ViewGroup, Gravity, View }
import ViewGroup.LayoutParams._
import android.widget.{ LinearLayout, TextView, FrameLayout }
import scala.reflect.macros.{ Context ⇒ MacroContext }
import org.macroid.Util.ByName

trait Transforms {
  import LayoutDsl._
  import TransformMacros._

  def id[A <: View](id: Int): ViewMutator[A] = x ⇒ x.setId(id)

  def hide[A <: View]: ViewMutator[A] = x ⇒ x.setVisibility(View.GONE)
  def show[A <: View]: ViewMutator[A] = x ⇒ x.setVisibility(View.VISIBLE)

  /** Center view in a `FrameLayout` */
  def center[A <: View](h: Boolean = true, v: Boolean = true): ViewMutator[A] = { x ⇒
    val ch = if (h) Gravity.CENTER_HORIZONTAL else 0
    val cv = if (v) Gravity.CENTER_VERTICAL else 0
    x.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, ch | cv))
  }

  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def layoutParams[A <: View](params: Any*): ViewMutator[A] = macro layoutParamsImpl[A]
  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def lp[A <: View](params: Any*): ViewMutator[A] = macro layoutParamsImpl[A]

  // TODO: move to a child project to allow separate type application
  def layoutParamsOf[A <: View, B <: ViewGroup](params: Any*): ViewMutator[A] = macro layoutParamsOfImpl[A, B]
  def lpOf[A <: View, B <: ViewGroup](params: Any*): ViewMutator[A] = macro layoutParamsOfImpl[A, B]

  def text[A <: TextView](text: CharSequence): ViewMutator[A] = x ⇒ x.setText(text)
  def text[A <: TextView](text: Int): ViewMutator[A] = x ⇒ x.setText(text)

  def vertical[A <: LinearLayout]: ViewMutator[A] = x ⇒ x.setOrientation(LinearLayout.VERTICAL)
  def horizontal[A <: LinearLayout]: ViewMutator[A] = x ⇒ x.setOrientation(LinearLayout.HORIZONTAL)

  /** Assign the view to the provided `var` */
  def wire[A <: View](v: A): ViewMutator[A] = macro wireImpl[A]

  /** Add views to the layout */
  def addViews[A <: ViewGroup](children: Seq[View]): ViewMutator[A] = x ⇒ children.foreach(c ⇒ x.addView(c))

  object On extends Dynamic {
    /** Override the listener treating `f` as a by-name argument. */
    def applyDynamic[A <: View](event: String)(f: Any) = macro onBlockImpl[A]
  }

  object FuncOn extends Dynamic {
    /** override the listener with `f` */
    def applyDynamic[A <: View](event: String)(f: Any) = macro onFuncImpl[A]
  }

  object ByNameOn extends Dynamic {
    /** override the listener with `f()` */
    def applyDynamic[A <: View](event: String)(f: ByName[Any]) = macro onByNameImpl[A]
  }
}

object Transforms extends Transforms

object TransformMacros {
  import LayoutDsl._

  class Helper[CTX <: MacroContext](val c: CTX) extends QuasiquoteCompat {
    import c.universe._

    def createWire(tpe: Type, v: Tree) = q"""
      { x: $tpe ⇒ $v = x }
    """

    def layoutParams(tpe: Type, l: Symbol, params: Seq[c.Expr[Any]]) = q"""
      { x: $tpe ⇒ x.setLayoutParams(new $l.LayoutParams(..$params)) }
    """

    def getListener(tpe: Type, setter: MethodSymbol, listener: Type, on: MethodSymbol, f: c.Expr[Any], mode: Int) = {
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
  }

  def wireImpl[A <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[A]): c.Expr[ViewMutator[A]] = {
    val helper = new Helper[c.type](c)
    val wire = helper.createWire(c.weakTypeOf[A], v.tree)
    c.Expr[ViewMutator[A]](wire)
  }

  def findLayoutParams[A <: View: c.WeakTypeTag](c: MacroContext)(helper: Helper[c.type], layoutType: c.Type, params: Seq[c.Expr[Any]]): c.Expr[ViewMutator[A]] = {
    import c.universe._
    var tp = layoutType

    // go up the inheritance chain until we find a suitable LayoutParams class in the companion
    while (scala.util.Try {
      c.typeCheck(helper.layoutParams(weakTypeOf[A], tp.typeSymbol.companionSymbol, params))
    }.isFailure && tp.baseClasses.length > 2) {
      tp = tp.baseClasses(1).asType.toType
    }
    if (tp.baseClasses.length > 2) {
      c.info(c.enclosingPosition, s"Using $tp.LayoutParams", force = true)
      c.Expr[ViewMutator[A]](helper.layoutParams(weakTypeOf[A], tp.typeSymbol.companionSymbol, params))
    } else {
      c.abort(c.enclosingPosition, "Could not find the appropriate LayoutParams constructor")
    }
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
    c.enclosingMethod.find { x ⇒
      isParent(x) && x.children.forall(_.find(isParent(_)).isEmpty)
    } flatMap {
      case x @ Apply(TypeApply(Ident(L), t), _) ⇒
        // avoid recursive type-checking
        val empty = Apply(TypeApply(Ident(L), t), List())
        Some(c.typeCheck(empty).tpe)
      case _ ⇒ None
    } map { x ⇒
      findLayoutParams[A](c)(helper, x, params)
    } getOrElse {
      c.abort(c.enclosingPosition, "Could not find layout type")
    }
  }

  def layoutParamsOfImpl[A <: View: c.WeakTypeTag, B <: ViewGroup: c.WeakTypeTag](c: MacroContext)(params: c.Expr[Any]*): c.Expr[ViewMutator[A]] = {
    val helper = new Helper[c.type](c)
    findLayoutParams[A](c)(helper, c.weakTypeOf[B], params)
  }

  def onBase[A <: View: c.WeakTypeTag](c: MacroContext)(helper: Helper[c.type], event: c.Expr[String]) = {
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

  def onBlockImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[ViewMutator[A]] = {
    import c.universe._
    val helper = new Helper[c.type](c)

    val (setter, listener, on) = onBase[A](c)(helper, event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType <:< on.returnType)
      c.Expr[ViewMutator[A]](helper.getListener(c.weakTypeOf[A], setter, listener, on, f, 1))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type ${on.returnType}")
    }
  }

  def onFuncImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[ViewMutator[A]] = {
    import c.universe._
    val helper = new Helper[c.type](c)

    val (setter, listener, on) = onBase[A](c)(helper, event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
      c.Expr[ViewMutator[A]](c.typeCheck(helper.getListener(weakTypeOf[A], setter, listener, on, f, 0)))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should have type signature ${on.typeSignature}")
    }
  }

  def onByNameImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[ByName[Any]]): c.Expr[ViewMutator[A]] = {
    import c.universe._
    val helper = new Helper[c.type](c)

    val (setter, listener, on) = onBase[A](c)(helper, event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
      c.Expr[ViewMutator[A]](helper.getListener(weakTypeOf[A], setter, listener, on, f, 2))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type ByName or Function0 and return ${on.returnType}")
    }
  }
}
