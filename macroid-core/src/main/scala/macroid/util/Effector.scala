package macroid.util

import scala.language.higherKinds
import scala.concurrent.{ Future, ExecutionContext }
import scala.util.Try

trait Effector[-F[_]] {
  def foreach[A](fa: F[A])(f: A ⇒ Any): Unit
}

object Effector {

  implicit object `TraversableOnce is Effector` extends Effector[TraversableOnce] {
    override def foreach[A](fa: TraversableOnce[A])(f: A ⇒ Any): Unit = fa.foreach(f)
  }

  implicit object `Option is Effector` extends Effector[Option] {
    def foreach[A](fa: Option[A])(f: A ⇒ Any) = fa.foreach(f)
  }

  implicit object `Try is Effector` extends Effector[Try] {
    def foreach[A](fa: Try[A])(f: A ⇒ Any) = fa.foreach(f)
  }

  implicit def `Future is Effector`(implicit ec: ExecutionContext) = new Effector[Future] {
    import macroid.UiThreading.InPlaceFuture

    def foreach[A](fa: Future[A])(f: A ⇒ Any) = fa.foreachInPlace(f)
  }
}
