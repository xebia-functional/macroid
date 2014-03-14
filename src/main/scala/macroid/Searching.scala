package macroid

import scala.language.dynamics

import android.view.View
import android.app.Activity
import macroid.util.{ Ui, SafeCast }
import macroid.support.{ Fragment, FragmentApi }

/** A class to generate unique Ids */
class IdGen(start: Int) extends Dynamic {
  private var ids = Map[String, Int]()
  private var counter = start

  private val lock = new Object

  def selectDynamic(tag: String) = lock synchronized {
    ids.get(tag) getOrElse {
      counter += 1
      ids += tag → counter
      counter
    }
  }
}

/** A toy class to allow more descriptive syntax for tags (Tag.foo instead of "foo") */
class TagGen extends Dynamic {
  def selectDynamic(tag: String) = tag
}

trait IdGeneration {
  val Id = new IdGen(1000)
  val Tag = new TagGen
}

trait CanFindViews[-X] {
  def find[V <: View](x: X, id: Int): Ui[Option[V]]
}

object CanFindViews {
  implicit object `View can find views` extends CanFindViews[View] {
    def find[V <: View](x: View, id: Int) = Ui(SafeCast[View, V](x.findViewById(id)))
  }

  implicit object `Activity can find views` extends CanFindViews[Activity] {
    def find[V <: View](x: Activity, id: Int) = Ui(SafeCast[View, V](x.findViewById(id)))
  }

  implicit def `Fragment can find views`[F](implicit fragment: Fragment[F]) = new CanFindViews[F] {
    def find[V <: View](x: F, id: Int) = Ui(SafeCast[View, V](fragment.view(x).findViewById(id)))
  }
}

private[macroid] trait ViewFinding {
  implicit class ViewFindingOps[X](x: X)(implicit canFindViews: CanFindViews[X]) {
    def find[V <: View](id: Int) = canFindViews.find[V](x, id)
  }
}

trait CanFindFragments[-X, -F] {
  def find[F1 <: F](x: X, tag: String): Ui[Option[F1]]
}

object CanFindFragments {
  implicit def `Activity can find fragments`[F, M, A](implicit fragmentApi: FragmentApi[F, M, A]) = new CanFindFragments[A, F] {
    def find[F1 <: F](x: A, tag: String) = Ui(fragmentApi.findFragmentByTag[F1](fragmentApi.activityManager(x), tag))
  }

  implicit def `Fragment can find fragments`[F, M, A](implicit fragmentApi: FragmentApi[F, M, A]) = new CanFindFragments[F, F] {
    def find[F1 <: F](x: F, tag: String) = Ui(fragmentApi.findFragmentByTag[F1](fragmentApi.fragmentManager(x), tag))
  }

  implicit def `FragmentManager can find fragments`[F, M, A](implicit fragmentApi: FragmentApi[F, M, A]) = new CanFindFragments[M, F] {
    def find[F1 <: F](x: M, tag: String) = Ui(fragmentApi.findFragmentByTag[F1](x, tag))
  }
}

private[macroid] trait FragmentFinding {
  implicit class FragmentFindingOps[X, F](x: X)(implicit canFindFragments: CanFindFragments[X, F]) {
    def findFrag[F1 <: F](tag: String) = canFindFragments.find[F1](x, tag)
  }
}

private[macroid] trait Searching extends ViewFinding with FragmentFinding

object Searching extends Searching
