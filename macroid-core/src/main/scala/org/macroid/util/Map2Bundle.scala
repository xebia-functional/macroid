package org.macroid.util

import android.os.Bundle

object Map2Bundle {
  def apply(m: Map[String, Any]): Bundle = {
    val bundle = new Bundle
    m foreach {
      case (k, v: Int) ⇒ bundle.putInt(k, v)
      case (k, v: String) ⇒ bundle.putString(k, v)
      case (k, v: Boolean) ⇒ bundle.putBoolean(k, v)
      case _ ⇒ ??? // TODO: support more things here!
    }
    bundle
  }
}
