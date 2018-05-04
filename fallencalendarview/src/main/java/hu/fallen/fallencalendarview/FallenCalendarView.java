package hu.fallen.fallencalendarview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;

public class FallenCalendarView extends FrameLayout {

    private int viewLevel = 0;
    private View child;

    public FallenCalendarView(@NonNull Context context) {
        this(context, null);
    }

    public FallenCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.fallenCalendarViewStyle);
    }

    public FallenCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FallenCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        // TODO LOLLIPOP super should be called to propagate defStyleRes
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FallenCalendarView, defStyleAttr, defStyleRes);
        try {
            viewLevel = a.getInteger(R.styleable.FallenCalendarView_viewLevel, 0);
        } finally {
            a.recycle();
        }
        onViewLevelChanged(context);
    }

    private void onViewLevelChanged(Context context) {
        removeAllViews();
        switch (ViewLevel.fromId(viewLevel)) {
            case yearList:
                child = new YearListView(context);
                break;
            default:
                TextView placeholder = new TextView(context);
                placeholder.setText(String.format(Locale.getDefault(), "viewLevel: %d", viewLevel));
                child = placeholder;
                break;
        }
        addView(child);
    }

    private enum ViewLevel {
        yearList(0), year(1), month(2), week(3), day(4);

        private final int id;

        ViewLevel(int id) { this.id = id; }

        static ViewLevel fromId(int id) {
            for (ViewLevel level : values()) {
                if (level.id == id) return level;
            }
            throw new IllegalArgumentException();
        }
    }
}
