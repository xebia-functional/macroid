package macroid.util

import macroid._

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Effector[-F[_]] {
  def foreach[A](fa: F[A])(f: A ⇒ Ui[Any]): Unit
}

object Effector {

  implicit object `TraversableOnce is Effector` extends Effector[TraversableOnce] {
    override def foreach[A](fa: TraversableOnce[A])(f: A ⇒ Ui[Any]): Unit =
      fa.foreach(a ⇒ f(a).run)
  }

  implicit object `Option is Effector` extends Effector[Option] {
    def foreach[A](fa: Option[A])(f: A ⇒ Ui[Any]) = fa.foreach(a ⇒ f(a).run)
  }

  implicit object `Try is Effector` extends Effector[Try] {
    def foreach[A](fa: Try[A])(f: A ⇒ Ui[Any]) = fa.foreach(a ⇒ f(a).run)
  }

  implicit def `Future is Effector`(implicit ec: ExecutionContext) =
    new Effector[Future] {
      def foreach[A](fa: Future[A])(f: A ⇒ Ui[Any]) = fa.mapUi(f)
    }
}
