package macroid.extras

import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import macroid.{ ContextWrapper, Tweak }
import macroid.extras.ResourcesExtras._

object FloatingActionButtonTweaks {
  type W = FloatingActionButton

  def fbaColorResource(id: Int, rippleId: Int)(implicit contextWrapper: ContextWrapper) = Tweak[W] { view ⇒
    view.setBackgroundTintList(contextWrapper.application.getResources.getColorStateList(id))
    view.setRippleColor(resGetColor(rippleId))
  }

  def fbaColor(color: Int, rippleColor: Int)(implicit contextWrapper: ContextWrapper) = Tweak[W] { view ⇒
    view.setBackgroundTintList(ColorStateList.valueOf(color))
    view.setRippleColor(rippleColor)
  }
}
