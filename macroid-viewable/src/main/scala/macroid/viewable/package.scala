package macroid

import android.view.View

package object viewable {
  implicit class ViewableOps[A](data: A) {
    def layout[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, viewable: Viewable[A, W]) =
      viewable.layout(data)
  }

  implicit class PagerAdapterOps[A](data: Seq[A]) {
    def pagerAdapter[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, viewable: Viewable[A, W]) =
      viewable.pagerAdapter(data)

    def pagerAdapterTweak[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, viewable: Viewable[A, W]) =
      viewable.pagerAdapterTweak(data)
  }

  implicit class ListAdapterOps[A](data: Seq[A]) {
    def listAdapter[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, listable: Listable[A, W]) =
      listable.listAdapter(data)

    def listAdapterTweak[W <: View](implicit ctx: ActivityContext, appCtx: AppContext, listable: Listable[A, W]) =
      listable.listAdapterTweak(data)
  }
}
