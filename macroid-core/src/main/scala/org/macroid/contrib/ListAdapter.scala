package org.macroid.contrib

import android.view.{ ViewGroup, View }
import android.content.Context
import android.widget.{ TextView, ArrayAdapter }
import scala.util.Try
import org.macroid.LayoutDsl
import scala.collection.JavaConversions._

abstract class ListAdapter[A, B <: View](implicit ctx: Context) extends ArrayAdapter[A](ctx, 0) {
  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    val v = Option(view).flatMap(x ⇒ Try(x.asInstanceOf[B]).toOption).getOrElse(makeView)
    fillView(v, getItem(position)); v
  }
  def makeView: B
  def fillView(view: B, data: A): Any
}

object ListAdapter extends LayoutDsl {
  def text[A](data: Seq[A])(t: Tweak[TextView], f: A ⇒ Tweak[TextView])(implicit ctx: Context) = new ListAdapter[A, TextView] {
    addAll(data)
    def makeView = new TextView(ctx) ~> t
    def fillView(view: TextView, data: A) = view ~> f(data)
  }
  def simple[A, B <: View](data: Seq[A])(v: ⇒ B, f: A ⇒ Tweak[B])(implicit ctx: Context) = new ListAdapter[A, B] {
    addAll(data)
    def makeView = v
    def fillView(view: B, data: A) = view ~> f(data)
  }
  def apply[A, B <: ViewGroup](data: Seq[A])(l: ⇒ B, f: A ⇒ Transformer)(implicit ctx: Context) = new ListAdapter[A, B] {
    addAll(data)
    def makeView = l
    def fillView(view: B, data: A) = view ~~> f(data)
  }
}
