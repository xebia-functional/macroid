package org.macroid.util

class Thunk[+A](v: ⇒ A) extends Function0[A] {
  def apply() = v
}
object Thunk {
  def apply[A](v: ⇒ A) = new Thunk(v)
}