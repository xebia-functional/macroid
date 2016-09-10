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
