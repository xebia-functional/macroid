package macroid.viewable

import android.view.View
import macroid.LayoutDsl._
import macroid.Tweaks._
import macroid.util.SafeCast
import macroid.{ Ui, AppContext, ActivityContext }

trait SlottedListable[A] extends Listable[A, View] {
  type Slots
  def makeSlots(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext): (Ui[View], Slots)
  def fillSlots(slots: Slots, data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[Any]

  override def viewTypeCount = 1
  override def viewType(data: A) = 0

  def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = {
    val (v, s) = makeSlots(viewType)
    v <~ hold(s)
  }

  def fillView(view: Ui[View], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view flatMap { v ⇒
    val (v1, s) = SafeCast[Any, Slots](v.getTag).map(x ⇒ (Ui(v), x)).getOrElse(makeSlots(viewType(data)))
    fillSlots(s, data).flatMap(_ ⇒ v1)
  }
}
