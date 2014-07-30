package macroid.viewable

import android.view.{ View, ViewGroup }
import android.widget.ArrayAdapter
import macroid.UiThreading._
import macroid.util.SafeCast
import macroid.{ ActivityContext, AppContext, Ui }

class ListableAdapter[A, W <: View](implicit ctx: ActivityContext, appCtx: AppContext, val listable: Listable[A, W])
  extends ArrayAdapter[A](ctx.get, 0) {

  override def getViewTypeCount = listable.viewTypeCount
  override def getItemViewType(position: Int) = if (0 <= position && position < getCount) {
    listable.viewType(getItem(position))
  } else {
    super.getItemViewType(position)
  }

  override def getView(position: Int, view: View, parent: ViewGroup): View = getUi {
    val v = SafeCast[View, W](view).map(x ⇒ Ui(x))
      .getOrElse(listable.makeView(getItemViewType(position)))
    listable.fillView(v, getItem(position))
  }
}
