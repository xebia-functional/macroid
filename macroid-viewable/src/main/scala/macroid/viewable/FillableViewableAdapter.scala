package macroid.viewable

import android.view.{ View, ViewGroup }
import android.widget.ArrayAdapter
import macroid.UiThreading._
import macroid.util.{ SafeCast, Ui }
import macroid.{ ActivityContext, AppContext }

class FillableViewableAdapter[A](implicit ctx: ActivityContext, appCtx: AppContext, fillableViewable: FillableViewable[A])
  extends ArrayAdapter[A](ctx.get, 0) {

  override def getViewTypeCount = fillableViewable.viewTypeCount
  override def getItemViewType(position: Int) = if (0 <= position && position < getCount) {
    fillableViewable.viewType(getItem(position))
  } else {
    super.getItemViewType(position)
  }

  override def getView(position: Int, view: View, parent: ViewGroup): View = getUi {
    val v = SafeCast[View, fillableViewable.W](view).map(x ⇒ Ui(x))
      .getOrElse(fillableViewable.makeView(getItemViewType(position)))
    fillableViewable.fillView(v, getItem(position))
  }
}

object FillableViewableAdapter {
  def apply[A](fillableViewable: FillableViewable[A])(implicit ctx: ActivityContext, appCtx: AppContext) =
    new FillableViewableAdapter[A]()(ctx, appCtx, fillableViewable)

  def apply[A](data: Seq[A])(fillableViewable: FillableViewable[A])(implicit ctx: ActivityContext, appCtx: AppContext) =
    new FillableViewableAdapter[A]()(ctx, appCtx, fillableViewable) {
      addAll(data: _*)
    }
}
