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

import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.view.ViewGroup.LayoutParams._
import macroid.FullDsl._
import macroid.Tweak

object DrawerLayoutTweaks {
  type W = DrawerLayout

  def dlContentSize(w: Int, h: Int): Tweak[View] = lp[W](w, h)

  val dlMatchWeightVertical: Tweak[View] = lp[W](MATCH_PARENT, 0, 1)
  val dlMatchWeightHorizontal: Tweak[View] = lp[W](0, MATCH_PARENT, 1)

  def dlLayoutGravity(gravity: Int): Tweak[View] = Tweak[View] { view ⇒
    val param = new DrawerLayout.LayoutParams(view.getLayoutParams.width, view.getLayoutParams.height)
    param.gravity = gravity
    view.setLayoutParams(param)
  }

  def dlCloseDrawer(drawerMenuView: Option[View]): Tweak[W] = Tweak[W] { view ⇒
    drawerMenuView foreach view.closeDrawer
  }

  def dlStatusBarBackground(res: Int): Tweak[W] = Tweak[W](_.setStatusBarBackground(res))

  def dlOpenDrawer: Tweak[W] = Tweak[W](_.openDrawer(GravityCompat.START))

  def dlCloseDrawer: Tweak[W] = Tweak[W](_.closeDrawer(GravityCompat.START))

}
