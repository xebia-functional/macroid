package macroid

import scala.language.implicitConversions
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.content.res.Configuration

/** A media query is a small wrapper around Boolean with nicer operators */
case class MediaQuery(b: Boolean) {
  /** Return Some(v) if the queried condition holds, otherwise None */
  def ?[A](v: A) = if (b) Some(v) else None
  // boolean ops
  def unary_! = MediaQuery(!b)
  def &(q: MediaQuery) = MediaQuery(b && q.b)
  def |(q: MediaQuery) = MediaQuery(b || q.b)
}

object MediaQuery {
  implicit def toBoolean(q: MediaQuery) = q.b
}

private[macroid] trait MediaQueryEssentials {
  protected def displayMetrics(implicit ctx: AppContext) = {
    val display = ctx.get.getSystemService(Context.WINDOW_SERVICE).asInstanceOf[WindowManager].getDefaultDisplay
    val metrics = new DisplayMetrics
    display.getMetrics(metrics)
    metrics
  }
}

private[macroid] trait DensityQueries extends MediaQueryEssentials {
  def ldpi(implicit ctx: AppContext) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_LOW)
  def mdpi(implicit ctx: AppContext) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM)
  def hdpi(implicit ctx: AppContext) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_HIGH)
  def xhdpi(implicit ctx: AppContext) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_XHIGH)
}

private[macroid] trait OrientationQueries {
  def portrait(implicit ctx: AppContext) =
    MediaQuery(ctx.get.getResources.getConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT)
  def landscape(implicit ctx: AppContext) =
    MediaQuery(ctx.get.getResources.getConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE)
}

private[macroid] trait DisplayUnits extends MediaQueryEssentials {
  implicit class Units[A](v: A)(implicit ctx: AppContext, numeric: Numeric[A]) {
    import Numeric.Implicits.infixNumericOps
    /** Using pixels is strictly discouraged! */
    def px = v.toInt()
    /** Density-independent points */
    def dp = (v.toFloat() * displayMetrics.density).toInt
    /** Scale-independent points */
    def sp = (v.toFloat() * displayMetrics.scaledDensity).toInt
  }
}

private[macroid] trait SizeQueries extends MediaQueryEssentials {
  /** Width is at least v */
  def minWidth(v: Int)(implicit ctx: AppContext) = MediaQuery(displayMetrics.widthPixels >= v)
  /** Same as minWidth(v) */
  def widerThan(v: Int)(implicit ctx: AppContext) = minWidth(v)
  /** Width is at most v */
  def maxWidth(v: Int)(implicit ctx: AppContext) = MediaQuery(displayMetrics.widthPixels <= v)
  /** Same as maxWidth(v) */
  def narrowerThan(v: Int)(implicit ctx: AppContext) = maxWidth(v)

  /** Height is at least v */
  def minHeight(v: Int)(implicit ctx: AppContext) = MediaQuery(displayMetrics.heightPixels >= v)
  /** Same as minHeight(v) */
  def higherThan(v: Int)(implicit ctx: AppContext) = minHeight(v)
  /** Height is at most v */
  def maxHeight(v: Int)(implicit ctx: AppContext) = MediaQuery(displayMetrics.heightPixels <= v)
  /** Same as maxHeight(v) */
  def lowerThan(v: Int)(implicit ctx: AppContext) = maxHeight(v)

  /** Both sides are at least v */
  def minSide(v: Int)(implicit ctx: AppContext) = minWidth(v) & minHeight(v)
  /** Same as minSide(v) */
  def biggerThan(v: Int)(implicit ctx: AppContext) = minSide(v)
  /** Both sides are at most v */
  def maxSide(v: Int)(implicit ctx: AppContext) = maxWidth(v) & maxHeight(v)
  /** Same as maxSide(v) */
  def smallerThan(v: Int)(implicit ctx: AppContext) = maxSide(v)
}

private[macroid] trait MediaQueries
  extends DensityQueries
  with OrientationQueries
  with DisplayUnits
  with SizeQueries {

  implicit class RichOption[A](o: Option[A]) {
    def |[B >: A](alternative: Option[B]) = o orElse alternative
    def |[B >: A](default: B) = o getOrElse default
  }
}

object MediaQueries extends MediaQueries
