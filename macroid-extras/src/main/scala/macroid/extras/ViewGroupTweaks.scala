package macroid.extras

import android.view.{ View, ViewGroup }
import macroid.Tweak

object ViewGroupTweaks {
  type W = ViewGroup

  def vgAddViewByIndex[V <: View](view: V, index: Int): Tweak[ViewGroup] = Tweak[ViewGroup](_.addView(view, index))

  def vgAddView[V <: View](view: V): Tweak[W] = Tweak[W](_.addView(view))

  def vgAddView[V <: View](view: V, params: ViewGroup.LayoutParams): Tweak[W] = Tweak[W](_.addView(view, params))

  def vgAddViews[V <: View](views: Seq[V]): Tweak[W] = Tweak[W] { rootView ⇒
    views foreach rootView.addView
  }

  def vgAddViews[V <: View](views: Seq[V], params: ViewGroup.LayoutParams): Tweak[W] = Tweak[W] { rootView ⇒
    views foreach (rootView.addView(_, params))
  }

  val vgRemoveAllViews: Tweak[W] = Tweak[W](_.removeAllViews())

  def vgRemoveView(view: View): Tweak[W] = Tweak[W](_.removeView(view))

  def vgRemoveViewAt(index: Int): Tweak[W] = Tweak[W](_.removeViewAt(index))

  def vgClipToPadding(clip: Boolean): Tweak[W] = Tweak[W](_.setClipToPadding(clip))

  def vgAddViewByIndexParams[V <: View](view: V, index: Int, params: ViewGroup.LayoutParams): Tweak[W] =
    Tweak[ViewGroup](_.addView(view, index, params))
}
