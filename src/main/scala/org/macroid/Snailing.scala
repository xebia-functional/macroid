package org.macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import scala.async.Async._
import scala.concurrent.{ Promise, ExecutionContext, Future }

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
  def snailWith(w: W, s: S): Future[Unit]
}

/** This trait defines snails, snailing operator (~@>) and its generalizations */
trait Snailing {
  /** Combining tweaks with snails */
  implicit class TweakSnailAddition[W <: View](t: Tweak[W]) {
    /** Combine (sequence) with a snail */
    def +@[V <: W](other: Snail[V]) = Snail[V] { x ⇒ t(x); other(x) }
  }

  /** Combining snails */
  implicit class SnailAddition[W <: View](s: Snail[W]) {
    /** Combine (sequence) with a tweak */
    def @+[V <: W](other: Tweak[V])(implicit ec: ExecutionContext) = Snail[V] { x ⇒
      s(x).map(_ ⇒ UiThreading.ui(other(x)))
    }
    /** Combine (sequence) with another snail */
    def @+@[V <: W](other: Snail[V])(implicit ec: ExecutionContext) = Snail[V] { x ⇒
      s(x).flatMap(_ ⇒ UiThreading.ui(other(x)))
    }
  }

  /** Snailing operator and its aliases */
  implicit class FutureSnailingOps[W](f: Future[W])(implicit ec: ExecutionContext) {
    /** Apply a snail on the UI thread */
    def ~@>[T](t: T)(implicit snailableWith: W SnailableWith T): Future[W] =
      f.flatMap(w ⇒ snailableWith.snailWith(w, t).map(_ ⇒ w))

    /** Apply a snail on the UI thread (plain text alias for `~@>`) */
    def snailWith[T](t: T)(implicit snailableWith: W SnailableWith T): Future[W] =
      f.flatMap(w ⇒ snailableWith.snailWith(w, t).map(_ ⇒ w))
  }

  /** Snailing operator and its aliases */
  implicit class SnailingOps[W](w: W)(implicit ec: ExecutionContext) {
    /** Apply a snail on the UI thread */
    def ~@>[T](t: T)(implicit snailableWith: W SnailableWith T): Future[W] =
      snailableWith.snailWith(w, t).map(_ ⇒ w)

    /** Apply a snail on the UI thread (plain text alias for `~@>`) */
    def snailWith[T](t: T)(implicit snailableWith: W SnailableWith T): Future[W] =
      snailableWith.snailWith(w, t).map(_ ⇒ w)
  }

  // format: OFF

  /** A widget can be snailed with a Snail */
  implicit def snailableWithSnail[W <: View, S <: Snail[W]](implicit ec: ExecutionContext) =
    new (W SnailableWith S) {
      def snailWith(w: W, s: S) = {
        val snailPromise = Promise[Unit]()
        UiThreading.fireUi(snailPromise.completeWith(s(w)))
        snailPromise.future
      }
    }

  /** A widget can be snailed with an Option */
  implicit def snailableWithOption[W <: View, S](implicit ec: ExecutionContext, snailableWith: W SnailableWith S) =
    new (W SnailableWith Option[S]) {
      def snailWith(w: W, o: Option[S]) = o.fold(Future.successful(()))(s ⇒ snailableWith.snailWith(w, s))
    }

  /** Option can be snailed with something */
  implicit def optionSnailable[W, S](implicit ec: ExecutionContext, snailableWith: W SnailableWith S) =
    new (Option[W] SnailableWith S) {
      def snailWith(o: Option[W], s: S) = o.fold(Future.successful(()))(w ⇒ snailableWith.snailWith(w, s))
    }

  /** A widget can be snailed with a Future */
  implicit def snailableWithFuture[W <: View, S](implicit ec: ExecutionContext, snailableWith: W SnailableWith S) =
    new (W SnailableWith Future[S]) {
      def snailWith(w: W, f: Future[S]) = f.flatMap(s ⇒ snailableWith.snailWith(w, s))
    }

  /** Future can be snailed with something */
  implicit def futureSnailable[W, S](implicit ec: ExecutionContext, snailableWith: W SnailableWith S) =
    new (Future[W] SnailableWith S) {
      def snailWith(f: Future[W], s: S) = f.flatMap(w ⇒ snailableWith.snailWith(w, s))
    }

  /** A widget can be snailed with a List */
  implicit def snailableWithList[W <: View, S](implicit ec: ExecutionContext, snailableWith: W SnailableWith S) =
    new (W SnailableWith List[S]) {
      def snailWith(w: W, l: List[S]) = async {
        val it = l.iterator
        while (it.hasNext) {
          await(snailableWith.snailWith(w, it.next()))
        }
      }
    }

  /** List can be snailed with something */
  implicit def listSnailable[W, S](implicit ec: ExecutionContext, snailableWith: W SnailableWith S) =
    new (List[W] SnailableWith S) {
      def snailWith(l: List[W], s: S) = async {
        val it = l.iterator
        while (it.hasNext) {
          await(snailableWith.snailWith(it.next(), s))
        }
      }
    }

  // format: ON
}

object Snailing extends Snailing
