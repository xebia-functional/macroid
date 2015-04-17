package macroid

import scala.language.dynamics
import scala.language.experimental.macros
import android.view.View
import scala.reflect.macros.{Context ⇒ MacroContext}
import scala.language.postfixOps

private[macroid] trait MultiEventTweaks {

  import MultiEventTweakMacros._

  type UnitHandler = String

  type FuncHandler = String

  sealed trait HandlerType

  case object HUnit extends HandlerType

  case object HUnitFunc extends HandlerType

  object UnitOn extends Dynamic {
    def applyDynamic[W <: View](event: String)(handler: Any): Tweak[W] = macro onUnitImpl[W]
  }

  object UnitFuncOn extends Dynamic {
    def applyDynamic[W <: View](event: String)(handler: Any): Tweak[W] = macro onUnitFuncImpl[W]
  }

  object MultiOn extends Dynamic {
    def applyDynamic[W <: View](event: String)(handlers: (String => Any)*): Tweak[W] = macro onMultipleFuncImpl[W]
  }
}

object MultiEventTweaks extends MultiEventTweaks

private object MultiEventTweakMacros {

  import MultiEventTweaks._

  def onUnitImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handler: c.Expr[Any]) = {
    import c.universe._

    val Expr(Literal(Constant(e: String))) = event

    onMultipleFuncImpl(c)(event)(c.Expr[UnitHandler => Any](q"(${newTermName(s"on${e.capitalize}")}: UnitHandler) => $handler"))
  }

  def onUnitFuncImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handler: c.Expr[Any]) = {
    import c.universe._

    val Expr(Literal(Constant(e: String))) = event

    onMultipleFuncImpl(c)(event)(c.Expr[FuncHandler => Any](q"(${newTermName(s"on${e.capitalize}")}: FuncHandler) => $handler"))
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
        case s: String if s.endsWith("UnitHandler") => HUnit
        case s: String if s.endsWith("FuncHandler") => HUnitFunc
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
        val defaultHandler = c.Expr[Nothing](q"(${m.name.toTermName}: macroid.MultiEventTweakMacros.UnitFuncEvent) => _root_.macroid.Ui")

        (defaultHandler, m, HUnitFunc)
      }

      handlerOnMethodTupleSequence ++ defaultHandlers
    } else handlerOnMethodTupleSequence

    (listener, newSet)
  }

  def getOverride(c: MacroContext)(on: c.universe.MethodSymbol, f: c.Expr[Any], handlerType: HandlerType) = {
    import c.universe._

    val args = on.paramss(0).map(_ ⇒ newTermName(c.fresh("arg")))
    val params = args zip on.paramss(0) map { case (a, p) ⇒ q"val $a: ${p.typeSignature}" }
    lazy val argIdents = args.map(a ⇒ Ident(a))

    val impl = handlerType match {
      case HUnit => q"$f.get"
      case HUnitFunc => q"$f(..$argIdents).get"
    }
    q"override def ${on.name.toTermName}(..$params) = $impl"
  }
}

