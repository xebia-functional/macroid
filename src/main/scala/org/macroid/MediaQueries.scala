package org.macroid

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import scalaz.Monoid

case class MediaQuery(b: Boolean) {
  def ?[A](v: A) = if (b) Some(v) else None
  def ?![A: Monoid](v: A) = if (b) v else implicitly[Monoid[A]].zero
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

  def minWidth(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.widthPixels >= v)
  def maxWidth(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.widthPixels <= v)

  def minHeight(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.heightPixels >= v)
  def maxHeight(v: Int)(implicit ctx: Context) = MediaQuery(displayMetrics.heightPixels <= v)
}

object MediaQueries extends MediaQueries

object MQ extends MediaQueries
