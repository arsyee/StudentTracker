package hu.fallen.fallencalendarview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.webkit.WebView;

import java.util.Calendar;

import timber.log.Timber;

class MonthView extends WebView implements CalendarContentInterface {
    private Calendar mCalendar;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        // TODO do something with defStyleRes
        int fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        Timber.d("font size changed from %d to %d", getSettings().getDefaultFontSize(), fontSize);
        getSettings().setDefaultFontSize(fontSize);
        getSettings().setStandardFontFamily("sans-serif-condensed");
    }

    @Override
    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
        loadDataWithBaseURL(
                "about:blank",
                getContent(mCalendar),
                "text/html",
                null,
                null);
    }

    private String getContent(Calendar calendar) {
        return String.format("%s %tD", getClass().getSimpleName(), calendar);
    }

}
