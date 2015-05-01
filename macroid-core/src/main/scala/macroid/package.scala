import scala.language.implicitConversions
import scala.concurrent.Future

package object macroid extends Tweaking with Snailing with Excerpting {
  implicit def futureToUiFuture[A](future: Future[A]): UiFuture[A] = UiFuture(future)
}
