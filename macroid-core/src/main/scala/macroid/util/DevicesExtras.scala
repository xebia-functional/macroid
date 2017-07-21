package macroid.util

import android.os._
import macroid.ContextWrapper
import macroid.FullDsl._

import scala.language.postfixOps

object DeviceMediaQueries {

  def tablet(implicit ctx: ContextWrapper) = widerThan(720 dp)

  def landscapeTablet(implicit ctx: ContextWrapper) =
    widerThan(720 dp) & landscape

  def portraitTablet(implicit ctx: ContextWrapper) =
    widerThan(720 dp) & portrait

}

object DeviceVersion {

  sealed trait SDKVersion {
    def version: Int

    def ==(a: SDKVersion) = this.version == a.version

    def >=(a: SDKVersion) = this.version >= a.version

    def >(a: SDKVersion) = this.version > a.version

    def <=(a: SDKVersion) = this.version <= a.version

    def <(a: SDKVersion) = this.version < a.version

    def !=(a: SDKVersion) = this.version != a.version

    def isSupported: Boolean = this > CurrentVersion

    def withOpThan[A](op: ⇒ Boolean)(f: ⇒ A): Option[A] =
      if (op) Some(f) else None

    def ifEqualThen[A](f: ⇒ A): Option[A] =
      withOpThan(this == CurrentVersion)(f)

    def ifNotEqualThen[A](f: ⇒ A): Option[A] =
      withOpThan(this != CurrentVersion)(f)

    def ifSupportedThen[A](f: ⇒ A): Option[A] =
      withOpThan(this <= CurrentVersion)(f)

    def ifNotSupportedThen[A](f: ⇒ A): Option[A] =
      withOpThan(this > CurrentVersion)(f)

  }

  object SDKVersion {

    def unapply(c: SDKVersion): Int = c.version

  }

  class Version(v: Int) extends SDKVersion {
    override def version: Int = v
  }

  import Build.VERSION._
  import Build.VERSION_CODES._

  case object CurrentVersion extends Version(SDK_INT)

  case object Nougat extends Version(N)

  case object Marshmallow extends Version(M)

  case object LollipopMR1 extends Version(LOLLIPOP_MR1)

  case object Lollipop extends Version(LOLLIPOP)

  case object KitKatWatch extends Version(KITKAT_WATCH)

  case object KitKat extends Version(KITKAT)


}
