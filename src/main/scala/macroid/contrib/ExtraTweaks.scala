package macroid.contrib

import android.graphics.{ Bitmap, Typeface }
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams._
import android.view.{ View, ViewGroup }
import android.widget._
import macroid.Tweak

/** Extra tweaks for TextView */
object TextTweaks {
  type W = TextView

  def color(color: Int) = Tweak[W](_.setTextColor(color))

  val bold = Tweak[W](x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD))
  val italic = Tweak[W](x ⇒ x.setTypeface(x.getTypeface, Typeface.ITALIC))
  val boldItalic = Tweak[W](x ⇒ x.setTypeface(x.getTypeface, Typeface.BOLD_ITALIC))

  val serif = Tweak[W](x ⇒ x.setTypeface(Typeface.SERIF, Option(x.getTypeface).map(_.getStyle).getOrElse(Typeface.NORMAL)))
  val sans = Tweak[W](x ⇒ x.setTypeface(Typeface.SANS_SERIF, Option(x.getTypeface).map(_.getStyle).getOrElse(Typeface.NORMAL)))
  val mono = Tweak[W](x ⇒ x.setTypeface(Typeface.MONOSPACE, Option(x.getTypeface).map(_.getStyle).getOrElse(Typeface.NORMAL)))

  def size(points: Int) = Tweak[W](_.setTextSize(TypedValue.COMPLEX_UNIT_SP, points))
  val medium = size(18)
  val large = size(22)
}

/** Extra tweaks for ImageView */
object ImageTweaks {
  type W = ImageView

  def res(resourceId: Int) = Tweak[W](_.setImageResource(resourceId))
  def bitmap(bitmap: Bitmap) = Tweak[W](_.setImageBitmap(bitmap))
  val adjustBounds = Tweak[W](_.setAdjustViewBounds(true))
}

/** Extra tweaks for ListView */
object ListTweaks {
  type W = ListView

  val noDivider = Tweak[W](_.setDivider(null))
  def adapter(adapter: ListAdapter) = Tweak[W](_.setAdapter(adapter))
}

/** Extra tweaks for backgrounds */
object BgTweaks {
  type W = View

  def res(resourceId: Int) = Tweak[W](_.setBackgroundResource(resourceId))
  def color(color: Int) = Tweak[W](_.setBackgroundColor(color))
}

/** Extra tweaks for SeekBar */
object SeekTweaks {
  type W = SeekBar

  def seek(p: Int) = Tweak[SeekBar](_.setProgress(p))
}

/** Extra layout params */
object LpTweaks {
  type W = View
  private def lp(w: Int, h: Int) = Tweak[W](_.setLayoutParams(new ViewGroup.LayoutParams(w, h)))

  val matchParent = lp(MATCH_PARENT, MATCH_PARENT)
  val wrapContent = lp(WRAP_CONTENT, WRAP_CONTENT)
  val matchWidth = lp(MATCH_PARENT, WRAP_CONTENT)
  val matchHeight = lp(WRAP_CONTENT, MATCH_PARENT)
}
