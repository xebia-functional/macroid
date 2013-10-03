package org.macroid

import scala.concurrent.{ ExecutionContext, Promise, Future }
import scala.util.{ Failure, Try }
import android.os.{ Looper, Handler }
import java.util.NoSuchElementException

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
    /** Same as map, but performed on UI thread */
    def mapUi[S](f: Function[A, S])(implicit ec: ExecutionContext): Future[S] = {
      value flatMap (x ⇒ runOnUiThread(f(x)))
    }
    /** Same as foreach, but performed on Ui thread */
    def foreachUi[S](f: Function[A, S])(implicit ec: ExecutionContext) {
      value foreach (x ⇒ runOnUiThread(f(x)))
    }
    /** Same as recover, but performed on UI thread */
    def recoverUi[U >: A](pf: PartialFunction[Throwable, U])(implicit ec: ExecutionContext): Future[U] = {
      value recoverWith { case t if pf.isDefinedAt(t) ⇒ runOnUiThread(pf(t)) }
    }
    /** Same as onFailure, but performed on UI thread */
    def onFailureUi(pf: PartialFunction[Throwable, Any])(implicit ec: ExecutionContext) {
      value onFailure { case v ⇒ runOnUiThread(pf.lift(v)) }
    }
  }
}

object Concurrency extends Concurrency
