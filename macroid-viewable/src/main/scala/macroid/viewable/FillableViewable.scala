package macroid.viewable

import macroid._
import macroid.LayoutDsl._
import macroid.Tweaks._
import macroid.util.Ui
import scala.util.Try
import android.view.{ View, ViewGroup }
import android.widget.TextView

trait FillableViewable[A] extends Viewable[A] {
  def makeView(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W]
  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Ui[Any]

  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = {
    val view = makeView
    fillView(view, data).flatMap(_ ⇒ view)
  }
}

object FillableViewable {
  def apply[A, V <: View](make: Ui[V], fill: Ui[V] ⇒ A ⇒ Ui[Any]) = new FillableViewable[A] {
    type W = V
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(view)(data)
  }

  def text(make: Tweak[TextView]) = new TweakFillableViewable[String] {
    type W = TextView
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] <~ make
    def tweak(data: String)(implicit ctx: ActivityContext, appCtx: AppContext) = Tweaks.text(data)
  }

  def text[A](make: Tweak[TextView], fill: A ⇒ Tweak[TextView]) = new TweakFillableViewable[A] {
    type W = TextView
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] <~ make
    def tweak(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(data)
  }

  def tw[A, V <: View](make: Ui[V], fill: A ⇒ Tweak[V]) = new TweakFillableViewable[A] {
    type W = V
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def tweak(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(data)
  }

  def tr[A](make: Ui[ViewGroup], fill: A ⇒ Transformer) = new TransformerFillableViewable[A] {
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
  def fillSlots(slots: Slots, data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Any

  type W = View

  def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = {
    val (v, s) = makeSlots
    v <~ hold(s)
  }

  def fillView(view: Ui[W], data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view map { v ⇒
    val slots = Option(v.getTag).flatMap(x ⇒ Try(x.asInstanceOf[Slots]).toOption).getOrElse(makeSlots._2)
    fillSlots(slots, data)
  }
}
