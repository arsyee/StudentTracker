package hu.fallen.studenttracker.utilities;

import android.Manifest;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class GroupPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
                                                implements LoaderManager.LoaderCallbacks<String> {
    private static final int PERMISSION_REQUEST_WRITE_CONTACTS_ID = 273;
    private static Group[] mGroups;
    private View mView = null;

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult || mView == null || !(getPreference() instanceof GroupPreference)) {
            Timber.d("called : %s, %s, %s", positiveResult, mView, getPreference());
            return;
        }
        RadioGroup studentSelector = mView.findViewById(R.id.student_selector);
        GroupPreference preference = (GroupPreference) getPreference();
        for (int i = 0; i < studentSelector.getChildCount(); ++i) {
            if (!(studentSelector.getChildAt(i) instanceof RadioButton)) continue;
            RadioButton radioButton = (RadioButton) studentSelector.getChildAt(i);
            if (radioButton.isChecked()) {
                preference.setGroup((String) radioButton.getTag());
                Timber.d("Saving %s", radioButton.getTag());
                return;
            }
        }
        Timber.d("Seems nothing is selected...");
    }

    public static GroupPreferenceDialogFragmentCompat newInstance(String key, Group[] groups) {
        mGroups = groups;

        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        GroupPreferenceDialogFragmentCompat fragment = new GroupPreferenceDialogFragmentCompat();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onBindDialogView(final View view) {
        super.onBindDialogView(view);
        mView = view;
        RadioGroup studentSelector = view.findViewById(R.id.student_selector);
        if (studentSelector == null) {
            Timber.e("RadioGroup not found");
            return;
        }
        String selected = null;
        DialogPreference preference = getPreference();
        if (preference instanceof GroupPreference) {
            selected = ((GroupPreference) preference).getGroup();
            Timber.d("Selected value on load: %s", selected);
        }
        if (mGroups != null) {
            for (Group g : mGroups) {
                // builder.append(g.mID).append(" - ").append(g.mName).append("\n");
                // textView.setText(builder.toString());
                RadioButton radioButton = new RadioButton(getContext());
                studentSelector.addView(radioButton);
                radioButton.setText(String.format("%s: %s (%d)", g.id, g.name, g.count));
                radioButton.setTag(g.id);
                if (g.id.equals(selected)) {
                    radioButton.setChecked(true);
                }
            }
        }

        final Button createNewGroup = view.findViewById(R.id.create_new_group);
        final LinearLayout newLayout = view.findViewById(R.id.new_layout);
        final EditText newGroupName = view.findViewById(R.id.new_group_name);
        final Button newGroupOk = view.findViewById(R.id.new_group_ok);
        createNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newLayout.setVisibility(View.VISIBLE);
                createNewGroup.setVisibility(View.GONE);
            }
        });
        newGroupOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGroup.setVisibility(View.VISIBLE);
                newLayout.setVisibility(View.GONE);
                Timber.d("OK Clicked: %s", newGroupName.getText());
                if (newGroupName.getText().toString().length() > 0) {
                    Timber.d("So we create a Loader...");
                    Bundle args = new Bundle();
                    args.putString("newGroup", newGroupName.getText().toString());
                    if (getActivity().getLoaderManager().getLoader(1) != null && getActivity().getLoaderManager().getLoader(1).isStarted()) {
                        Timber.d("Loader is already running...");
                        getActivity().getLoaderManager().destroyLoader(1);
                    }
                    getActivity().getLoaderManager().initLoader(1, args, GroupPreferenceDialogFragmentCompat.this).forceLoad();
                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        Timber.d("This is where the Loader is created...");
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Permission denied...");
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_WRITE_CONTACTS_ID);
            return null;
        } else {
            return new AsyncTaskLoader<String>(this.getContext()) {
                @Override
                public String loadInBackground() {
                    Timber.d("loadInBackground called with %s", args.getString("newGroup"));
                    ContentValues values = new ContentValues();
                    values.put(ContactsContract.Groups.TITLE, args.getString("newGroup"));
                    Uri uri = GroupPreferenceDialogFragmentCompat.this.getActivity().getContentResolver().insert(
                            ContactsContract.Groups.CONTENT_URI,
                            values
                    );
                    Timber.d("Insert returned with %s", uri.toString());
                    return Long.toString(ContentUris.parseId(uri));
                }
            };
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Timber.d("Group added: %s", data);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // pass
    }

    public static class Group {
        String id;
        String name;
        int count;

        Group(String id, String name, int count) {
            this.id = id;
            this.name = name;
            this.count = count;
        }
    }
}
