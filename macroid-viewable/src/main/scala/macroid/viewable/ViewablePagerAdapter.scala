package macroid.viewable

import android.support.v4.view.PagerAdapter
import android.view.{ ViewGroup, View }
import macroid.{ AppContext, ActivityContext }
import macroid.UiThreading._

class ViewablePagerAdapter[A, W <: View](data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext, val viewable: Viewable[A, W]) extends PagerAdapter {
  override def instantiateItem(container: ViewGroup, position: Int) = {
    val view = getUi(viewable.layout(data(position)))
    container.addView(view, 0)
    view
  }

  override def destroyItem(container: ViewGroup, position: Int, `object`: Any) = {
    container.removeView(`object`.asInstanceOf[View])
  }

  def getCount = data.length

  def isViewFromObject(view: View, `object`: Any) = view == `object`
}
