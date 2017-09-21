package macroid.extras

import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import macroid.Ui

import scala.language.implicitConversions

object SeekBarEventsExtras {

  case class OnSeekBarChangeListenerHandler(
      onProgressChangedHandler: (SeekBar, Int, Boolean) ⇒ Ui[Option[View]] =
        (seekBar: SeekBar, progress: Int, fromUser: Boolean) ⇒ Ui(Some(seekBar)),
      onStopTrackingTouchHandler: (SeekBar) ⇒ Ui[Option[View]] = (seekBar: SeekBar) ⇒
        Ui(Some(seekBar)),
      onStartTrackingTouchHandler: (SeekBar) ⇒ Ui[Option[View]] = (seekBar: SeekBar) ⇒
        Ui(Some(seekBar))
  )

  implicit def onSeekBarChangeListener(
      listener: OnSeekBarChangeListenerHandler): OnSeekBarChangeListener = {
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
