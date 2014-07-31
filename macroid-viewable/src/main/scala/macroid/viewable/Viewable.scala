package macroid.viewable

import macroid.contrib.PagerTweaks

import scala.language.higherKinds
import android.view.View
import android.widget.TextView
import macroid._
import macroid.LayoutBuilding._
import macroid.Tweaking._

import scala.reflect.ClassTag

trait PartialViewable[A, +W <: View] { self ⇒
  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Option[Ui[W]]

  def contraFlatMap[B](f: B ⇒ Option[A]): PartialViewable[B, W] =
    new PartialViewable[B, W] {
      def layout(data: B)(implicit ctx: ActivityContext, appCtx: AppContext) =
        f(data).flatMap(self.layout)
    }

  def orElse[W1 >: W <: View](alternative: PartialViewable[A, W1]): PartialViewable[A, W1] =
    new PartialViewable[A, W1] {
      def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        self.layout(data) orElse alternative.layout(data)
    }

  def cond(p: A ⇒ Boolean): PartialViewable[A, W] =
    new PartialViewable[A, W] {
      def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        if (p(data)) self.layout(data) else None
    }

  def toParent[B](implicit classTag: ClassTag[A]): PartialViewable[B, W] =
    new PartialViewable[B, W] {
      def layout(data: B)(implicit ctx: ActivityContext, appCtx: AppContext) = data match {
        case x: A ⇒ self.layout(x)
        case _ ⇒ None
      }
    }

  def toTotal: Viewable[A, W] =
    new Viewable[A, W] {
      def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        self.layout(data).get
    }
}

trait Viewable[A, +W <: View] { self ⇒
  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]

  def contraMap[B](f: B ⇒ A): Viewable[B, W] =
    new Viewable[B, W] {
      def layout(data: B)(implicit ctx: ActivityContext, appCtx: AppContext) =
        self.layout(f(data))
    }

  def toPartial: PartialViewable[A, W] =
    new PartialViewable[A, W] {
      def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) =
        Some(self.layout(data))
    }

  def cond(p: A ⇒ Boolean): PartialViewable[A, W] = toPartial.cond(p)
  def toParent[B](implicit evidence: ClassTag[A]): PartialViewable[B, W] = toPartial.toParent[B]

  def pagerAdapter(data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext): ViewablePagerAdapter[A, W] =
    new ViewablePagerAdapter[A, W](data)(ctx, appCtx, this)

  def pagerAdapterTweak(data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext) =
    PagerTweaks.adapter(pagerAdapter(data))
}

object Viewable {
  def apply[A, W <: View](lay: A ⇒ Ui[W]): Viewable[A, W] =
    new Viewable[A, W] {
      def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = lay(data)
    }

  def text(tweak: Tweak[TextView]): Viewable[String, TextView] =
    new Viewable[String, TextView] {
      def layout(data: String)(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] <~ tweak <~ Tweaks.text(data)
    }
}
