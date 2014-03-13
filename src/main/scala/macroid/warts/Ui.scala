package macroid.warts

import org.brianmckenna.wartremover.{ WartTraverser, WartUniverse }

object Ui extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def checkUiStatements(statements: List[Tree]) = statements foreach { stat ⇒
      if (stat.tpe != null && stat.tpe <:< typeOf[macroid.util.Ui[_]])
        u.error(stat.pos, s"This statement returns an Ui action, but does not actually perform it. Call the `run` method or wrap it in `runUi`.")
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
