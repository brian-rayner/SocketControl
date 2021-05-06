package uk.co.brayner.socketcontrol.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import uk.co.brayner.socketcontrol.R;

public class SettingsFragment extends PreferenceFragmentCompat
{

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
  {
    setPreferencesFromResource(R.xml.root_preferences, rootKey);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set listener on changes to the tariff selection.  If Octopus GO
    // is selected, show the options for GO, otherwise hide them.

    ListPreference pref = findPreference("tariff");
    pref.setOnPreferenceChangeListener((preference, newValue) ->
    {
      showOptions ((String) newValue);
      return true;
    });

    // SHow options according to current tariff

    showOptions(pref.getValue());
  }

  private void showOptions (String tariff)
  {
    Preference pref_duration = findPreference("go_duration");
    Preference pref_start = findPreference("go_start_time");

    if (tariff.equals("go"))
    {
      pref_duration.setVisible(true);
      pref_start.setVisible(true);
    } else
    {
      pref_duration.setVisible(false);
      pref_start.setVisible(false);
    }
  }
}