package macroid.viewable

import android.view.View
import macroid.Tweaks._
import macroid.util.SafeCast
import macroid.{ContextWrapper, Ui}

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

  def fillView(view: Ui[View], data: A)(implicit ctx: ContextWrapper) =
    view flatMap { v ⇒
      val (v1, s) = SafeCast[Any, Slots](v.getTag)
        .map(x ⇒ (Ui(v), x))
        .getOrElse(makeSlots(viewType(data)))
      fillSlots(s, data).flatMap(_ ⇒ v1)
    }
}
