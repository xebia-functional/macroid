package org.macroid.viewable

import org.macroid._
import android.view.View
import android.widget.TextView
import org.macroid.LayoutBuilding._
import org.macroid.Tweaking._
import org.macroid.AppContext
import org.macroid.ActivityContext

trait Viewable[A] {
  type W <: View
  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): W
}

object Viewable {
  def text(tweak: Tweak[TextView]) = new Viewable[String] {
    type W = TextView
    def layout(data: String)(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] ~> tweak ~> Tweaks.text(data)
  }
}

object Viewing {
  implicit class ViewableOps[A](data: A)(implicit ctx: ActivityContext, appCtx: AppContext, val viewable: Viewable[A]) {
    def layout = viewable.layout(data)
  }
}
