package macroid

import scala.language.{postfixOps, dynamics}
import scala.language.experimental.macros
import android.text.Html
import android.view.{ ViewGroup, View }
import android.widget.{ LinearLayout, TextView }
import scala.reflect.macros.{ Context ⇒ MacroContext }

private[macroid] trait BasicTweaks {
  import BasicTweakMacros._

  /** Set this view’s id */
  def id(id: Int) = Tweak[View](_.setId(id))

  /** Attach arbitrary value to a view by means of setTag */
  def hold[A](value: A) = Tweak[View](_.setTag(value))

  /** Assign the view to the provided `var` */
  def wire[W <: View](v: W): Tweak[W] = macro wireImpl[W]
  /** Assign the view to the provided slot */
  def wire[W <: View](v: Option[W]): Tweak[W] = macro wireOptionImpl[W]
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
  def padding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, all: Int = -1) = if (all >= 0) {
    Tweak[View](_.setPadding(all, all, all, all))
  } else {
    Tweak[View](_.setPadding(left, top, right, bottom))
  }
}

private[macroid] trait LayoutTweaks {
  import LayoutTweakMacros._

  /** Use `LayoutParams` of the specified layout class */
  def layoutParams[L <: ViewGroup](params: Any*): Tweak[View] = macro layoutParamsImpl[L]
  /** Use `LayoutParams` of the specified layout class */
  def lp[L <: ViewGroup](params: Any*): Tweak[View] = macro layoutParamsImpl[L]

  /** Make this layout vertical */
  val vertical = Tweak[LinearLayout](_.setOrientation(LinearLayout.VERTICAL))
  /** Make this layout horizontal */
  val horizontal = Tweak[LinearLayout](_.setOrientation(LinearLayout.HORIZONTAL))

  /** Add views to the layout */
  def addViews(children: Seq[Ui[View]], removeOld: Boolean = false, reverse: Boolean = false) = Tweak[ViewGroup] { x ⇒
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

  def html(html: String) = Tweak[TextView](_.setText(Html.fromHtml(html)))
}

private[macroid] trait EventTweaks {
  import EventTweakMacros._

  type UnitListener = String

  type FuncListener = String

  object On extends Dynamic {
    /** Set event handler */
    def applyDynamic[W <: View](event: String)(handler: Ui[Any]): Tweak[W] = macro onUnitImpl[W]
  }

  object FuncOn extends Dynamic {
    /** Set event handler */
    def applyDynamic[W <: View](event: String)(handler: Any): Tweak[W] = macro onUnitFuncImpl[W]
  }

  object MultiOn extends Dynamic {
    /** Set all event handlers */
    def applyDynamic[W <: View](event: String)(handlers: (String => Any)*): Tweak[W] = macro onMultipleFuncImpl[W]
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

object BasicTweakMacros {
  def wireImpl[W <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[W]): c.Expr[Tweak[W]] = {
    import c.universe._
    c.Expr[Tweak[W]](q"_root_.macroid.Tweak[${weakTypeOf[W]}] { x ⇒ $v = x }")
  }

  def wireOptionImpl[W <: View: c.WeakTypeTag](c: MacroContext)(v: c.Expr[Option[W]]): c.Expr[Tweak[W]] = {
    import c.universe._
    c.Expr[Tweak[W]](q"_root_.macroid.Tweak[${weakTypeOf[W]}] { x ⇒ $v = Some(x) }")
  }
}

object LayoutTweakMacros {
  def layoutParams(c: MacroContext)(l: c.Type, params: Seq[c.Expr[Any]]) = {
    import c.universe._
    q"_root_.macroid.Tweak[_root_.android.view.View] { x ⇒ x.setLayoutParams(new ${l.typeSymbol.companionSymbol}.LayoutParams(..$params)) }"
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

  def layoutParamsImpl[L <: ViewGroup: c.WeakTypeTag](c: MacroContext)(params: c.Expr[Any]*): c.Expr[Tweak[View]] = {
    findLayoutParams(c)(c.weakTypeOf[L], params)
  }
}

object EventTweakMacros {

  sealed trait ListenerType

  object FuncListener extends ListenerType

  object UnitListener extends ListenerType

  def onUnitImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handler: c.Expr[Any]) = {
    import c.universe._

    val Expr(Literal(Constant(e: String))) = event

    val h = q"(${newTermName(s"on${e.capitalize}")}: _root_.macroid.Tweaks.UnitListener) => $handler"
    onMultipleFuncImpl(c)(event)(c.Expr[Tweaks.UnitListener => Any](h))
  }

  def onUnitFuncImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handler: c.Expr[Any]) = {
    import c.universe._

    val Expr(Literal(Constant(e: String))) = event

    val h = q"(${newTermName(s"on${e.capitalize}")}: _root_.macroid.Tweaks.FuncListener) => $handler"
    onMultipleFuncImpl(c)(event)(c.Expr[Tweaks.FuncListener => Any](h))
  }

  def onMultipleFuncImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handlers: (c.Expr[String => Any])*) = {
    import c.universe._

    val tpe = weakTypeOf[W]
    val (_, setter) = findSetter(c)(event, tpe)

    val (listener, handlerOnMethodTupleSequence) = onBaseMultipleHandlers(c)(setter, handlers)

    val overrides = handlerOnMethodTupleSequence map { case (handler, on, handlerType) =>
      getOverride(c)(on, c.Expr(c.resetLocalAttrs(handler.tree.children.last)), handlerType)
    }

    c.Expr[Tweak[View]](q"_root_.macroid.Tweak[$tpe] { x ⇒ x.$setter(new $listener { ..$overrides })}")
  }

  def findSetter(c: MacroContext)(event: c.Expr[String], tp: c.Type) = {
    import c.universe._

    val Expr(Literal(Constant(eventName: String))) = event
    val setter = scala.util.Try {
      val s = tp.member(newTermName(s"setOn${eventName.capitalize}Listener")).asMethod
      assert(s != NoSymbol)
      s
    } getOrElse {
      c.abort(c.enclosingPosition, s"Could not find method setOn${eventName.capitalize}Listener in $tp. " +
          s"You may need to provide the type argument explicitly")
    }
    (eventName, setter)
  }

  def onBaseMultipleHandlers(c: MacroContext)(setter: c.universe.MethodSymbol, handlers: Seq[c.Expr[String => Any]]) = {
    import c.universe._

    val listener = setter.paramss(0)(0).typeSignature
    val handlerOnMethodTupleSequence = handlers map { h =>
      val (eventName, typ) = h.tree.children.head match {
        case ValDef(_, name, tpt, _) => (name.toString, tpt)
      }

      val handlerType = typ.toString() match {
        case s: String if s.endsWith("UnitListener") => UnitListener
        case s: String if s.endsWith("FuncListener") => FuncListener
      }

      val on = scala.util.Try {
        val x = listener.member(newTermName(s"$eventName")).asMethod
        assert(x != NoSymbol)
        x
      } getOrElse {
        c.abort(c.enclosingPosition, s"Unsupported event listener class in $setter")
      }

      (h, on, handlerType)
    }

    val pendingMembersToOverride = listener.members filter (m => m.isMethod
        && m.name.toString.startsWith("on")) toSet

    val newSet = if (pendingMembersToOverride.nonEmpty) {
      val specifiedMembers = handlerOnMethodTupleSequence map (t => t._2) toSet
      val notImplemented = pendingMembersToOverride.asInstanceOf[Set[MethodSymbol]] diff specifiedMembers

      val defaultHandlers = notImplemented map { m =>
        val defaultHandler = c.Expr[Nothing](q"(${m.name.toTermName}: FuncListener) => _root_.macroid.Ui")

        (defaultHandler, m, FuncListener)
      }

      handlerOnMethodTupleSequence ++ defaultHandlers
    } else handlerOnMethodTupleSequence

    (listener, newSet)
  }

  def getOverride(c: MacroContext)(on: c.universe.MethodSymbol, f: c.Expr[Any], handlerType: ListenerType) = {
    import c.universe._

    val args = on.paramss(0).map(_ ⇒ newTermName(c.fresh("arg")))
    val params = args zip on.paramss(0) map { case (a, p) ⇒ q"val $a: ${p.typeSignature}" }
    lazy val argIdents = args.map(a ⇒ Ident(a))

    val impl = handlerType match {
      case UnitListener => q"$f.get"
      case FuncListener => q"$f(..$argIdents).get"
    }
    q"override def ${on.name.toTermName}(..$params) = $impl"
  }
}