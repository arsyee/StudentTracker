package hu.fallen.fallencalendarview;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FallenCalendarView extends ConstraintLayout
        implements DatePickerDialog.OnDateSetListener, DialogInterface.OnClickListener {

    public static final int YEAR_OFFSET_START = -1;
    public static final int YEAR_OFFSET_END = 3;
    private ViewLevel viewLevel = ViewLevel.day;

    @BindView(R2.id.debug) TextView tvDebugView;
    @BindView(R2.id.tv_viewlevel) TextView tvViewLevel;
    @BindView(R2.id.tv_year) TextView tvYear;
    @BindView(R2.id.tv_month) TextView tvMonth;
    @BindView(R2.id.tv_day) TextView tvDay;
    @BindView(R2.id.bt_today) Button btToday;

    @BindView(R2.id.yv_year) YearView wvYear;

    private Calendar mCalendar;
    private int thisYear;

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
        tvDay.setOnClickListener(new DayOnClickListener());
        tvYear.setOnClickListener(new YearOnClickListener());
        btToday.setOnClickListener(new TodayButtonOnClickListener());

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

    private class TodayButtonOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            mCalendar = Calendar.getInstance();
            onChanged();
        }
    }

    private class DayOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            new DatePickerDialog(
                    FallenCalendarView.this.getContext(),
                    FallenCalendarView.this,
                    mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mCalendar.set(year, month, dayOfMonth);
        viewLevel = ViewLevel.day;
        onChanged();
    }

    private class YearOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(FallenCalendarView.this.getContext());
            builder.setTitle(String.format(Locale.getDefault(), "%d - %s", mCalendar.get(Calendar.YEAR), getResources().getString(R.string.select_year)));
            thisYear = Calendar.getInstance().get(Calendar.YEAR);
            ArrayList<String> years = new ArrayList<>();
            for (int i = YEAR_OFFSET_START; i < YEAR_OFFSET_END; ++i) {
                years.add(Integer.toString(thisYear + i));
            }
            String[] yearArray = new String[years.size()];
            yearArray = years.toArray(yearArray);
            Timber.d("Adding %d items to builder", yearArray.length);
            builder.setItems(yearArray, FallenCalendarView.this);
            builder.create().show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Timber.d("DialogInterface.OnClickListener.onClick called with: %d", which);
        mCalendar.set(Calendar.YEAR, thisYear + YEAR_OFFSET_START + which);
        viewLevel = ViewLevel.year;
        onChanged();
    }
}
