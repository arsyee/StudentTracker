package hu.fallen.studenttracker.utilities;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import hu.fallen.studenttracker.utilities.GroupPreferenceDialogFragmentCompat.Group;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PERMISSION_REQUEST_READ_CONTACTS_ID = 724;

    private Preference mPreference = null;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Timber.d("onCreatePreferences");
        addPreferencesFromResource(R.xml.pref_settings);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        getActivity().getLoaderManager().destroyLoader(0);
        mPreference = preference;
        if (preference instanceof GroupPreference) {
            getActivity().getLoaderManager().initLoader(0, null, this);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS_ID:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onCreateLoader(0, null);
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader");
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS_ID);
            return null;
        } else {
            String[] PROJECTION = {
                    ContactsContract.Groups._ID,
                    ContactsContract.Groups.TITLE,
                    ContactsContract.Groups.SUMMARY_COUNT
            };
            return new CursorLoader(
                    this.getContext(),
                    ContactsContract.Groups.CONTENT_SUMMARY_URI,
                    PROJECTION,
                    null,
                    null,
                    null
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            Timber.e("onLoadFinished: cursor is null.");
            onLoaderReset(loader);
            return;
        }
        Timber.d("onLoadFinished");
        Group[] groups = new Group[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); ++i) {
            cursor.moveToPosition(i);
            Group group = new Group(String.format("%s (%d)", cursor.getString(1), cursor.getInt(2)),
                    cursor.getString(0));
            groups[i] = group;
        }
        if (mPreference instanceof GroupPreference) {
            GroupPreferenceDialogFragmentCompat dialogFragment = GroupPreferenceDialogFragmentCompat.newInstance(mPreference.getKey(), groups);
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // there is nothing to do here
    }
}
