package org.macroid

import android.support.v4.app.Fragment
import android.app.Activity

trait LayoutDsl
  extends Searching
  with LayoutBuilding
  with FragmentBuilding
  with Tweaking
  with Snailing
  with Transforming

object LayoutDsl
  extends LayoutDsl

trait FullDsl
  extends LayoutDsl
  with Tweaks
  with Snails
  with MediaQueries
  with UiThreading

object FullDsl extends FullDsl

trait FullDslActivity
  extends ActivityContexts
  with FullDsl { self: Activity ⇒ }

trait FullDslFragment
  extends FragmentContexts
  with FullDsl { self: Fragment ⇒ }