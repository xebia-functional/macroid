package macroid.support

import android.view.View
import android.os.Bundle
import android.app.Activity
import macroid.util.SafeCast

sealed trait Fragment[-F] {
  def view: F ⇒ View
  def activity: F ⇒ Activity
  def setArguments(f: F, b: Bundle): Unit
}

private[macroid] trait ModernFragment {
  import android.app.{ Fragment ⇒ F }
  implicit object modernFragment extends Fragment[F] {
    def view = _.getView
    def activity = _.getActivity
    def setArguments(f: F, b: Bundle) = f.setArguments(b)
  }
}

private[macroid] trait LegacyFragment {
  import android.support.v4.app.{ Fragment ⇒ F }
  implicit object legacyFragment extends Fragment[F] {
    def view = _.getView
    def activity = _.getActivity
    def setArguments(f: F, b: Bundle) = f.setArguments(b)
  }
}

object Fragment extends ModernFragment with LegacyFragment

sealed trait FragmentApi[-F, M, A] {
  def fragmentManager: F ⇒ M
  def activityManager: A ⇒ M
  def findFragmentByTag[F1 <: F](m: M, t: String): Option[F1]
  def addFragment(m: M, i: Int, t: String, f: F): Unit
}

private[macroid] trait ModernFragmentApi {
  import android.app.{ Fragment ⇒ F, FragmentManager ⇒ M, Activity ⇒ A }
  implicit object modernFragmentApi extends FragmentApi[F, M, A] {
    def fragmentManager = _.getFragmentManager
    def activityManager = _.getFragmentManager
    def findFragmentByTag[F1 <: F](m: M, t: String) = SafeCast[F, F1](m.findFragmentByTag(t))
    def addFragment(m: M, i: Int, t: String, f: F) = m.beginTransaction().add(i, f, t).commit()
  }
}

private[macroid] trait LegacyFragmentApi {
  import android.support.v4.app.{ Fragment ⇒ F, FragmentManager ⇒ M, FragmentActivity ⇒ A }
  implicit object legacyFragmentApi extends FragmentApi[F, M, A] {
    def fragmentManager = _.getChildFragmentManager
    def activityManager = _.getSupportFragmentManager
    def findFragmentByTag[F1 <: F](m: M, t: String) = SafeCast[F, F1](m.findFragmentByTag(t))
    def addFragment(m: M, i: Int, t: String, f: F) = m.beginTransaction().add(i, f, t).commit()
  }
}

object FragmentApi extends ModernFragmentApi with LegacyFragmentApi
