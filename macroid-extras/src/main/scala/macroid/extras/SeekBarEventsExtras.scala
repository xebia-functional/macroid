/*
 * Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package macroid.extras

import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import macroid.Ui

import scala.language.implicitConversions

object SeekBarEventsExtras {

  case class OnSeekBarChangeListenerHandler(
    onProgressChangedHandler: (SeekBar, Int, Boolean) ⇒ Ui[Option[View]] = (seekBar: SeekBar, progress: Int, fromUser: Boolean) ⇒ Ui(Some(seekBar)),
    onStopTrackingTouchHandler: (SeekBar) ⇒ Ui[Option[View]] = (seekBar: SeekBar) ⇒ Ui(Some(seekBar)),
    onStartTrackingTouchHandler: (SeekBar) ⇒ Ui[Option[View]] = (seekBar: SeekBar) ⇒ Ui(Some(seekBar))
  )

  implicit def onSeekBarChangeListener(listener: OnSeekBarChangeListenerHandler): OnSeekBarChangeListener = {
    new OnSeekBarChangeListener {
      override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean): Unit =
        listener.onProgressChangedHandler(seekBar, progress, fromUser).run

      override def onStopTrackingTouch(seekBar: SeekBar): Unit =
        listener.onStopTrackingTouchHandler(seekBar).run

      override def onStartTrackingTouch(seekBar: SeekBar): Unit =
        listener.onStartTrackingTouchHandler(seekBar).run

    }
  }

}
