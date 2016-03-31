package macroid.viewable

import android.support.v4.view.PagerAdapter
import android.view.{ ViewGroup, View }
import macroid.ContextWrapper

/** A `PagerAdapter` based on the `Viewable` typeclass */
class ViewablePagerAdapter[A, +W <: View](data: Seq[A])(implicit ctx: ContextWrapper, viewable: Viewable[A, W]) extends PagerAdapter {
  override def instantiateItem(container: ViewGroup, position: Int) = {
    val view = viewable.view(data(position)).get
    container.addView(view, 0)
    view
  }

  override def destroyItem(container: ViewGroup, position: Int, `object`: Any) = {
    container.removeView(`object`.asInstanceOf[View])
  }

  def getCount = data.length

  def isViewFromObject(view: View, `object`: Any) = view == `object`
}

class ViewableNamedPagerAdapter[A, +W <: View](m: Vector[(String, A)])(implicit ctx: ContextWrapper, viewable: Viewable[A, W]) extends ViewablePagerAdapter[A, W](m.map(_._2)) {
  override def getPageTitle(position: Int): CharSequence = m.map(_._1).apply(position)
}