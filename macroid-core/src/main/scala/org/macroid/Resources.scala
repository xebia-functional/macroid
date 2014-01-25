package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.{ Context ⇒ MacroContext }

trait Resources {
  import ResourceMacros._

  case class Res(R: AnyRef) {
    object string extends Dynamic {
      def applyDynamic(id: String)(implicit ctx: AppContext): String = macro stringImpl
    }
  }
}

object ResourceMacros {
  def stringImpl(c: MacroContext)(id: c.Expr[String])(ctx: c.Expr[AppContext]) = {
    import c.universe._
    val Expr(Literal(Constant(idValue: String))) = id
    val R = c.prefix.tree
    ???
    c.Expr[String](q"$ctx.get.getResources.getString($R.string.${newTermName(idValue)})")
  }
}
