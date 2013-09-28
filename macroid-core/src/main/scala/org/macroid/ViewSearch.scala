package org.macroid

import scala.language.dynamics
import android.view.View
import android.support.v4.app.{ FragmentActivity, Fragment, FragmentManager }
import java.util.concurrent.atomic.AtomicInteger

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

/** This trait provides view searching functionality that does not rely on either Activity or Fragment methods */
trait BasicViewSearch {
  val Id = new IdGen(1000)
  val Tag = new TagGen

  /** Find a view with a given id in `root` */
  def findView[A <: View](root: View, id: Int): Option[A] = Option(root.findViewById(id).asInstanceOf[A])
}

/** This trait provides (abstract) view and fragment searching capabilities */
sealed trait ViewSearch extends BasicViewSearch {
  def fragmentManager: FragmentManager

  /** Find a view with a given id in root view */
  def findView[A <: View](id: Int): Option[A]
  /** Find a fragment with a given tag */
  def findFrag[A <: Fragment](tag: String) = Option(fragmentManager.findFragmentByTag(tag).asInstanceOf[A])
}

/** An implementation of ViewSearch to be used by Activities */
trait ActivityViewSearch extends ViewSearch { self: FragmentActivity ⇒
  def fragmentManager = getSupportFragmentManager
  def findView[A <: View](id: Int) = Option(findViewById(id).asInstanceOf[A])
}

/** An implementation of ViewSearch to be used by Fragments */
trait FragmentViewSearch extends ViewSearch { self: Fragment ⇒
  def fragmentManager = getChildFragmentManager
  def findView[A <: View](id: Int) = Option(getView.findViewById(id).asInstanceOf[A])
}