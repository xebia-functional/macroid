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

  def ivBlank: Tweak[ImageView] = Tweak[ImageView](_.setImageBitmap(null))

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
