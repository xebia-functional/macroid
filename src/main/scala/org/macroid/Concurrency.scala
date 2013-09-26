package org.macroid

import scala.concurrent.{ ExecutionContext, Promise, Future }
import scala.util.Try
import android.os.{ Looper, Handler }

trait Concurrency {
  lazy val uiHandler = new Handler(Looper.getMainLooper)
  lazy val uiThread = Looper.getMainLooper.getThread

  /** Run the supplied block of code on UI thread */
  @inline def runOnUiThread[A](f: ⇒ A): Future[A] = {
    val uiPromise = Promise[A]()
    if (uiThread == Thread.currentThread) {
      uiPromise.complete(Try(f))
    } else uiHandler.post(new Runnable {
      def run() { uiPromise.complete(Try(f)) }
    })
    uiPromise.future
  }

  /** Run supplied block of code on UI thread (shortcut for runOnUiThread) */
  @inline def Ui[A](f: ⇒ A): Future[A] = runOnUiThread(f)

  implicit class RichFuture[A](val value: Future[A]) {
    /** Same as onSuccess, but performed on UI thread */
    def onSuccessUi(f: PartialFunction[A, Any])(implicit c: ExecutionContext): Future[A] = {
      value onSuccess { case v ⇒ runOnUiThread(f.lift(v)) }
      value
    }
    /** Same as onFailure, but performed on UI thread */
    def onFailureUi(f: PartialFunction[Throwable, Any])(implicit c: ExecutionContext): Future[A] = {
      value onFailure { case v ⇒ runOnUiThread(f.lift(v)) }
      value
    }
  }
}

object Concurrency extends Concurrency
