package macroid.contrib

import android.widget.{ GridLayout, LinearLayout }
import android.content.Context

object Layouts {
  class VerticalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.VERTICAL)
  }

  class HorizontalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.HORIZONTAL)
  }
}
