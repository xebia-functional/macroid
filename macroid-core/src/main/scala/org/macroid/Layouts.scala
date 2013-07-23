package org.macroid

import android.widget.LinearLayout
import android.content.Context

trait Layouts {
  class VerticalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.VERTICAL)
  }
  class HorizontalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.HORIZONTAL)
  }
}
