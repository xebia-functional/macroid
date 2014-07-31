package macroid

import android.view.View

package object viewable {
  implicit class ViewableOps[A](data: A) {
    /** Create layout for this value */
    def layout[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, viewable: Viewable[A, W]) =
      viewable.layout(data)
  }

  implicit class PagerAdapterOps[A](data: Seq[A]) {
    /** Create a `PagerAdapter` for this sequence */
    def pagerAdapter[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, viewable: Viewable[A, W]) =
      viewable.pagerAdapter(data)

    /** Create a tweak to set the `PagerAdapter` with this sequence */
    def pagerAdapterTweak[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, viewable: Viewable[A, W]) =
      viewable.pagerAdapterTweak(data)
  }

  implicit class ListAdapterOps[A](data: Seq[A]) {
    /** Create a `ListAdapter` for this sequence */
    def listAdapter[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, listable: Listable[A, W]) =
      listable.listAdapter(data)

    /** Create a tweak to set the `ListAdapter` with this sequence */
    def listAdapterTweak[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, listable: Listable[A, W]) =
      listable.listAdapterTweak(data)
  }
}
