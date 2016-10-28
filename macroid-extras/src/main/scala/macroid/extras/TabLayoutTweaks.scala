package macroid.extras

import android.support.design.widget.TabLayout
import macroid.Tweak

object TabLayoutTweaks {
  type W = TabLayout

  def tlAddTabs(titles: (String, AnyRef)*): Tweak[W] = Tweak[W] { view ⇒
    titles foreach {
      case (title, tag) ⇒ view.addTab(view.newTab().setText(title).setTag(tag))
    }
  }

  def tlAddListener(listener: TabLayout.OnTabSelectedListener): Tweak[W] =
    Tweak[W](_.addOnTabSelectedListener(listener))
}
