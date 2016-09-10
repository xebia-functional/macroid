/*
 *
 *   Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may
 *   not use this file except in compliance with the License. You may obtain
 *   a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package macroid.extras

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.animation.AnimationUtils
import macroid.{ ContextWrapper, Tweak }

object RecyclerViewTweaks {
  type W = RecyclerView

  val rvFixedSize: Tweak[W] = Tweak[W](_.setHasFixedSize(true))

  val rvNoFixedSize: Tweak[W] = Tweak[W](_.setHasFixedSize(false))

  def rvLayoutManager(layoutManager: RecyclerView.LayoutManager): Tweak[W] = Tweak[W](_.setLayoutManager(layoutManager))

  def rvAdapter[VH <: RecyclerView.ViewHolder](adapter: RecyclerView.Adapter[VH]): Tweak[W] = Tweak[W](_.setAdapter(adapter))

  def rvAddItemDecoration(decoration: RecyclerView.ItemDecoration): Tweak[W] = Tweak[W](_.addItemDecoration(decoration))

  def rvItemAnimator(animator: RecyclerView.ItemAnimator): Tweak[W] = Tweak[W](_.setItemAnimator(animator))

  def rvLayoutAnimation(res: Int)(implicit contextWrapper: ContextWrapper) = Tweak[W] { view ⇒
    view.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(contextWrapper.application, res))
  }

  def rvSwapAdapter[VH <: RecyclerView.ViewHolder](adapter: RecyclerView.Adapter[VH]): Tweak[W] =
    Tweak[RecyclerView](_.swapAdapter(adapter, false))

  def rvScrollToTop: Tweak[W] = Tweak[W](_.scrollToPosition(0))

  def rvInvalidateItemDecorations: Tweak[W] = Tweak[W](_.invalidateItemDecorations())

  def rvItemTouchHelperCallback(callback: ItemTouchHelper.Callback): Tweak[W] = Tweak[W] { rv ⇒
    val touchHelper = new ItemTouchHelper(callback)
    touchHelper.attachToRecyclerView(rv)
  }

}
