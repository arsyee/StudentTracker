package hu.fallen.studenttracker.utilities;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import hu.fallen.studenttracker.R;
import hu.fallen.studenttracker.misc.IDs;
import timber.log.Timber;

public class CalendarPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
                                                implements LoaderManager.LoaderCallbacks<String> {
    private static Calendar[] mCalendars;
    private View mView = null;

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult || mView == null || !(getPreference() instanceof CalendarPreference)) {
            Timber.d("called : %s, %s, %s", positiveResult, mView, getPreference());
            return;
        }
        RadioGroup studentSelector = mView.findViewById(R.id.student_selector);
        CalendarPreference preference = (CalendarPreference) getPreference();
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

    public static CalendarPreferenceDialogFragmentCompat newInstance(String key, Calendar[] calendars) {
        mCalendars = calendars;

        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        CalendarPreferenceDialogFragmentCompat fragment = new CalendarPreferenceDialogFragmentCompat();
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
        if (preference instanceof CalendarPreference) {
            selected = ((CalendarPreference) preference).getGroup();
            Timber.d("Selected value on load: %s", selected);
        }
        if (mCalendars != null) {
            String account = null;
            for (Calendar g : mCalendars) {
                if (!g.account_name.equals(account)) {
                    TextView textView = new TextView(getContext());
                    studentSelector.addView(textView);
                    textView.setText(String.format("%s (%s)", g.account_name, g.account_type));
                    account = g.account_name;
                }
                // builder.append(g.mID).append(" - ").append(g.mName).append("\n");
                // textView.setText(builder.toString());
                RadioButton radioButton = new RadioButton(getContext());
                studentSelector.addView(radioButton);
                radioButton.setText(String.format("%s: %s", g.id, g.display_name));
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
                    if (getActivity().getLoaderManager().getLoader(IDs.LOADER_ID_CREATE_GROUP) != null && getActivity().getLoaderManager().getLoader(IDs.LOADER_ID_CREATE_GROUP).isStarted()) {
                        Timber.d("Loader is already running...");
                        getActivity().getLoaderManager().destroyLoader(IDs.LOADER_ID_CREATE_GROUP);
                    }
                    // getActivity().getLoaderManager().initLoader(IDs.LOADER_ID_CREATE_GROUP, args, CalendarPreferenceDialogFragmentCompat.this).forceLoad();
                    // TODO: create Calendar instead of group!
                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        Timber.d("This is where the Loader is created...");
        return new AsyncTaskLoader<String>(this.getContext()) {
            @Override
            public String loadInBackground() {
                Timber.d("loadInBackground called with %s", args.getString("newGroup"));
                ContentValues values = new ContentValues();
                values.put(ContactsContract.Groups.TITLE, args.getString("newGroup"));
                Uri uri = CalendarPreferenceDialogFragmentCompat.this.getActivity().getContentResolver().insert(
                        ContactsContract.Groups.CONTENT_URI,
                        values
                );
                Timber.d("Insert returned with %s", uri.toString());
                return Long.toString(ContentUris.parseId(uri));
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Timber.d("Calendar added: %s", data);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // pass
    }

    public static class Calendar {
        String id;
        String account_name;
        String account_type;
        String display_name;

        Calendar(String id, String account_name, String account_type, String display_name) {
            this.id = id;
            this.account_name = account_name;
            this.account_type = account_type;
            this.display_name = display_name;
        }
    }
}
