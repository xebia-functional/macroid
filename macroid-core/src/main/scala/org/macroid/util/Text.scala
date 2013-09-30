package org.macroid.util

import scala.language.implicitConversions
import android.content.Context

object Text {
  def apply(id: Int)(implicit ctx: Context): CharSequence = ctx.getResources.getString(id)
}
