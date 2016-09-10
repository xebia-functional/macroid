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
import android.view.View
import android.view.ViewGroup.LayoutParams._
import android.widget.FrameLayout
import macroid.FullDsl._
import macroid.Tweak

object FrameLayoutTweaks {
  type W = FrameLayout

  def flContentSize(w: Int, h: Int) = lp[W](w, h)

  val flMatchWeightVertical: Tweak[View] = lp[W](MATCH_PARENT, 0, 1)

  val flMatchWeightHorizontal: Tweak[View] = lp[W](0, MATCH_PARENT, 1)

  def flLayoutGravity(gravity: Int): Tweak[View] = Tweak[View] { view ⇒
    val param = new FrameLayout.LayoutParams(view.getLayoutParams)
    param.gravity = gravity
    view.setLayoutParams(param)
  }

  def flForeground(drawable: Drawable): Tweak[W] = Tweak[W](_.setForeground(drawable))

  def flForegroundGravity(foregroundGravity: Int): Tweak[W] = Tweak[W](_.setForegroundGravity(foregroundGravity))

  def flLayoutMargin(
    marginLeft: Int = 0,
    marginTop: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0
  ): Tweak[View] = Tweak[View] { view ⇒
    val params = new FrameLayout.LayoutParams(view.getLayoutParams)
    params.setMargins(marginLeft, marginTop, marginRight, marginBottom)
    view.setLayoutParams(params)
  }
}
