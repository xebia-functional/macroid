package org.macroid

import android.app.Activity
import android.content.Context
import scala.ref.WeakReference
import scala.annotation.implicitNotFound
import org.macroid.support.{ Fragment, FragmentApi }

@implicitNotFound("Could not find `AppContext`. If you are inside Activity or Fragment, extend Contexts[Activity] or Contexts[Fragment], otherwise pass an instance of `AppContext` from outside.") /** Global application context, which is safe to hold on to */
case class AppContext(app: Context) {
  def get = app
}

@implicitNotFound("Could not find `ActivityContext`. If you are inside Activity or Fragment, extend Contexts[Activity] or Contexts[Fragment], otherwise pass an instance of `ActivityContext` from outside.") /** Activity context, stored as a WeakReference */
case class ActivityContext(activity: WeakReference[Activity]) {
  def get = activity()
}
object ActivityContext {
  def apply(activity: Activity) = new ActivityContext(WeakReference(activity))
}

@implicitNotFound("Could not find `FragmentManagerContext[${F}, ${M}]`. If you are inside Activity or Fragment, extend Contexts[Activity] or Contexts[Fragment], otherwise pass an instance of `FragmentManagerContext` from outside.") /** FragmentManager context */
case class FragmentManagerContext[-F, M](manager: M)(implicit val fragmentApi: FragmentApi[F, M, _]) {
  def get = manager
}

trait Contexts[X] { self: X ⇒
  implicit def activityAppContext(implicit activity: X <:< Activity) =
    AppContext(activity(self).getApplicationContext)

  implicit def activityActivityContext(implicit activity: X <:< Activity) =
    ActivityContext(activity(self))

  implicit def fragmentAppContext(implicit fragment: Fragment[X]) =
    AppContext(fragment.activity(self).getApplicationContext)

  implicit def fragmentActivityContext(implicit fragment: Fragment[X]) =
    ActivityContext(fragment.activity(self))

  implicit def activityManagerContext[M, F, A >: X <: Activity](implicit fragmentApi: FragmentApi[F, M, A]) =
    FragmentManagerContext[F, M](fragmentApi.activityManager(self))

  implicit def fragmentManagerContext[M, F >: X, A <: Activity](implicit fragmentApi: FragmentApi[F, M, A]) =
    FragmentManagerContext[F, M](fragmentApi.fragmentManager(self))
}
