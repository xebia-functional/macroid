package org.macroid.util

import scala.language.implicitConversions
import android.content.Context
import org.macroid.AppContext

object Text {
  def apply(id: Int)(implicit ctx: AppContext): CharSequence = ctx.get.getResources.getString(id)
}
