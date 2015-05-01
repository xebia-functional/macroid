package macroid

import android.os.Looper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{ Failure, Success, Try }

/** A UI action that can be sent to the UI thread for execution */
class Ui[+A](private[Ui] val v: () ⇒ A) {
  import macroid.UiThreading._

  /** map combinator */
  def map[B](f: A ⇒ B) = Ui(f(v()))

  /** flatMap combinator */
  def flatMap[B](f: A ⇒ Ui[B]) = Ui(f(v()).v())

  /** Replace the resulting value with a new one */
  def withResult[B](result: B) = Ui { v(); result }

  /** Wait until this action is finished and replace the resulting value with a new one */
  def withResultAsync[B, C](result: B)(implicit evidence: A <:< Future[C], ec: ExecutionContext) = Ui {
    evidence(v()) map (_ ⇒ result)
  }

  /** Combine (sequence) with another UI action */
  def ~[B](next: ⇒ Ui[B]) = Ui { v(); next.v() }

  /** Wait until this action is finished and combine (sequence) it with another one */
  def ~~[B, C](next: ⇒ Ui[B])(implicit evidence: A <:< Future[C]) = Ui {
    evidence(v()) mapUi (_ ⇒ next)
  }

  /** Run the action on the UI thread */
  def run = if (Ui.uiThread == Thread.currentThread) {
    Try(v()) match {
      case Success(x) ⇒ Future.successful(x)
      case Failure(x) ⇒ Future.failed(x)
    }
  } else {
    Future(v())(UiThreadExecutionContext)
  }

  /** Get the result of executing the action on the current (hopefully, UI!) thread */
  def get = v()
}

object Ui {
  private[macroid] lazy val uiThread = Looper.getMainLooper.getThread

  /** A UI action that does nothing */
  def nop = Ui(())

  /** Create a UI action */
  def apply[A](v: ⇒ A) = new Ui(() ⇒ v)

  /** Combine (sequence) several UI actions together */
  def sequence[A](vs: Ui[A]*) = Ui(vs.map(_.v()))
}
