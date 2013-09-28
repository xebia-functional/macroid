package org.macroid

import scala.language.implicitConversions
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

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

trait MediaQueries {
  private def displayMetrics(implicit ctx: Context) = {
    val display = ctx.getSystemService(Context.WINDOW_SERVICE).asInstanceOf[WindowManager].getDefaultDisplay
    val metrics = new DisplayMetrics
    display.getMetrics(metrics)
    metrics
  }

  implicit class RichOption[A](o: Option[A]) {
    def |[B >: A](alternative: Option[B]) = o orElse alternative
    def |[B >: A](default: B) = o getOrElse default
  }

  def ldpi(implicit ctx: Context) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_LOW)
  def mdpi(implicit ctx: Context) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM)
  def hdpi(implicit ctx: Context) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_HIGH)
  def xhdpi(implicit ctx: Context) = MediaQuery(displayMetrics.densityDpi == DisplayMetrics.DENSITY_XHIGH)

  implicit class Units(v: Double)(implicit ctx: Context) {
    /** Using pixels is strictly discouraged! */
    def px = v.toInt
    /** Density-independent points */
    def dp = (v * displayMetrics.density).toInt
    /** Scale-independent points */
    def sp = (v * displayMetrics.scaledDensity).toInt
  }

  /** Width is at least v */
  def minWidth(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.widthPixels >= v)
  /** Same as minWidth(v) */
  def widerThan(v: Int)(implicit ctx: Context) = minWidth(v)
  /** Width is at most v */
  def maxWidth(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.widthPixels <= v)
  /** Same as maxWidth(v) */
  def narrowerThan(v: Int)(implicit ctx: Context) = maxWidth(v)

  /** Height is at least v */
  def minHeight(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.heightPixels >= v)
  /** Same as minHeight(v) */
  def higherThan(v: Int)(implicit ctx: Context) = minHeight(v)
  /** Height is at most v */
  def maxHeight(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.heightPixels <= v)
  /** Same as maxHeight(v) */
  def lowerThan(v: Int)(implicit ctx: Context) = maxHeight(v)
}

object MediaQueries extends MediaQueries

object MQ extends MediaQueries
