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
import android.widget.GridLayout
import macroid.Tweak

object GridLayoutTweaks {
  type W = GridLayout

  def glAddView[V <: View](
    view: V,
    column: Int,
    row: Int,
    width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    left: Int = GridLayout.UNDEFINED,
    top: Int = GridLayout.UNDEFINED,
    right: Int = GridLayout.UNDEFINED,
    bottom: Int = GridLayout.UNDEFINED
  ): Tweak[W] = Tweak[W] { rootView ⇒
    val param = new GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(column))
    param.setMargins(left, top, right, bottom)
    param.height = height
    param.width = width
    rootView.addView(view, param)
  }

  def glAddViews[V <: View](
    views: Seq[V],
    columns: Int,
    rows: Int,
    width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    left: Int = GridLayout.UNDEFINED,
    top: Int = GridLayout.UNDEFINED,
    right: Int = GridLayout.UNDEFINED,
    bottom: Int = GridLayout.UNDEFINED
  ): Tweak[W] = Tweak[W] { rootView ⇒
    for {
      row ← 0 until rows
      column ← 0 until columns
    } yield {
      views.lift((row * rows) + column) foreach {
        view ⇒
          val param = new GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(column))
          param.setMargins(left, top, right, bottom)
          param.height = height
          param.width = width
          rootView.addView(view, param)
      }
    }
  }
}
