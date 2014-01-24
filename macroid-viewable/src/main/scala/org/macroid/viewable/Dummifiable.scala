package org.macroid.viewable

trait Dummifiable[A] {
  def dummy: A
}

object Dummifiable {
  def dummy[A: Dummifiable] = implicitly[Dummifiable[A]].dummy
}
