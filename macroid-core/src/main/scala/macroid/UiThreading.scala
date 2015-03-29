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

  /** Helpers to perform Future callbacks in-place if the future is already completed */
  implicit class InPlaceFuture[T](future: Future[T]) {
    /** Same as map, but performed on the current thread (if completed) */
    def mapInPlace[S](f: Function[T, S])(implicit ec: ExecutionContext) = if (future.isCompleted) {
      future.value.get match {
        case Success(x) ⇒ Future.successful(f(x))
        case Failure(t) ⇒ Future.failed(t)
      }
    } else {
      future.map(f)
    }

    /** Same as flatMap, but performed on the current thread (if completed) */
    def flatMapInPlace[S](f: Function[T, Future[S]])(implicit ec: ExecutionContext) = if (future.isCompleted) {
      future.value.get match {
        case Success(x) ⇒ f(x)
        case Failure(t) ⇒ Future.failed(t)
      }
    } else {
      future.flatMap(f)
    }

    /** Same as foreach, but performed on the current thread (if completed) */
    def foreachInPlace[U](f: Function[T, U])(implicit ec: ExecutionContext) = if (future.isCompleted) {
      future.value.get.foreach(f)
    } else {
      future.foreach(f)
    }
  }

  /** Helpers to run UI actions as Future callbacks */
  implicit class UiFuture[T](future: Future[T]) {
    private def applyUi[A, B](f: Function[A, Ui[B]]) = f andThen (_.get)
    private def partialApplyUi[A, B](f: PartialFunction[A, Ui[B]]) = f andThen (_.get)

    /** Same as map, but performed on the UI thread */
    def mapUi[S](f: Function[T, Ui[S]]) =
      future.map(applyUi(f))(UiThreadExecutionContext)

    /** Same as flatMap, but performed on the UI thread */
    def flatMapUi[S](f: Function[T, Ui[Future[S]]]) =
      future.flatMap(applyUi(f))(UiThreadExecutionContext)

    /** Same as foreach, but performed on the UI thread */
    def foreachUi[U](f: Function[T, Ui[U]]) =
      future.foreach(applyUi(f))(UiThreadExecutionContext)

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
