package org.macroid.contrib

import org.macroid.{ LayoutDsl, Tweaks }
import android.widget.{ ImageView, TextView }
import android.graphics.{ Bitmap, Typeface }
import android.content.Context
import android.util.TypedValue
import android.view.View

trait ExtraTweaks extends LayoutDsl with Tweaks {
  object TextStyle {
    val bold: Tweak[TextView] = x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD)
    val italic: Tweak[TextView] = x ⇒ x.setTypeface(x.getTypeface, Typeface.ITALIC)
    val boldItalic: Tweak[TextView] = x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD_ITALIC)
  }

  object TextSize {
    def medium(implicit ctx: Context): Tweak[TextView] = _.setTextAppearance(ctx, android.R.style.TextAppearance_Medium)
    def large(implicit ctx: Context): Tweak[TextView] = _.setTextAppearance(ctx, android.R.style.TextAppearance_Large)
    def sp(points: Int): Tweak[TextView] = _.setTextSize(TypedValue.COMPLEX_UNIT_SP, points)
  }

  object TextFace {
    val serif: Tweak[TextView] = _.setTypeface(Typeface.SERIF)
    val sans: Tweak[TextView] = _.setTypeface(Typeface.SANS_SERIF)
    val mono: Tweak[TextView] = _.setTypeface(Typeface.MONOSPACE)
  }

  object Bg {
    def res(id: Int)(implicit ctx: Context): Tweak[View] = _.setBackgroundResource(id)
    def resource(id: Int)(implicit ctx: Context): Tweak[View] = _.setBackgroundResource(id)
    def color(color: Int): Tweak[View] = _.setBackgroundColor(color)
  }

  object Image {
    def bitmap(bitmap: Bitmap): Tweak[ImageView] = _.setImageBitmap(bitmap)
    def adjustBounds: Tweak[ImageView] = _.setAdjustViewBounds(true)
  }
}

object ExtraTweaks extends ExtraTweaks