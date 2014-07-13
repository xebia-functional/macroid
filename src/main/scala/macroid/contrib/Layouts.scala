package macroid.contrib

import android.widget.{ GridLayout, LinearLayout, RelativeLayout }
import android.content.Context

object Layouts {
  class VerticalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.VERTICAL)
  }

  class HorizontalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.HORIZONTAL)
  }
}

/** Smart Relative Layout params */
class SmartRelativeLayout(ctx: Context) extends RelativeLayout(ctx)
object SmartRelativeLayout {
  // Android uses -1 to indicate no anchor so we are using it
  // instead of Option to make writing rules cleaner
  case class Rule(verb: Int, anchor: Int = -1)
  class LayoutParams(w: Int, h: Int, rules: Rule*) extends RelativeLayout.LayoutParams(w, h) {
    rules foreach {
      case Rule(verb, -1) ⇒ addRule(verb)
      case Rule(verb, anchor) ⇒ addRule(verb, anchor)
    }
  }
}
