package org.macroid

import android.os.Bundle
import scala.concurrent.{Future, ExecutionContext}
import scalaz.Functor
import io.dylemma.frp.{EventStream, Observer}
import rx.Rx

package object util {
  def map2bundle(m: Map[String, Any]): Bundle = {
    val bundle = new Bundle
    m foreach {
      case (k, v: Int) ⇒ bundle.putInt(k, v)
      case (k, v: String) ⇒ bundle.putString(k, v)
      case (k, v: Boolean) ⇒ bundle.putBoolean(k, v)
      case _ ⇒ ??? // TODO: support more things here!
    }
    bundle
  }

  class Thunk[+A](v: ⇒ A) extends Function0[A] {
    def apply() = v
  }
  object Thunk {
    def apply[A](v: ⇒ A) = new Thunk(v)
  }

  trait SyncFunctor[F[_]] {
    val async: Boolean
  }
  trait Functors {
    implicit val listF = scalaz.std.list.listInstance
    implicit val optionF = scalaz.std.option.optionInstance
    implicit def futureF(implicit ec: ExecutionContext) = new Functor[Future] {
      def map[A, B](fa: Future[A])(f: A ⇒ B) = fa.map(f)
    }
    implicit def eventStreamF(implicit ob: Observer) = new Functor[EventStream] {
      def map[A, B](fa: EventStream[A])(f: A ⇒ B) = fa.map(f)
    }
    implicit object rxF extends Functor[Rx] {
      def map[A, B](fa: Rx[A])(f: A ⇒ B) = fa.map(f)
    }
  }
  object SyncFunctors extends Functors {
    implicit object listSF extends SyncFunctor[List] { val async = false }
    implicit object optionSF extends SyncFunctor[Option] { val async = false }
    implicit object futureSF extends SyncFunctor[Future] { val async = true }
    implicit object eventStreamSF extends SyncFunctor[EventStream] { val async = true }
    implicit object rxSF extends SyncFunctor[Rx] { val async = true }
  }
}
