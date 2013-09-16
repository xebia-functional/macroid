package org.macroid

import scala.concurrent.{ ExecutionContext, Promise, Future }
import scala.util.Try
import scala.util.continuations._
import android.os.{ Looper, Handler }
import akka.dataflow.DataflowFuture

trait Concurrency {
  lazy val handler = new Handler(Looper.getMainLooper)
  lazy val uiThread = Looper.getMainLooper.getThread

  /** Run the supplied block of code on UI thread */
  @inline def runOnUiThread[A](f: ⇒ A): Future[A] = {
    val uiPromise = Promise[A]()
    if (uiThread == Thread.currentThread) {
      uiPromise.complete(Try(f))
    } else handler.post(new Runnable {
      def run() { uiPromise.complete(Try(f)) }
    })
    uiPromise.future
  }

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

  /** Use inside `flow` blocks to await futures */
  @inline def await[A](f: Future[A])(implicit c: ExecutionContext) = f.apply()

  /** Perform the rest of the code in current `flow` block on UI thread */
  @inline def switchToUiThread(): Unit @cps[Future[Any]] = shift { f: (Unit ⇒ Future[Any]) ⇒
    runOnUiThread(f())
  }
}

object Concurrency extends Concurrency
