package org.macroid.contrib

import android.view.{ ViewGroup, View }
import android.widget.{ TextView, ArrayAdapter }
import scala.util.Try
import org.macroid.{ Transformer, Tweak, ActivityContext, LayoutDsl }
import scala.collection.JavaConversions._

abstract class ListAdapter[A, B <: View](implicit ctx: ActivityContext) extends ArrayAdapter[A](ctx.get, 0) {
  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    val v = Option(view).flatMap(x ⇒ Try(x.asInstanceOf[B]).toOption).getOrElse(makeView)
    fillView(v, parent, getItem(position)); v
  }
  def makeView: B
  def fillView(view: B, parent: ViewGroup, data: A): Any
}

object ListAdapter {
  import LayoutDsl._

  def text[A](data: Seq[A])(t: Tweak[TextView], f: A ⇒ Tweak[TextView])(implicit ctx: ActivityContext) = new ListAdapter[A, TextView] {
    addAll(data)
    def makeView = new TextView(ctx.get) ~> t
    def fillView(view: TextView, parent: ViewGroup, data: A) = view ~> f(data)
  }
  def simple[A, B <: View](data: Seq[A])(v: ⇒ B, f: A ⇒ Tweak[B])(implicit ctx: ActivityContext) = new ListAdapter[A, B] {
    addAll(data)
    def makeView = v
    def fillView(view: B, parent: ViewGroup, data: A) = view ~> f(data)
  }
  def apply[A, B <: ViewGroup](data: Seq[A])(l: ⇒ B, f: A ⇒ Transformer)(implicit ctx: ActivityContext) = new ListAdapter[A, B] {
    addAll(data)
    def makeView = l
    def fillView(view: B, parent: ViewGroup, data: A) = view ~~> f(data)
  }
}
