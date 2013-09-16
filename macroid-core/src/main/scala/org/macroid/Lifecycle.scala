package org.macroid

import scala.language.experimental.macros
import scala.reflect.macros.{ Context ⇒ MacroContext }
import scala.annotation.StaticAnnotation
import org.macroid.Util.ByName

trait LifecycleManager {
  def doOnPauseResume(p: ⇒ Any, r: ⇒ Any)
}

trait Lifecycle extends LifecycleManager { self ⇒
  private var toPause = List[ByName[Any]]()
  private var toResume = List[ByName[Any]]()

  def doOnPause(f: ⇒ Any) { toPause ::= ByName(f) }
  def doOnResume(f: ⇒ Any) { toResume ::= ByName(f) }
  def doOnPauseResume(p: ⇒ Any, r: ⇒ Any) {
    doOnPause(p)
    doOnResume(r)
  }

  implicit val lifecycleManager: LifecycleManager = self
}

class wireLifecycle extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro LifecycleMacros.macroTransform
}

object LifecycleMacros {
  def macroTransform(c: MacroContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    annottees(0).tree match {
      case ClassDef(mods, name, tparams, Template(parents, self, body)) ⇒
        val newbody = (body map {
          // shove to***.foreach(_()) into on***
          case DefDef(m, meth, tp, vps, tpt, rhs) if Set(newTermName("onPause"), newTermName("onResume"))(meth.toTermName) ⇒
            val to = meth.decoded.replace("on", "to")
            DefDef(m, meth, tp, vps, tpt, Block(rhs, q"$to.foreach(_())"))
          case x ⇒ x
        }) ::: (List("Pause", "Resume") flatMap { meth ⇒
          // override on*** methods that were not overriden in annottee
          val On = newTermName(s"on$meth")
          val To = newTermName(s"to$meth")
          if (!body.exists {
            case DefDef(_, On, _, _, _, _) ⇒ true
            case _ ⇒ false
          }) q"def $On() { super.$On(); $To.foreach(_()) }" :: Nil else Nil
        })
        c.Expr(Block(ClassDef(mods, name, tparams, Template(parents, self, newbody))))
      case _ ⇒ c.abort(c.enclosingPosition, "Expecting class definition")
    }
  }
}