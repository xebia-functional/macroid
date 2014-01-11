package org.macroid.util

import scala.reflect.macros.{ Context ⇒ MacroContext }

private[macroid] object MacroUtils {
  /**
   * Find the immediate parent tree where `parent` is defined, and return the result of
   * applying `parent` to it.
   */
  def fromImmediateParentTree[A](c: MacroContext)(parent: PartialFunction[c.Tree, A]) = {
    import c.universe._

    // a parent contains the current macro application
    def isParent(x: Tree) = parent.isDefinedAt(x) & x.find(_.pos == c.macroApplication.pos).isDefined

    // an immediate parent is a parent and contains no other parents
    val enclosure = c.enclosingMethod.orElse(c.enclosingClass)
    enclosure.find { x ⇒
      isParent(x) && x.children.forall(_.find(isParent).isEmpty)
    } map { x ⇒
      parent(x)
    }
  }

  /** `List[Option[Future[X]]]` => `X` */
  def innerType(c: MacroContext)(t: c.Type): Option[c.Type] = {
    import c.universe._
    t match {
      case TypeRef(_, x, Nil) ⇒ Some(x.asType.toType)
      case TypeRef(_, _, x :: Nil) ⇒ innerType(c)(x)
      case _ ⇒ None
    }
  }
}
