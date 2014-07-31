package macroid

import android.view.View

package object viewable {
  implicit class ViewableOps[A, W <: View](data: A)(implicit val viewable: Viewable[A, W]) {
    def layout(implicit ctx: ActivityContext, appCtx: AppContext): Ui[W] =
      viewable.layout(data)
  }

  implicit class PagerAdapterOps[A, W <: View](data: Seq[A])(implicit val viewable: Viewable[A, W]) {
    def pagerAdapter(implicit ctx: ActivityContext, appCtx: AppContext): ViewablePagerAdapter[A, W] =
      viewable.pagerAdapter(data)

    def pagerAdapterTweak(implicit ctx: ActivityContext, appCtx: AppContext) =
      viewable.pagerAdapterTweak(data)
  }

  implicit class ListAdapterOps[A, W <: View](data: Seq[A])(implicit val listable: Listable[A, W]) {
    def listAdapter(implicit ctx: ActivityContext, appCtx: AppContext): ListableListAdapter[A, W] =
      listable.listAdapter(data)

    def listAdapterTweak(implicit ctx: ActivityContext, appCtx: AppContext) =
      listable.listAdapterTweak(data)
  }
}
