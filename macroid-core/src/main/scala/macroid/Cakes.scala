package macroid

private[macroid] trait LayoutDsl
  extends Searching
  with LayoutBuilding
  with FragmentBuilding
  with Tweaking
  with Snailing

object LayoutDsl extends LayoutDsl

private[macroid] trait DialogDsl
  extends DialogBuilding
  with Phrasing

object DialogDsl extends DialogDsl

private[macroid] trait ToastDsl
  extends ToastBuilding
  with Loafing

object ToastDsl extends ToastDsl

private[macroid] trait FullDsl
  extends UiThreading
  with LayoutDsl with Tweaks with Snails
  with ToastDsl with Loafs
  with DialogDsl with Phrases
  with Resources
  with MediaQueries

object FullDsl extends FullDsl
