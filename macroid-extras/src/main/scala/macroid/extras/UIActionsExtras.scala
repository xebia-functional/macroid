package macroid.extras

import android.app.Activity
import android.content.{ Context, Intent }
import android.os.{ Bundle, Handler, Parcelable, Vibrator }
import android.widget.Toast
import macroid.{ ActivityContextWrapper, ContextWrapper, Ui }

object UIActionsExtras {

  def uiShortToast(msg: Int)(implicit c: ContextWrapper): Ui[Unit] =
    Ui(Toast.makeText(c.application, msg, Toast.LENGTH_SHORT).show())

  def uiLongToast(msg: Int)(implicit c: ContextWrapper): Ui[Unit] =
    Ui(Toast.makeText(c.application, msg, Toast.LENGTH_LONG).show())

  def uiShortToast(msg: String)(implicit c: ContextWrapper): Ui[Unit] =
    Ui(Toast.makeText(c.application, msg, Toast.LENGTH_SHORT).show())

  def uiLongToast(msg: String)(implicit c: ContextWrapper): Ui[Unit] =
    Ui(Toast.makeText(c.application, msg, Toast.LENGTH_LONG).show())

  def uiHandler(f: ⇒ Ui[_]): Ui[Unit] =
    Ui {
      new Handler().post(new Runnable {
        override def run(): Unit = f.run
      })
    }

  def uiHandlerDelayed(f: ⇒ Ui[_], delayMilis: Long): Ui[Unit] =
    Ui {
      new Handler().postDelayed(new Runnable {
        override def run(): Unit = f.run
      }, delayMilis)
    }

  def uiVibrate(millis: Long = 100)(implicit contextWrapper: ContextWrapper): Ui[Any] = Ui {
    contextWrapper.application.getSystemService(Context.VIBRATOR_SERVICE) match {
      case vibrator: Vibrator ⇒ vibrator.vibrate(millis)
      case _ ⇒
    }
  }

  private implicit class IntentVisitor(intent: Intent) {
    def withExtra(key: String, value: Any): Unit = {
      value match {
        case v: Short ⇒ intent.putExtra(key, v)
        case v: Int ⇒ intent.putExtra(key, v)
        case v: Byte ⇒ intent.putExtra(key, v)
        case v: Double ⇒ intent.putExtra(key, v)
        case v: Float ⇒ intent.putExtra(key, v)
        case v: Long ⇒ intent.putExtra(key, v)
        case v: String ⇒ intent.putExtra(key, v)
        case v: Boolean ⇒ intent.putExtra(key, v)
        case v: Char ⇒ intent.putExtra(key, v)

        // this will only fail when item type is not primitive or Serializable or Parcelable
        case v: Array[_] ⇒ intent.putExtra(key, v)

        case v: CharSequence ⇒ intent.putExtra(key, v)
        case v: Serializable ⇒ intent.putExtra(key, v)
        case v: Bundle ⇒ intent.putExtra(key, v)
        case v: Parcelable ⇒ intent.putExtra(key, v)
        // Vector and List works fine, because they are Serializable.
        case v: Seq[_] ⇒ throw new IllegalArgumentException(s"class: ${v.getClass} is not supported, could you use Array instead")
        case v ⇒ throw new IllegalArgumentException(s"class: ${v.getClass} is not supported")
      }
    }
  }

  private def intentFor(cls: Class[_])(implicit context: ContextWrapper): Intent = {
    new Intent(context.getOriginal, cls)
  }

  implicit class activityClassMethod(cls: Class[_ <: Activity]) {

    def withExtras(extras: (String, Any)*)(implicit context: ContextWrapper) = {
      intentFor(cls).withExtras(extras: _*)
    }

    def withFlag(flag: Int)(implicit context: ContextWrapper) = {
      intentFor(cls).addFlags(flag)
    }

    def startActivity()(implicit context: ContextWrapper): Ui[Unit] = {
      intentFor(cls).startActivity()
    }

    def startActivityForResult(requestCode: Int, bundle: Bundle = null)(implicit context: ActivityContextWrapper): Ui[Unit] = {
      intentFor(cls).startActivityForResult(requestCode, bundle)
    }
  }

  implicit class intentMethod(intent: Intent) {
    def withExtras(extras: (String, Any)*): Intent = {
      extras.foreach({
        case (key, value) ⇒ intent.withExtra(key, value)
      })
      intent
    }

    def startActivity()(implicit context: ContextWrapper): Ui[Unit] = {
      Ui(context.getOriginal.startActivity(intent))
    }

    def startActivityForResult(requestCode: Int, bundle: Bundle = null)(implicit context: ActivityContextWrapper): Ui[Unit] = {
      val currentActivity = context.getOriginal
      Ui(currentActivity.startActivityForResult(intent, requestCode, bundle))
    }
  }

}

object ActionsExtras {

  def aShortToast(msg: Int)(implicit c: ContextWrapper): Unit =
    Toast.makeText(c.application, msg, Toast.LENGTH_SHORT).show()

  def aLongToast(msg: Int)(implicit c: ContextWrapper): Unit =
    Toast.makeText(c.application, msg, Toast.LENGTH_LONG).show()

  def aShortToast(msg: String)(implicit c: ContextWrapper): Unit =
    Toast.makeText(c.application, msg, Toast.LENGTH_SHORT).show()

  def aLongToast(msg: String)(implicit c: ContextWrapper): Unit =
    Toast.makeText(c.application, msg, Toast.LENGTH_LONG).show()

}

