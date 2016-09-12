package macroid.extras

import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.view.ViewGroup.LayoutParams._
import macroid.FullDsl._
import macroid.Tweak

object DrawerLayoutTweaks {
  type W = DrawerLayout

  def dlContentSize(w: Int, h: Int): Tweak[View] = lp[W](w, h)

  val dlMatchWeightVertical: Tweak[View] = lp[W](MATCH_PARENT, 0, 1)
  val dlMatchWeightHorizontal: Tweak[View] = lp[W](0, MATCH_PARENT, 1)

  def dlLayoutGravity(gravity: Int): Tweak[View] = Tweak[View] { view ⇒
    val param = new DrawerLayout.LayoutParams(view.getLayoutParams.width, view.getLayoutParams.height)
    param.gravity = gravity
    view.setLayoutParams(param)
  }

  def dlCloseDrawer(drawerMenuView: Option[View]): Tweak[W] = Tweak[W] { view ⇒
    drawerMenuView foreach view.closeDrawer
  }

  def dlStatusBarBackground(res: Int): Tweak[W] = Tweak[W](_.setStatusBarBackground(res))

  def dlOpenDrawer: Tweak[W] = Tweak[W](_.openDrawer(GravityCompat.START))

  def dlCloseDrawer: Tweak[W] = Tweak[W](_.closeDrawer(GravityCompat.START))

  def dlOpenDrawerEnd: Tweak[W] = Tweak[W](_.openDrawer(GravityCompat.END))

  def dlCloseDrawerEnd: Tweak[W] = Tweak[W](_.closeDrawer(GravityCompat.END))

  def dlLockedClosedStart: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START))

  def dlLockedClosedEnd: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END))

  def dlLockedClosed: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED))

  def dlUnlocked: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED))

  def dlUnlockedStart: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START))

  def dlUnlockedEnd: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END))

  def dlLockedOpen: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN))

  def dlLockedOpenStart: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.START))

  def dlLockedOpenEnd: Tweak[W] = Tweak[W](_.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.END))

  def dlSwapDrawer: Tweak[W] = Tweak[W] { view ⇒
    if (view.isDrawerOpen(GravityCompat.START)) {
      view.closeDrawer(GravityCompat.START)
    } else {
      view.openDrawer(GravityCompat.START)
    }
  }

  def dlSwapDrawerEnd: Tweak[W] = Tweak[W] { view ⇒
    if (view.isDrawerOpen(GravityCompat.END)) {
      view.closeDrawer(GravityCompat.END)
    } else {
      view.openDrawer(GravityCompat.END)
    }
  }

}
