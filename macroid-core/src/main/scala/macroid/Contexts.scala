package macroid

import android.app.{Service, Activity, Application}
import android.content.Context
import scala.ref.WeakReference
import scala.annotation.implicitNotFound
import macroid.support.{ Fragment, FragmentApi }

@implicitNotFound("""Could not find a `ContextWrapper`.
If you are inside Activity, Fragment or Service, extend Contexts[Activity], Contexts[Fragment] or Contexts[Service],
otherwise pass an instance of `ContextWrapper` from outside.""")
/** A wrapper that contains two contexts:
  * 1. the application context (which should be always alive)
  * 2. the current context, usually Activity or Service
  *    (which is more specific, but may die and is stored as a weak reference)
  */
sealed trait ContextWrapper {
  type C <: Context

  def original: WeakReference[C]
  def application: Context

  def getOriginal: C = original.get.get
  def bestAvailable: Context = original.get getOrElse application
}

case class ActivityContextWrapper(original: WeakReference[Activity], application: Context)
  extends ContextWrapper { type C = Activity }

case class ServiceContextWrapper(original: WeakReference[Service], application: Context)
  extends ContextWrapper { type C = Service }

case class ApplicationContextWrapper(original: WeakReference[Application], application: Context)
  extends ContextWrapper { type C = Application }

object ContextWrapper {
  def apply(activity: Activity): ActivityContextWrapper =
    ActivityContextWrapper(WeakReference(activity), activity.getApplicationContext)

  def apply(app: Application): ApplicationContextWrapper =
    ApplicationContextWrapper(WeakReference(app), app)

  def apply(service: Service): ServiceContextWrapper =
    ServiceContextWrapper(WeakReference(service), service.getApplicationContext)

  def apply[F](fragment: F)(implicit fragmentImpl: Fragment[F]): ActivityContextWrapper =
    ContextWrapper(fragmentImpl.activity(fragment))
}

@implicitNotFound("""Could not find `FragmentManagerContext[${F}, ${M}]`.
If you are inside Activity or Fragment, extend Contexts[Activity] or Contexts[Fragment],
otherwise pass an instance of `FragmentManagerContext` from outside.
Please note that for support fragments you need to extends Contexts[FragmentActivity]""")
/** FragmentManager context */
case class FragmentManagerContext[-F, M](manager: M)(implicit val fragmentApi: FragmentApi[F, M, _]) {
  def get = manager
}

trait Contexts[X] { self: X ⇒
  implicit def activityContextWrapper(implicit activity: X <:< Activity): ActivityContextWrapper =
    ContextWrapper(activity(self))

  implicit def fragmentContextWrapper(implicit fragment: Fragment[X]): ActivityContextWrapper =
    ContextWrapper(self)(fragment)

  implicit def serviceContextWrapper(implicit service: X <:< Service): ServiceContextWrapper =
    ContextWrapper(service(self))

  implicit def activityManagerContext[M, F, A >: X](implicit fragmentApi: FragmentApi[F, M, A]) =
    FragmentManagerContext[F, M](fragmentApi.activityManager(self))

  implicit def fragmentManagerContext[M, F >: X, A](implicit fragmentApi: FragmentApi[F, M, A]) =
    FragmentManagerContext[F, M](fragmentApi.fragmentManager(self))
}
