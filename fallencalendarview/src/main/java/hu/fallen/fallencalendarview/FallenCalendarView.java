package hu.fallen.fallencalendarview;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
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
    @BindView(R2.id.bt_today) Button btToday;

    @BindView(R2.id.yv_year) WebView wvYear;

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
        btToday.setOnClickListener(new TodayButtonOnclickListener());
        int fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        Timber.d("WebView font size changed from %d to %d", wvYear.getSettings().getDefaultFontSize(), fontSize);
        wvYear.getSettings().setDefaultFontSize(fontSize);
        wvYear.getSettings().setStandardFontFamily("sans-serif-condensed");

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
        int columnNum = 3;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnNum = 6;
        }
        wvYear.loadData(getYearCalendar(mCalendar, columnNum, getContext().getResources().getStringArray(R.array.days), getContext().getResources().getStringArray(R.array.months)), "text/html", null);
    }

    private static String getYearCalendar(Calendar calendar, int columnNum, String[] dayNames, String[] monthNames) {
        int[] monthLength = {31, 28, 31,
                             30, 31, 30,
                             31, 31, 30,
                             31, 30, 31};
        if (calendar.getActualMaximum(Calendar.DAY_OF_YEAR) > 365) monthLength[1]++;

        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        int firstDayOfYear = firstDayOfYear(calendar);

        String header;
        {
            StringBuilder hb = new StringBuilder();
            for (int i = 0; i < 7; ++i) {
                int day = (firstDayOfWeek - 1 + i) % 7 + 1;
                hb.append("<td>").append(dayNames[day].charAt(0)).append("</td>");
            }
            header = hb.toString();
        }

        int currentDayOfWeek = firstDayOfYear;

        StringBuilder sb = new StringBuilder();
        sb.append("<table width='100%' height='100%'>");
        for (int month = 0; month < 12; ++month) {
            if (month % columnNum == 0) sb.append("<tr width='").append(100/columnNum).append("%'>");
            sb.append("<td><font size='5'>");
            sb.append(monthNames[month]);
            sb.append("</font><table><tr>").append(header).append("</tr>");
            int currentDayOfMonth = 1;
            while (currentDayOfMonth <= monthLength[month]) {
                sb.append("<tr>");
                for (int i = 0; i < 7; ++i) {
                    sb.append("<td>");
                    int day = (firstDayOfWeek - 1 + i) % 7 + 1;
                    if (currentDayOfMonth <= monthLength[month] && day == currentDayOfWeek) {
                        sb.append(currentDayOfMonth);
                        ++currentDayOfMonth;
                        currentDayOfWeek = currentDayOfWeek % 7 + 1;
                    }
                    sb.append("</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</table></td>");
            if (month % columnNum == columnNum - 1) sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    static int firstDayOfYear(Calendar calendar) {
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        final int MAX = 53 * 7;
        return (MAX + dayOfWeek - dayOfYear) % 7 + 1;
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

    private class TodayButtonOnclickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            mCalendar = Calendar.getInstance();
            onChanged();
        }
    }
}
