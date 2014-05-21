package macroid.akkafragments

import akka.actor.Actor
import akka.event.Logging._
import android.util.Log

class AkkaAndroidLogger extends Actor {
  def receive = {
    case Error(cause, logSource, logClass, message) ⇒
      Log.e("AKKA", s"$message [$logSource]: $cause")

    case Warning(logSource, logClass, message) ⇒
      Log.w("AKKA", s"$message [$logSource]")

    case Info(logSource, logClass, message) ⇒
      Log.i("AKKA", s"$message [$logSource]")

    case Debug(logSource, logClass, message) ⇒
      Log.d("AKKA", s"$message [$logSource]")

    case InitializeLogger(_) ⇒
      Log.d("AKKA", "Logging started")
      sender ! LoggerInitialized
  }
}
