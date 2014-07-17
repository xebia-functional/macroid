package macroid

import android.view.{ ViewGroup, View }
import scala.concurrent.Future

/** A Tweak is something that mutates a widget */
case class Tweak[-W <: View](f: W ⇒ Unit) {
  def apply(w: W) = f(w)

  /** Combine (sequence) with another tweak */
  def +[W1 <: W](that: Tweak[W1]): Tweak[W1] = Tweak { x ⇒
    this(x)
    that(x)
  }

  /** Combine (sequence) with a snail */
  def ++[W1 <: W](that: Snail[W1]): Snail[W1] = Snail { x ⇒
    this(x)
    that(x)
  }
}

object Tweak {
  /** A tweak that does nothing */
  def blank[W <: View] = Tweak[W](_ ⇒ ())
}

/** A snail mutates the view slowly (e.g. animation) */
case class Snail[-W <: View](f: W ⇒ Future[Unit]) {
  import UiThreading._

  def apply(w: W) = f(w)

  /** Combine (sequence) with another snail */
  def ++[W1 <: W](that: Snail[W1]): Snail[W1] = Snail { x ⇒
    // make sure to keep the UI thread
    this(x).flatMapUi(_ ⇒ that(x))
  }

  /** Combine (sequence) with a tweak */
  def +[W1 <: W](that: Tweak[W1]): Snail[W1] = Snail { x ⇒
    // make sure to keep the UI thread
    this(x).mapUi(_ ⇒ that(x))
  }
}

object Snail {
  /** A snail that does nothing */
  def blank[W <: View] = Snail[W](_ ⇒ Future.successful(()))
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
