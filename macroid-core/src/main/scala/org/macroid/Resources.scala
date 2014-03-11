package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.{ Context ⇒ MacroContext }

private[macroid] trait Resources {
  import ResourceMacros._

  case class Res(R: AnyRef) {
    object string extends Dynamic {
      def selectDynamic(id: String)(implicit ctx: AppContext): String = macro stringImpl
    }
  }
}

object Resources extends Resources

object ResourceMacros {
  def stringImpl(c: MacroContext)(id: c.Expr[String])(ctx: c.Expr[AppContext]) = {
    import c.universe._
    val Expr(Literal(Constant(idValue: String))) = id
    val Select(Apply(_, List(r)), _) = c.prefix.tree
    c.Expr[String](q"$ctx.get.getResources.getString($r.string.${newTermName(idValue)})")
  }
}
