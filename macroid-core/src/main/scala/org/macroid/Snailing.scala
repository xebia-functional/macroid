package org.macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import scala.async.Async._
import scala.concurrent.{ ExecutionContext, Future }
import org.macroid.util.{ UiThreadExecutionContext, Ui }

/** A snail mutates the view slowly (e.g. animation) */
case class Snail[-W <: View](f: W ⇒ Future[Unit]) {
  def apply(w: W) = f(w)
}

object Snail {
  /** A snail that does nothing */
  def blank[W <: View] = Snail[W] { x ⇒ Future.successful(()) }
}

@implicitNotFound("Don't know how to snail ${W} with ${S}. Try importing an instance of Snailable[${W}, ${S}]")
trait SnailableWith[W, S] {
  /** Warning: this method expects to be called from the UI thread. */
  def snailWith(w: W, s: S): Future[Unit]
}

object SnailableWith {
  import UiThreading._

  // format: OFF

  implicit def `Widget is snailable with Snail`[W <: View, S <: Snail[W]] =
    new (W SnailableWith S) {
      def snailWith(w: W, s: S) = s(w)
    }

  implicit def `Widget is snailable with Option`[W <: View, S](implicit snailable: W SnailableWith S) =
    new (W SnailableWith Option[S]) {
      def snailWith(w: W, o: Option[S]) = o.fold(Future.successful(()))(s ⇒ snailable.snailWith(w, s))
    }

  implicit def `Option is snailable`[W, S](implicit snailable: W SnailableWith S) =
    new (Option[W] SnailableWith S) {
      def snailWith(o: Option[W], s: S) = o.fold(Future.successful(()))(w ⇒ snailable.snailWith(w, s))
    }

  implicit def `Widget is snailable with Future`[W <: View, S](implicit snailable: W SnailableWith S) =
    new (W SnailableWith Future[S]) {
      // make sure to handle the future on the UI thread
      def snailWith(w: W, f: Future[S]) = f.flatMapUi(s ⇒ snailable.snailWith(w, s))
    }

  implicit def `Future is snailable`[W, S](implicit snailable: W SnailableWith S) =
    new (Future[W] SnailableWith S) {
      // make sure to handle the future on the UI thread
      def snailWith(f: Future[W], s: S) = f.flatMapUi(w ⇒ snailable.snailWith(w, s))
    }

  implicit def `Widget is snailable with List`[W <: View, S](implicit snailable: W SnailableWith S) =
    new (W SnailableWith List[S]) {
      def snailWith(w: W, l: List[S]) = async {
        val it = l.iterator
        while (it.hasNext) {
          await(snailable.snailWith(w, it.next()))
        }
        // make sure to call this business from the UI thread
      }(UiThreadExecutionContext)
    }

  implicit def `List is snailable`[W, S](implicit snailable: W SnailableWith S) =
    new (List[W] SnailableWith S) {
      def snailWith(l: List[W], s: S) = async {
        val it = l.iterator
        while (it.hasNext) {
          await(snailable.snailWith(it.next(), s))
        }
        // make sure to call this business from the UI thread
      }(UiThreadExecutionContext)
    }

  // format: ON
}

/** This trait defines snails, snailing operator (~@>) and its generalizations */
private[macroid] trait Snailing {
  import UiThreading._

  /** Combining tweaks with snails */
  implicit class TweakSnailAddition[W <: View](t: Tweak[W]) {
    /** Combine (sequence) with a snail */
    def +@[W1 <: W](other: Snail[W1]) = Snail[W1] { x ⇒ t(x); other(x) }
  }

  /** Combining snails */
  implicit class SnailAddition[W <: View](s: Snail[W]) {
    /** Combine (sequence) with a tweak */
    def @+[W1 <: W](other: Tweak[W1]) = Snail[W1] { x ⇒
      // make sure to keep the UI thread
      s(x).mapUi(_ ⇒ other(x))
    }
    /** Combine (sequence) with another snail */
    def @+@[W1 <: W](other: Snail[W1]) = Snail[W1] { x ⇒
      // make sure to keep the UI thread
      s(x).flatMapUi(_ ⇒ other(x))
    }
  }

  /** Snailing operator */
  implicit class UiFutureSnailingOps[W](ui: Ui[Future[W]])(implicit ec: ExecutionContext) {
    /** Apply a snail */
    def ~@>[T](t: T)(implicit snailableWith: W SnailableWith T): Ui[Future[W]] =
      // make sure to keep the UI thread
      ui map { f ⇒ f.flatMapUi(w ⇒ snailableWith.snailWith(w, t).map(_ ⇒ w)) }
  }

  /** Snailing operator */
  implicit class UiSnailingOps[W](ui: Ui[W])(implicit ec: ExecutionContext) {
    /** Apply a snail */
    def ~@>[T](t: T)(implicit snailable: W SnailableWith T): Ui[Future[W]] =
      ui map { w ⇒ snailable.snailWith(w, t).map(_ ⇒ w) }
  }

  /** Snailing operator */
  implicit class FutureSnailingOps[W](f: Future[W])(implicit ec: ExecutionContext) {
    /** Apply a snail */
    def ~@>[T](t: T)(implicit snailableWith: W SnailableWith T): Ui[Future[W]] =
      // make sure to keep the UI thread
      Ui(f.flatMapUi(w ⇒ snailableWith.snailWith(w, t).map(_ ⇒ w)))
  }

  /** Snailing operator */
  implicit class SnailingOps[W](w: W)(implicit ec: ExecutionContext) {
    /** Apply a snail */
    def ~@>[T](t: T)(implicit snailableWith: W SnailableWith T): Ui[Future[W]] =
      Ui(snailableWith.snailWith(w, t).map(_ ⇒ w))
  }
}

object Snailing extends Snailing
