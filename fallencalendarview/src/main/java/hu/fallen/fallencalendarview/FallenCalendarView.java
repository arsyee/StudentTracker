package hu.fallen.fallencalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FallenCalendarView extends ConstraintLayout implements ScaleGestureDetector.OnScaleGestureListener {

    private final ScaleGestureDetector mScaleDetector;
    private ViewLevel viewLevel = ViewLevel.day;
    private TextView debugView;

    private Date mCalendar;
    private DateFormat mDateFormat;

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
        Timber.d("FallenCalendarView created");
        inflate(context, R.layout.fallen_calendar_view, this);
        // TODO LOLLIPOP super should be called to propagate defStyleRes
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FallenCalendarView, defStyleAttr, defStyleRes);
        try {
            viewLevel = ViewLevel.fromId(a.getInteger(R.styleable.FallenCalendarView_viewLevel, 0));
        } finally {
            a.recycle();
        }

        debugView = findViewById(R.id.debug);

        mCalendar = new Date();
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        mScaleDetector = new ScaleGestureDetector(context, this);
        onViewLevelChanged(context);
    }

    private void onViewLevelChanged(Context context) {
        switch (viewLevel) {
            default:
                if (debugView != null) {
                    debugView.setText(String.format(Locale.getDefault(), "viewLevel: %s, date: %s", viewLevel, mDateFormat.format(mCalendar)));
                }
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // Timber.d("onScale: %f", detector.getScaleFactor());
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // Timber.d("onScaleBegin: %s", detector);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Timber.d("onScaleEnd: %f", detector.getScaleFactor());
        if (detector.getScaleFactor() > 1.0) {
            viewLevel = viewLevel.next();
        } else {
            viewLevel = viewLevel.prev();
        }
        onViewLevelChanged(this.getContext());
    }

    private enum ViewLevel {
        yearList, year, month, week, day;

        static ViewLevel fromId(int id) {
            if (id >= 0 && id < values().length) {
                return values()[id];
            } else {
                return values()[0];
            }
        }

        public ViewLevel next() {
            if (ordinal() < values().length - 1) {
                return values()[ordinal() + 1];
            } else {
                return values()[values().length - 1];
            }
        }

        public ViewLevel prev() {
            if (ordinal() > 0) {
                return values()[ordinal() - 1];
            } else {
                return values()[0];
            }
        }
    }
}
