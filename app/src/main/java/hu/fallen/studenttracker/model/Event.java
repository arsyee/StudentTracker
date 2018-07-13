package hu.fallen.studenttracker.model;

import android.net.Uri;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {
    private final Map<String, String> data;
    final Map<String, String> changeList;

    Event() {
        data = new HashMap<>();
        changeList = new HashMap<>();
    }

    void put(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public void set(String key, String value) {
        if (Data.FREETEXT_PARAM.contains(key)) {
            changeList.put(key, value);
        }
    }

    @Override
    public String toString() {
        return String.format("%s (changes: %s)", data, changeList);
    }

    public Calendar getStartTime() {
        if (data.get(Data.DTSTART) == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(data.get(Data.DTSTART)));
        return calendar;
    }

    public Calendar getEndTime() {
        if (data.get(Data.DTEND) == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(data.get(Data.DTEND)));
        return calendar;
    }

    public void setStartTime(Calendar startTime) {
        data.put(Data.DTSTART, Long.toString(startTime.getTimeInMillis()));
    }

    public static class Data {
        public static final Uri CONTENT_URI = CalendarContract.Events.CONTENT_URI;

        public static final String _ID = CalendarContract.Events._ID;
        public static final String CALENDAR_ID = CalendarContract.Events.CALENDAR_ID;
        public static final String TITLE = CalendarContract.Events.TITLE;
        public static final String DESCRIPTION = CalendarContract.Events.DESCRIPTION;
        public static final String DTSTART = CalendarContract.Events.DTSTART;
        public static final String DTEND = CalendarContract.Events.DTEND;

        static final List<String> FREETEXT_PARAM = new ArrayList<>();

        static {
            // FREETEXT_PARAM.add();
        }

        public static final String[] PROJECTION = {
                _ID,
                CALENDAR_ID,
                TITLE,
                DESCRIPTION,
                DTSTART,
                DTEND,
        };
    }
}
