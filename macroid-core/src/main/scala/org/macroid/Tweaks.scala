package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros
import android.view.{ ViewGroup, View }
import android.widget.{ LinearLayout, TextView }
import scala.reflect.macros.{ Context ⇒ MacroContext }
import org.macroid.util.Thunk
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import scala.concurrent.{ ExecutionContext, Future, Promise, future }
import scala.util.Success

/** This trait provides the most useful tweaks. For an expanded set, see `contrib.ExtraTweaks` */
trait Tweaks extends Tweaking {
  import TweakMacros._

  /** Set this view’s id */
  def id(id: Int): Tweak[View] = x ⇒ x.setId(id)

  /** Hide this view (uses View.GONE) */
  val hide: Tweak[View] = x ⇒ x.setVisibility(View.GONE)
  /** Show this view (uses View.VISIBLE) */
  val show: Tweak[View] = x ⇒ x.setVisibility(View.VISIBLE)
  /** Conditionally show/hide this view */
  def show(c: Boolean): Tweak[View] = if (c) show else hide

  /** Disable this view */
  val disable: Tweak[View] = x ⇒ x.setEnabled(false)
  /** Enable this view */
  val enable: Tweak[View] = x ⇒ x.setEnabled(true)
  /** Conditionally enable/disable this view */
  def enable(c: Boolean): Tweak[View] = if (c) enable else disable

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
  /** Set text */
  def text(text: Either[Int, CharSequence]): Tweak[TextView] = text match {
    case Right(t) ⇒ { x ⇒ x.setText(t) }
    case Left(t) ⇒ { x ⇒ x.setText(t) }
  }

  /** Set padding */
  def padding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, all: Int = -1): Tweak[View] = if (all >= 0) {
    x: View ⇒ x.setPadding(all, all, all, all)
  } else {
    x: View ⇒ x.setPadding(left, top, right, bottom)
  }

  /** Make this layout vertical */
  val vertical: Tweak[LinearLayout] = x ⇒ x.setOrientation(LinearLayout.VERTICAL)
  /** Make this layout horizontal */
  val horizontal: Tweak[LinearLayout] = x ⇒ x.setOrientation(LinearLayout.HORIZONTAL)

  /** Assign the view to the provided `var` */
  def wire[A <: View](v: A): Tweak[A] = macro wireImpl[A]
  /** Assign the view to the provided slot */
  def wire[A <: View](v: Option[A]): Tweak[A] = macro wireOptionImpl[A]

  /** Add views to the layout */
  def addViews(children: Seq[View], removeOld: Boolean = false): Tweak[ViewGroup] = { x ⇒
    if (removeOld) x.removeAllViews()
    children.foreach(c ⇒ x.addView(c))
  }
  /** Add view to the layout in reversed order (uses addView(child, 0)) */
  def addViewsReverse(children: Seq[View], removeOld: Boolean = false): Tweak[ViewGroup] = { x ⇒
    if (removeOld) x.removeAllViews()
    children.foreach(c ⇒ x.addView(c, 0))
  }

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

object TweakMacros extends Tweaking {
  def wireImpl[A <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[A]): c.Expr[Tweak[A]] = {
    import c.universe._
    c.Expr[Tweak[A]](q"{ x: ${weakTypeOf[A]} ⇒ $v = x }")
  }

  def wireOptionImpl[A <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[Option[A]]): c.Expr[Tweak[A]] = {
    import c.universe._
    c.Expr[Tweak[A]](q"{ x: ${weakTypeOf[A]} ⇒ $v = Some(x) }")
  }

  def layoutParams(c: MacroContext)(l: c.Type, params: Seq[c.Expr[Any]]) = {
    import c.universe._
    q"{ x: android.view.View ⇒ x.setLayoutParams(new ${l.typeSymbol.companionSymbol}.LayoutParams(..$params)) }"
  }

  def findLayoutParams(c: MacroContext)(layoutType: c.Type, params: Seq[c.Expr[Any]]): c.Expr[Tweak[View]] = {
    import c.universe._
    var tp = layoutType

    // go up the inheritance chain until we find a suitable LayoutParams class in the companion
    while (scala.util.Try(c.typeCheck(layoutParams(c)(tp, params))).isFailure) {
      if (tp.baseClasses.length > 2) {
        tp = tp.baseClasses(1).asType.toType
      } else {
        c.abort(c.enclosingPosition, "Could not find the appropriate LayoutParams constructor")
      }
    }
    c.Expr[Tweak[View]](layoutParams(c)(tp, params))
  }

  /* @xeno-by was quite impressed with this hack... */
  def findImmediateParentTree(c: MacroContext)(parent: PartialFunction[c.Tree, Boolean]) = {
    import c.universe._

    // a parent contains the current macro application
    def isParent(x: Tree) = parent.isDefinedAt(x) & x.find(_.pos == c.macroApplication.pos).isDefined

    // an immediate parent is a parent and contains no other parents
    c.enclosingMethod.find { x ⇒
      isParent(x) && x.children.forall(_.find(isParent).isEmpty)
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

  def innerType(c: MacroContext)(t: c.Type): Option[c.Type] = {
    import c.universe._
    t match {
      case TypeRef(_, x, Nil) ⇒ Some(x.asType.toType)
      case TypeRef(_, _, x :: Nil) ⇒ innerType(c)(x)
      case _ ⇒ None
    }
  }

  def onBase[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String]) = {
    import c.universe._

    // fight against variance (A may be inferred as View, since Tweak[View] <:< Tweak[A <: View])
    val TildeArrow = newTermName("~>").encodedName
    val tweaking: PartialFunction[Tree, Boolean] = { case Apply(Select(_, TildeArrow), _) ⇒ true }

    // find `widget ~> On....` application and get widget’s exact type
    var tp = findImmediateParentTree(c)(tweaking) flatMap {
      // extract the target of ~> application
      case Apply(Select(victim, _), _) ⇒ Some(c.typeCheck(victim).tpe.widen)
      case _ ⇒ None
    } flatMap { t ⇒
      // in case of Option[T] or even Future[Option[T]], we need just T
      innerType(c)(t)
    } getOrElse {
      // resort to A
      weakTypeOf[A]
    }

    // find the setter
    val Expr(Literal(Constant(eventName: String))) = event
    val setter = scala.util.Try {
      val s = tp.member(newTermName(s"setOn${eventName.capitalize}Listener")).asMethod
      assert(s != NoSymbol); s
    } getOrElse {
      c.abort(c.enclosingPosition, s"Could not find method setOn${eventName.capitalize}Listener in $tp. You may need to provide the type argument explicitly")
    }

    // settle on widget’s type less eagerly
    var oldtp = tp
    while (scala.util.Try {
      assert(tp.member(newTermName(s"setOn${eventName.capitalize}Listener")).asMethod != NoSymbol)
    }.isSuccess && tp.baseClasses.length > 1) {
      oldtp = tp
      tp = tp.baseClasses(1).asType.toType
    }
    tp = oldtp
    fixTypeInference(c)(tp)

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

  sealed trait ListenerType
  object BlockListener extends ListenerType
  object FuncListener extends ListenerType
  object ThunkListener extends ListenerType

  def getListener(c: MacroContext)(tpe: c.Type, setter: c.universe.MethodSymbol, listener: c.Type, on: c.universe.MethodSymbol, f: c.Expr[Any], mode: ListenerType) = {
    import c.universe._
    val args = on.paramss(0).map(_ ⇒ newTermName(c.fresh("arg")))
    val params = args zip on.paramss(0) map { case (a, p) ⇒ q"val $a: ${p.typeSignature}" }
    lazy val argIdents = args.map(a ⇒ Ident(a))
    val impl = mode match {
      case BlockListener ⇒ q"$f"
      case FuncListener ⇒ q"$f(..$argIdents)"
      case ThunkListener ⇒ q"$f()"
    }
    q"{ x: $tpe ⇒ x.$setter(new $listener { override def ${on.name}(..$params) = $impl })}"
  }

  def onBlockImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on, tp) = onBase[A](c)(event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType <:< on.returnType)
      c.Expr[Tweak[A]](getListener(c)(tp, setter, listener, on, c.Expr(c.resetLocalAttrs(f.tree)), BlockListener))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type ${on.returnType}")
    }
  }

  def onFuncImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on, tp) = onBase[A](c)(event)
    scala.util.Try {
      c.Expr[Tweak[A]](c.typeCheck(getListener(c)(tp, setter, listener, on, f, FuncListener)))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should have type signature ${on.typeSignature}")
    }
  }

  def onThunkImpl[A <: View: c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(f: c.Expr[Thunk[Any]]): c.Expr[Tweak[A]] = {
    import c.universe._

    val (setter, listener, on, tp) = onBase[A](c)(event)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
      c.Expr[Tweak[A]](getListener(c)(tp, setter, listener, on, f, ThunkListener))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type Thunk[${on.returnType}]")
    }
  }
}
