package org.macroid.util

import android.os.{ Looper, Handler }
import scala.concurrent.ExecutionContext

object UiThreadExecutionContext extends ExecutionContext {
  private lazy val uiHandler = new Handler(Looper.getMainLooper)

  def reportFailure(t: Throwable) = t.printStackTrace()
  def execute(runnable: Runnable) = uiHandler.post(runnable)
}
