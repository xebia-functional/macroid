package macroid.extras

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.animation.AnimationUtils
import macroid.{ContextWrapper, Tweak}

object RecyclerViewTweaks {
  type W = RecyclerView

  val rvFixedSize: Tweak[W] = Tweak[W](_.setHasFixedSize(true))

  val rvNoFixedSize: Tweak[W] = Tweak[W](_.setHasFixedSize(false))

  def rvLayoutManager(layoutManager: RecyclerView.LayoutManager): Tweak[W] =
    Tweak[W](_.setLayoutManager(layoutManager))

  def rvAdapter[VH <: RecyclerView.ViewHolder](adapter: RecyclerView.Adapter[VH]): Tweak[W] =
    Tweak[W](_.setAdapter(adapter))

  def rvAddItemDecoration(decoration: RecyclerView.ItemDecoration): Tweak[W] =
    Tweak[W](_.addItemDecoration(decoration))

  def rvItemAnimator(animator: RecyclerView.ItemAnimator): Tweak[W] =
    Tweak[W](_.setItemAnimator(animator))

  def rvLayoutAnimation(res: Int)(implicit contextWrapper: ContextWrapper) =
    Tweak[W] { view ⇒
      view.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(contextWrapper.application, res))
    }

  def rvSwapAdapter[VH <: RecyclerView.ViewHolder](adapter: RecyclerView.Adapter[VH]): Tweak[W] =
    Tweak[RecyclerView](_.swapAdapter(adapter, false))

  def rvScrollToTop: Tweak[W] = Tweak[W](_.scrollToPosition(0))

  def rvInvalidateItemDecorations: Tweak[W] =
    Tweak[W](_.invalidateItemDecorations())

  def rvItemTouchHelperCallback(callback: ItemTouchHelper.Callback): Tweak[W] =
    Tweak[W] { rv ⇒
      val touchHelper = new ItemTouchHelper(callback)
      touchHelper.attachToRecyclerView(rv)
    }

  def rvAddOnScrollListener(
      scrolled: (Int, Int) ⇒ Unit,
      scrollStateChanged: (Int) ⇒ Unit
  ): Tweak[W] =
    Tweak[RecyclerView](_.addOnScrollListener(new OnScrollListener {
      override def onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int): Unit = scrolled(dx, dy)

      override def onScrollStateChanged(recyclerView: RecyclerView, newState: Int): Unit =
        scrollStateChanged(newState)
    }))

  def rvSmoothScrollBy(dx: Int = 0, dy: Int = 0): Tweak[W] =
    Tweak[RecyclerView](_.smoothScrollBy(dx, dy))

  def rvScrollBy(dx: Int = 0, dy: Int = 0): Tweak[W] =
    Tweak[RecyclerView](_.scrollBy(dx, dy))

}
