package org.macroid

import android.view.{ ViewGroup, View }

case class Transformer(f: PartialFunction[View, Unit]) {
  def apply(w: View): Unit = {
    f.lift.apply(w)
    w match {
      case Layout(children @ _*) ⇒ children.foreach(apply)
      case _ ⇒ ()
    }
  }
}

object Transformer {
  /** A Transformer that does nothing */
  def blank = Transformer(PartialFunction.empty)
}

/** Layout extractor */
object Layout {
  def unapplySeq(w: View): Option[Seq[View]] = w match {
    case g: ViewGroup ⇒ Some((0 until g.getChildCount).map(i ⇒ g.getChildAt(i)))
    case _ ⇒ None
  }
}

trait Transforming {
  /** Transforming operator */
  implicit class TransformingOps[L <: ViewGroup](l: L) {
    /** Apply transformer. Always runs on UI thread */
    def ~~>(t: Transformer) = { UiThreading.runOnUiThread(t(l)); l }
    /** Apply transformer. Always runs on UI thread (plain text alias for ~~>) */
    def transformWith(t: Transformer) = { UiThreading.runOnUiThread(t(l)); l }
  }
}

object Transforming extends Transforming
