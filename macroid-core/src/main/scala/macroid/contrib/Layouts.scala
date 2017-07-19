package macroid.contrib

import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.{FrameLayout, LinearLayout, RelativeLayout}
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
    class LayoutParams(w: Int, h: Int, rules: Rule*)
        extends RelativeLayout.LayoutParams(w, h) {
      rules foreach {
        case Rule(verb, -1) ⇒ addRule(verb)
        case Rule(verb, anchor) ⇒ addRule(verb, anchor)
      }
    }
  }

  /** A FrameLayout that does not expose its children’s ids */
  class RootFrameLayout(ctx: Context) extends FrameLayout(ctx) {
    override def addView(child: View, index: Int, params: LayoutParams) = {
      child.getClass
        .getMethod("setIsRootNamespace", classOf[Boolean])
        .invoke(child, Boolean.box(true))
      super.addView(child, index, params)
    }
  }
}
