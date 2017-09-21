package macroid.extras

import android.support.design.widget.NavigationView
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.view.MenuItem
import macroid.Tweak

object NavigationViewTweaks {
  type W = NavigationView

  def nvNavigationItemSelectedListener(onItem: (Int) ⇒ Boolean) =
    Tweak[W](
      _.setNavigationItemSelectedListener(
        new OnNavigationItemSelectedListener {
          override def onNavigationItemSelected(menuItem: MenuItem): Boolean =
            onItem(menuItem.getItemId)
        }
      ))
}
