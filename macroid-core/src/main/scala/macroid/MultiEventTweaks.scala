package macroid

import scala.language.dynamics
import scala.language.experimental.macros
import android.view.View
import scala.reflect.macros.{Context ⇒ MacroContext}
import scala.language.postfixOps

private[macroid] trait MultiEventTweaks {

  import MultiEventTweakMacros._

  object UnitOn extends Dynamic {
    def applyDynamic[W <: View](event: String)(handler: Any): Tweak[W] = macro onUnitImpl[W]
  }

  object UnitFuncOn extends Dynamic {
    def applyDynamic[W <: View](event: String)(handler: Any): Tweak[W] = macro onUnitFuncImpl[W]
  }

  object BoolOn extends Dynamic {
    def applyDynamic[W <: View](event: String)(handler: Any): Tweak[W] = macro onBoolImpl[W]
  }

  object MultiOn extends Dynamic {
    def applyDynamic[W <: View](event: String)(handlers: (String => Any)*): Tweak[W] = macro onMultipleFuncImpl[W]
  }
}

object MultiEventTweakMacros {

  type UnitEvent = String
  
  type UnitFuncEvent = String

  type BoolEvent = String

  sealed trait HandlerType

  case object HUnit extends HandlerType

  case object HUnitFunc extends HandlerType

  case object HBool extends HandlerType

  def onUnitImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handler: c.Expr[Any]) = {
    import c.universe._

    val Expr(Literal(Constant(e: String))) = event

    onMultipleFuncImpl(c)(event)(c.Expr[UnitEvent => Any](q"(${newTermName(s"on${e.capitalize}")}: UnitEvent) => $handler"))
  }

  def onUnitFuncImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handler: c.Expr[Any]) = {
    import c.universe._

    val Expr(Literal(Constant(e: String))) = event

    onMultipleFuncImpl(c)(event)(c.Expr[UnitFuncEvent => Any](q"(${newTermName(s"on${e.capitalize}")}: UnitFuncEvent) => $handler"))
  }

  def onBoolImpl[W <: View : c.WeakTypeTag](c: MacroContext)(event: c.Expr[String])(handler: c.Expr[Any]) = {
    import c.universe._

    val Expr(Literal(Constant(e: String))) = event

    onMultipleFuncImpl(c)(event)(c.Expr[BoolEvent => Any](q"(${newTermName(s"on${e.capitalize}")}: BoolEvent) => $handler"))
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
      val (name, typ) = h.tree.children.head match {
        case ValDef(_, n, tpt, _) => (n, tpt)
      }

      val eventName = name match {
        case TermName(tn: String) => tn
      }

      val handlerType = typ.toString() match {
        case s: String if s.endsWith("UnitEvent") => HUnit
        case s: String if s.endsWith("UnitFuncEvent") => HUnitFunc
        case s: String if s.endsWith("BoolEvent") => HBool
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

    val pendingMembersToOverride = listener.members filter (m => m.isAbstract && m.isMethod) toSet
    val newSet = if (pendingMembersToOverride.nonEmpty) {
      val specifiedMembers = handlerOnMethodTupleSequence map (t => t._2) toSet
      val notImplemented = pendingMembersToOverride.asInstanceOf[Set[MethodSymbol]] diff specifiedMembers

      val defaultHandlers = notImplemented map { m =>
        val defaultHandler = c.Expr[Nothing](q"(${m.name}: macroid.MultiEventTweakMacros.UnitFuncEvent) => macroid.Ui")

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
      case HBool =>
        val lastBodyLineOfCode = f.tree.children.lastOption

        lastBodyLineOfCode match {
          case Some(Literal(Constant(b: Boolean))) =>
            val children = f.tree.children.dropRight(1)
            val newF = c.Expr[Any](q"{..$children}")
            q"($newF ~ macroid.Ui { $b }).get"
          case Some(q"$someValue.get") =>
            q"$f"
          case None =>
            val newF = c.Expr[Any](q"{macroid.Ui.nop}")
            q"($newF ~ macroid.Ui { false }).get"
          case _ =>
            c.abort(c.enclosingPosition, s"The ${on.name.toTermName} handler must returns a boolean. You must end it with " +
                s"true or false values, or doing something like this if you want return `true`: \n" +
                s"val yourUiCode = dummyAnim(circle, color) ~ Ui { true }\n" +
                s"yourUiCode.get\n")
        }
    }
    q"override def ${on.name.toTermName}(..$params) = $impl"
  }
}