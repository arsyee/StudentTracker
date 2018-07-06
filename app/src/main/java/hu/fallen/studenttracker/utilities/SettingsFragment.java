package hu.fallen.studenttracker.utilities;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import hu.fallen.studenttracker.misc.IDs;
import hu.fallen.studenttracker.utilities.GroupPreferenceDialogFragmentCompat.Group;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private Preference mPreference = null;

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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Timber.d("onCreatePreferences");
        addPreferencesFromResource(R.xml.pref_settings);

        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0; i < screen.getPreferenceCount(); ++i) {
            Preference p = screen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                setPreferenceSummary(p, screen.getSharedPreferences().getString(p.getKey(), null));
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
        if (p instanceof GroupPreference) {
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
        if (preference instanceof GroupPreference) {
            getActivity().getLoaderManager().initLoader(IDs.LOADER_ID_GROUPS_FOR_DIALOG, null, this);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case IDs.PERMISSION_REQUEST_READ_CONTACTS_ID:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader");
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, IDs.PERMISSION_REQUEST_READ_CONTACTS_ID);
            return null;
        } else {
            String[] PROJECTION = {
                    ContactsContract.Groups._ID,
                    ContactsContract.Groups.TITLE,
                    ContactsContract.Groups.SUMMARY_COUNT
            };
            CursorLoaderWrapper cursorLoader = new CursorLoaderWrapper(
                    this.getContext(),
                    ContactsContract.Groups.CONTENT_SUMMARY_URI,
                    PROJECTION,
                    null,
                    null,
                    null
            );
            cursorLoader.payload = args;
            return cursorLoader;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            Timber.e("onLoadFinished: cursor is null.");
            onLoaderReset(loader);
            return;
        }
        Timber.d("onLoadFinished: %d", loader.getId());
        Group[] groups = new Group[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); ++i) {
            cursor.moveToPosition(i);
            Group group = new Group(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getInt(2));
            groups[i] = group;
        }
        if (loader.getId() == IDs.LOADER_ID_GROUPS_FOR_DIALOG) {
            if (mPreference instanceof GroupPreference) {
                GroupPreferenceDialogFragmentCompat dialogFragment = GroupPreferenceDialogFragmentCompat.newInstance(mPreference.getKey(), groups);
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
            for (Group g : groups) {
                if (g.id.equals(value)) groupName = g.name;
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
