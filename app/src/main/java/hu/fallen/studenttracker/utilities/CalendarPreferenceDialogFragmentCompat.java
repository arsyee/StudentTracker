package hu.fallen.studenttracker.utilities;

import android.app.LoaderManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import hu.fallen.studenttracker.R;
import hu.fallen.studenttracker.misc.IDs;
import hu.fallen.studenttracker.model.Calendar;
import hu.fallen.studenttracker.model.CalendarModel;
import timber.log.Timber;

public class CalendarPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
                                                implements LoaderManager.LoaderCallbacks<String> {
    private View mView = null;
    private CalendarModel mCalendarModel = null;

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
                preference.setCalendar((String) radioButton.getTag());
                Timber.d("Saving %s", radioButton.getTag());
                return;
            }
        }
        Timber.d("Seems nothing is selected...");
    }

    public static CalendarPreferenceDialogFragmentCompat newInstance(String key) {
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
        final RadioGroup studentSelector = view.findViewById(R.id.student_selector);
        if (studentSelector == null) {
            Timber.e("RadioGroup not found");
            return;
        }
        final String selected;
        DialogPreference preference = getPreference();
        if (preference instanceof CalendarPreference) {
            selected = ((CalendarPreference) preference).getCalendar();
            Timber.d("Selected value on load: %s", selected);
        } else {
            selected = null;
        }
        mCalendarModel = ViewModelProviders.of(getActivity()).get(CalendarModel.class);
        mCalendarModel.getCalendars().observe(this, new Observer<List<Calendar>>() {
            @Override
            public void onChanged(@Nullable List<Calendar> calendars) {
                String account = null;
                for (Calendar calendar : mCalendarModel.getCalendars().getValue()) {
                    if (!calendar.get(Calendar.Data.ACCOUNT_NAME).equals(account)) {
                        TextView textView = new TextView(getContext());
                        studentSelector.addView(textView);
                        textView.setText(String.format("%s (%s)", calendar.get(Calendar.Data.ACCOUNT_NAME), calendar.get(Calendar.Data.ACCOUNT_TYPE)));
                        account = calendar.get(Calendar.Data.ACCOUNT_NAME);
                    }
                    // builder.append(g.mID).append(" - ").append(g.mName).append("\n");
                    // textView.setText(builder.toString());
                    RadioButton radioButton = new RadioButton(getContext());
                    studentSelector.addView(radioButton);
                    radioButton.setText(String.format("%s: %s", calendar.get(Calendar.Data._ID), calendar.get(Calendar.Data.CALENDAR_DISPLAY_NAME)));
                    radioButton.setButtonTintList(new ColorStateList(
                            new int[][]{ new int[]{android.R.attr.state_enabled} },
                            new int[] { Integer.parseInt(calendar.get(Calendar.Data.COLOR)) }
                    ));
                    radioButton.setTag(calendar.get(Calendar.Data._ID));
                    if (calendar.get(Calendar.Data._ID).equals(selected)) {
                        radioButton.setChecked(true);
                    }
                }
            }
        });

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
}
