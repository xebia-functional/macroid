package macroid.viewable

import macroid._
import macroid.LayoutDsl._
import macroid.Tweaks._
import macroid.util.{ SafeCast, Ui }
import scala.util.Try
import android.view.{ View, ViewGroup }
import android.widget.TextView

trait FillableViewable[A] extends Viewable[A] {
  def makeView(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]
  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]

  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fillView(makeView, data)
}

object FillableViewable {
  def apply[A, W1 <: View](make: Ui[W1])(fill: Ui[W1] ⇒ A ⇒ Ui[W1]): FillableViewable[A] = new FillableViewable[A] {
    type W = W1
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(view)(data)
  }

  def text(make: Tweak[TextView]): FillableViewable[String] = new TweakFillableViewable[String] {
    type W = TextView
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] <~ make
    def tweak(data: String)(implicit ctx: ActivityContext, appCtx: AppContext) = Tweaks.text(data)
  }

  def text[A](make: Tweak[TextView], fill: A ⇒ Tweak[TextView]): FillableViewable[A] = new TweakFillableViewable[A] {
    type W = TextView
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] <~ make
    def tweak(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(data)
  }

  def tw[A, W1 <: View](make: Ui[W1])(fill: A ⇒ Tweak[W1]): FillableViewable[A] = new TweakFillableViewable[A] {
    type W = W1
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def tweak(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(data)
  }

  def tr[A](make: Ui[ViewGroup])(fill: A ⇒ Transformer): FillableViewable[A] = new TransformerFillableViewable[A] {
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def transformer(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(data)
  }
}

trait TweakFillableViewable[A] extends FillableViewable[A] {
  def tweak(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Tweak[W]

  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view <~ tweak(data)
}

trait TransformerFillableViewable[A] extends FillableViewable[A] {
  def transformer(data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Transformer

  type W = ViewGroup
  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view <~~ transformer(data)
}

trait SlottedFillableViewable[A] extends FillableViewable[A] {
  type Slots
  def makeSlots(implicit ctx: ActivityContext, appCtx: AppContext): (Ui[W], Slots)
  def fillSlots(slots: Slots, data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[Any]

  type W = View

  def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = {
    val (v, s) = makeSlots
    v <~ hold(s)
  }

  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view flatMap { v ⇒
    val (v1, s) = SafeCast[Any, Slots](v.getTag).map(x ⇒ (Ui(v), x)).getOrElse(makeSlots)
    fillSlots(s, data).flatMap(_ ⇒ v1)
  }
}
