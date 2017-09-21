package macroid.extras

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils.TruncateAt
import android.text.style.UnderlineSpan
import android.text.{ Spannable, SpannableString, Spanned }
import android.util.TypedValue
import android.widget.TextView
import macroid.extras.ResourcesExtras._
import macroid.{ ContextWrapper, Tweak }

object TextViewTweaks {
  type W = TextView

  def tvColor(color: Int): Tweak[W] = Tweak[W](_.setTextColor(color))

  def tvColorResource(resColor: Int)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.setTextColor(resGetColor(resColor)))

  val tvBold: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD))

  val tvItalic: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(x.getTypeface, Typeface.ITALIC))

  val tvBoldItalic: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD_ITALIC))

  val tvNormalLight: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL)))

  val tvBoldLight: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD)))

  val tvItalicLight: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC)))

  val tvBoldItalicLight: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD_ITALIC)))

  val tvNormalCondensed: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL)))

  val tvBoldCondensed: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD)))

  val tvItalicCondensed: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-condensed", Typeface.ITALIC)))

  val tvBoldItalicCondensed: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)))

  val tvNormalMedium: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)))

  val tvBoldMedium: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD)))

  val tvItalicMedium: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-medium", Typeface.ITALIC)))

  val tvBoldItalicMedium: Tweak[W] = Tweak[W](x ⇒ x.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD_ITALIC)))

  def tvSize(points: Int): Tweak[W] = Tweak[W](_.setTextSize(TypedValue.COMPLEX_UNIT_SP, points.toFloat))

  def tvSizeResource(res: Int)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.application.getResources.getDimension(res)))

  def tvLines(lines: Int): Tweak[W] = Tweak[W](_.setLines(lines))

  def tvMaxLines(lines: Int): Tweak[W] = Tweak[W](_.setMaxLines(lines))

  def tvMinLines(lines: Int): Tweak[W] = Tweak[W](_.setMinLines(lines))

  def tvEllipsize(truncateAt: TruncateAt): Tweak[W] = Tweak[W](_.setEllipsize(truncateAt))

  def tvAllCaps(allCaps: Boolean = true): Tweak[W] = Tweak[W](_.setAllCaps(allCaps))

  def tvGravity(gravity: Int): Tweak[W] = Tweak[W](_.setGravity(gravity))

  def tvText(text: String): Tweak[W] = Tweak[W](_.setText(text))

  def tvText(text: Int): Tweak[W] = Tweak[W](_.setText(text))

  def tvText(text: Spannable): Tweak[W] = Tweak[W](_.setText(text))

  def tvText(text: Spanned): Tweak[W] = Tweak[W](_.setText(text))

  def tvDrawablePadding(padding: Int): Tweak[W] = Tweak[W](_.setCompoundDrawablePadding(padding))

  def tvHint(text: String): Tweak[W] = Tweak[W](_.setHint(text))

  def tvHint(text: Int): Tweak[W] = Tweak[W](_.setHint(text))

  def tvHintColor(color: Int): Tweak[W] = Tweak[W](_.setHintTextColor(color))

  def tvCompoundDrawablesWithIntrinsicBounds(
    left: Option[Drawable] = None,
    top: Option[Drawable] = None,
    right: Option[Drawable] = None,
    bottom: Option[Drawable] = None
  ) =
    Tweak[TextView](_.setCompoundDrawablesWithIntrinsicBounds(left.orNull, top.orNull, right.orNull, bottom.orNull))

  def tvCompoundDrawablesWithIntrinsicBoundsResources(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) =
    Tweak[TextView](_.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom))

  def tvShadowLayer(radius: Float, dx: Int, dy: Int, color: Int): Tweak[W] =
    Tweak[W](_.setShadowLayer(radius, dx.toFloat, dy.toFloat, color))

  def tvUnderlineText(text: String): Tweak[W] = Tweak[W] { tv ⇒
    val content = new SpannableString(text)
    content.setSpan(new UnderlineSpan(), 0, text.length, 0)
    tv.setText(content)
  }

}
