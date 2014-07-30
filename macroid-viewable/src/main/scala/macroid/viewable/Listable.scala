package macroid.viewable

import scala.language.higherKinds

import android.view.{ View, ViewGroup }
import android.widget.TextView
import macroid.LayoutDsl._
import macroid._

import scala.reflect.ClassTag

private[viewable] trait AbstractListable[A, W <: View] {
  protected type R[+X]

  def viewTypeCount: Int
  def viewType(data: A): R[Int]

  def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]
  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext): R[Ui[W]]
}

trait PartialListable[A, W <: View] extends AbstractListable[A, W] { self ⇒
  protected final type R[+X] = Option[X]

  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
    viewType(data).flatMap(t ⇒ fillView(makeView(t), data))

  def contraFlatMap[B](f: B ⇒ Option[A]): PartialListable[B, W] = new PartialListable[B, W] {
    def viewTypeCount = self.viewTypeCount
    def viewType(data: B) = f(data).flatMap(self.viewType)
    def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
    def fillView(view: Ui[W], data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = f(data).flatMap(self.fillView(view, _))
  }

  def orElse[A1 >: A](alternative: PartialListable[A1, W])(implicit classTag: ClassTag[A]): PartialListable[A1, W] =
    new PartialListable[A1, W] {
      def viewTypeCount =
        self.viewTypeCount +
          alternative.viewTypeCount

      def viewType(data: A1) = data match {
        // inlined self.toParent[A1]
        case x: A ⇒ self.viewType(x) orElse alternative.viewType(data).map(_ + self.viewTypeCount)
        case _ ⇒ alternative.viewType(data).map(_ + self.viewTypeCount)
      }

      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) =
        if (viewType < self.viewTypeCount) {
          self.makeView(viewType)
        } else {
          alternative.makeView(viewType - self.viewTypeCount)
        }

      def fillView(view: Ui[W], data: A1)(implicit ctx: ActivityContext, appCtx: AppContext) = data match {
        // inlined self.toParent[A1]
        case x: A ⇒ self.fillView(view, x) orElse alternative.fillView(view, data)
        case _ ⇒ alternative.fillView(view, data)
      }
    }

  def cond(p: A ⇒ Boolean): PartialListable[A, W] =
    new PartialListable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) =
        if (p(data)) self.viewType(data) else None
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
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
      def fillView(view: Ui[W], data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = data match {
        case x: A ⇒ self.fillView(view, x)
        case _ ⇒ None
      }
    }

  def toTotal: Listable[A, W] =
    new Listable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) = self.viewType(data).get
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = self.fillView(view, data).get
    }
}

trait Listable[A, W <: View] extends AbstractListable[A, W] { self ⇒
  protected final type R[+X] = X

  def contraMap[B](f: B ⇒ A): Listable[B, W] =
    new Listable[B, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: B) = self.viewType(f(data))
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView(view: Ui[W], data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = self.fillView(view, f(data))
    }

  def toPartial: PartialListable[A, W] =
    new PartialListable[A, W] {
      def viewTypeCount = self.viewTypeCount
      def viewType(data: A) = Some(self.viewType(data))
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = self.makeView(viewType)
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = Some(self.fillView(view, data))
    }

  def cond(p: A ⇒ Boolean): PartialListable[A, W] = toPartial.cond(p)
  def toParent[B](implicit evidence: ClassTag[A]): PartialListable[B, W] = toPartial.toParent[B]
}

object Listable {
  implicit def `Listable is Viewable`[A, W <: View](implicit listable: Listable[A, W]): Viewable[A, W] =
    new Viewable[A, W] {
      def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        listable.fillView(listable.makeView(listable.viewType(data)), data)
    }

  def apply[A, W <: View](make: Ui[W])(fill: Ui[W] ⇒ A ⇒ Ui[W]): Listable[A, W] =
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

  def tw[A, W <: View](make: Ui[W])(fill: A ⇒ Tweak[W]): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view <~ fill(data)
    }

  def tr[A, W <: ViewGroup](make: Ui[W])(fill: A ⇒ Transformer): Listable[A, W] =
    new Listable[A, W] {
      val viewTypeCount = 1
      def viewType(data: A) = 0
      def makeView(viewType: Int)(implicit ctx: ActivityContext, appCtx: AppContext) = make
      def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view <~ fill(data)
    }
}
