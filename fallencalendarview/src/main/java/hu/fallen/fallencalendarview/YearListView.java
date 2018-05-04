package hu.fallen.fallencalendarview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

class YearListView extends android.support.v7.widget.AppCompatTextView {
    public YearListView(Context context) {
        this(context, null);
    }

    public YearListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YearListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public YearListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        // TODO do something with defStyleRes
        setText("yearList");
    }
}
