package macroid.util

import macroid.UiThreading
import scala.concurrent.{ ExecutionContext, Future }
import android.os.{ Handler, Looper }
import scala.util.{ Failure, Success, Try }

object UiThreadExecutionContext extends ExecutionContext {
  private lazy val uiHandler = new Handler(Looper.getMainLooper)

  def reportFailure(t: Throwable) = t.printStackTrace()
  def execute(runnable: Runnable) = uiHandler.post(runnable)
}

class Ui[+A](v: () ⇒ A) {
  import UiThreading._

  def map[B](f: A ⇒ B) = Ui(f(v()))
  def flatMap[B](f: A ⇒ Ui[B]) = Ui(f(v()).get)

  /** Run the code on the UI thread */
  def run = if (Ui.uiThread == Thread.currentThread) {
    Try(v()) match {
      case Success(x) ⇒ Future.successful(x)
      case Failure(x) ⇒ Future.failed(x)
    }
  } else {
    Future(v())(UiThreadExecutionContext)
  }

  /** Run the code on the UI thread, flattening the Future it returns */
  def flatRun[B](implicit evidence: A <:< Future[B]) = Future.successful(()).flatMapUi(_ ⇒ evidence(v()))

  /** Get the result of executing the code on the current (hopefully, UI!) thread */
  def get = v()
}

object Ui {
  private lazy val uiThread = Looper.getMainLooper.getThread

  def apply[A](v: ⇒ A) = new Ui(() ⇒ v)
  def sequence[A](vs: Ui[A]*) = Ui(vs.map(_.get))
}
