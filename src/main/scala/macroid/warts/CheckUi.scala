package macroid.warts

import org.brianmckenna.wartremover.{ WartTraverser, WartUniverse }

object CheckUi extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def checkUiStatements(statements: List[Tree]) = statements foreach {
      case LabelDef(_, _, _) ⇒
      case stat ⇒
        if (stat.tpe != null && stat.tpe <:< typeOf[macroid.util.Ui[_]])
          u.error(stat.pos, s"This statement returns an Ui action, but does not actually perform it. Call the `run` method, wrap it in `runUi`, or combine it with other Ui actions.")
    }

    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case Block(statements, _) ⇒
            checkUiStatements(statements)
          case ClassDef(_, _, _, Template((_, _, statements))) ⇒
            checkUiStatements(statements)
          case ModuleDef(_, _, Template((_, _, statements))) ⇒
            checkUiStatements(statements)
          case _ ⇒
        }
        super.traverse(tree)
      }
    }
  }
}
