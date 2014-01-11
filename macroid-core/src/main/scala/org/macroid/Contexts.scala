package org.macroid

import scala.language.experimental.macros
import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import scala.ref.WeakReference
import scala.reflect.macros.{ Context ⇒ MacroContext }
import scala.annotation.implicitNotFound

@implicitNotFound("Could not find `AppContext`. If you are inside Activity or Fragment, add `implicit val context = this`, otherwise pass an instance of `AppContext` from outside.") /** Global application context, which is safe to hold on to */
case class AppContext(ctx: Context) {
  def get = ctx
}

@implicitNotFound("Could not find `ActivityContext`. If you are inside Activity or Fragment, add `implicit val context = this`, otherwise pass an instance of `ActivityContext` from outside.") /** Activity context, which is not recommended to hold on to */
case class ActivityContext(activity: WeakReference[Activity]) {
  def get = activity()
}

trait Contexts {
  import ContextMacros._

  implicit def activity2app(implicit activity: Activity): AppContext =
    AppContext(activity.getApplicationContext)

  implicit def activity2activity(implicit activity: Activity): ActivityContext =
    ActivityContext(WeakReference(activity))

  implicit def fragment2app(implicit fragment: Fragment): AppContext =
    AppContext(fragment.getActivity.getApplicationContext)

  implicit def fragment2activity(implicit fragment: Fragment): ActivityContext =
    ActivityContext(WeakReference(fragment.getActivity))

  implicit def inferActivity: Activity = macro inferActivityImpl
  implicit def inferFragment: Fragment = macro inferFragmentImpl
}

object Contexts extends Contexts

object ContextMacros {
  def inferActivityImpl(c: MacroContext) = {
    import c.universe._
    c.Expr[Activity](q"this")
  }

  def inferFragmentImpl(c: MacroContext) = {
    import c.universe._
    c.Expr[Fragment](q"this")
  }
}
