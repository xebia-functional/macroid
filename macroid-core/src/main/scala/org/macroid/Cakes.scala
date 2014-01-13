package org.macroid

trait LayoutDsl
  extends Contexts
  with Searching
  with LayoutBuilding
  with FragmentBuilding
  with Tweaking
  with Snailing
  with Transforming

object LayoutDsl extends LayoutDsl

trait FullDsl
  extends LayoutDsl
  with Tweaks
  with Snails
  with Toasts
  with MediaQueries
  with UiThreading

object FullDsl extends FullDsl
