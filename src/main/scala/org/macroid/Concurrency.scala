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

  /** Run supplied block of code on UI thread without tracking its progress */
  @inline def fireForget[A](f: ⇒ A) {
    if (uiThread == Thread.currentThread) {
      Try(f)
    } else uiHandler.post(new Runnable {
      def run() { Try(f) }
    })
  }

  implicit class UiFuture[A](val value: Future[A]) {
    /** Same as onSuccess, but performed on UI thread */
    def onSuccessUi(pf: PartialFunction[A, Any])(implicit c: ExecutionContext): Future[A] = {
      value onSuccess { case v ⇒ runOnUiThread(pf.lift(v)) }
      value
    }
    /** Same as onFailure, but performed on UI thread */
    def onFailureUi(pf: PartialFunction[Throwable, Any])(implicit c: ExecutionContext): Future[A] = {
      value onFailure { case v ⇒ runOnUiThread(pf.lift(v)) }
      value
    }
    /** Same as recover, but performed on UI thread */
    def recoverUi[U >: A](pf: PartialFunction[Throwable, U])(implicit c: ExecutionContext): Future[U] = {
      val uiPromise = Promise[U]()
      value recover {
        case t if pf.isDefinedAt(t) ⇒ uiPromise.completeWith(runOnUiThread(pf(t)))
        case _ ⇒ uiPromise.completeWith(value)
      }
      uiPromise.future
    }
  }
}

object Concurrency extends Concurrency
