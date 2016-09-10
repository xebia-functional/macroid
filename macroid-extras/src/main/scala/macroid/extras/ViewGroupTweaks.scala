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
}
