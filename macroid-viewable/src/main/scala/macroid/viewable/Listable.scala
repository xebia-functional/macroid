package macroid.viewable

import macroid.contrib.ListTweaks

import scala.language.higherKinds

import android.view.{ View, ViewGroup }
import android.widget.TextView
import macroid.LayoutDsl._
import macroid._

import scala.reflect.ClassTag

trait PartialListable[A, +W <: View] { self ⇒
  def viewTypeCount: Int
  def viewType(data: A): Option[Int]

  def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]
  def fillView[W1 >: W <: View](view: Ui[W1], data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Option[Ui[W1]]

  def contraFlatMap[B](f: B ⇒ Option[A]): PartialListable[B, W] = new PartialListable[B, W] {
    def viewTypeCount = self.viewTypeCount
    def viewType(data: B) = f(data).flatMap(self.viewType)
    def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
    def fillView[W1 >: W <: View](view: Ui[W1], data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = f(data).flatMap(self.fillView(view, _))
  }

  def orElse[W1 >: W <: View](alternative: PartialListable[A, W1]): PartialListable[A, W1] =
    new PartialListable[A, W1] {
      def viewTypeCount =
        self.viewTypeCount +
          alternative.viewTypeCount

      def viewType(data: A) =
        self.viewType(data) orElse
          alternative.viewType(data).map(_ + self.viewTypeCount)

      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) =
        if (viewType < self.viewTypeCount) {
          self.makeView(viewType)
        } else {
          alternative.makeView(viewType - self.viewTypeCount)
        }

      def fillView[W2 >: W1 <: View](view: Ui[W2], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        self.viewType(data).fold(self.fillView(view, data))(_ ⇒ alternative.fillView(view, data))
    }

  def cond(p: A ⇒ Boolean): PartialListable[A, W] =
    new PartialListable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) =
        if (p(data)) self.viewType(data) else None
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView[W1 >: W <: View](view: Ui[W1], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        if (p(data)) self.fillView(view, data) else None
    }

  def toParent[B](implicit classTag: ClassTag[A]): PartialListable[B, W] =
    new PartialListable[B, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: B) = data match {
        case x: A ⇒ self.viewType(x)
        case _ ⇒ None
      }
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView[W1 >: W <: View](view: Ui[W1], data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = data match {
        case x: A ⇒ self.fillView(view, x)
        case _ ⇒ None
      }
    }

  def toTotal[W1 >: W <: View]: Listable[A, W1] =
    new Listable[A, W1] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) = self.viewType(data).get
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView(view: Ui[W1], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = self.fillView(view, data).get
    }
}

trait Listable[A, W <: View] { self ⇒
  def viewTypeCount: Int
  def viewType(data: A): Int

  def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]
  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]

  def contraMap[B](f: B ⇒ A): Listable[B, W] =
    new Listable[B, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: B) = self.viewType(f(data))
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView(view: Ui[W], data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = self.fillView(view, f(data))
    }

  def toPartial(implicit classTag: ClassTag[W]): PartialListable[A, W] =
    new PartialListable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) = Some(self.viewType(data))
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView[W1 >: W <: View](view: Ui[W1], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view match {
        case x: Ui[W] ⇒ Some(self.fillView(x, data))
        case _ ⇒ None
      }
    }

  def cond(p: A ⇒ Boolean)(implicit classTag: ClassTag[W]): PartialListable[A, W] = toPartial.cond(p)
  def toParent[B](implicit classTagA: ClassTag[A], classTagW: ClassTag[W]): PartialListable[B, W] = toPartial.toParent[B]

  def listAdapter(data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext): ListableListAdapter[A, W] =
    new ListableListAdapter[A, W](data)(ctx, appCtx, this)

  def listAdapterTweak(data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext) =
    ListTweaks.adapter(listAdapter(data))
}

object Listable {
  implicit def `Listable is Viewable`[A, W <: View](implicit listable: Listable[A, W]): Viewable[A, W] =
    new Viewable[A, W] {
      def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        listable.fillView(listable.makeView(listable.viewType(data)), data)
    }

  def apply[A, W <: View](make: ⇒ Ui[W])(fill: Ui[W] ⇒ A ⇒ Ui[W]): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(view)(data)
    }

  def text(tweak: Tweak[TextView]): Listable[String, TextView] =
    new Listable[String, TextView] {
      val viewTypeCount = 1
      def viewType(data: String) = 0
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] <~ tweak
      def fillView(view: Ui[TextView], data: String)(implicit ctx: ActivityContext, appCtx: AppContext) = view <~ Tweaks.text(data)
    }

  type Slotted[A] = SlottedListable[A]

  def tw[A, W <: View](make: ⇒ Ui[W])(fill: A ⇒ Tweak[W]): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view <~ fill(data)
    }

  def tr[A, W <: ViewGroup](make: ⇒ Ui[W])(fill: A ⇒ Transformer): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view <~ fill(data)
    }
}
