package hu.fallen.studenttracker.utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import hu.fallen.studenttracker.R;

public class CalendarPreference extends DialogPreference {
    public CalendarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CalendarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CalendarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public CalendarPreference(Context context) {
        this(context, null);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    String getCalendar() {
        return getPersistedString(null);
    }

    void setCalendar(String calendar) {
        persistString(calendar);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        persistString(restorePersistedValue ? getPersistedString(null) : null);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_group;
    }
}
