package org.macroid

import android.support.v4.app.{ Fragment, FragmentActivity }

trait FullDslActivity extends ActivityContexts
  with ActivityViewSearch
  with LayoutDsl
  with FragmentDsl
  with Tweaks
  with Snails
  with MediaQueries
  with UiThreading { self: FragmentActivity ⇒ }

trait FullDslFragment extends FragmentContexts
  with FragmentViewSearch
  with LayoutDsl
  with FragmentDsl
  with Tweaks
  with Snails
  with MediaQueries
  with UiThreading { self: Fragment ⇒ }