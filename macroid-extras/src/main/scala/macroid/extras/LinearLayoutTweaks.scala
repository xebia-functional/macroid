package macroid.extras

import android.view.View
import android.view.ViewGroup.LayoutParams._
import android.widget.LinearLayout
import macroid.FullDsl._
import macroid.{ ContextWrapper, Tweak }

object LinearLayoutTweaks {
  type W = LinearLayout

  val llHorizontal: Tweak[W] = Tweak[W](_.setOrientation(LinearLayout.HORIZONTAL))

  val llVertical: Tweak[W] = Tweak[W](_.setOrientation(LinearLayout.VERTICAL))

  val llMatchWeightVertical: Tweak[View] = lp[W](MATCH_PARENT, 0, 1)

  val llMatchWeightHorizontal: Tweak[View] = lp[W](0, MATCH_PARENT, 1)

  val llWrapWeightVertical: Tweak[View] = lp[W](WRAP_CONTENT, 0, 1)

  val llWrapWeightHorizontal: Tweak[View] = lp[W](0, WRAP_CONTENT, 1)

  def llGravity(gravity: Int): Tweak[W] = Tweak[W](_.setGravity(gravity))

  def llDividerPadding(res: Int, padding: Int)(implicit context: ContextWrapper): Tweak[W] = Tweak[W] { view ⇒
    view.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE)
    view.setDividerDrawable(context.application.getResources.getDrawable(res))
    view.setDividerPadding(padding)
  }

  def llLayoutGravity(gravity: Int): Tweak[View] = Tweak[View] { view ⇒
    val param = new LinearLayout.LayoutParams(view.getLayoutParams)
    param.gravity = gravity
    view.setLayoutParams(param)
  }

  def llLayoutMargin(
    marginLeft: Int = 0,
    marginTop: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0
  ): Tweak[View] = Tweak[View] { view ⇒
    val params = new LinearLayout.LayoutParams(view.getLayoutParams)
    params.setMargins(marginLeft, marginTop, marginRight, marginBottom)
    view.setLayoutParams(params)
  }
}
