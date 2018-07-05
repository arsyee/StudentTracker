package hu.fallen.studenttracker.utilities;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import hu.fallen.studenttracker.R;

public class GroupPreference extends DialogPreference {
    public GroupPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public GroupPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GroupPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public GroupPreference(Context context) {
        this(context, null);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_group;
    }
}
