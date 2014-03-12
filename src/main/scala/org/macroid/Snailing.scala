package org.macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import scala.async.Async._
import scala.concurrent.{ ExecutionContext, Future }
import org.macroid.util.{ AfterFuture, UiThreadExecutionContext, Ui }

/** A snail mutates the view slowly (e.g. animation) */
case class Snail[-W <: View](f: W ⇒ Future[Unit]) {
  def apply(w: W) = f(w)
}

object Snail {
  /** A snail that does nothing */
  def blank[W <: View] = Snail[W] { x ⇒ Future.successful(()) }
}

@implicitNotFound("Don't know how to snail ${W} with ${S}. Try importing an instance of CanSnail[${W}, ${S}, ...]")
trait CanSnail[W, S, R] {
  def snail(w: W, s: S): Ui[Future[R]]
}

object CanSnail {
  import UiThreading._

  implicit def `Widget is snailable with Snail`[W <: View, S <: Snail[W]](implicit ec: ExecutionContext) =
    new CanSnail[W, S, W] {
      def snail(w: W, s: S) = Ui { s(w).map(_ ⇒ w) }
    }

  implicit def `Widget is snailable with Option`[W <: View, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[W, Option[S], W] {
      def snail(w: W, o: Option[S]) = o.fold(Ui(Future.successful(w))) { s ⇒
        canSnail.snail(w, s).map(f ⇒ f.map(_ ⇒ w))
      }
    }

  implicit def `Option is snailable`[W, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[Option[W], S, Option[W]] {
      def snail(o: Option[W], s: S) = o.fold(Ui(Future.successful(o))) { w ⇒
        canSnail.snail(w, s).map(f ⇒ f.map(_ ⇒ o))
      }
    }

  implicit def `Widget is snailable with Future`[W <: View, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[W, Future[S], W] {
      // we can call Ui.get, since we are already inside the UI thread
      def snail(w: W, f: Future[S]) = Ui(f.flatMapUi(s ⇒ canSnail.snail(w, s).get.map(_ ⇒ w)))
    }

  implicit def `Future is snailable`[W, S, R](implicit ec: ExecutionContext, canSnail: CanSnail[W, S, R]) =
    new CanSnail[Future[W], S, W] {
      // we can call Ui.get, since we are already inside the UI thread
      def snail(f: Future[W], s: S) = Ui(f.flatMapUi(w ⇒ canSnail.snail(w, s).get.map(_ ⇒ w)))
    }

  implicit def `Widget is snailable with List`[W <: View, S, R](implicit canSnail: CanSnail[W, S, R]) =
    new CanSnail[W, List[S], W] {
      def snail(w: W, l: List[S]) = Ui(async {
        val it = l.iterator
        while (it.hasNext) {
          // we can call Ui.get, since we are already inside the UI thread
          await(canSnail.snail(w, it.next()).get)
        }
        w
      }(UiThreadExecutionContext))
    }

  implicit def `List is snailable`[W, S, R](implicit canSnail: CanSnail[W, S, R]) =
    new CanSnail[List[W], S, List[W]] {
      def snail(l: List[W], s: S) = Ui(async {
        val it = l.iterator
        while (it.hasNext) {
          // we can call Ui.get, since we are already inside the UI thread
          await(canSnail.snail(it.next(), s).get)
        }
        l
      }(UiThreadExecutionContext))
    }
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
  implicit class SnailingOps[W](w: W) {
    /** Apply a snail */
    def ~@>[T, R](t: T)(implicit canSnail: CanSnail[W, T, R]): Ui[Future[R]] = canSnail.snail(w, t)
  }
}

object Snailing extends Snailing
