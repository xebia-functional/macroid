package macroid.extras

import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v4.view.{ PagerAdapter, ViewPager }
import macroid.Tweak

object ViewPagerTweaks {
  type W = ViewPager

  def vpAdapter(adapter: PagerAdapter): Tweak[W] = Tweak[W](_.setAdapter(adapter))

  def vpOnPageChangeListener(listener: OnPageChangeListener): Tweak[W] = Tweak[W](_.setOnPageChangeListener(listener))

  def vpCurrentItem(currentItem: Int): Tweak[W] = Tweak[W](_.setCurrentItem(currentItem))

  def vpCurrentItem(currentItem: Int, smoothScroll: Boolean): Tweak[W] =
    Tweak[W](_.setCurrentItem(currentItem, smoothScroll))

  def vpPageTransformer(reverseDrawingOrder: Boolean, transformer: ViewPager.PageTransformer): Tweak[W] =
    Tweak[W](_.setPageTransformer(reverseDrawingOrder, transformer))

  def vpOffscreenPageLimit(limit: Int): Tweak[W] = Tweak[W](_.setOffscreenPageLimit(limit))

  def vpPageMargin(marginPixels: Int): Tweak[W] = Tweak[W](_.setPageMargin(marginPixels))

  def vpPageMarginDrawable(resId: Int): Tweak[W] = Tweak[W](_.setPageMarginDrawable(resId))

}
