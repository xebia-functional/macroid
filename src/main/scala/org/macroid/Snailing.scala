package org.macroid

import scala.language.higherKinds
import android.view.View
import scala.annotation.implicitNotFound
import scala.async.Async._
import scala.concurrent.{ Promise, ExecutionContext, Future }

/** A snail mutates the view slowly (e.g. animation) */
case class Snail[-A <: View](f: A ⇒ Future[Unit]) {
  def apply(v: A) = f(v)
}

object Snail {
  /** A snail that does nothing */
  def blank[A <: View] = Snail[A] { x ⇒ Future.successful(()) }
}

@implicitNotFound("Don't know how to snail ${V} with ${S}. Try importing an instance of Snailable[${V}, ${S}]")
trait SnailableWith[V, S] {
  def snailWith(v: V, s: S): Future[Unit]
}

/** This trait defines snails, snailing operator (~@>) and its generalizations */
trait Snailing {
  /** Combining tweaks with snails */
  implicit class TweakSnailAddition[A <: View](t: Tweak[A]) {
    /** Combine (sequence) with a snail */
    def +@[B <: A](other: Snail[B]) = Snail[B] { x ⇒ t(x); other(x) }
  }

  /** Combining snails */
  implicit class SnailAddition[A <: View](s: Snail[A]) {
    /** Combine (sequence) with a tweak */
    def @+[B <: A](other: Tweak[B])(implicit ec: ExecutionContext) = Snail[B] { x ⇒
      s(x).map(_ ⇒ UiThreading.ui(other(x)))
    }
    /** Combine (sequence) with another snail */
    def @+@[B <: A](other: Snail[B])(implicit ec: ExecutionContext) = Snail[B] { x ⇒
      s(x).flatMap(_ ⇒ UiThreading.ui(other(x)))
    }
  }

  /** Snailing operator and its aliases */
  implicit class FutureSnailingOps[V](f: Future[V])(implicit ec: ExecutionContext) {
    /** Apply a snail on the UI thread */
    def ~@>[T](t: T)(implicit snailableWith: V SnailableWith T): Future[V] =
      f.flatMap(v ⇒ snailableWith.snailWith(v, t).map(_ ⇒ v))

    /** Apply a snail on the UI thread (plain text alias for `~@>`) */
    def snailWith[T](t: T)(implicit snailableWith: V SnailableWith T): Future[V] =
      f.flatMap(v ⇒ snailableWith.snailWith(v, t).map(_ ⇒ v))
  }

  /** Snailing operator and its aliases */
  implicit class SnailingOps[V](v: V)(implicit ec: ExecutionContext) {
    /** Apply a snail on the UI thread */
    def ~@>[T](t: T)(implicit snailableWith: V SnailableWith T): Future[V] =
      snailableWith.snailWith(v, t).map(_ ⇒ v)

    /** Apply a snail on the UI thread (plain text alias for `~@>`) */
    def snailWith[T](t: T)(implicit snailableWith: V SnailableWith T): Future[V] =
      snailableWith.snailWith(v, t).map(_ ⇒ v)
  }

  // format: OFF

  /** A widget can be snailed with a Snail */
  implicit def snailableWithSnail[V <: View, S <: Snail[V]](implicit ec: ExecutionContext) =
    new (V SnailableWith S) {
      def snailWith(v: V, s: S) = {
        val snailPromise = Promise[Unit]()
        UiThreading.fireUi(snailPromise.completeWith(s(v)))
        snailPromise.future
      }
    }

  /** A widget can be snailed with an Option */
  implicit def snailableWithOption[V <: View, S](implicit ec: ExecutionContext, snailableWith: V SnailableWith S) =
    new (V SnailableWith Option[S]) {
      def snailWith(v: V, o: Option[S]) = o.fold(Future.successful(()))(s ⇒ snailableWith.snailWith(v, s))
    }

  /** Option can be snailed with something */
  implicit def optionSnailable[V, S](implicit ec: ExecutionContext, snailableWith: V SnailableWith S) =
    new (Option[V] SnailableWith S) {
      def snailWith(o: Option[V], s: S) = o.fold(Future.successful(()))(v ⇒ snailableWith.snailWith(v, s))
    }

  /** A widget can be snailed with a Future */
  implicit def snailableWithFuture[V <: View, S](implicit ec: ExecutionContext, snailableWith: V SnailableWith S) =
    new (V SnailableWith Future[S]) {
      def snailWith(v: V, f: Future[S]) = f.flatMap(s ⇒ snailableWith.snailWith(v, s))
    }

  /** Future can be snailed with something */
  implicit def futureSnailable[V, S](implicit ec: ExecutionContext, snailableWith: V SnailableWith S) =
    new (Future[V] SnailableWith S) {
      def snailWith(f: Future[V], s: S) = f.flatMap(v ⇒ snailableWith.snailWith(v, s))
    }

  /** A widget can be snailed with a List */
  implicit def snailableWithList[V <: View, S](implicit ec: ExecutionContext, snailableWith: V SnailableWith S) =
    new (V SnailableWith List[S]) {
      def snailWith(v: V, l: List[S]) = async {
        val it = l.iterator
        while (it.hasNext) {
          await(snailableWith.snailWith(v, it.next()))
        }
      }
    }

  /** List can be snailed with something */
  implicit def listSnailable[V, S](implicit ec: ExecutionContext, snailableWith: V SnailableWith S) =
    new (List[V] SnailableWith S) {
      def snailWith(l: List[V], s: S) = async {
        val it = l.iterator
        while (it.hasNext) {
          await(snailableWith.snailWith(it.next(), s))
        }
      }
    }

  // format: ON
}

object Snailing extends Snailing
