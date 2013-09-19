package org.macroid

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment

trait ActivityContext { self: Activity ⇒
  implicit lazy val ctx: Context = this
}

trait FragmentContext { self: Fragment ⇒
  implicit def ctx: Context = getActivity
}