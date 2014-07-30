package macroid

import android.view.View
import macroid.contrib.ListTweaks

package object viewable {
  implicit class ViewableOps[A, W <: View](data: A)(implicit ctx: ActivityContext, appCtx: AppContext, val viewable: Viewable[A, W]) {
    def layout: Ui[W] = viewable.layout(data)
  }

  implicit class ListableOps[A, W <: View](data: Seq[A])(implicit ctx: ActivityContext, appCtx: AppContext, val viewable: Listable[A, W]) {
    def adapter: ListableAdapter[A, W] = new ListableAdapter[A, W] {
      addAll(data: _*)
    }

    def adapterTweak = ListTweaks.adapter(adapter)
  }
}
