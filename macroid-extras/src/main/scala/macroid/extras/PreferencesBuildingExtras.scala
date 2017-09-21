package macroid.extras

import android.preference.{Preference, PreferenceFragment}

object PreferencesBuildingExtra {

  def connect[W <: Preference](preference: String)(
      implicit root: RootPreferencesFragment): Option[W] =
    Option(root.fragment.findPreference(preference).asInstanceOf[W])

}

case class RootPreferencesFragment(fragment: PreferenceFragment, preferenceResId: Int) {
  fragment.addPreferencesFromResource(preferenceResId)
}
