package org.macroid

import android.widget.Toast
import android.view.View
import scala.concurrent.{ ExecutionContext, Future }

case class Loaf(f: Toast ⇒ Unit) {
  def apply(t: Toast) = f(t)
}

private[macroid] trait Loafs {
  val long = Loaf(_.setDuration(Toast.LENGTH_LONG))
  def gravity(g: Int, xOffset: Int = 0, yOffset: Int = 0) = Loaf(_.setGravity(g, xOffset, yOffset))
  val fry = Loaf(_.show())
}

private[macroid] trait ToastBuilding {
  def toast(text: CharSequence)(implicit ctx: AppContext) = UiThreading.runOnUiThread {
    Toast.makeText(ctx.get, text, Toast.LENGTH_SHORT)
  }

  def toast(view: ⇒ View)(implicit ctx: AppContext) = UiThreading.runOnUiThread {
    new Toast(ctx.get) { setView(view); setDuration(Toast.LENGTH_SHORT) }
  }
}

private[macroid] trait Toasts extends ToastBuilding with Loafs {
  import UiThreading._

  implicit class ToastOps(toast: Future[Toast])(implicit ec: ExecutionContext) {
    def ~>(loaf: Loaf) = toast mapUi { t ⇒ loaf(t); t }
  }
}

object Toasts extends Toasts
