package org.macroid

import scala.language.dynamics
import scala.language.experimental.macros

import android.view.View
import android.support.v4.app.{ FragmentActivity, Fragment, FragmentManager }
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Try
import android.app.Activity

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

trait CanFindViews[-X] {
  def find(x: X, id: Int): View
}

trait IdGeneration {
  val Id = new IdGen(1000)
  val Tag = new TagGen
}

private[macroid] trait Searching {
  private def safeCast[From, To](x: From) =
    Try(Option(x).map(_.asInstanceOf[To])).toOption.flatten

  def findFrag[F <: Fragment](tag: String)(implicit manager: ManagerContext) =
    safeCast[Fragment, F](manager.get.findFragmentByTag(tag))

  implicit class SearchingOps[X](x: X)(implicit canFind: CanFindViews[X]) {
    def find[V <: View](id: Int) = safeCast[View, V](canFind.find(x, id))
  }

  implicit object viewCanFindViews extends CanFindViews[View] {
    def find(x: View, id: Int) = x.findViewById(id)
  }

  implicit object activityCanFindViews extends CanFindViews[Activity] {
    def find(x: Activity, id: Int) = x.findViewById(id)
  }

  implicit object fragmentCanFindViews extends CanFindViews[Fragment] {
    def find(x: Fragment, id: Int) = x.getView.findViewById(id)
  }
}

object Searching extends Searching
