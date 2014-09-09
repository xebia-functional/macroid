package macroid

import android.util.Log

case class LogTag(tag: String)

trait AutoLogTag {
  implicit lazy val logTag = LogTag(getClass.getSimpleName.take(32))
}

case class LogBuilder(tag: String) {
  def v(message: String) = Log.v(tag, message)
  def v(message: String, throwable: Throwable) = Log.v(tag, message, throwable)
  def i(message: String) = Log.i(tag, message)
  def i(message: String, throwable: Throwable) = Log.i(tag, message, throwable)
  def d(message: String) = Log.d(tag, message)
  def d(message: String, throwable: Throwable) = Log.d(tag, message, throwable)
  def w(message: String) = Log.w(tag, message)
  def w(throwable: Throwable) = Log.w(tag, throwable)
  def w(message: String, throwable: Throwable) = Log.w(tag, message, throwable)
  def e(message: String) = Log.e(tag, message)
  def e(message: String, throwable: Throwable) = Log.e(tag, message, throwable)
  def wtf(message: String) = Log.wtf(tag, message)
  def wtf(throwable: Throwable) = Log.wtf(tag, throwable)
  def wtf(message: String, throwable: Throwable) = Log.wtf(tag, message, throwable)
}

private[macroid] trait Logging {
  def log(implicit tag: LogTag = LogTag("")) = LogBuilder(tag.tag)
  def log(tag: String) = LogBuilder(tag)
}

object Logging extends Logging
