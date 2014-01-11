package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros
import android.view.{ ViewGroup, View }
import android.widget.{ ProgressBar, LinearLayout, TextView }
import scala.reflect.macros.{ Context ⇒ MacroContext }
import org.macroid.util.{ MacroUtils, Thunk }
import scala.concurrent.{ ExecutionContext, Future }
import scala.annotation.implicitNotFound

trait BasicTweaks {
  import BasicTweakMacros._

  /** Set this view’s id */
  def id(id: Int) = Tweak[View](_.setId(id))

  /** Assign the view to the provided `var` */
  def wire[W <: View](v: W): Tweak[W] = macro wireImpl[W]
  /** Assign the view to the provided slot */
  def wire[W <: View](v: Option[W]): Tweak[W] = macro wireOptionImpl[W]
}

trait VisibilityTweaks {
  /** Hide this view (uses View.GONE) */
  val hide = Tweak[View](_.setVisibility(View.GONE))
  /** Show this view (uses View.VISIBLE) */
  val show = Tweak[View](_.setVisibility(View.VISIBLE))
  /** Conditionally show/hide this view */
  def show(c: Boolean): Tweak[View] = if (c) show else hide
}

trait AbilityTweaks {
  /** Disable this view */
  val disable = Tweak[View](_.setEnabled(false))
  /** Enable this view */
  val enable = Tweak[View](_.setEnabled(true))
  /** Conditionally enable/disable this view */
  def enable(c: Boolean): Tweak[View] = if (c) enable else disable
}

trait PaddingTweaks {
  // TODO: replace with setPaddingRelative!

  /** Set padding */
  def padding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, all: Int = -1) = if (all >= 0) {
    Tweak[View](_.setPadding(all, all, all, all))
  } else {
    Tweak[View](_.setPadding(left, top, right, bottom))
  }
}

@implicitNotFound("Could not infer the type of the parent layout. Please provide it explicitly.") /** The type of the parent layout */
trait LayoutType {
  type L <: ViewGroup
}

trait LayoutTweaks {
  import LayoutTweakMacros._

  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def layoutParams(params: Any*)(implicit ltp: LayoutType): Tweak[View] = macro layoutParamsImpl
  /** Automatically find the appropriate `LayoutParams` class from the parent layout. */
  def lp(params: Any*)(implicit ltp: LayoutType): Tweak[View] = macro layoutParamsImpl

  implicit def inferLayoutType: LayoutType = macro inferLayoutTypeImpl

  /** Use `LayoutParams` of the specified layout class */
  def layoutParamsOf[L <: ViewGroup](params: Any*): Tweak[View] = macro layoutParamsOfImpl[L]
  /** Use `LayoutParams` of the specified layout class */
  def lpOf[L <: ViewGroup](params: Any*): Tweak[View] = macro layoutParamsOfImpl[L]

  /** Make this layout vertical */
  val vertical = Tweak[LinearLayout](_.setOrientation(LinearLayout.VERTICAL))
  /** Make this layout horizontal */
  val horizontal = Tweak[LinearLayout](_.setOrientation(LinearLayout.HORIZONTAL))

  /** Add views to the layout */
  def addViews(children: Seq[View], removeOld: Boolean = false) = Tweak[ViewGroup] { x ⇒
    if (removeOld) x.removeAllViews()
    children.foreach(c ⇒ x.addView(c))
  }
  /** Add view to the layout in reversed order (uses addView(child, 0)) */
  def addViewsReverse(children: Seq[View], removeOld: Boolean = false) = Tweak[ViewGroup] { x ⇒
    if (removeOld) x.removeAllViews()
    children.foreach(c ⇒ x.addView(c, 0))
  }
}

trait TextTweaks {
  /** Set text */
  def text(text: CharSequence) = Tweak[TextView](_.setText(text))
  /** Set text */
  def text(text: Int) = Tweak[TextView](_.setText(text))
  /** Set text */
  def text(text: Either[Int, CharSequence]) = text match {
    case Right(t) ⇒ Tweak[TextView](_.setText(t))
    case Left(t) ⇒ Tweak[TextView](_.setText(t))
  }
}

trait ProgressTweaks extends VisibilityTweaks {
  import Tweaking._

  /** Show this progress bar with indeterminate progress and hide it once `future` is done */
  def showProgress(future: Future[Any])(implicit ec: ExecutionContext) = Tweak[ProgressBar] { x ⇒
    x.setIndeterminate(true)
    x ~> show ~> future.recover { case _ ⇒ }.map(_ ⇒ hide)
  }
  /** Show this progress bar with determinate progress and hide it once all futures are done */
  def showProgress(futures: Seq[Future[Any]])(implicit ec: ExecutionContext) = Tweak[ProgressBar] { x ⇒
    val length = futures.length
    x.setIndeterminate(false)
    x.setProgress(0)
    x.setMax(length)
    x ~> show
    futures.foreach(f ⇒ f.recover { case _ ⇒ }.foreach { _ ⇒
      UiThreading.fireUi {
        x.incrementProgressBy(1)
        if (x.getProgress == x.getMax - 1) x ~> hide
      }
    })
  }
}

trait EventTweaks {
  import EventTweakMacros._

  object On extends Dynamic {
    /** Override the listener treating `f` as a by-name argument. */
    def applyDynamic(event: String)(f: Any)(implicit wtp: WidgetType): Tweak[wtp.W] = macro onBlockImpl
  }

  object FuncOn extends Dynamic {
    /** Override the listener with `f` */
    def applyDynamic(event: String)(f: Any)(implicit wtp: WidgetType): Tweak[wtp.W] = macro onFuncImpl
  }

  object ThunkOn extends Dynamic {
    /** Override the listener with `f()` */
    def applyDynamic(event: String)(f: Thunk[Any])(implicit wtp: WidgetType): Tweak[wtp.W] = macro onThunkImpl
  }
}

/** This trait provides the most useful tweaks. For an expanded set, see `contrib.ExtraTweaks` */
trait Tweaks
  extends Tweaking
  with BasicTweaks
  with VisibilityTweaks
  with AbilityTweaks
  with PaddingTweaks
  with LayoutTweaks
  with TextTweaks
  with ProgressTweaks
  with EventTweaks

object Tweaks extends Tweaks

object BasicTweakMacros {
  def wireImpl[W <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[W]): c.Expr[Tweak[W]] = {
    import c.universe._
    c.Expr[Tweak[W]](q"${weakTypeOf[Tweak[W]]} { x ⇒ $v = x }")
  }

  def wireOptionImpl[W <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[Option[W]]): c.Expr[Tweak[W]] = {
    import c.universe._
    c.Expr[Tweak[W]](q"${weakTypeOf[Tweak[W]]} { x ⇒ $v = Some(x) }")
  }
}

object LayoutTweakMacros {
  def inferLayoutTypeImpl(c: MacroContext) = {
    import c.universe._
    val L = newTermName("l")
    val lay: PartialFunction[Tree, Type] = {
      case Apply(TypeApply(Ident(L), t), _) ⇒
        // avoid recursive typechecking
        c.typeCheck(Apply(TypeApply(Ident(L), t), List())).tpe
    }
    val tp = MacroUtils.fromImmediateParentTree(c)(lay)
    c.Expr[LayoutType](writeLayoutType(c)(tp.getOrElse(sys.error(""))))
  }

  def writeLayoutType(c: MacroContext)(tp: c.Type) = {
    import c.universe._
    q"new ${typeOf[LayoutType]} { type L = $tp }"
  }

  def readLayoutType(c: MacroContext)(ltp: c.Expr[LayoutType]) = {
    import c.universe._
    c.typeCheck(q"val x = $ltp; val y: x.L = ???; y").tpe
  }

  def layoutParams(c: MacroContext)(l: c.Type, params: Seq[c.Expr[Any]]) = {
    import c.universe._
    q"org.macroid.Tweak[android.view.View] { x ⇒ x.setLayoutParams(new ${l.typeSymbol.companionSymbol}.LayoutParams(..$params)) }"
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

  def layoutParamsImpl(c: MacroContext)(params: c.Expr[Any]*)(ltp: c.Expr[LayoutType]): c.Expr[Tweak[View]] = {
    findLayoutParams(c)(readLayoutType(c)(ltp), params)
  }

  def layoutParamsOfImpl[L <: ViewGroup: c.WeakTypeTag](c: MacroContext)(params: c.Expr[Any]*): c.Expr[Tweak[View]] = {
    findLayoutParams(c)(c.weakTypeOf[L], params)
  }
}

object EventTweakMacros {
  def onBase(c: MacroContext)(event: c.Expr[String], wtp: c.Expr[WidgetType]) = {
    import c.universe._
    val tp = TweakingMacros.readWidgetType(c)(wtp)

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
    q"org.macroid.Tweak[$tpe] { x ⇒ x.$setter(new $listener { override def ${on.name.toTermName}(..$params) = $impl })}"
  }

  def onBlockImpl(c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any])(wtp: c.Expr[WidgetType]) = {
    import c.universe._

    val (setter, listener, on, tp) = onBase(c)(event, wtp)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType <:< on.returnType)
      c.Expr[Tweak[View]](getListener(c)(tp, setter, listener, on, c.Expr(c.resetLocalAttrs(f.tree)), BlockListener))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type ${on.returnType}")
    }
  }

  def onFuncImpl(c: MacroContext)(event: c.Expr[String])(f: c.Expr[Any])(wtp: c.Expr[WidgetType]) = {
    import c.universe._

    val (setter, listener, on, tp) = onBase(c)(event, wtp)
    scala.util.Try {
      c.Expr[Tweak[View]](c.typeCheck(getListener(c)(tp, setter, listener, on, f, FuncListener)))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should have type signature ${on.typeSignature}")
    }
  }

  def onThunkImpl(c: MacroContext)(event: c.Expr[String])(f: c.Expr[Thunk[Any]])(wtp: c.Expr[WidgetType]) = {
    import c.universe._

    val (setter, listener, on, tp) = onBase(c)(event, wtp)
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert(f.actualType.member(newTermName("apply")).asMethod.returnType <:< on.returnType)
      c.Expr[Tweak[View]](getListener(c)(tp, setter, listener, on, f, ThunkListener))
    } getOrElse {
      c.abort(c.enclosingPosition, s"f should be of type Thunk[${on.returnType}]")
    }
  }
}
