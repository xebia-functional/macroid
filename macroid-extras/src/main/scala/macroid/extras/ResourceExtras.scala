package macroid.extras

import android.graphics.drawable.Drawable
import macroid.ContextWrapper
import macroid.extras.DeviceVersion.Lollipop

object ResourcesExtras {

  private def resGetResource[A](resource: String, resourceType: String)(f: (ContextWrapper, Int) ⇒ A)(implicit c: ContextWrapper): Option[A] = {
    val resourceId = c.bestAvailable.getResources.getIdentifier(resource, resourceType, c.bestAvailable.getPackageName)
    resourceId match {
      case 0 ⇒ None
      case _ ⇒ Some(f(c, resourceId))
    }
  }

  def resGetBoolean(resourceId: Int)(implicit c: ContextWrapper): Boolean = c.bestAvailable.getResources.getBoolean(resourceId)

  def resGetBoolean(resource: String)(implicit c: ContextWrapper): Option[Boolean] =
    resGetResource(resource, "boolean")((c, resourceId) ⇒ c.bestAvailable.getResources.getBoolean(resourceId))

  def resGetColor(resourceId: Int)(implicit c: ContextWrapper): Int = c.bestAvailable.getResources.getColor(resourceId)

  def resGetColor(resource: String)(implicit c: ContextWrapper): Option[Int] =
    resGetResource(resource, "color")((c, resourceId) ⇒ c.bestAvailable.getResources.getColor(resourceId))

  def resGetDimension(resourceId: Int)(implicit c: ContextWrapper): Float = c.bestAvailable.getResources.getDimension(resourceId)

  def resGetDimension(resource: String)(implicit c: ContextWrapper): Option[Float] =
    resGetResource(resource, "dimen")((c, resourceId) ⇒ c.bestAvailable.getResources.getDimension(resourceId))

  def resGetDimensionPixelSize(resourceId: Int)(implicit c: ContextWrapper): Int = c.bestAvailable.getResources.getDimensionPixelSize(resourceId)

  def resGetDimensionPixelSize(resource: String)(implicit c: ContextWrapper): Option[Int] =
    resGetResource(resource, "dimen")((c, resourceId) ⇒ c.bestAvailable.getResources.getDimensionPixelSize(resourceId))

  def resGetDrawable(resourceId: Int)(implicit c: ContextWrapper): Drawable = getDrawable(resourceId)

  def resGetDrawable(resource: String)(implicit c: ContextWrapper): Option[Drawable] =
    resGetResource(resource, "drawable")((c, resourceId) ⇒ getDrawable(resourceId)(c))

  def resGetDrawableIdentifier(resource: String)(implicit c: ContextWrapper): Option[Int] =
    resGetResource(resource, "drawable")((c, resourceId) ⇒ resourceId)

  def resGetIdentifier(resource: String, resourceType: String)(implicit c: ContextWrapper): Option[Int] =
    resGetResource(resource, resourceType)((_, resourceId) ⇒ resourceId)

  def resGetInteger(resourceId: Int)(implicit c: ContextWrapper): Int = c.bestAvailable.getResources.getInteger(resourceId)

  def resGetInteger(resource: String)(implicit c: ContextWrapper): Option[Int] =
    resGetResource(resource, "integer")((c, resourceId) ⇒ c.bestAvailable.getResources.getInteger(resourceId))

  def resGetResourcePackageName(resourceId: Int)(implicit c: ContextWrapper): String =
    c.bestAvailable.getResources.getResourcePackageName(resourceId)

  def resGetString(resourceId: Int)(implicit c: ContextWrapper): String = c.bestAvailable.getResources.getString(resourceId)

  def resGetString(resource: String)(implicit c: ContextWrapper): Option[String] =
    resGetResource(resource, "string")((c, resourceId) ⇒ c.bestAvailable.getResources.getString(resourceId))

  def resGetString(resourceId: Int, formatArgs: AnyRef*)(implicit c: ContextWrapper): String =
    c.bestAvailable.getResources.getString(resourceId, formatArgs: _*)

  def resGetString(resource: String, formatArgs: AnyRef*)(implicit c: ContextWrapper): Option[String] =
    resGetResource(resource, "string")((c, resourceId) ⇒ c.bestAvailable.getResources.getString(resourceId, formatArgs: _*))

  def resGetQuantityString(resourceId: Int, quantity: Int)(implicit c: ContextWrapper): String =
    c.bestAvailable.getResources.getQuantityString(resourceId, quantity)

  def resGetQuantityString(resourceId: Int, quantity: Int, formatArgs: AnyRef*)(implicit c: ContextWrapper): String =
    c.bestAvailable.getResources.getQuantityString(resourceId, quantity, formatArgs: _*)

  private[this] def getDrawable(resourceId: Int)(implicit c: ContextWrapper) =
    Lollipop.ifSupportedThen {
      c.bestAvailable.getResources.getDrawable(resourceId, c.bestAvailable.getTheme)
    } getOrElse {
      c.bestAvailable.getResources.getDrawable(resourceId)
    }

}
