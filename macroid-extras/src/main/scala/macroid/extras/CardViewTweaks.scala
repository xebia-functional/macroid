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

import android.support.v7.widget.CardView
import macroid.{ ContextWrapper, Tweak }
import macroid.extras.ResourcesExtras._

object CardViewTweaks {
  type W = CardView

  def cvRadius(radius: Float): Tweak[W] = Tweak[W](_.setRadius(radius))

  def cvElevations(elevation: Float): Tweak[W] = Tweak[W](_.setCardElevation(elevation))

  def cvMaxElevations(elevation: Float): Tweak[W] = Tweak[W](_.setMaxCardElevation(elevation))

  def cvPreventCornerOverlap(preventCornerOverlap: Boolean): Tweak[W] = Tweak[W](_.setPreventCornerOverlap(preventCornerOverlap))

  def cvCardBackgroundColor(color: Int): Tweak[W] = Tweak[W](_.setCardBackgroundColor(color))

  def cvCardBackgroundColorResource(resColor: Int)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.setCardBackgroundColor(resGetColor(resColor)))

  def cvPadding(left: Int, top: Int, right: Int, bottom: Int): Tweak[W] =
    Tweak[W](_.setPadding(left, top, right, bottom))

  def cvContentPadding(left: Int, top: Int, right: Int, bottom: Int): Tweak[W] =
    Tweak[W](_.setContentPadding(left, top, right, bottom))

  def cvPaddingRelative(start: Int, top: Int, right: Int, bottom: Int): Tweak[W] =
    Tweak[W](_.setPaddingRelative(start, top, right, bottom))

}
