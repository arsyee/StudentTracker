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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FallenCalendarView extends ConstraintLayout {

    private ViewLevel viewLevel = ViewLevel.day;

    @BindView(R2.id.debug) TextView tvDebugView;
    @BindView(R2.id.tv_viewlevel) TextView tvViewLevel;
    @BindView(R2.id.tv_year) TextView tvYear;
    @BindView(R2.id.tv_month) TextView tvMonth;
    @BindView(R2.id.tv_day) TextView tvDay;
    @BindView(R2.id.bt_today) Button btToday;

    @BindView(R2.id.yv_year) YearView wvYear;

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

    // TODO Configuration options: colors, grid, fontSize
    // TODO Generalize to 2D ViewPager: swipe left-right to page, pinch to go in and out

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

        mCalendar = Calendar.getInstance();

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
        wvYear.setCalendar(mCalendar);
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
