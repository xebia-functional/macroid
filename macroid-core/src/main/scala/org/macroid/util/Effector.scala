package org.macroid.util

import scala.language.higherKinds
import scala.concurrent.{ Future, ExecutionContext }
import io.dylemma.frp.{ EventStream, Observer }

trait Effector[F[_]] {
  def foreach[A](fa: F[A])(f: A ⇒ Any): Unit
}

trait Effectors {
  implicit object listEffector extends Effector[List] {
    def foreach[A](fa: List[A])(f: A ⇒ Any) = fa.foreach(f)
  }

  implicit object optionEffector extends Effector[Option] {
    def foreach[A](fa: Option[A])(f: A ⇒ Any) = fa.foreach(f)
  }

  implicit def futureEffector(implicit ec: ExecutionContext) = new Effector[Future] {
    def foreach[A](fa: Future[A])(f: A ⇒ Any) = fa.foreach(f)
  }

  implicit def eventStreamEffector(implicit obs: Observer) = new Effector[EventStream] {
    def foreach[A](fa: EventStream[A])(f: A ⇒ Any) = fa.foreach(f)
  }
}

object Effectors extends Effectors
