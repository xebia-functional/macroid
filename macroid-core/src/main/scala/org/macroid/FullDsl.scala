package org.macroid

import android.support.v4.app.{ Fragment, FragmentActivity }

trait FullDslActivity extends ActivityContext
  with ActivityViewSearch
  with LayoutDsl
  with FragmentDsl
  with Tweaks
  with Snails
  with MediaQueries
  with Concurrency { self: FragmentActivity ⇒ }

trait FullDslFragment extends FragmentContext
  with FragmentViewSearch
  with LayoutDsl
  with FragmentDsl
  with Tweaks
  with Snails
  with MediaQueries
  with Concurrency { self: Fragment ⇒ }