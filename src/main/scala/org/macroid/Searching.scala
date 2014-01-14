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

trait CanFindById[-X, Y] {
  def find(x: X, id: Int): Option[Y]
}

trait CanFindByTag[-X, Y] {
  def find(x: X, tag: String): Option[Y]
}

trait CanManageFragments[-X] {
  def fragmentManager(x: X): FragmentManager
}

trait IdGeneration {
  val Id = new IdGen(1000)
  val Tag = new TagGen
}

private[macroid] trait Searching {
  def contentView[X](implicit hasContentView: X, canFind: CanFindById[X, View]) = hasContentView

  implicit class SearchingOps[X](x: X) {
    def find[Y](id: Int)(implicit canFind: CanFindById[X, Y]) = canFind.find(x, id)
    def find[Y](tag: String)(implicit canFind: CanFindByTag[X, Y]) = canFind.find(x, tag)
  }

  private def safeCast[From, To](x: From) =
    Try(Option(x).map(_.asInstanceOf[To])).toOption.flatten

  // format: off

  implicit def viewCanFindView[Y <: View] =
    new (View CanFindById Y) {
      def find(x: View, id: Int) = safeCast[View, Y](x.findViewById(id))
    }

  implicit def activityCanFindView[Y <: View] =
    new (Activity CanFindById Y) {
      def find(x: Activity, id: Int) = safeCast[View, Y](x.findViewById(id))
    }

  implicit def fragmentCanFindView[Y <: View] =
    new (Fragment CanFindById Y) {
      def find(x: Fragment, id: Int) = safeCast[View, Y](x.getView.findViewById(id))
    }

  implicit def managerCanFindFragmentById[X, Y <: Fragment](implicit canManage: CanManageFragments[X]) =
    new (X CanFindById Y) {
      def find(x: X, id: Int) = safeCast[Fragment, Y](canManage.fragmentManager(x).findFragmentById(id))
    }

  implicit def managerCanFindFragmentByTag[X, Y <: Fragment](implicit canManage: CanManageFragments[X]) =
    new (X CanFindByTag Y) {
      def find(x: X, tag: String) = safeCast[Fragment, Y](canManage.fragmentManager(x).findFragmentByTag(tag))
    }

  implicit object fragmentActivityCanManageFragments
    extends CanManageFragments[FragmentActivity] {
    def fragmentManager(x: FragmentActivity) = x.getSupportFragmentManager
  }

  implicit object fragmentCanManageFragments
    extends CanManageFragments[Fragment] {
    def fragmentManager(x: Fragment) = x.getChildFragmentManager
  }

  // format: on
}

object Searching extends Searching
