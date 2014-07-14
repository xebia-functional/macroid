package macroid

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }
import macroid.util.{ Ui, UiThreadExecutionContext }

private[macroid] trait UiThreading {
  /** Run UI code on the UI thread */
  def runUi[A](ui: Ui[A]) = ui.run

  /** Run UI code on the UI thread */
  def runUi[A](ui1: Ui[A], ui2: Ui[A], uis: Ui[A]*) = Ui.sequence(ui1 +: ui2 +: uis: _*).run

  /** Get the result of executing UI code on the current (hopefully, UI!) tread */
  def getUi[A](ui: Ui[A]) = ui.get

  /** Helpers to perform Future callbacks in-place if it is completed */
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

  /** Helpers to run Future callbacks on the UI thread */
  implicit class UiFuture[T](future: Future[T]) {
    /** Same as map, but performed on the UI thread */
    def mapUi[S](f: Function[T, S]) = future.map(f)(UiThreadExecutionContext)

    /** Same as flatMap, but performed on the UI thread */
    def flatMapUi[S](f: Function[T, Future[S]]) = future.flatMap(f)(UiThreadExecutionContext)

    /** Same as foreach, but performed on the UI thread */
    def foreachUi[U](f: Function[T, U]) = future.foreach(f)(UiThreadExecutionContext)

    /** Same as recover, but performed on the UI thread */
    def recoverUi[U >: T](pf: PartialFunction[Throwable, U]) = future.recover(pf)(UiThreadExecutionContext)

    /** Same as onSuccess, but performed on the UI thread */
    def onSuccessUi[U >: T](pf: PartialFunction[T, U]) = future.onSuccess(pf)(UiThreadExecutionContext)

    /** Same as onFailure, but performed on the UI thread */
    def onFailureUi[U](pf: PartialFunction[Throwable, U]) = future.onFailure(pf)(UiThreadExecutionContext)

    /** Same as onComplete, but performed on the UI thread */
    def onCompleteUi[U](pf: PartialFunction[Try[T], U]) = future.onComplete(pf)(UiThreadExecutionContext)
  }
}

object UiThreading extends UiThreading
