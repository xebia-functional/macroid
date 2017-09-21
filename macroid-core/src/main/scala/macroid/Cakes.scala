package macroid

private[macroid] trait LayoutDsl extends Searching with LayoutBuilding with FragmentBuilding

object LayoutDsl extends LayoutDsl

private[macroid] trait DialogDsl extends DialogBuilding with Phrasing

object DialogDsl extends DialogDsl

private[macroid] trait ToastDsl extends ToastBuilding with Loafing

object ToastDsl extends ToastDsl

private[macroid] trait FullDsl
    extends MediaQueries
    with LayoutDsl
    with Tweaks
    with Snails
    with Excerpts
    with DialogDsl
    with Phrases
    with ToastDsl
    with Loafs

object FullDsl extends FullDsl
