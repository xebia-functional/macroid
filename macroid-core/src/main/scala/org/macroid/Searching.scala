package org.macroid

import scala.language.dynamics

import android.view.View
import java.util.concurrent.atomic.AtomicInteger
import android.app.Activity
import org.macroid.util.SafeCast
import org.macroid.support.{ Fragment, FragmentApi }

/** A class to generate unique Ids */
class IdGen(start: Int) extends Dynamic {
  var ids = Map[String, Int]()
  val counter = new AtomicInteger(start)

  def selectDynamic(tag: String) = ids.get(tag) getOrElse {
    val id = counter.incrementAndGet()
    // TODO: this part does not seem thread-safe!
    ids += tag → id
    id
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
  def find[V <: View](x: X, id: Int): Option[V]
}

private[macroid] trait ViewFinding {
  implicit class ViewFindingOps[X](x: X)(implicit canFindViews: CanFindViews[X]) {
    def find[V <: View](id: Int) = canFindViews.find[V](x, id)
  }

  implicit object viewCanFindViews extends CanFindViews[View] {
    def find[V <: View](x: View, id: Int) = SafeCast[View, V](x.findViewById(id))
  }

  implicit object activityCanFindViews extends CanFindViews[Activity] {
    def find[V <: View](x: Activity, id: Int) = SafeCast[View, V](x.findViewById(id))
  }

  implicit def fragmentCanFindViews[F](implicit fragment: Fragment[F]) = new CanFindViews[F] {
    def find[V <: View](x: F, id: Int) = SafeCast[View, V](fragment.view(x).findViewById(id))
  }
}

trait CanFindFragments[-X, -F] {
  def find[F1 <: F](x: X, tag: String): Option[F1]
}

private[macroid] trait FragmentFinding {
  def findFrag[F] = new FragmentFinder[F]

  class FragmentFinder[F] {
    def apply[X](tag: String)(implicit managerCtx: FragmentManagerContext[F, X]) =
      managerCtx.fragmentApi.findFragmentByTag[F](managerCtx.get, tag)
  }

  implicit class FragmentFindingOps[X, F](x: X)(implicit canFindFragments: CanFindFragments[X, F]) {
    def findFrag[F1 <: F](tag: String) = canFindFragments.find[F1](x, tag)
  }

  implicit def activityCanFindFragments[F, M, A <: Activity](implicit fragmentApi: FragmentApi[F, M, A]) = new CanFindFragments[A, F] {
    def find[F1 <: F](x: A, tag: String) = fragmentApi.findFragmentByTag[F1](fragmentApi.activityManager(x), tag)
  }

  implicit def fragmentCanFindFragments[F, M, A <: Activity](implicit fragmentApi: FragmentApi[F, M, A]) = new CanFindFragments[F, F] {
    def find[F1 <: F](x: F, tag: String) = fragmentApi.findFragmentByTag[F1](fragmentApi.fragmentManager(x), tag)
  }
}

private[macroid] trait Searching extends ViewFinding with FragmentFinding

object Searching extends Searching
