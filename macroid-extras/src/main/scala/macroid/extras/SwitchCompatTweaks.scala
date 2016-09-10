/*
 *
 *   Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may
 *   not use this file except in compliance with the License. You may obtain
 *   a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

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
