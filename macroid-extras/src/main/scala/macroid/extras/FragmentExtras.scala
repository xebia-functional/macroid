package macroid.extras

import android.support.v4.app.{ Fragment, FragmentManager }
import macroid.{ FragmentBuilder, FragmentManagerContext }

object FragmentExtras {

  def addFragment[F <: Fragment](
    builder: FragmentBuilder[F],
    id: Option[Int] = None,
    tag: Option[String] = None
  )(implicit managerContext: FragmentManagerContext[Fragment, FragmentManager]) = {
    builder.factory map (managerContext.manager.beginTransaction().add(id.getOrElse(0), _, tag.getOrElse("")).commit())
  }

  def removeFragment(fragment: Fragment)(implicit managerContext: FragmentManagerContext[Fragment, FragmentManager]) = {
    managerContext.manager.beginTransaction().remove(fragment).commit()
  }

  def replaceFragment[F <: Fragment](
    builder: FragmentBuilder[F],
    id: Int,
    tag: Option[String] = None
  )(implicit managerContext: FragmentManagerContext[Fragment, FragmentManager]) = {
    builder.factory.map(managerContext.manager.beginTransaction().replace(id, _, tag.orNull).commit())
  }

  def findFragmentByTag[T <: Fragment](tag: String)(implicit managerContext: FragmentManagerContext[Fragment, FragmentManager]): Option[T] =
    Option(managerContext.manager.findFragmentByTag(tag)) map (_.asInstanceOf[T])

  def findFragmentById[T <: Fragment](id: Int)(implicit managerContext: FragmentManagerContext[Fragment, FragmentManager]): Option[T] =
    Option(managerContext.manager.findFragmentById(id)) map (_.asInstanceOf[T])
}