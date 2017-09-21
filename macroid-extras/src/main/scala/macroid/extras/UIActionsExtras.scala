package macroid.extras

import android.content.Context
import android.os.{Handler, Vibrator}
import android.widget.Toast
import macroid.{ContextWrapper, Ui}

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

  def uiVibrate(millis: Long = 100)(
      implicit contextWrapper: ContextWrapper): Ui[Any] = Ui {
    contextWrapper.application
      .getSystemService(Context.VIBRATOR_SERVICE) match {
      case vibrator: Vibrator ⇒ vibrator.vibrate(millis)
      case _ ⇒
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
