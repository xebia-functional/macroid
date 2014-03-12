package macroid.util

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal

object AfterFuture {
  def apply[A, B](f: Future[A], value: B)(implicit ec: ExecutionContext) = f.map(_ ⇒ value).recover { case NonFatal(_) ⇒ value }
}
