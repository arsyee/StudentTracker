package hu.fallen.studenttracker.utilities;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class GroupPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
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
    protected void onBindDialogView(View view) {
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
    }

    public static class Group {
        private String id;
        private String name;
        private int count;

        Group(String id, String name, int count) {
            this.id = id;
            this.name = name;
            this.count = count;
        }
    }
}
