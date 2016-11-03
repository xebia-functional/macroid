package macroid.extras

import android.view.View
import android.view.ViewGroup.LayoutParams._
import android.widget.TableRow
import macroid.Tweak

object TableRowTweaks {
  type W = TableRow

  def trLayoutGravity(gravity: Int): Tweak[View] = Tweak[View] { view ⇒
    val param = new TableRow.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    param.gravity = gravity
    view.setLayoutParams(param)
  }

  def trLayoutMargins(value: Int): Tweak[View] = Tweak[View] { view ⇒
    val param = new TableRow.LayoutParams(view.getLayoutParams)
    param.setMargins(value, value, value, value)
    view.setLayoutParams(param)
  }
}
