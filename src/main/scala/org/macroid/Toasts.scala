package org.macroid

import android.widget.Toast
import android.content.Context
import android.view.View
import scala.concurrent.{ ExecutionContext, Future }

private[macroid] trait Toasts {
  type Bread = Toast ⇒ Unit

  implicit class RichToast(toast: Toast) {
    def ~>(bread: Bread) = { UiThreading.fireUi(bread(toast)); toast }
  }

  implicit class RichFutureToast(toast: Future[Toast]) {
    def ~>(bread: Bread)(implicit ctx: ExecutionContext) = toast.map(t ⇒ t ~> bread)
  }

  def toast(text: CharSequence)(implicit ctx: AppContext) =
    UiThreading.runOnUiThread(Toast.makeText(ctx.get, text, Toast.LENGTH_SHORT))

  def toast(view: View)(implicit ctx: AppContext) = new Toast(ctx.get) {
    setView(view)
    setDuration(Toast.LENGTH_SHORT)
  }

  val fry: Bread = _.show()
  val long: Bread = _.setDuration(Toast.LENGTH_LONG)
  def gravity(g: Int, xOffset: Int = 0, yOffset: Int = 0): Bread = _.setGravity(g, xOffset, yOffset)
}

object Toasts extends Toasts