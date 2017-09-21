package macroid.viewable

import android.view.{View, ViewGroup}
import android.widget.TextView
import macroid.LayoutDsl._
import macroid.Tweaks._
import macroid._
import macroid.contrib.ListTweaks

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

/** Expresses the fact that *some* of the values of type `A` can be displayed with a widget or layout of type `W` in two steps:
  * 1) creating the layout
  * 2) filling the layout
  */
trait PartialListable[A, +W <: View] { self ⇒

  /** Supported number of different layout types */
  def viewTypeCount: Int

  /** Layout type for a specific value, or None if not defined for this value */
  def viewType(data: A): Option[Int]

  /** Create the layout */
  def makeView(viewType: Int)(implicit ctx: ContextWrapper): Ui[W]

  /** Fill the layout with data. Returns None if not defined for this value */
  def fillView[W1 >: W <: View](view: Ui[W1], data: A)(
      implicit ctx: ContextWrapper): Option[Ui[W1]]

  /** Map the underlying data type `A` */
  def contraFlatMap[B](f: B ⇒ Option[A]): PartialListable[B, W] =
    new PartialListable[B, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: B) = f(data).flatMap(self.viewType)
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        self.makeView(viewType)
      def fillView[W1 >: W <: View](view: Ui[W1], data: B)(
          implicit ctx: ContextWrapper) =
        f(data).flatMap(self.fillView(view, _))
    }

  /** Combine with an alternative partial */
  def orElse[W1 >: W <: View](
      alternative: PartialListable[A, W1]): PartialListable[A, W1] =
    new PartialListable[A, W1] {
      def viewTypeCount =
        self.viewTypeCount +
          alternative.viewTypeCount

      def viewType(data: A) =
        self.viewType(data) orElse
          alternative.viewType(data).map(_ + self.viewTypeCount)

      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        if (viewType < self.viewTypeCount) {
          self.makeView(viewType)
        } else {
          alternative.makeView(viewType - self.viewTypeCount)
        }

      def fillView[W2 >: W1 <: View](view: Ui[W2], data: A)(
          implicit ctx: ContextWrapper) =
        self.fillView(view, data) orElse
          alternative.fillView(view, data)
    }

  /** Specify a condition to further limit this partial */
  def cond(p: A ⇒ Boolean): PartialListable[A, W] =
    new PartialListable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) =
        if (p(data)) self.viewType(data) else None
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        self.makeView(viewType)
      def fillView[W1 >: W <: View](view: Ui[W1], data: A)(
          implicit ctx: ContextWrapper) =
        if (p(data)) self.fillView(view, data) else None
    }

  /** Make a partial defined for a subset of the provided supertype */
  def toParent[B](implicit classTag: ClassTag[A]): PartialListable[B, W] =
    new PartialListable[B, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: B) = data match {
        case x: A ⇒ self.viewType(x)
        case _ ⇒ None
      }
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        self.makeView(viewType)
      def fillView[W1 >: W <: View](view: Ui[W1], data: B)(
          implicit ctx: ContextWrapper) = data match {
        case x: A ⇒ self.fillView(view, x)
        case _ ⇒ None
      }
    }

  /** Convert back to total listable */
  def toTotal[W1 >: W <: View]: Listable[A, W1] =
    new Listable[A, W1] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) = self.viewType(data).get
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        self.makeView(viewType)
      def fillView(view: Ui[W1], data: A)(implicit ctx: ContextWrapper) =
        self.fillView(view, data).get
    }
}

/** Expresses the fact that data type `A` can be displayed with a widget or layout of type `W` in two steps:
  * 1) creating the layout
  * 2) filling the layout
  * Therefore suitable for use in `ListAdapter`s
  */
@implicitNotFound(
  "Don't know how to display data type ${A} in a list. Try importing an instance of Listable[${A}, ...]")
trait Listable[A, W <: View] { self ⇒

  /** Supported number of different layout types */
  def viewTypeCount: Int

  /** Layout type for a specific value */
  def viewType(data: A): Int

  /** Create the layout */
  def makeView(viewType: Int)(implicit ctx: ContextWrapper): Ui[W]

  /** Fill the layout with data. Will be always called with layouts created by `makeView` using `viewType(data)` */
  def fillView(view: Ui[W], data: A)(implicit ctx: ContextWrapper): Ui[W]

  /** Map the underlying data type `A` */
  def contraMap[B](f: B ⇒ A): Listable[B, W] =
    new Listable[B, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: B) = self.viewType(f(data))
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        self.makeView(viewType)
      def fillView(view: Ui[W], data: B)(implicit ctx: ContextWrapper) =
        self.fillView(view, f(data))
    }

  /** Add extra fillView function */
  def addFillView(fill: (Ui[W], A) ⇒ Ui[W]) =
    new Listable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) = self.viewType(data)
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        self.makeView(viewType)
      def fillView(view: Ui[W], data: A)(implicit ctx: ContextWrapper) =
        fill(self.fillView(view, data), data)
    }

  /** Convert to partial listable for composition with alternatives */
  def toPartial(implicit classTag: ClassTag[W]): PartialListable[A, W] =
    new PartialListable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) = Some(self.viewType(data))
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        self.makeView(viewType)
      def fillView[W1 >: W <: View](view: Ui[W1], data: A)(
          implicit ctx: ContextWrapper) = view match {
        case x: Ui[W] ⇒ Some(self.fillView(x, data))
        case _ ⇒ None
      }
    }

  /** Convert to partial listable based on a condition */
  def cond(p: A ⇒ Boolean)(
      implicit classTag: ClassTag[W]): PartialListable[A, W] = toPartial.cond(p)

  /** Convert to partial listable defined for a subset of a supertype */
  def toParent[B](implicit classTagA: ClassTag[A],
                  classTagW: ClassTag[W]): PartialListable[B, W] =
    toPartial.toParent[B]

  /** Convert to a viewable */
  def toViewable: Viewable[A, W] =
    new Viewable[A, W] {
      def view(data: A)(implicit ctx: ContextWrapper) =
        fillView(makeView(viewType(data)), data)
    }

  /** An adapter to use with a `ListView` */
  def listAdapter(data: Seq[A])(
      implicit ctx: ContextWrapper): ListableListAdapter[A, W] =
    new ListableListAdapter[A, W](data)(ctx, this)

  /** A tweak to set the adapter of a `ListView` */
  def listAdapterTweak(data: Seq[A])(implicit ctx: ContextWrapper) =
    ListTweaks.adapter(listAdapter(data))
}

/** A builder to define listables for a particular data type */
class ListableBuilder[A] {

  /** Define a listable by providing the make and fill functions */
  def apply[W <: View](make: ⇒ Ui[W])(fill: Ui[W] ⇒ A ⇒ Ui[W]): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ContextWrapper) =
        fill(view)(data)
    }

  /** Define a listable by providing the make function and a tweak to fill the layout with data */
  def tw[W <: View](make: ⇒ Ui[W])(fill: A ⇒ Tweak[W]): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ContextWrapper) =
        view <~ fill(data)
    }

  /** Define a listable by providing the make function and a transformer to fill the layout with data */
  def tr[W <: ViewGroup](make: ⇒ Ui[W])(fill: A ⇒ Transformer): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ContextWrapper) =
        view <~ fill(data)
    }
}

object Listable {

  /** Build a listable for a particular data type */
  def apply[A] = new ListableBuilder[A]

  /** Define a listable for strings by providing the `TextView` style */
  def text(tweak: Tweak[TextView]): Listable[String, TextView] =
    new Listable[String, TextView] {
      val viewTypeCount = 1
      def viewType(data: String) = 0
      def makeView(viewType: Int)(implicit ctx: ContextWrapper) =
        w[TextView] <~ tweak
      def fillView(view: Ui[TextView], data: String)(
          implicit ctx: ContextWrapper) = view <~ Tweaks.text(data)
    }

  /** An alias for SlottedListable */
  type Slotted[A] = SlottedListable[A]

  /** Wrap an existing listable into some *outer* layout */
  def wrap[A, W <: View, W1 <: View](x: Listable[A, W])(
      wrapper: Ui[W] ⇒ Ui[W1]): SlottedListable[A] =
    new SlottedListable[A] {
      class Slots {
        var x = slot[W]
      }

      override val viewTypeCount = x.viewTypeCount
      override def viewType(data: A) = x.viewType(data)

      def makeSlots(viewType: Int)(implicit ctx: ContextWrapper) = {
        val slots = new Slots
        val view = wrapper(x.makeView(viewType) <~ wire(slots.x))
        (view, slots)
      }

      def fillSlots(slots: Slots, data: A)(implicit ctx: ContextWrapper) =
        slots.x.fold(Ui.nop)(z ⇒ x.fillView(Ui(z), data) ~ Ui.nop)
    }

  /** Combine two listables into a bigger *outer* layout */
  def combine[A1, A2, W1 <: View, W2 <: View](
      x: Listable[A1, W1],
      y: Listable[A2, W2]
  )(
      glue: (Ui[W1], Ui[W2]) ⇒ Ui[View]
  ): SlottedListable[(A1, A2)] =
    new SlottedListable[(A1, A2)] {
      class Slots {
        var x = slot[W1]
        var y = slot[W2]
      }

      override val viewTypeCount =
        x.viewTypeCount * y.viewTypeCount

      override def viewType(data: (A1, A2)) =
        x.viewType(data._1) * y.viewTypeCount + y.viewType(data._2)

      def makeSlots(viewType: Int)(implicit ctx: ContextWrapper) = {
        val slots = new Slots
        val (xType, yType) =
          (viewType / y.viewTypeCount, viewType % y.viewTypeCount)
        val view = glue(
          x.makeView(xType) <~ wire(slots.x),
          y.makeView(yType) <~ wire(slots.y)
        )
        (view, slots)
      }

      def fillSlots(slots: Slots, data: (A1, A2))(
          implicit ctx: ContextWrapper) =
        slots.x.fold(Ui.nop)(z ⇒ x.fillView(Ui(z), data._1) ~ Ui.nop) ~
          slots.y.fold(Ui.nop)(z ⇒ y.fillView(Ui(z), data._2) ~ Ui.nop)
    }
}
