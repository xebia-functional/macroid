package org.macroid

import android.widget.Toast
import android.content.Context
import android.view.View

trait Toasts {
  type Bread = Toast ⇒ Unit

  implicit class RichToast(toast: Toast) {
    def ~>(bread: Bread) = { bread(toast); toast }
  }

  def toast(text: CharSequence)(implicit ctx: Context) = Toast.makeText(ctx, text, Toast.LENGTH_SHORT)

  def toast(view: View)(implicit ctx: Context) = new Toast(ctx) {
    setView(view)
    setDuration(Toast.LENGTH_SHORT)
  }

  val fry: Bread = toast ⇒ Concurrency.fireUi(toast.show())
  val long: Bread = _.setDuration(Toast.LENGTH_LONG)
  def gravity(g: Int, xOffset: Int = 0, yOffset: Int = 0): Bread = _.setGravity(g, xOffset, yOffset)
}

object Toasts extends Toasts