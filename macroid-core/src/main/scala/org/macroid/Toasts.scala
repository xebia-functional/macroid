package org.macroid

import android.widget.Toast
import android.content.Context
import android.view.View
import scala.concurrent.{ ExecutionContext, Future }

trait Toasts {
  type Bread = Toast ⇒ Unit

  implicit class RichToast(toast: Toast) {
    def ~>(bread: Bread) = { Concurrency.fireUi(bread(toast)); toast }
  }

  implicit class RichFutureToast(toast: Future[Toast]) {
    def ~>(bread: Bread)(implicit ctx: ExecutionContext) = toast.map(t ⇒ t ~> bread)
  }

  def toast(text: CharSequence)(implicit ctx: Context) =
    Concurrency.runOnUiThread(Toast.makeText(ctx, text, Toast.LENGTH_SHORT))

  def toast(view: View)(implicit ctx: Context) = new Toast(ctx) {
    setView(view)
    setDuration(Toast.LENGTH_SHORT)
  }

  val fry: Bread = _.show()
  val long: Bread = _.setDuration(Toast.LENGTH_LONG)
  def gravity(g: Int, xOffset: Int = 0, yOffset: Int = 0): Bread = _.setGravity(g, xOffset, yOffset)
}

object Toasts extends Toasts