package macroid.contrib

import android.net.Uri
import android.graphics.{ Bitmap, Typeface }
import android.support.v4.view.{ PagerAdapter, ViewPager }
import android.text.InputType
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

  val serif = Tweak[W](x ⇒ x.setTypeface(Typeface.SERIF, typefaceStyle(x)))
  val sans = Tweak[W](x ⇒ x.setTypeface(Typeface.SANS_SERIF, typefaceStyle(x)))
  val mono = Tweak[W](x ⇒ x.setTypeface(Typeface.MONOSPACE, typefaceStyle(x)))

  /** Set a typeface with the given name
   *
   * Example:
   * {{
   * w[TextView] <~ typeface("sans-serif-light")
   * }}
   */
  def typeface(name: String) = Tweak[W](x ⇒ x.setTypeface(Typeface.create(name, 0), typefaceStyle(x)))

  val numeric = Tweak[W](x ⇒ x.setInputType(InputType.TYPE_CLASS_NUMBER))
  val date = Tweak[W](x ⇒ x.setInputType(InputType.TYPE_CLASS_DATETIME))
  val phone = Tweak[W](x ⇒ x.setInputType(InputType.TYPE_CLASS_PHONE))

  def size(points: Int) = Tweak[W](_.setTextSize(TypedValue.COMPLEX_UNIT_SP, points))
  val medium = size(18)
  val large = size(22)

  def allCaps: Tweak[W] = allCaps(false)
  def allCaps(value: Boolean) = Tweak[W](_.setAllCaps(value))

  private def typefaceStyle(x: W) = Option(x.getTypeface).map(_.getStyle).getOrElse(Typeface.NORMAL)
}

/** Extra tweaks for ImageView */
object ImageTweaks {
  type W = ImageView

  def res(resourceId: Int) = Tweak[W](_.setImageResource(resourceId))
  def bitmap(bitmap: Bitmap) = Tweak[W](_.setImageBitmap(bitmap))
  def uri(uri: Uri) = Tweak[W](_.setImageURI(uri))
  val adjustBounds = Tweak[W](_.setAdjustViewBounds(true))
}

/** Extra tweaks for ListView */
object ListTweaks {
  type W = ListView

  val noDivider = Tweak[W](_.setDivider(null))
  def adapter(adapter: ListAdapter) = Tweak[AbsListView](_.setAdapter(adapter))
}

/** Extra tweaks for ViewPager */
object PagerTweaks {
  type W = ViewPager

  def page(index: Int, smoothScroll: Boolean = false) = Tweak[W](_.setCurrentItem(index, smoothScroll))
  def adapter(adapter: PagerAdapter) = Tweak[W](_.setAdapter(adapter))
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
