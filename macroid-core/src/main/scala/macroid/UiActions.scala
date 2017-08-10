package macroid

import android.os.{Handler, Looper}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/** An ExecutionContext associated with the UI thread */
object UiThreadExecutionContext extends ExecutionContext {
  private lazy val uiHandler = new Handler(Looper.getMainLooper)

  def reportFailure(t: Throwable) = t.printStackTrace()
  def execute(runnable: Runnable) = uiHandler.post(runnable)
}

/** A UI action that can be sent to the UI thread for execution */
class Ui[+A](private[Ui] val v: () ⇒ A) {

  /** map combinator */
  def map[B](f: A ⇒ B) = Ui(f(v()))

  /** flatMap combinator */
  def flatMap[B](f: A ⇒ Ui[B]) = Ui(f(v()).v())

  /** Replace the resulting value with a new one */
  def withResult[B](result: B) = Ui { v(); result }

  /** Wait until this action is finished and replace the resulting value with a new one */
  def withResultAsync[B, C](result: B)(implicit evidence: A <:< Future[C], ec: ExecutionContext) =
    Ui {
      evidence(v()) map (_ ⇒ result)
    }

  /** Combine (sequence) with another UI action */
  def ~[B](next: ⇒ Ui[B]) = Ui { v(); next.v() }

  /** Wait until this action is finished and combine (sequence) it with another one */
  def ~~[B, C](next: ⇒ Ui[B])(implicit evidence: A <:< Future[C]) = Ui {
    evidence(v()) mapUi (_ ⇒ next)
  }

  /** Run the action on the UI thread */
  def run =
    if (Ui.uiThread == Thread.currentThread) {
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

  /** Run a UI action on the UI thread */
  def run[A](ui: Ui[A]) = ui.run

  /** Run several UI actions on the UI thread */
  def run[A](ui1: Ui[A], ui2: Ui[A], uis: Ui[A]*) =
    sequence(ui1 +: ui2 +: uis: _*).run

  /** Get the result of executing an UI action on the current (hopefully, UI!) thread */
  def get[A](ui: Ui[A]) = ui.get
}

/** Helpers to run UI actions as Future callbacks */
case class UiFuture[T](future: Future[T]) extends AnyVal {
  private def applyUi[A, B](f: Function[A, Ui[B]]): Function[A, B] =
    x ⇒ f(x).get
  private def partialApplyUi[A, B](f: PartialFunction[A, Ui[B]]) =
    f andThen (_.get)

  /** Same as map, but performed on the UI thread.
   *
   * If the future is already completed and the current thread is the UI thread,
   * the UI action will be applied in-place, rather than asynchronously.
   */
  def mapUi[S](f: Function[T, Ui[S]]) =
    if (future.isCompleted && Ui.uiThread == Thread.currentThread) {
      future.value.get.map(applyUi(f)) match {
        case Success(x) ⇒ Future.successful(x)
        case Failure(t) ⇒ Future.failed(t)
      }
    } else {
      future.map(applyUi(f))(UiThreadExecutionContext)
    }

  /** Same as flatMap, but performed on the UI thread
   *
   * If the future is already completed and the current thread is the UI thread,
   * the UI action will be applied in-place, rather than asynchronously.
   */
  def flatMapUi[S](f: Function[T, Ui[Future[S]]]) = {
    if (future.isCompleted && Ui.uiThread == Thread.currentThread) {
      future.value.get.map(applyUi(f)) match {
        case Success(x) ⇒ x
        case Failure(t) ⇒ Future.failed(t)
      }
    } else {
      future.flatMap(applyUi(f))(UiThreadExecutionContext)
    }
  }

  /** Same as foreach, but performed on the UI thread
   *
   * If the future is already completed and the current thread is the UI thread,
   * the UI action will be applied in-place, rather than asynchronously.
   */
  def foreachUi[U](f: Function[T, Ui[U]]) =
    if (future.isCompleted && Ui.uiThread == Thread.currentThread) {
      future.value.get.foreach(applyUi(f))
    } else {
      future.foreach(applyUi(f))(UiThreadExecutionContext)
    }

  /** Same as recover, but performed on the UI thread */
  def recoverUi[U >: T](pf: PartialFunction[Throwable, Ui[U]]) =
    future.recover(partialApplyUi(pf))(UiThreadExecutionContext)

  /** Same as onSuccess, but performed on the UI thread */
  def onSuccessUi[U >: T](pf: PartialFunction[T, Ui[U]]) =
    future.onSuccess(partialApplyUi(pf))(UiThreadExecutionContext)

  /** Same as onFailure, but performed on the UI thread */
  def onFailureUi[U](pf: PartialFunction[Throwable, Ui[U]]) =
    future.onFailure(partialApplyUi(pf))(UiThreadExecutionContext)

  /** Same as onComplete, but performed on the UI thread */
  def onCompleteUi[U](pf: PartialFunction[Try[T], Ui[U]]) =
    future.onComplete(partialApplyUi(pf))(UiThreadExecutionContext)
}
