package hu.fallen.studenttracker.utilities;

import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
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
        TextView textView = view.findViewById(R.id.group_text);
        StringBuilder builder = new StringBuilder();
        if (mGroups == null) {
            builder.append("Groups were not specified!");
        } else {
            for (Group g : mGroups) {
                builder.append(g.mID).append(" - ").append(g.mName).append("\n");
            }
        }
        textView.setText(builder.toString());
        Timber.d("called");
    }

    public static class Group {
        public String mName;
        public String mID;

        public Group(String name, String id) {
            mName = name;
            mID = id;
        }
    }
}
