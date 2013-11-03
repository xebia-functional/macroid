package org.macroid

import scala.language.experimental.macros
import android.os.Bundle
import scala.reflect.macros.{ Context ⇒ MacroContext }
import scalaz.Monoid

trait Bundles {
  import BundleMacros._
  def bundle(pairs: (String, Any)*) = macro bundleImpl

  implicit class RichBundle(b: Bundle) {
    def +(other: Bundle) = {
      val c = new Bundle
      c.putAll(b)
      c.putAll(other)
      c
    }
  }

  implicit object bundleMonoid extends Monoid[Bundle] {
    def append(b1: Bundle, b2: ⇒ Bundle) = b1 + b2
    def zero = new Bundle
  }
}

object Bundles extends Bundles

object BundleMacros {
  def bundleImpl(c: MacroContext)(pairs: c.Expr[(String, Any)]*) = {
    import c.universe._
    val (singular, plural) = weakTypeOf[Bundle].members
      .filter(_.name.toString.startsWith("put"))
      .filterNot(_.name.toString == "putAll")
      .map(_.asMethod)
      .partition(x ⇒ (!x.name.toString.endsWith("Array") && !x.name.toString.endsWith("List")) || x.name.toString.contains("Sparse"))
    val (plain, able) = singular.partition(!_.name.toString.endsWith("able"))
    val b = newTermName(c.fresh("bundle"))
    val puts = pairs map { pair ⇒
      val TypeRef(_, _, List(_, value)) = pair.actualType
      val put = plain.find(value =:= _.paramss(0)(1).typeSignature) orElse
        able.find(value <:< _.paramss(0)(1).typeSignature) getOrElse
        c.abort(pair.tree.pos, s"Could not put $value in a Bundle")
      q"$b.$put($pair._1, $pair._2)"
    }
    c.Expr[Bundle](q"val $b = new android.os.Bundle; ..$puts; $b")
  }
}