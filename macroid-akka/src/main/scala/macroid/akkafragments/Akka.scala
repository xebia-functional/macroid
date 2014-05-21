package macroid.akkafragments

import akka.actor.{ ActorSelection, ActorSystem, Actor }
import android.support.v4.app.Fragment
import com.typesafe.config.ConfigFactory
import android.app.Activity
import scala.reflect.ClassTag
import macroid.util.Ui

trait AkkaActivity { self: Activity ⇒
  val actorSystemName: String
  lazy val actorSystem = ActorSystem(
    actorSystemName,
    ConfigFactory.load(getApplication.getClassLoader),
    getApplication.getClassLoader
  )
}

trait AkkaFragment extends Fragment {
  def actorSystem = getActivity.asInstanceOf[AkkaActivity].actorSystem
  def actor: Option[ActorSelection]

  abstract override def onStart() = {
    super.onStart()
    actor.foreach(_ ! FragmentActor.AttachUi(this))
  }

  abstract override def onStop() = {
    super.onStop()
    actor.foreach(_ ! FragmentActor.DetachUi)
  }
}

object FragmentActor {
  case class AttachUi[F <: Fragment](fragment: F)
  case object DetachUi
}

abstract class FragmentActor[F <: Fragment: ClassTag] extends Actor {
  import FragmentActor._

  private var attachedUi: Option[F] = None

  def withUi(f: F ⇒ Ui[Any]) = attachedUi.fold(()) { frag ⇒
    f(frag).run
  }

  def receiveUi: PartialFunction[Any, Any] = {
    case a @ AttachUi(f: F) ⇒ attachedUi = Some(f); a
    case d @ DetachUi ⇒ attachedUi = None; d
    case x ⇒ x
  }
}
