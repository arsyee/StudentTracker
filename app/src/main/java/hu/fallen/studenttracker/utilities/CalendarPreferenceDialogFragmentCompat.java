package hu.fallen.studenttracker.utilities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import hu.fallen.studenttracker.model.GoogleCalendar;
import hu.fallen.studenttracker.model.GoogleCalendarModel;
import timber.log.Timber;

public class CalendarPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private View mView = null;
    private GoogleCalendarModel mGoogleCalendarModel = null;

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
        mGoogleCalendarModel = ViewModelProviders.of(getActivity()).get(GoogleCalendarModel.class);
        mGoogleCalendarModel.getCalendars().observe(this, new Observer<List<GoogleCalendar>>() {
            @Override
            public void onChanged(@Nullable List<GoogleCalendar> googleCalendars) {
                String account = null;
                for (GoogleCalendar googleCalendar : mGoogleCalendarModel.getCalendars().getValue()) {
                    if (!googleCalendar.get(GoogleCalendar.Data.ACCOUNT_NAME).equals(account)) {
                        TextView textView = new TextView(getContext());
                        studentSelector.addView(textView);
                        textView.setText(String.format("%s (%s)", googleCalendar.get(GoogleCalendar.Data.ACCOUNT_NAME), googleCalendar.get(GoogleCalendar.Data.ACCOUNT_TYPE)));
                        account = googleCalendar.get(GoogleCalendar.Data.ACCOUNT_NAME);
                    }
                    // builder.append(g.mID).append(" - ").append(g.mName).append("\n");
                    // textView.setText(builder.toString());
                    RadioButton radioButton = new RadioButton(getContext());
                    studentSelector.addView(radioButton);
                    radioButton.setText(String.format("%s: %s", googleCalendar.get(GoogleCalendar.Data._ID), googleCalendar.get(GoogleCalendar.Data.CALENDAR_DISPLAY_NAME)));
                    radioButton.setButtonTintList(new ColorStateList(
                            new int[][]{ new int[]{android.R.attr.state_enabled} },
                            new int[] { Integer.parseInt(googleCalendar.get(GoogleCalendar.Data.COLOR)) }
                    ));
                    radioButton.setTag(googleCalendar.get(GoogleCalendar.Data._ID));
                    if (googleCalendar.get(GoogleCalendar.Data._ID).equals(selected)) {
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
                    Snackbar.make(v, "TODO: create new calendar!", Snackbar.LENGTH_LONG).show();
                    // TODO: create GoogleCalendar instead of group!
                }
            }
        });
    }

}
