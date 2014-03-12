package macroid.viewable

import android.view.{ ViewGroup, View }
import android.widget.ArrayAdapter
import scala.util.Try
import macroid.{ AppContext, ActivityContext }
import macroid.UiThreading._
import macroid.util.Ui

class FillableViewableAdapter[A](implicit ctx: ActivityContext, appCtx: AppContext, fillableViewable: FillableViewable[A])
  extends ArrayAdapter[A](ctx.get, 0) {

  override def getView(position: Int, view: View, parent: ViewGroup): View = getUi {
    val v = Option(view).flatMap(x ⇒ Try(Ui(x.asInstanceOf[fillableViewable.W])).toOption).getOrElse(fillableViewable.makeView)
    fillableViewable.fillView(v, getItem(position)); v
  }
}

object FillableViewableAdapter {
  def apply[A](fillableViewable: FillableViewable[A])(implicit ctx: ActivityContext, appCtx: AppContext) =
    new FillableViewableAdapter[A]()(ctx, appCtx, fillableViewable)

  def apply[A](data: Seq[A])(fillableViewable: FillableViewable[A])(implicit ctx: ActivityContext, appCtx: AppContext) =
    new FillableViewableAdapter[A]()(ctx, appCtx, fillableViewable) {
      addAll(data: _*)
    }
}
