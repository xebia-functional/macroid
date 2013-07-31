package org.macroid

import scala.language.dynamics
import android.view.View
import android.support.v4.app.{ FragmentActivity, Fragment, FragmentManager }
import java.util.concurrent.atomic.AtomicInteger

class IdGen(start: Int) extends Dynamic {
  var ids = Map[String, Int]()
  val counter = new AtomicInteger(start)

  def selectDynamic(tag: String) = ids.get(tag) getOrElse {
    val id = counter.incrementAndGet()
    ids += tag → id
    id
  }
}

class TagGen extends Dynamic {
  def selectDynamic(tag: String) = tag
}

sealed trait ViewSearch {
  def fragmentManager: FragmentManager
  implicit val Id = new IdGen(1000)
  implicit val Tag = new TagGen

  def findView[A <: View](root: View, id: Int): A = root.findViewById(id).asInstanceOf[A]
  def findView[A <: View](id: Int): A
  def findFrag[A <: Fragment](tag: String) = fragmentManager.findFragmentByTag(tag).asInstanceOf[A]
}

trait ActivityViewSearch extends ViewSearch { self: FragmentActivity ⇒
  def fragmentManager = getSupportFragmentManager
  def findView[A <: View](id: Int) = findViewById(id).asInstanceOf[A]
}

trait FragmentViewSearch extends ViewSearch { self: Fragment ⇒
  def fragmentManager = getChildFragmentManager
  def findView[A <: View](id: Int) = getView.findViewById(id).asInstanceOf[A]
}