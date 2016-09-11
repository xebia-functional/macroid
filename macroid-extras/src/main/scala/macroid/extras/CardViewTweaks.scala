package macroid.extras

import android.support.v7.widget.CardView
import macroid.{ ContextWrapper, Tweak }
import macroid.extras.ResourcesExtras._

object CardViewTweaks {
  type W = CardView

  def cvRadius(radius: Float): Tweak[W] = Tweak[W](_.setRadius(radius))

  def cvElevations(elevation: Float): Tweak[W] = Tweak[W](_.setCardElevation(elevation))

  def cvMaxElevations(elevation: Float): Tweak[W] = Tweak[W](_.setMaxCardElevation(elevation))

  def cvPreventCornerOverlap(preventCornerOverlap: Boolean): Tweak[W] = Tweak[W](_.setPreventCornerOverlap(preventCornerOverlap))

  def cvCardBackgroundColor(color: Int): Tweak[W] = Tweak[W](_.setCardBackgroundColor(color))

  def cvCardBackgroundColorResource(resColor: Int)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.setCardBackgroundColor(resGetColor(resColor)))

  def cvPadding(left: Int, top: Int, right: Int, bottom: Int): Tweak[W] =
    Tweak[W](_.setPadding(left, top, right, bottom))

  def cvContentPadding(left: Int, top: Int, right: Int, bottom: Int): Tweak[W] =
    Tweak[W](_.setContentPadding(left, top, right, bottom))

  def cvPaddingRelative(start: Int, top: Int, right: Int, bottom: Int): Tweak[W] =
    Tweak[W](_.setPaddingRelative(start, top, right, bottom))

}
