package macroid.extras

import android.support.v7.widget.SwitchCompat
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import macroid.{ ContextWrapper, Tweak }

object SwitchCompatTweaks {
  type W = SwitchCompat

  def scChecked(checked: Boolean)(implicit contextWrapper: ContextWrapper) = Tweak[W](_.setChecked(checked))

  def scCheckedChangeListener(onCheckedChange: (Boolean) ⇒ Unit)(implicit contextWrapper: ContextWrapper) =
    Tweak[W](
      _.setOnCheckedChangeListener(new OnCheckedChangeListener {
        override def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean): Unit = onCheckedChange(isChecked)
      })
    )

}
