package macroid.extras

import android.view.{View, ViewGroup}
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
    val param =
      new GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(column))
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
      views.lift((row * rows) + column) foreach { view ⇒
        val param = new GridLayout.LayoutParams(GridLayout.spec(row),
                                                GridLayout.spec(column))
        param.setMargins(left, top, right, bottom)
        param.height = height
        param.width = width
        rootView.addView(view, param)
      }
    }
  }
}
