package org.macroid.util

import scala.concurrent.{ Future, ExecutionContext }
import scalaz.Functor
import io.dylemma.frp.{ EventStream, Observer }
import rx.Rx
import org.macroid.LayoutBuilding
import android.view.View

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

object Functors extends Functors
