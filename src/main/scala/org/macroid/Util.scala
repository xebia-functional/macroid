package org.macroid

import android.os.Bundle

object Util {
  def map2bundle(m: Map[String, Any]): Bundle = {
    val bundle = new Bundle
    m foreach {
      case (k, v: Int) ⇒ bundle.putInt(k, v)
      case (k, v: String) ⇒ bundle.putString(k, v)
      case (k, v: Boolean) ⇒ bundle.putBoolean(k, v)
      case _ ⇒ ;
    }
    bundle
  }
}
