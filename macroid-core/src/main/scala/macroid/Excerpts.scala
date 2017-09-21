package macroid

import android.view.View
import android.widget.TextView

private[macroid] trait Excerpts {
  val getId   = Excerpt[View, Int](_.getId)
  val getText = Excerpt[TextView, CharSequence](_.getText)
}

object Excerpts extends Excerpts
