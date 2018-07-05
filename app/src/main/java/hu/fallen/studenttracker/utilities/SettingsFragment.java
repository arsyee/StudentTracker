package hu.fallen.studenttracker.utilities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import hu.fallen.studenttracker.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private GroupPreferenceDialogFragmentCompat.Group[] mGroups = null;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof GroupPreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            mGroups = new GroupPreferenceDialogFragmentCompat.Group[]{new GroupPreferenceDialogFragmentCompat.Group("Group 1", "ID1")};
            dialogFragment = GroupPreferenceDialogFragmentCompat.newInstance(preference.getKey(), mGroups);
        }

        // If it was one of our cutom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
