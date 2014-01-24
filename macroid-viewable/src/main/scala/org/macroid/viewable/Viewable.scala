package org.macroid.viewable

import org.macroid.{ AppContext, ActivityContext }
import android.view.View

trait Viewable[A] {
  type W <: View
  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): W
}

object Viewable {
  implicit class ViewableOps[A](data: A)(implicit ctx: ActivityContext, appCtx: AppContext, val viewable: Viewable[A]) {
    def layout = viewable.layout(data)
  }
}
