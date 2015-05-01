package macroid.viewable

import android.view.{ View, ViewGroup }
import android.widget.ArrayAdapter
import macroid.util.SafeCast
import macroid.{ContextWrapper, Ui}

/** A `ListAdapter` based on the `Listable` typeclass */
class ListableListAdapter[A, W <: View](data: Seq[A])(implicit ctx: ContextWrapper, listable: Listable[A, W])
  extends ArrayAdapter[A](ctx.getOriginal, 0) {

  addAll(data: _*)

  override def getViewTypeCount = listable.viewTypeCount
  override def getItemViewType(position: Int) = if (0 <= position && position < getCount) {
    listable.viewType(getItem(position))
  } else {
    super.getItemViewType(position)
  }

  override def getView(position: Int, view: View, parent: ViewGroup): View = Ui.get {
    val v = SafeCast[View, W](view).map(x ⇒ Ui(x))
      .getOrElse(listable.makeView(getItemViewType(position)))
    listable.fillView(v, getItem(position))
  }
}
