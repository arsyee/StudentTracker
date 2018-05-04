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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FallenCalendarView extends ConstraintLayout implements ScaleGestureDetector.OnScaleGestureListener {

    private final ScaleGestureDetector mScaleDetector;
    private ViewLevel viewLevel = ViewLevel.day;

    @BindView(R2.id.debug) TextView tvDebugView;
    @BindView(R2.id.tv_viewlevel) TextView tvViewLevel;
    @BindView(R2.id.tv_year) TextView tvYear;
    @BindView(R2.id.tv_month) TextView tvMonth;
    @BindView(R2.id.tv_day) TextView tvDay;

    private Calendar mCalendar;

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

        ButterKnife.bind(this, this);

        mCalendar = Calendar.getInstance();

        mScaleDetector = new ScaleGestureDetector(context, this);
        onChanged();
    }

    private void onChanged() {
        switch (viewLevel) {
            default:
                tvDebugView.setText(String.format(Locale.getDefault(), "viewLevel: %s, date: %2$tY-%2$tm-%2$td", viewLevel, mCalendar));
                break;
        }
        tvYear.setText(String.format(Locale.getDefault(), "%tY", mCalendar));
        tvMonth.setText(String.format(Locale.getDefault(), "%tm", mCalendar));
        tvDay.setText(String.format(Locale.getDefault(), "%td", mCalendar));
        tvViewLevel.setText(String.format(Locale.getDefault(), "%s", viewLevel));
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
        onChanged();
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
