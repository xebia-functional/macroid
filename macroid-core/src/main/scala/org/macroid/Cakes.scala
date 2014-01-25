package org.macroid

private[macroid] trait LayoutDsl
  extends Searching
  with LayoutBuilding
  with FragmentBuilding
  with Tweaking
  with Snailing
  with Transforming

object LayoutDsl extends LayoutDsl

private[macroid] trait FullDsl
  extends LayoutDsl
  with Tweaks
  with Snails
  with Toasts
  with Dialogs
  with Resources
  with MediaQueries
  with UiThreading

object FullDsl extends FullDsl
