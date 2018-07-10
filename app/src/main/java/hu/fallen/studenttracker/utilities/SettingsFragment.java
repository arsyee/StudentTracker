package hu.fallen.studenttracker.utilities;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import android.widget.TextView;

import hu.fallen.studenttracker.misc.Config;
import hu.fallen.studenttracker.misc.IDs;
import hu.fallen.studenttracker.utilities.CalendarPreferenceDialogFragmentCompat.Calendar;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private Preference mPreference = null;
    private boolean mPermissionsGranted = false;

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
            Bundle args = new Bundle();
            args.putString("key", p.getKey());
            args.putString("value", value);
            getActivity().getLoaderManager().destroyLoader(IDs.LOADER_ID_GROUPS_FOR_SUMMARY);
            getActivity().getLoaderManager().initLoader(IDs.LOADER_ID_GROUPS_FOR_SUMMARY, args, this);
        } else {
            // String summary = *func(p, value); // calculate the summary
            // p.setSummary(); // and set it
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        getActivity().getLoaderManager().destroyLoader(IDs.LOADER_ID_GROUPS_FOR_DIALOG);
        mPreference = preference;
        if (preference instanceof CalendarPreference) {
            getActivity().getLoaderManager().initLoader(IDs.LOADER_ID_GROUPS_FOR_DIALOG, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader");
        String[] PROJECTION = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        };
        CursorLoaderWrapper cursorLoader = new CursorLoaderWrapper(
                this.getContext(),
                CalendarContract.Calendars.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null
        );
        cursorLoader.payload = args;
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            Timber.e("onLoadFinished: cursor is null.");
            onLoaderReset(loader);
            return;
        }
        Timber.d("onLoadFinished: %d", loader.getId());
        CalendarPreferenceDialogFragmentCompat.Calendar[] calendars = new CalendarPreferenceDialogFragmentCompat.Calendar[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); ++i) {
            cursor.moveToPosition(i);
            Calendar calendar = new CalendarPreferenceDialogFragmentCompat.Calendar(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
            calendars[i] = calendar;
        }
        if (loader.getId() == IDs.LOADER_ID_GROUPS_FOR_DIALOG) {
            if (mPreference instanceof CalendarPreference) {
                CalendarPreferenceDialogFragmentCompat dialogFragment = CalendarPreferenceDialogFragmentCompat.newInstance(mPreference.getKey(), calendars);
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(),
                        "android.support.v7.preference" +
                                ".PreferenceFragment.DIALOG");
            }
        } else if (loader.getId() == IDs.LOADER_ID_GROUPS_FOR_SUMMARY && loader instanceof CursorLoaderWrapper) {
            Bundle args = ((CursorLoaderWrapper) loader).payload;
            String key = args.getString("key");
            String value = args.getString("value");
            String groupName = "";
            for (CalendarPreferenceDialogFragmentCompat.Calendar g : calendars) {
                if (g.id.equals(value)) groupName = g.account_name;
            }

            Timber.d("We should have everything to update: %s, %s, %s", key, value, groupName);
            PreferenceScreen screen = getPreferenceScreen();
            for (int i = 0; i < screen.getPreferenceCount(); ++i) {
                Preference p = screen.getPreference(i);
                if (p.getKey().equals(key)) {
                    p.setSummary(groupName);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // there is nothing to do here
    }

    static class CursorLoaderWrapper extends CursorLoader {
        private Bundle payload;
        CursorLoaderWrapper(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
        }
    }
}
