package macroid.viewable

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.view.ViewGroup
import macroid.Tweaks._
import macroid.util.SafeCast
import macroid.{ ContextWrapper, Tweak, Ui }

/** A `Listable` that works by saving widget slots inside the layout's tag and filling them later */
trait SlottedListable[A] extends Listable[A, View] {
  /** The slots type. Example:
    * {{{
    * class Slots {
    * var textView = slot[TextView]
    * var imageView = slot[ImageView]
    * }
    * }}}
    */
  type Slots

  /** Create the layout and a `Slots` instance, wire the slots, and return both. Example:
    * {{{
    * val slots = new Slots
    * val view = w[TextView] <~ wire(slots.textView)
    * (view, slots)
    * }}}
    */
  def makeSlots(viewType: Int)(implicit ctx: ContextWrapper): (Ui[View], Slots)

  /** Fill the slots with data */
  def fillSlots(slots: Slots, data: A)(implicit ctx: ContextWrapper): Ui[Any]

  // Implemented for convenience, override if necessary
  def viewTypeCount = 1
  def viewType(data: A) = 0

  def makeView(viewType: Int)(implicit ctx: ContextWrapper) = {
    val (v, s) = makeSlots(viewType)
    v <~ hold(s)
  }

  def fillView(view: Ui[View], data: A)(implicit ctx: ContextWrapper) = view flatMap { v ⇒
    val (v1, s) = SafeCast[Any, Slots](v.getTag).map(x ⇒ (Ui(v), x)).getOrElse(makeSlots(viewType(data)))
    fillSlots(s, data).flatMap(_ ⇒ v1)
  }

  /** An adapter to use with a `RecyclerView` */
  def recyclerViewAdapter(data: Seq[A])(implicit ctx: ContextWrapper): RecyclerViewAdapter =
    new RecyclerViewAdapter(data)(ctx)

  /** A tweak to set the adapter of a `RecyclerView` */
  def recyclerViewAdapterTweak(data: Seq[A])(implicit ctx: ContextWrapper) =
    adapter(recyclerViewAdapter(data))

  class RecyclerViewAdapter(data: Seq[A])(implicit ctx: ContextWrapper)
    extends RecyclerView.Adapter[VHolderProxy] {

    override def getItemViewType(position: Int): Int = if (0 <= position && position < getItemCount) {
      viewType(data(position))
    } else {
      super.getItemViewType(position)
    }

    override def getItemCount: Int = data.size

    override def onCreateViewHolder(viewGroup: ViewGroup, position: Int): VHolderProxy = {
      val slot = makeSlots(getItemViewType(position))

      VHolderProxy(slot)
    }

    override def onBindViewHolder(vh: VHolderProxy, i: Int): Unit = {
      fillSlots(vh.vh._2, data(i)).run
    }
  }

  case class VHolderProxy(vh: (Ui[View], Slots)) extends ViewHolder(vh._1.get)

  def adapter(adapter: RecyclerViewAdapter) = Tweak[RecyclerView](_.setAdapter(adapter))
}
