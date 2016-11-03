package macroid.extras

import android.graphics.drawable.Drawable
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.View.OnClickListener
import macroid.{ Tweak, Ui }

object ToolbarTweaks {
  type W = Toolbar

  def tbTitle(title: String): Tweak[W] = Tweak[W](_.setTitle(title))

  def tbTitle(title: Int): Tweak[W] = Tweak[W](_.setTitle(title))

  def tbTextColor(color: Int): Tweak[W] = Tweak[W](_.setTitleTextColor(color))

  def tbBackgroundColor(color: Int): Tweak[W] = Tweak[Toolbar](_.setBackgroundColor(color))

  def tbLogo(res: Int): Tweak[W] = Tweak[W](_.setLogo(res))

  def tbLogo(drawable: Drawable): Tweak[W] = Tweak[W](_.setLogo(drawable))

  def tbNavigationIcon(res: Int): Tweak[W] = Tweak[W](_.setNavigationIcon(res))

  def tbNavigationIcon(drawable: Drawable): Tweak[W] = Tweak[W](_.setNavigationIcon(drawable))

  def tbNavigationOnClickListener(click: (View) ⇒ Ui[_]): Tweak[W] = Tweak[W](_.setNavigationOnClickListener(new OnClickListener {
    override def onClick(v: View): Unit = click(v).run
  }))

  def tbChangeHeightLayout(height: Int): Tweak[W] = Tweak[W] { view ⇒
    view.getLayoutParams.height = height
    view.requestLayout()
  }

}
