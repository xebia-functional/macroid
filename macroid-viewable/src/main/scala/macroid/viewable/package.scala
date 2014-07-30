package macroid

import android.view.View
import macroid.contrib.{ PagerTweaks, ListTweaks }

package object viewable {
  implicit class ViewableOps[A, W <: View](data: A)(implicit ctx: ActivityContext, appCtx: AppContext, val viewable: Viewable[A, W]) {
    def layout: Ui[W] = viewable.layout(data)
  }

  implicit class PagerAdapterOps[A, W <: View](data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext, val viewable: Viewable[A, W]) {
    def pagerAdapter: ViewablePagerAdapter[A, W] = new ViewablePagerAdapter[A, W](data)

    def pagerAdapterTweak = PagerTweaks.adapter(pagerAdapter)
  }

  implicit class ListAdapterOps[A, W <: View](data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext, val listable: Listable[A, W]) {
    def listAdapter: ListableListAdapter[A, W] = new ListableListAdapter[A, W](data)

    def listAdapterTweak = ListTweaks.adapter(listAdapter)
  }
}
