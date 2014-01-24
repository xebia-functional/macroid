package org.macroid.viewable

import org.macroid.{ Transformer, Tweak, AppContext, ActivityContext }
import org.macroid.LayoutBuilding._
import org.macroid.Tweaking._
import org.macroid.Transforming._
import scala.util.Try
import android.view.{ View, ViewGroup }
import android.widget.TextView

trait FillableViewable[A] extends Viewable[A] {
  def makeView(implicit ctx: ActivityContext, appCtx: AppContext): W
  def fillView(view: W, data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Any

  def layout(data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = {
    val view = makeView
    fillView(view, data); view
  }
}

object FillableViewable {
  def apply[A, V <: View](make: ⇒ V, fill: V ⇒ A ⇒ Any) = new FillableViewable[A] {
    type W = V
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def fillView(view: W, data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = fill(view)(data)
  }

  def text[A](make: Tweak[TextView], fill: A ⇒ Tweak[TextView]) = new TweakFillableViewable[A] {
    type W = TextView
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = w[TextView] ~> make
    def tweak(data: A) = fill(data)
  }

  def tw[A, V <: View](make: ⇒ V, fill: A ⇒ Tweak[V]) = new TweakFillableViewable[A] {
    type W = V
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def tweak(data: A) = fill(data)
  }

  def tr[A](make: ⇒ ViewGroup, fill: A ⇒ Transformer) = new TransformerFillableViewable[A] {
    def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = make
    def transformer(data: A) = fill(data)
  }
}

trait TweakFillableViewable[A] extends FillableViewable[A] {
  def tweak(data: A): Tweak[W]

  def fillView(view: W, data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view ~> tweak(data)
}

trait TransformerFillableViewable[A] extends FillableViewable[A] {
  def transformer(data: A): Transformer

  type W = ViewGroup
  def fillView(view: W, data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = view ~~> transformer(data)
}

trait SlottedFillableViewable[A] extends FillableViewable[A] {
  type Slots
  def makeSlots(implicit ctx: ActivityContext, appCtx: AppContext): (W, Slots)
  def fillSlots(slots: Slots, data: A)(implicit ctx: ActivityContext, appCtx: AppContext): Any

  type W = View

  def makeView(implicit ctx: ActivityContext, appCtx: AppContext) = {
    val (v, s) = makeSlots
    v.setTag(s); v
  }

  def fillView(view: W, data: A)(implicit ctx: ActivityContext, appCtx: AppContext) = {
    val slots = Option(view.getTag).flatMap(x ⇒ Try(x.asInstanceOf[Slots]).toOption).getOrElse(makeSlots._2)
    fillSlots(slots, data)
  }
}
