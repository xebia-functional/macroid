package macroid.extras

import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import macroid.ActivityContextWrapper

object ThemeExtras {

  def themeGetDrawable(themeId: Int, attr: Int)(implicit activityContext: ActivityContextWrapper): Option[Drawable] =
    activityContext.original.get map {
      activity ⇒
        val contextTheme = new ContextThemeWrapper(activity, themeId)
        val a = contextTheme.getTheme.obtainStyledAttributes(Array(attr))
        val attributeResourceId = a.getResourceId(0, 0)
        val drawable = contextTheme.getResources.getDrawable(attributeResourceId)
        a.recycle()
        drawable
    }

  def themeGetDrawable(attr: Int)(implicit activityContext: ActivityContextWrapper): Option[Drawable] =
    activityContext.original.get map {
      activity ⇒
        val a = activity.getTheme.obtainStyledAttributes(Array(attr))
        val attributeResourceId = a.getResourceId(0, 0)
        val drawable = activity.getResources.getDrawable(attributeResourceId)
        a.recycle()
        drawable
    }

}