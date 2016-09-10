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

import android.graphics.drawable.Drawable
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.View.OnClickListener
import macroid.{ Tweak, Ui }

object ToolbarTweaks {
  type W = Toolbar

  def tbTitle(title: String): Tweak[W] = Tweak[W](_.setTitle(title))

  def tbTitle(title: Int): Tweak[W] = Tweak[W](_.setTitle(title))

  def tbTextColor(color: Int): Tweak[W] = Tweak[W](_.setTitleTextColor(color))

  def tbBackgroundColor(color: Int): Tweak[W] = Tweak[Toolbar](_.setBackgroundColor(color))

  def tbLogo(res: Int): Tweak[W] = Tweak[W](_.setLogo(res))

  def tbLogo(drawable: Drawable): Tweak[W] = Tweak[W](_.setLogo(drawable))

  def tbNavigationIcon(res: Int): Tweak[W] = Tweak[W](_.setNavigationIcon(res))

  def tbNavigationIcon(drawable: Drawable): Tweak[W] = Tweak[W](_.setNavigationIcon(drawable))

  def tbNavigationOnClickListener(click: (View) ⇒ Ui[_]): Tweak[W] = Tweak[W](_.setNavigationOnClickListener(new OnClickListener {
    override def onClick(v: View): Unit = click(v).run
  }))

  def tbChangeHeightLayout(height: Int): Tweak[W] = Tweak[W] { view ⇒
    view.getLayoutParams.height = height
    view.requestLayout()
  }

}
