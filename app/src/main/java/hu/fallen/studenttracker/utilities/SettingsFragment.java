package hu.fallen.studenttracker.utilities;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import hu.fallen.studenttracker.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
    }
}
