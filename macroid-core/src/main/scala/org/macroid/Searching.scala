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
  /** Find a view with a given id */
  def findView(x: X, id: Int): View
}

trait CanManageFragments[-X] {
  /** Fragment manager */
  def fragmentManager(x: X): FragmentManager
}

private[macroid] trait Searching {
  val Id = new IdGen(1000)
  val Tag = new TagGen

  implicit class SearchingOps[X](x: X) {
    def findView[V <: View](id: Int)(implicit canFind: CanFindViews[X]) =
      Try(Option(canFind.findView(x, id)).map(_.asInstanceOf[V])).toOption.flatten

    def findFrag[F <: Fragment](tag: String)(implicit canManage: CanManageFragments[X]) =
      Try(Option(canManage.fragmentManager(x).findFragmentByTag(tag)).map(_.asInstanceOf[F])).toOption.flatten
  }

  implicit object viewCanFindViews extends CanFindViews[View] {
    def findView(x: View, id: Int) = x.findViewById(id)
  }

  implicit object activityCanFindViews extends CanFindViews[Activity] {
    def findView(x: Activity, id: Int) = x.findViewById(id)
  }

  implicit object fragmentActivityCanManageFragments extends CanManageFragments[FragmentActivity] {
    def fragmentManager(x: FragmentActivity) = x.getSupportFragmentManager
  }

  implicit object fragmentCanFindViewsAndManageFragments extends CanFindViews[Fragment] with CanManageFragments[Fragment] {
    def findView(x: Fragment, id: Int) = x.getView.findViewById(id)
    def fragmentManager(x: Fragment) = x.getChildFragmentManager
  }
}

object Searching extends Searching
