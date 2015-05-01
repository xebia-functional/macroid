import scala.language.implicitConversions
import scala.concurrent.Future

package object macroid extends Tweaking with Snailing {
  implicit def futureToUiFuture[A](future: Future[A]): UiFuture[A] = UiFuture(future)
}
