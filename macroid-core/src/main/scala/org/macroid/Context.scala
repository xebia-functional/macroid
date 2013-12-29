package org.macroid

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import scala.ref.WeakReference

/** Global application context, which is safe to hold on to */
case class AppContext(ctx: Context) {
  def get = ctx
}

/** Activity context, which is not recommended to hold on to */
case class ActivityContext(ctx: WeakReference[Activity]) {
  def get = ctx()
}

trait ActivityContexts { self: Activity ⇒
  implicit def activityCtx = ActivityContext(WeakReference(this))
  implicit def appCtx = AppContext(getApplicationContext)
}

trait FragmentContexts { self: Fragment ⇒
  implicit def activityCtx = ActivityContext(WeakReference(getActivity))
  implicit def appCtx = AppContext(getActivity.getApplicationContext)
}