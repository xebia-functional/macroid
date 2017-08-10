package macroid.util

import scala.util.Try

object SafeCast {
  def apply[From, To](x: From) =
    Try(Option(x).map(_.asInstanceOf[To])).toOption.flatten
}
