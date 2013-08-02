package org.macroid

import android.widget.{ GridLayout, LinearLayout }
import android.content.Context

object Layouts {
  class VerticalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.VERTICAL)
  }

  class HorizontalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.HORIZONTAL)
  }

  class GravityGridLayout(ctx: Context) extends GridLayout(ctx)
  object GravityGridLayout {
    class LayoutParams(gravity: Int) extends GridLayout.LayoutParams {
      setGravity(gravity)
    }
  }
}
