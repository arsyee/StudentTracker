package hu.fallen.studenttracker.utilities;

import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;

import timber.log.Timber;

public class GroupPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    @Override
    public void onDialogClosed(boolean positiveResult) {
        Timber.d("called");
    }

    public static GroupPreferenceDialogFragmentCompat newInstance(String key) {

        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        GroupPreferenceDialogFragmentCompat fragment = new GroupPreferenceDialogFragmentCompat();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Timber.d("called");
    }
}
