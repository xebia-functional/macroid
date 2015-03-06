package macroid.viewable

import android.view.View
import android.widget.TextView
import macroid.LayoutBuilding._
import macroid.Tweaking._
import macroid._
import macroid.contrib.PagerTweaks

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

/** Expresses the fact that *some* of the values of type `A` can be displayed with a widget or layout of type `W` */
trait PartialViewable[A, +W <: View] { self ⇒
  /** Create the layout for a value of type `A`. Returns None if not defined for this value */
  def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Option[Ui[W]]

  /** Map the underlying data type `A` */
  def contraFlatMap[B](f: B ⇒ Option[A]): PartialViewable[B, W] =
    new PartialViewable[B, W] {
      def view(data: B)(implicit ctx: ActivityContext, appCtx: AppContext) =
        f(data).flatMap(self.view)
    }

  /** Combine with an alternative partial */
  def orElse[W1 >: W <: View](alternative: PartialViewable[A, W1]): PartialViewable[A, W1] =
    new PartialViewable[A, W1] {
      def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        self.view(data) orElse alternative.view(data)
    }

  /** Specify a condition to further limit this partial */
  def cond(p: A ⇒ Boolean): PartialViewable[A, W] =
    new PartialViewable[A, W] {
      def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        if (p(data)) self.view(data) else None
    }

  /** Make a partial defined for a subset of the provided supertype */
  def toParent[B](implicit classTag: ClassTag[A]): PartialViewable[B, W] =
    new PartialViewable[B, W] {
      def view(data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = data match {
        case x: A ⇒ self.view(x)
        case _ ⇒ None
      }
    }

  /** Convert back to total viewable */
  def toTotal: Viewable[A, W] =
    new Viewable[A, W] {
      def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        self.view(data).get
    }
}

/** Expresses the fact that data type `A` can be displayed with a widget or layout of type `W` */
@implicitNotFound("Don't know how to display data type ${A}. Try importing an instance of Viewable[${A}, ...]")
trait Viewable[A, +W <: View] { self ⇒
  /** Create the layout for a value of type `A` */
  def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]

  /** Map the underlying data type `A` */
  def contraMap[B](f: B ⇒ A): Viewable[B, W] =
    new Viewable[B, W] {
      def view(data: B)(implicit ctx: ActivityContext, appCtx: AppContext) =
        self.view(f(data))
    }

  /** Convert to partial viewable for composition with alternatives */
  def toPartial: PartialViewable[A, W] =
    new PartialViewable[A, W] {
      def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        Some(self.view(data))
    }

  /** Convert to partial viewable based on a condition */
  def cond(p: A ⇒ Boolean): PartialViewable[A, W] = toPartial.cond(p)

  /** Convert to partial viewable defined for a subset of a supertype */
  def toParent[B](implicit evidence: ClassTag[A]): PartialViewable[B, W] = toPartial.toParent[B]

  /** An adapter to use with a `ViewPager` */
  def pagerAdapter(data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext): ViewablePagerAdapter[A, W] =
    new ViewablePagerAdapter[A, W](data)(ctx, appCtx, this)

  /** A tweak to set the adapter of a `ViewPager` */
  def pagerAdapterTweak(data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext) =
    PagerTweaks.adapter(pagerAdapter(data))
}

/** A builder to define viewables for a particular data type */
class ViewableBuilder[A] {
  /** Define a viewable by providing the layout function */
  def apply[W <: View](layout: A ⇒ Ui[W]): Viewable[A, W] =
    new Viewable[A, W] {
      def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = layout(data)
    }
}

object Viewable {
  implicit def `Listable is Viewable`[A, W <: View](implicit listable: Listable[A, W]): Viewable[A, W] =
    new Viewable[A, W] {
      def view(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        listable.fillView(listable.makeView(listable.viewType(data)), data)
    }

  /** Build a viewable for a particular data type */
  def apply[A] = new ViewableBuilder[A]

  /** Define a viewable for strings by providing the `TextView` style */
  def text(tweak: Tweak[TextView]): Viewable[String, TextView] =
    new Viewable[String, TextView] {
      def view(data: String)(implicit ctx: ActivityContext, appCtx: AppContext) =
        w[TextView] <~ tweak <~ Tweaks.text(data)
    }
}
