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

import android.graphics.PorterDuff.Mode
import android.graphics.drawable.Drawable
import android.graphics.{ Bitmap, PorterDuffColorFilter }
import android.net.Uri
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import macroid.{ ContextWrapper, Tweak }
import macroid.extras.ResourcesExtras._

object ImageViewTweaks {
  type W = ImageView

  def ivSrc(drawable: Drawable): Tweak[W] = Tweak[W](_.setImageDrawable(drawable))

  def ivSrc(res: Int): Tweak[W] = Tweak[W](_.setImageResource(res))

  def ivSrc(bitmap: Bitmap): Tweak[W] = Tweak[W](_.setImageBitmap(bitmap))

  def ivSrc(uri: Uri): Tweak[W] = Tweak[W](_.setImageURI(uri))

  def ivScaleType(scaleType: ScaleType): Tweak[W] = Tweak[W](_.setScaleType(scaleType))

  def ivCropToPadding(cropToPadding: Boolean): Tweak[W] = Tweak[W](_.setCropToPadding(cropToPadding))

  def ivBaseline(baseline: Int): Tweak[W] = Tweak[W](_.setBaseline(baseline))

  def ivBaselineAlignBottom(aligned: Boolean): Tweak[W] = Tweak[W](_.setBaselineAlignBottom(aligned))

  def ivImageAlpha(alpha: Int): Tweak[W] = Tweak[W](_.setImageAlpha(alpha))

  def ivAdjustViewBounds(adjustViewBounds: Boolean): Tweak[W] = Tweak[W](_.setAdjustViewBounds(adjustViewBounds))

  def ivColorFilterResource(res: Int, mode: Mode = Mode.MULTIPLY)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.setColorFilter(new PorterDuffColorFilter(resGetColor(res), mode)))

  def ivColorFilter(color: Int, mode: Mode = Mode.MULTIPLY): Tweak[W] =
    Tweak[W](_.setColorFilter(new PorterDuffColorFilter(color, mode)))

}
