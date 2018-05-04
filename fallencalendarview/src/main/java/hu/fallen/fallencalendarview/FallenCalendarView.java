package hu.fallen.fallencalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FallenCalendarView extends ConstraintLayout
        implements ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private final ScaleGestureDetector mScaleDetector;
    private final GestureDetector mGestureDetector;

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
        mGestureDetector = new GestureDetector(context, this);
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
        boolean result = mScaleDetector.onTouchEvent(event);
        if (!mScaleDetector.isInProgress()) {
            result = mGestureDetector.onTouchEvent(event);
        }
        return result || super.onTouchEvent(event);
    }

    // Implementation of ScaleGestureDetector.OnScaleGestureListener

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

    // Implementation of GestureDetector.OnGestureListener

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Timber.d("onFling: %f, %f, %s, %s", velocityX, velocityY, e1, e2);

        final int SWIPE_THRESHOLD = 100;
        final int SWIPE_VELOCITY_THRESHOLD = 100;

        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        return onSwipeRight();
                    } else {
                        return onSwipeLeft();
                    }
                }
            }
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    return onSwipeBottom();
                } else {
                    return onSwipeTop();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private boolean onSwipeRight() {
        mCalendar.add(getCalendarField(viewLevel), 1);
        onChanged();
        return true;
    }

    private boolean onSwipeLeft() {
        mCalendar.add(getCalendarField(viewLevel), -1);
        onChanged();
        return true;
    }

    private boolean onSwipeBottom() {
        return false;
    }

    private boolean onSwipeTop() {
        return false;
    }

    private int getCalendarField(ViewLevel viewLevel) {
        switch (viewLevel) {
            case yearList:
                return Calendar.YEAR;
            case year:
                return Calendar.YEAR;
            case month:
                return Calendar.MONTH;
            case week:
                return Calendar.WEEK_OF_YEAR;
            case day:
                return Calendar.DAY_OF_MONTH;
        }
        return Calendar.DAY_OF_MONTH;
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
