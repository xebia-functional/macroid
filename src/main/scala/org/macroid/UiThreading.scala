package org.macroid

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import android.os.Looper
import org.macroid.util.{ Ui, UiThreadExecutionContext }

private[macroid] trait UiThreading {
  private lazy val uiThread = Looper.getMainLooper.getThread

  /** An operation unfortunately missing in Future API */
  implicit class CompletedFuture(future: Future.type) {
    def completed[T](value: Try[T]) = value match {
      case Success(result) ⇒ Future.successful(result)
      case Failure(exception) ⇒ Future.failed(exception)
    }
  }

  /**
   * Run the supplied block of code on the UI thread.
   * If the calling thread is already the UI thread, the code is run in-place.
   */
  private[macroid] def runOnUiThread[A](f: ⇒ A): Future[A] = if (uiThread == Thread.currentThread) {
    Future.completed(Try(f))
  } else {
    Future(f)(UiThreadExecutionContext)
  }

  /** Run UI code on the UI thread */
  def runUi[A](ui: Ui[A]) = ui.run

  /** Get the result of executing UI code on the current (hopefully, UI!) tread */
  def getUi[A](ui: Ui[A]) = ui.get

  /** Helpers to run Future callbacks on the UI thread */
  implicit class UiFuture[T](future: Future[T]) {
    /** Same as map, but performed on UI thread */
    def mapUi[S](f: Function[T, S]) = future.map(f)(UiThreadExecutionContext)

    /** Same as flatMap, but performed on UI thread */
    def flatMapUi[S](f: Function[T, Future[S]]) = future.flatMap(f)(UiThreadExecutionContext)

    /** Same as foreach, but performed on UI thread */
    def foreachUi[U](f: Function[T, U]) = future.foreach(f)(UiThreadExecutionContext)

    /** Same as recover, but performed on UI thread */
    def recoverUi[U >: T](pf: PartialFunction[Throwable, U]) = future.recover(pf)(UiThreadExecutionContext)

    /** Same as onSuccess, but performed on UI thread */
    def onSuccessUi[U >: T](pf: PartialFunction[T, U]) = future.onSuccess(pf)(UiThreadExecutionContext)

    /** Same as onFailure, but performed on UI thread */
    def onFailureUi[U](pf: PartialFunction[Throwable, U]) = future.onFailure(pf)(UiThreadExecutionContext)

    /** Same as onComplete, but performed on UI thread */
    def onCompleteUi[U](pf: PartialFunction[Try[T], U]) = future.onComplete(pf)(UiThreadExecutionContext)
  }
}

object UiThreading extends UiThreading
