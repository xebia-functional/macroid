package macroid.extras

import android.view.{ LayoutInflater, ViewGroup }
import macroid.ContextWrapper

object LayoutBuildingExtra {

  def connect[W](id: Int)(implicit root: RootView): Option[W] = Some(root.view.findViewById(id).asInstanceOf[W])

}

class RootView(layout: Int)(implicit context: ContextWrapper) {
  val view = LayoutInflater.from(context.application).inflate(layout, null).asInstanceOf[ViewGroup]
}
