package macroid.extras

import android.view.View
import android.widget.TableLayout
import macroid.Tweak

object TableLayoutTweaks {
  type W = TableLayout

  def tlLayoutMargins(value: Int): Tweak[View] = Tweak[View] { view ⇒
    val param = new TableLayout.LayoutParams(view.getLayoutParams)
    param.setMargins(value, value, value, value)
    view.setLayoutParams(param)
  }

  def tlStretchAllColumns(stretchAllColumns: Boolean): Tweak[W] = Tweak[W](_.setStretchAllColumns(stretchAllColumns))
}
