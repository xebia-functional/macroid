package macroid

import android.annotation._

import scala.language.dynamics
import scala.language.experimental.macros
import android.text._
import android.view._
import android.widget._
import macrocompat._

import scala.reflect.macros._

private[macroid] trait BasicTweaks {

  /** Set this view’s id */
  def id(id: Int) = Tweak[View](_.setId(id))

  /** Attach arbitrary value to a view by means of setTag */
  def hold[A](value: A) = Tweak[View](_.setTag(value))

  /** Assign the view to the provided `var` */
  def wire[W <: View](v: W): Tweak[W] = macro BasicTweakMacros.wireImpl[W]

  /** Assign the view to the provided slot */
  def wire[W <: View](v: Option[W]): Tweak[W] =
    macro BasicTweakMacros.wireOptionImpl[W]
}

private[macroid] trait VisibilityTweaks {

  /** Hide this view (uses View.GONE) */
  val hide = Tweak[View](_.setVisibility(View.GONE))

  /** Show this view (uses View.VISIBLE) */
  val show = Tweak[View](_.setVisibility(View.VISIBLE))

  /** Conditionally show/hide this view */
  def show(c: Boolean): Tweak[View] = if (c) show else hide
}

private[macroid] trait AbilityTweaks {

  /** Disable this view */
  val disable = Tweak[View](_.setEnabled(false))

  /** Enable this view */
  val enable = Tweak[View](_.setEnabled(true))

  /** Conditionally enable/disable this view */
  def enable(c: Boolean): Tweak[View] = if (c) enable else disable
}

private[macroid] trait PaddingTweaks {
  // TODO: replace with setPaddingRelative!

  /** Set padding */
  def padding(left: Int = 0,
              top: Int = 0,
              right: Int = 0,
              bottom: Int = 0,
              all: Int = -1) =
    if (all >= 0) {
      Tweak[View](_.setPadding(all, all, all, all))
    } else {
      Tweak[View](_.setPadding(left, top, right, bottom))
    }
}

private[macroid] trait LayoutTweaks {

  /** Use `LayoutParams` of the specified layout class */
  def layoutParams[L <: ViewGroup](params: Any*): Tweak[View] =
    macro LayoutTweakMacros.layoutParamsImpl[L]

  /** Use `LayoutParams` of the specified layout class */
  def lp[L <: ViewGroup](params: Any*): Tweak[View] =
    macro LayoutTweakMacros.layoutParamsImpl[L]

  /** Make this layout vertical */
  val vertical = Tweak[LinearLayout](_.setOrientation(LinearLayout.VERTICAL))

  /** Make this layout horizontal */
  val horizontal =
    Tweak[LinearLayout](_.setOrientation(LinearLayout.HORIZONTAL))

  /** Add views to the layout */
  def addViews(children: Seq[Ui[View]],
               removeOld: Boolean = false,
               reverse: Boolean = false) = Tweak[ViewGroup] { x ⇒
    if (removeOld) x.removeAllViews()
    children.foreach(c ⇒ if (reverse) x.addView(c.get, 0) else x.addView(c.get))
  }
}

private[macroid] trait TextTweaks {

  /** Set text */
  def text(text: CharSequence) = Tweak[TextView](_.setText(text))

  /** Set text */
  def text(text: Int) = Tweak[TextView](_.setText(text))

  /** Set text */
  def text(text: Either[Int, CharSequence]) = text match {
    case Right(t) ⇒ Tweak[TextView](_.setText(t))
    case Left(t) ⇒ Tweak[TextView](_.setText(t))
  }

  /** Set hint */
  def hint(hint: CharSequence) = Tweak[TextView](_.setHint(hint))

  /** Set hint */
  def hint(hint: Int) = Tweak[TextView](_.setHint(hint))

  /** Set hint */
  def hint(hint: Either[Int, CharSequence]) = hint match {
    case Right(t) ⇒ Tweak[TextView](_.setHint(t))
    case Left(t) ⇒ Tweak[TextView](_.setHint(t))
  }

  /**
    *
    * @param html
    * @param flag Only supported for api 25 and above
    * @return
    */

  @TargetApi(24)
  def html(html: String, flag: Int) =
    Tweak[TextView](_.setText(Html.fromHtml(html, flag)))

  @deprecated(message = "", since = "API 24")
  def html(html: String) =
    Tweak[TextView](_.setText(Html.fromHtml(html)))
}

private[macroid] trait EventTweaks {

  object On extends Dynamic {

    /** Set event handler */
    def applyDynamic[W <: View](event: String)(handler: Ui[Any]): Tweak[W] =
      macro EventTweakMacros.onUnitImpl[W]
  }

  object FuncOn extends Dynamic {

    /** Set event handler */
    def applyDynamic[W <: View](event: String)(handler: Any): Tweak[W] =
      macro EventTweakMacros.onFuncImpl[W]
  }
}

/** This trait provides the most useful tweaks. For an expanded set, see `contrib.ExtraTweaks` */
private[macroid] trait Tweaks
    extends BasicTweaks
    with VisibilityTweaks
    with AbilityTweaks
    with PaddingTweaks
    with LayoutTweaks
    with TextTweaks
    with EventTweaks

object Tweaks extends Tweaks

@bundle
class BasicTweakMacros(val c: blackbox.Context) {
  import c.universe._

  def wireImpl[W <: View: c.WeakTypeTag](v: c.Expr[W]): Tree = {
    q"_root_.macroid.Tweak[${weakTypeOf[W]}] { x ⇒ $v = x }"
  }

  def wireOptionImpl[W <: View: c.WeakTypeTag](v: c.Expr[Option[W]]): Tree = {
    q"_root_.macroid.Tweak[${weakTypeOf[W]}] { x ⇒ $v = Some(x) }"
  }
}

@bundle
class LayoutTweakMacros(val c: blackbox.Context) {
  import c.universe._

  def layoutParams(l: c.Type, params: Seq[c.Expr[Any]]) = {
    q"_root_.macroid.Tweak[_root_.android.view.View] { x ⇒ x.setLayoutParams(new ${l.typeSymbol.companion}.LayoutParams(..$params)) }"
  }

  def findLayoutParams(layoutType: c.Type, params: Seq[c.Expr[Any]]): Tree = {
    var tp = layoutType

    // go up the inheritance chain until we find a suitable LayoutParams class in the companion
    while (scala.util.Try(c.typecheck(layoutParams(tp, params))).isFailure) {
      if (tp.baseClasses.length > 2) {
        tp = tp.baseClasses(1).asType.toType
      } else {
        c.abort(c.enclosingPosition,
                "Could not find the appropriate LayoutParams constructor")
      }
    }
    layoutParams(tp, params)
  }

  def layoutParamsImpl[L <: ViewGroup: c.WeakTypeTag](
      params: c.Expr[Any]*): Tree = {
    findLayoutParams(c.weakTypeOf[L], params)
  }
}

@bundle
class EventTweakMacros(val c: blackbox.Context) {
  import c.universe._

  private def onBase(event: c.Expr[String], tp: c.Type) = {
    // find the setter
    val Expr(Literal(Constant(eventName: String))) = event
    val setter = scala.util.Try {
      val s =
        tp.member(TermName(s"setOn${eventName.capitalize}Listener")).asMethod
      assert(s != NoSymbol); s
    } getOrElse {
      c.abort(
        c.enclosingPosition,
        s"Could not find method setOn${eventName.capitalize}Listener in $tp. You may need to provide the type argument explicitly")
    }

    // find the method to override
    val listener = setter.paramLists(0)(0).typeSignature
    val on = scala.util.Try {
      val x = listener.member(TermName(s"on${eventName.capitalize}")).asMethod
      assert(x != NoSymbol); x
    } getOrElse {
      c.abort(c.enclosingPosition,
              s"Unsupported event listener class in $setter")
    }

    (setter, listener, on, tp)
  }

  sealed trait ListenerType
  object FuncListener extends ListenerType
  object UnitListener extends ListenerType

  def getListener(tpe: c.Type,
                  setter: c.universe.MethodSymbol,
                  listener: c.Type,
                  on: c.universe.MethodSymbol,
                  f: c.Expr[Any],
                  mode: ListenerType): Tree = {
    val args = on.paramLists(0).map(_ ⇒ TermName(c.freshName("arg")))
    val params = args zip on.paramLists(0) map {
      case (a, p) ⇒ q"val $a: ${p.typeSignature}"
    }
    lazy val argIdents = args.map(a ⇒ Ident(a))
    val impl = mode match {
      case FuncListener ⇒ q"$f(..$argIdents).get"
      case UnitListener ⇒ q"$f.get"
    }
    q"_root_.macroid.Tweak[$tpe] { x ⇒ x.$setter(new $listener { override def ${on.name.toTermName}(..$params) = $impl })}"
  }

  def onUnitImpl[W <: View: c.WeakTypeTag](event: c.Expr[String])(
      handler: c.Expr[Ui[Any]]): Tree = {
    val (setter, listener, on, tp) = onBase(event, weakTypeOf[W])
    scala.util.Try {
      if (!(on.returnType =:= typeOf[Unit])) assert((handler.actualType match {
        case TypeRef(_, _, t :: _) ⇒ t
      }) <:< on.returnType)
      getListener(tp,
                  setter,
                  listener,
                  on,
                  c.Expr(c.untypecheck(handler.tree)),
                  UnitListener)
    } getOrElse {
      c.abort(c.enclosingPosition,
              s"handler should be of type Ui[${on.returnType}]")
    }
  }

  def onFuncImpl[W <: View: c.WeakTypeTag](event: c.Expr[String])(
      handler: c.Expr[Any]): Tree = {
    val (setter, listener, on, tp) = onBase(event, weakTypeOf[W])
    scala.util.Try {
      c.typecheck(
        getListener(tp,
                    setter,
                    listener,
                    on,
                    c.Expr(c.untypecheck(handler.tree)),
                    FuncListener))
    } getOrElse {
      c.abort(c.enclosingPosition,
              s"handler should have type signature ${on.paramLists.head
                .mkString("(", ",", ")")}⇒Ui[${on.returnType}]")
    }
  }
}
