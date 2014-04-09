package macroid.contrib

import macroid.{ Tweak, AppContext }
import android.widget.{ ImageView, TextView }
import android.graphics.{ Bitmap, Typeface }
import android.util.TypedValue
import android.view.View

private[macroid] trait ExtraTweaks {
  object TextStyle {
    val bold = Tweak[TextView](x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD))
    val italic = Tweak[TextView](x ⇒ x.setTypeface(x.getTypeface, Typeface.ITALIC))
    val boldItalic = Tweak[TextView](x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD_ITALIC))
  }

  object TextSize {
    def medium(implicit ctx: AppContext) = Tweak[TextView](_.setTextAppearance(ctx.get, android.R.style.TextAppearance_Medium))
    def large(implicit ctx: AppContext) = Tweak[TextView](_.setTextAppearance(ctx.get, android.R.style.TextAppearance_Large))
    def sp(points: Int) = Tweak[TextView](_.setTextSize(TypedValue.COMPLEX_UNIT_SP, points))
  }

  object TextFace {
    val serif = Tweak[TextView](x ⇒ x.setTypeface(Typeface.SERIF, Option(x.getTypeface).map(_.getStyle).getOrElse(Typeface.NORMAL)))
    val sans = Tweak[TextView](x ⇒ x.setTypeface(Typeface.SANS_SERIF, Option(x.getTypeface).map(_.getStyle).getOrElse(Typeface.NORMAL)))
    val mono = Tweak[TextView](x ⇒ x.setTypeface(Typeface.MONOSPACE, Option(x.getTypeface).map(_.getStyle).getOrElse(Typeface.NORMAL)))
  }

  object Bg {
    def res(id: Int) = Tweak[View](_.setBackgroundResource(id))
    def resource(id: Int) = Tweak[View](_.setBackgroundResource(id))
    def color(color: Int) = Tweak[View](_.setBackgroundColor(color))
  }

  object Image {
    def bitmap(bitmap: Bitmap) = Tweak[ImageView](_.setImageBitmap(bitmap))
    def adjustBounds = Tweak[ImageView](_.setAdjustViewBounds(true))
  }
}

object ExtraTweaks extends ExtraTweaks
