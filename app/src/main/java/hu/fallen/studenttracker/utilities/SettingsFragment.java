package hu.fallen.studenttracker.utilities;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import hu.fallen.studenttracker.misc.Config;
import hu.fallen.studenttracker.misc.IDs;
import hu.fallen.studenttracker.model.GoogleCalendar;
import hu.fallen.studenttracker.model.GoogleCalendarModel;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean mPermissionsGranted = false;
    private GoogleCalendarModel mGoogleCalendarModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean permissionsGranted = Config.checkPermissions(this.getActivity());
        if (mPermissionsGranted != permissionsGranted) {
            mPermissionsGranted = permissionsGranted;
            refreshPreferences();
        }
        if (!mPermissionsGranted) {
            Snackbar snackbar = Snackbar.make(getView(), R.string.permission_explanation, Snackbar.LENGTH_INDEFINITE);
            ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(10);
            snackbar.setAction(R.string.grant, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR,
                    }, IDs.PERMISSION_REQUEST_CONTACTS_ID);
                }
            });
            snackbar.show();
        } else if (getActivity() != null) {
            mGoogleCalendarModel = ViewModelProviders.of(getActivity()).get(GoogleCalendarModel.class);
            mGoogleCalendarModel.getCalendars().observe(this, new Observer<List<GoogleCalendar>>() {
                @Override
                public void onChanged(@Nullable List<GoogleCalendar> googleCalendars) {
                    refreshPreferences();
                }
            });
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Timber.d("onCreatePreferences");
        addPreferencesFromResource(R.xml.pref_settings);

        mPermissionsGranted = Config.checkPermissions(this.getActivity());
        refreshPreferences();
    }

    private void refreshPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0; i < screen.getPreferenceCount(); ++i) {
            Preference p = screen.getPreference(i);
            if (!mPermissionsGranted) {
                p.setEnabled(false);
            } else { // don't try to set summary without permissions
                p.setEnabled(true);
                if (!(p instanceof CheckBoxPreference)) {
                    setPreferenceSummary(p, screen.getSharedPreferences().getString(p.getKey(), null));
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        Timber.d("onSharedPreferenceChanged called: %s -> %s", key, p);
        if (null != p) {
            if (!(p instanceof CheckBoxPreference)) {
                setPreferenceSummary(p, sharedPreferences.getString(p.getKey(), null));
            }
        }
    }

    private void setPreferenceSummary(Preference p, String value) {
        if (p instanceof CalendarPreference) {
            if (mGoogleCalendarModel != null) {
                for (GoogleCalendar googleCalendar : mGoogleCalendarModel.getCalendars().getValue()) {
                    if (googleCalendar.get(GoogleCalendar.Data._ID).equals(value)) {
                        String key = p.getKey();
                        String calendarName = googleCalendar.get(GoogleCalendar.Data.CALENDAR_DISPLAY_NAME);
                        Timber.d("We should have everything to update: %s, %s, %s", key, value, calendarName);
                        p.setSummary(calendarName);
                    }
                }
            }
        // } else {
            // String summary = *func(p, value); // calculate the summary
            // p.setSummary(); // and set it
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof CalendarPreference) {
            CalendarPreferenceDialogFragmentCompat dialogFragment = CalendarPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),"android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case IDs.PERMISSION_REQUEST_CONTACTS_ID:
                if (Config.checkPermissions(getContext())) {
                    mPermissionsGranted = true;
                    refreshPreferences();
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
