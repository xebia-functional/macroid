package macroid

import android.util.Log

case class LogTag(tag: String)

private[macroid] trait Logging {
  implicit class LoggingStringContext(sc: StringContext) {
    def logV(args: Any*) = new LogWriter(sc.s(args: _*), Log.v)
    def logI(args: Any*) = new LogWriter(sc.s(args: _*), Log.i)
    def logD(args: Any*) = new LogWriter(sc.s(args: _*), Log.d)
    def logW(args: Any*) = new LogWriter(sc.s(args: _*), Log.w)
    def logE(args: Any*) = new LogWriter(sc.s(args: _*), Log.e)
    def logWtf(args: Any*) = new LogWriter(sc.s(args: _*), Log.wtf)
  }

  class LogWriter(msg: String, f: (String, String) ⇒ Int) {
    def apply(tag: String) = f(tag, msg)
    def apply()(implicit tag: LogTag = LogTag("")) = f(tag.tag, msg)
  }
}

object Logging extends Logging
