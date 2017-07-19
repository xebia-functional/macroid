package macroid.extras

import android.widget.ScrollView
import macroid.Tweak

object ScrollViewTweaks {
  type W = ScrollView

  val svRemoveVerticalScrollBar: Tweak[W] =
    Tweak[W](_.setVerticalScrollBarEnabled(false))

  val svRemoveHorizontalScrollBar: Tweak[W] =
    Tweak[W](_.setHorizontalScrollBarEnabled(false))

}
