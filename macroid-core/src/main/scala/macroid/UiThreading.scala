package macroid

import android.os.{ Looper, Handler }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/** An ExecutionContext associated with the UI thread */
object UiThreadExecutionContext extends ExecutionContext {
  private lazy val uiHandler = new Handler(Looper.getMainLooper)

  def reportFailure(t: Throwable) = t.printStackTrace()
  def execute(runnable: Runnable) = uiHandler.post(runnable)
}

private[macroid] trait UiThreading {
  /** Run UI code on the UI thread */
  def runUi[A](ui: Ui[A]) = ui.run

  /** Run UI code on the UI thread */
  def runUi[A](ui1: Ui[A], ui2: Ui[A], uis: Ui[A]*) = Ui.sequence(ui1 +: ui2 +: uis: _*).run

  /** Get the result of executing UI code on the current (hopefully, UI!) tread */
  def getUi[A](ui: Ui[A]) = ui.get

  /** Helpers to run UI actions as Future callbacks */
  implicit class UiFuture[T](future: Future[T]) {
    private def applyUi[A, B](f: Function[A, Ui[B]]): Function[A, B] = x ⇒ f(x).get
    private def partialApplyUi[A, B](f: PartialFunction[A, Ui[B]]) = f andThen (_.get)

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
}

object UiThreading extends UiThreading
