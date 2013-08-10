package org.macroid

import android.os.Bundle
import scalaz.Monoid

object Util {
  def map2bundle(m: Map[String, Any]): Bundle = {
    val bundle = new Bundle
    m foreach {
      case (k, v: Int) ⇒ bundle.putInt(k, v)
      case (k, v: String) ⇒ bundle.putString(k, v)
      case (k, v: Boolean) ⇒ bundle.putBoolean(k, v)
      case _ ⇒ ???
    }
    bundle
  }

  class ByName[+A](v: ⇒ A) extends Function0[A] {
    def apply() = v
  }
  object ByName {
    def apply[A](v: ⇒ A) = new ByName(v)
  }
}
