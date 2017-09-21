package macroid

import android.view.{ ViewGroup, View }
import scala.concurrent.Future

/** A Tweak is something that mutates a widget */
case class Tweak[-W <: View](f: W ⇒ Unit) {
  def apply(w: W) = Ui(f(w))

  /** Combine (sequence) with another tweak */
  def +[W1 <: W](that: Tweak[W1]) = Tweak[W1] { x ⇒
    this.f(x)
    that.f(x)
  }

  /** Combine (sequence) with a snail */
  def ++[W1 <: W](that: Snail[W1]) = Snail[W1] { x ⇒
    this.f(x)
    that.f(x)
  }
}

object Tweak {
  /** A tweak that does nothing */
  def blank[W <: View] = Tweak[W](_ ⇒ ())
}

/** A snail mutates the view slowly (e.g. animation) */
case class Snail[-W <: View](f: W ⇒ Future[Unit]) {
  def apply(w: W) = Ui(f(w))

  /** Combine (sequence) with another snail */
  def ++[W1 <: W](that: Snail[W1]) = Snail[W1] { x ⇒
    // make sure to keep the UI thread
    this.f(x).flatMap(_ ⇒ that.f(x))(UiThreadExecutionContext)
  }

  /** Combine (sequence) with a tweak */
  def +[W1 <: W](that: Tweak[W1]) = Snail[W1] { x ⇒
    // make sure to keep the UI thread
    this.f(x).map(_ ⇒ that.f(x))(UiThreadExecutionContext)
  }
}

object Snail {
  /** A snail that does nothing */
  def blank[W <: View] = Snail[W](_ ⇒ Future.successful(()))
}

case class Transformer(f: PartialFunction[View, Ui[Any]]) {
  def apply(w: View): Ui[Any] = {
    val self = f.applyOrElse(w, Function.const(Ui.nop))
    val children = w match {
      case Transformer.Layout(children @ _*) ⇒ Ui.sequence(children.map(apply): _*)
      case _ ⇒ Ui.nop
    }
    self ~ children
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

/** An Excerpt is something that gets a property out of a widget */
case class Excerpt[-W <: View, +R](f: W ⇒ R) {
  def apply(w: W) = Ui(f(w))

  /** map combinator */
  def map[R1](g: R ⇒ R1) = Excerpt(f andThen g)

  /** Combine (tuple) with another excerpt */
  def +[W1 <: W, R1](that: Excerpt[W1, R1]) = Excerpt[W1, (R, R1)] { x ⇒
    (this.f(x), that.f(x))
  }
}
