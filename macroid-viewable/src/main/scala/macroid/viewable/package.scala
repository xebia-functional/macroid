package macroid

import android.view.View

package object viewable {
  implicit class ViewableOps[A](data: A) {
    /** Create layout for this value */
    def view[W <: View](implicit ctx: ContextWrapper, viewable: Viewable[A, W]) =
      viewable.view(data)
  }

  implicit class PagerAdapterOps[A](data: Seq[A]) {
    /** Create a `PagerAdapter` for this sequence */
    def pagerAdapter[W <: View](implicit ctx: ContextWrapper, viewable: Viewable[A, W]) =
      viewable.pagerAdapter(data)

    /** Create a tweak to set the `PagerAdapter` with this sequence */
    def pagerAdapterTweak[W <: View](implicit ctx: ContextWrapper, viewable: Viewable[A, W]) =
      viewable.pagerAdapterTweak(data)
  }

  implicit class ListAdapterOps[A](data: Seq[A]) {
    /** Create a `ListAdapter` for this sequence */
    def listAdapter[W <: View](implicit ctx: ContextWrapper, listable: Listable[A, W]) =
      listable.listAdapter(data)

    /** Create a tweak to set the `ListAdapter` with this sequence */
    def listAdapterTweak[W <: View](implicit ctx: ContextWrapper, listable: Listable[A, W]) =
      listable.listAdapterTweak(data)
  }
}
