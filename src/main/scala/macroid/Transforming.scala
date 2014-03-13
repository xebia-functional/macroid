package macroid

import android.view.{ ViewGroup, View }
import macroid.util.Ui

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

trait CanTransform[L, T, R] {
  def transform(l: L, t: T): Ui[R]
}

object CanTransform {
  implicit def `Layout is transformable`[L <: ViewGroup] =
    new CanTransform[L, Transformer, L] {
      def transform(l: L, t: Transformer) = Ui { t(l); l }
    }

  implicit def `Ui is transformable`[L, T, R](implicit canTransform: CanTransform[L, T, R]) =
    new CanTransform[Ui[L], T, L] {
      def transform(ui: Ui[L], t: T) = ui flatMap { l ⇒ canTransform.transform(l, t).map(_ ⇒ l) }
    }
}

private[macroid] trait Transforming {
  /** Transforming operator */
  implicit class TransformingOps[L](l: L) {
    /** Apply a transformer */
    def <~~[T, R](t: T)(implicit canTransform: CanTransform[L, T, R]) = canTransform.transform(l, t)
  }
}

object Transforming extends Transforming
