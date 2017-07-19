package macroid.extras

import android.graphics.PorterDuff
import android.widget.ProgressBar
import macroid.Tweak

object ProgressBarTweaks {
  type W = ProgressBar

  def pbColor(color: Int): Tweak[W] =
    Tweak[W](
      _.getIndeterminateDrawable.setColorFilter(color,
                                                PorterDuff.Mode.MULTIPLY))

}
