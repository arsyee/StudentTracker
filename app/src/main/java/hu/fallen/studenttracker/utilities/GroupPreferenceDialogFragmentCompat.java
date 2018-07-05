package hu.fallen.studenttracker.utilities;

import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class GroupPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private static Group[] mGroups;

    @Override
    public void onDialogClosed(boolean positiveResult) {
        Timber.d("called");
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
        RadioGroup studentSelector = view.findViewById(R.id.student_selector);
        if (mGroups != null) {
            for (Group g : mGroups) {
                // builder.append(g.mID).append(" - ").append(g.mName).append("\n");
                // textView.setText(builder.toString());
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(String.format("%s (%d)", g.name, g.count));
                radioButton.setTag(g.id);
                studentSelector.addView(radioButton);
            }
        }
        Timber.d("called");
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
