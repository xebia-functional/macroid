package macroid.contrib

import android.widget.{ GridLayout, LinearLayout, RelativeLayout }
import android.content.Context

object Layouts {
  /** A LinearLayout that is preset to be vertical */
  class VerticalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.VERTICAL)
  }

  /** A LinearLayout that is preset to be horizontal */
  class HorizontalLinearLayout(ctx: Context) extends LinearLayout(ctx) {
    setOrientation(LinearLayout.HORIZONTAL)
  }

  /** A RelativeLayout offering more flexible LayoutParams */
  class RuleRelativeLayout(ctx: Context) extends RelativeLayout(ctx)
  object RuleRelativeLayout {
    case class Rule(verb: Int, anchor: Int = -1)
    class LayoutParams(w: Int, h: Int, rules: Rule*) extends RelativeLayout.LayoutParams(w, h) {
      rules foreach {
        case Rule(verb, -1) ⇒ addRule(verb)
        case Rule(verb, anchor) ⇒ addRule(verb, anchor)
      }
    }
  }
}
