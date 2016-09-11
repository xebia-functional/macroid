package macroid.extras

import android.graphics.PorterDuff
import android.view.View
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.{AdapterView, Spinner, SpinnerAdapter}
import macroid.Tweak

object SpinnerTweaks {
  type W = Spinner

  def sAdapter(adapter: SpinnerAdapter) = Tweak[W](_.setAdapter(adapter))

  def sSelection(position: Int) = Tweak[W](_.setSelection(position))

  def sItemSelectedListener(onItem: (Int => Unit)) = Tweak[W](_.setOnItemSelectedListener(new OnItemSelectedListener {
    override def onNothingSelected(parent: AdapterView[_]): Unit = {}

    override def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = onItem(position)
  }))

  def sChangeDropdownColor(color: Int) = Tweak[W](_.getBackground.setColorFilter(color, PorterDuff.Mode.SRC_ATOP))


}
