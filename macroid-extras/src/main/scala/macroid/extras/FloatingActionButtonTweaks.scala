package macroid.extras

import android.annotation._
import android.content.res._
import android.support.design.widget._
import macroid.{ContextWrapper, Tweak}
import macroid.extras.ResourcesExtras._

object FloatingActionButtonTweaks {
  type W = FloatingActionButton

  @TargetApi(23)
  def fbaColorResources(id: Int, rippleId: Int)(
      implicit contextWrapper: ContextWrapper) = Tweak[W] { view ⇒
    view.setBackgroundTintList(
      contextWrapper.application.getResources
        .getColorStateList(id, null))
    view.setRippleColor(resGetColor(rippleId))
  }

  @deprecated(message = "", since = "API 23")
  def fbaColorResource(id: Int, rippleId: Int)(
      implicit contextWrapper: ContextWrapper) = Tweak[W] { view ⇒
    view.setBackgroundTintList(
      contextWrapper.application.getResources.getColorStateList(id))
    view.setRippleColor(resGetColor(rippleId))
  }

  def fbaColor(color: Int, rippleColor: Int)(
      implicit contextWrapper: ContextWrapper) = Tweak[W] { view ⇒
    view.setBackgroundTintList(ColorStateList.valueOf(color))
    view.setRippleColor(rippleColor)
  }
}
