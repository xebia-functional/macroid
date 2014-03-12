package macroid.viewable

trait Dummifiable[A] {
  def dummy: A
}

object Dummifying {
  def dummy[A: Dummifiable] = implicitly[Dummifiable[A]].dummy
}
