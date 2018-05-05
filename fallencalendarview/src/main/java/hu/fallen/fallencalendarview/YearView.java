package hu.fallen.fallencalendarview;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.webkit.WebView;

import java.util.Calendar;

import timber.log.Timber;

class YearView extends WebView {
    private Calendar mCalendar;

    public YearView(Context context) {
        this(context, null);
    }

    public YearView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YearView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public YearView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        // TODO do something with defStyleRes
        int fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        Timber.d("font size changed from %d to %d", getSettings().getDefaultFontSize(), fontSize);
        getSettings().setDefaultFontSize(fontSize);
        getSettings().setStandardFontFamily("sans-serif-condensed");
    }

    static String getYearCalendar(Calendar calendar, int columnNum, String[] dayNames, String[] monthNames, String css) {
        int[] monthLength = {31, 28, 31,
                             30, 31, 30,
                             31, 31, 30,
                             31, 30, 31};
        if (calendar.getActualMaximum(Calendar.DAY_OF_YEAR) > 365) monthLength[1]++;

        Calendar today = Calendar.getInstance();
        int thisYear = today.get(Calendar.YEAR);
        int thisMonth = today.get(Calendar.MONTH);
        int thisDay = today.get(Calendar.DAY_OF_MONTH);

        int selectedYear = calendar.get(Calendar.YEAR);
        int selectedMonth = calendar.get(Calendar.MONTH);
        int selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        int firstDayOfYear = firstDayOfYear(calendar);

        String header;
        {
            StringBuilder hb = new StringBuilder();
            for (int i = 0; i < 7; ++i) {
                int day = (firstDayOfWeek - 1 + i) % 7 + 1;
                hb.append("<th");
                if (day == Calendar.SUNDAY) hb.append(" class='sunday'");
                hb.append(">").append(dayNames[day].charAt(0)).append("</th>");
            }
            header = hb.toString();
        }

        int currentDayOfWeek = firstDayOfYear;

        StringBuilder sb = new StringBuilder(9000); // measurements show we usually end up below this
        sb.append("<html><head><style>\n").append(css).append("\n</style></head><body><table class='year'>");
        for (int month = 0; month < 12; ++month) {
            if (month % columnNum == 0) sb.append("<tr width='").append(100/columnNum).append("%'>");
            sb.append("<td><font size='5'>");
            if (month == thisMonth) sb.append("<b>");
            sb.append(monthNames[month]);
            if (month == thisMonth) sb.append("</b>");
            sb.append("</font><table><tr>").append(header).append("</tr>");
            int currentDayOfMonth = 1;
            while (currentDayOfMonth <= monthLength[month]) {
                boolean bThisWeek = selectedYear == thisYear && month == thisMonth && currentDayOfMonth <= thisDay && currentDayOfMonth + 7 > thisDay;
                if (bThisWeek) {
                    sb.append("<tr class='thisweek'>");
                } else {
                    sb.append("<tr>");
                }
                for (int i = 0; i < 7; ++i) {
                    String cssClass = null;
                    if (selectedYear == thisYear && month == thisMonth && currentDayOfMonth == thisDay) {
                        cssClass = "today";
                    } else if (month == selectedMonth && currentDayOfMonth == selectedDay) {
                        cssClass = "current";
                    }
                    if (currentDayOfWeek == Calendar.SUNDAY) {
                        if (cssClass == null) {
                            cssClass = "sunday";
                        } else {
                            cssClass = cssClass + " sunday";
                        }
                    }
                    if (cssClass == null){
                        sb.append("<td>");
                    } else {
                        sb.append("<td class='").append(cssClass).append("'>");
                    }
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
        sb.append("</table></body></html>");
        Timber.d("Builder length: %d", sb.length());
        // Timber.d(sb.toString());
        return sb.toString();
    }

    static int firstDayOfYear(Calendar calendar) {
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        final int MAX = 53 * 7;
        return (MAX + dayOfWeek - dayOfYear) % 7 + 1;
    }

    public void setCalendar(Calendar calendar) {
        this.mCalendar = calendar;
        int columnNum = 3;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnNum = 6;
        }
        loadDataWithBaseURL(
                "about:blank",
                YearView.getYearCalendar(
                        mCalendar,
                        columnNum,
                        getResources().getStringArray(R.array.days),
                        getResources().getStringArray(R.array.months),
                        getResources().getString(R.string.year_css)),
                "text/html",
                null,
                null);
    }
}
