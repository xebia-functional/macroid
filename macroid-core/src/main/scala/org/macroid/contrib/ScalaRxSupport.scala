package org.macroid.contrib

import scalaz.Functor
import rx.Rx

object ScalaRxSupport {
  implicit object rxF extends Functor[Rx] {
    def map[A, B](fa: Rx[A])(f: A ⇒ B) = fa.map(f)
  }
}
