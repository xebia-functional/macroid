package macroid.viewable

import android.view.View
import android.widget.TextView
import macroid._
import macroid.LayoutBuilding._
import macroid.Tweaking._
import macroid.util.Ui

trait Viewable[A] {
  type W <: View
  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]
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
