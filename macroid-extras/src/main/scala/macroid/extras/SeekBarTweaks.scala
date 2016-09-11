package macroid.extras

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import macroid.Tweak

object SeekBarTweaks {
  type W = SeekBar

  def sbMax(maxValue: Int): Tweak[W] = Tweak[W](_.setMax(maxValue))

  def sbProgress(progressValue: Int): Tweak[W] = Tweak[W](_.setProgress(progressValue))

  def sbOnSeekBarChangeListener(listener: OnSeekBarChangeListener): Tweak[W] = Tweak[W](_.setOnSeekBarChangeListener(listener))
}
