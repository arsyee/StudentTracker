package hu.fallen.studenttracker.model;

import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleCalendar {
    private final Map<String, String> data;
    final Map<String, String> changeList;

    GoogleCalendar() {
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

    public static class Data {
        public static final Uri CONTENT_URI = CalendarContract.Calendars.CONTENT_URI;

        public static final String _ID = CalendarContract.Calendars._ID;
        public static final String ACCOUNT_NAME = CalendarContract.Calendars.ACCOUNT_NAME;
        public static final String ACCOUNT_TYPE = CalendarContract.Calendars.ACCOUNT_TYPE;
        public static final String CALENDAR_DISPLAY_NAME = CalendarContract.Calendars.CALENDAR_DISPLAY_NAME;
        public static final String VISIBLE = CalendarContract.Calendars.VISIBLE;
        public static final String COLOR = CalendarContract.Calendars.CALENDAR_COLOR;

        static final List<String> FREETEXT_PARAM = new ArrayList<>();

        static {
            // FREETEXT_PARAM.add();
        }

        public static final String[] PROJECTION = {
                _ID,
                ACCOUNT_NAME,
                ACCOUNT_TYPE,
                CALENDAR_DISPLAY_NAME,
                COLOR
        };
    }
}
