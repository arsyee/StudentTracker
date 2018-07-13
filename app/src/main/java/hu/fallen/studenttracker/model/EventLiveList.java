package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class EventLiveList extends MutableLiveData<List<Event>> {
    private final Context context;
    private final ContentObserver calendarObserver;

    private final Calendar monthStart = Calendar.getInstance();;
    private final Calendar monthEnd = Calendar.getInstance();;

    EventLiveList(Context context, int year, int month) {
        this.context = context;
        setValue(new ArrayList<Event>());
        monthStart.set(Calendar.YEAR, year);
        monthStart.set(Calendar.MONTH, month - 1);
        monthEnd.set(Calendar.YEAR, year);
        monthEnd.set(Calendar.MONTH, month - 1);
        monthEnd.add(Calendar.MONTH, 1);
        loadSchedule();

        calendarObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadSchedule();
            }
        };
        context.getContentResolver().registerContentObserver(Event.Data.CONTENT_URI, true, calendarObserver);
    }

    void unregisterContentObserver() {
        context.getContentResolver().unregisterContentObserver(calendarObserver);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadSchedule() {
        new AsyncTask<Void, Void, List<Event>>() {
            @Override
            protected List<Event> doInBackground(Void... voids) {
                String SELECTION = Event.Data.DTSTART + " >= ? AND " + Event.Data.DTSTART + " < ?";
                String[] selectionArgs = {
                        Long.toString(monthStart.getTimeInMillis()),
                        Long.toString(monthEnd.getTimeInMillis()),
                };
                @SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(
                        Event.Data.CONTENT_URI,
                        Event.Data.PROJECTION,
                        SELECTION,
                        selectionArgs,
                        Event.Data.CALENDAR_ID
                );

                List<Event> events = new ArrayList<>();
                if (cursor == null) return events;
                for (int i = 0; i < cursor.getCount(); ++i) {
                    cursor.moveToPosition(i);
                    Event event = new Event(cursor);
                    Timber.d("Event found: %s", event);
                    events.add(event);
                }
                cursor.close();
                return events;
            }

            @Override
            protected void onPostExecute(List<Event> schedule) {
                setValue(schedule);
            }
        }.execute();
    }

    public void add(Calendar time) {
        /*
        List<WeekViewEvent> value = getValue();
        value.add(createEvent(time, 120));
        setValue(value);
        Timber.d("Number of events: %d", value.size());
        */
    }

    public void remove(WeekViewEvent event) {
        /*
        List<WeekViewEvent> value = getValue();
        Timber.d("Removing %s (%s)", str(event), value.contains(event));
        value.remove(event);
        setValue(value);
        Timber.d("Number of events: %d", value.size());
        */
    }

}
