package macroid

import android.view.{ ViewGroup, View }
import scala.concurrent.Future
import macroid.util.Ui

/** A Tweak is something that mutates a widget */
case class Tweak[-W <: View](f: W ⇒ Unit) {
  def apply(w: W) = f(w)
}

object Tweak {
  /** A tweak that does nothing */
  def blank[W <: View] = Tweak[W](x ⇒ ())
}

/** A snail mutates the view slowly (e.g. animation) */
case class Snail[-W <: View](f: W ⇒ Future[Unit]) {
  def apply(w: W) = f(w)
}

object Snail {
  /** A snail that does nothing */
  def blank[W <: View] = Snail[W] { x ⇒ Future.successful(()) }
}

case class Transformer(f: PartialFunction[View, Ui[Any]]) {
  def apply(w: View): Unit = {
    f.lift.apply(w).foreach(_.get)
    w match {
      case Transformer.Layout(children @ _*) ⇒ children.foreach(apply)
      case _ ⇒ ()
    }
  }
}

object Transformer {
  /** A Transformer that does nothing */
  def blank = Transformer(PartialFunction.empty)

  /** Layout extractor */
  object Layout {
    def unapplySeq(w: View): Option[Seq[View]] = w match {
      case g: ViewGroup ⇒ Some((0 until g.getChildCount).map(i ⇒ g.getChildAt(i)))
      case _ ⇒ None
    }
  }
}
