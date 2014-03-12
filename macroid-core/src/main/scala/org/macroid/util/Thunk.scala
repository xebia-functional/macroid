package org.macroid.util

class Thunk[+A](v: ⇒ A) extends Function0[A] {
  def apply() = v
  def map[B](f: A ⇒ B) = Thunk(f(v))
}

object Thunk {
  def apply[A](v: ⇒ A) = new Thunk(v)
}
