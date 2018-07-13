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

public class EventLiveData extends MutableLiveData<Event> {
    private final Context context;
    private final ContentObserver calendarObserver;

    EventLiveData(Context context) {
        this.context = context;
        setValue(new Event());

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
                replaceWith(uri.toString());
            }
        };
    }

    public void notifyClientChange() {
        // TODO: this may be a bad idea, maybe I need a separate Event instance for the local changes
        // what happens, if Event is changed both from the UI and the CalendarProvider?
        setValue(getValue());
    }

    void unregisterContentObserver() {
        context.getContentResolver().unregisterContentObserver(calendarObserver);
    }

    @SuppressLint("StaticFieldLeak")
    public void replaceWith(final String uriString) {
        new AsyncTask<Void, Void, Event>() {
            @Override
            protected Event doInBackground(Void... voids) {
                @SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(
                        Uri.parse(uriString),
                        Event.Data.PROJECTION,
                        null,
                        null,
                        null
                );

                Event event = new Event();
                if (cursor == null) return event;
                if (cursor.getCount() != 1) {
                    Timber.e("Number of events found: %d", cursor.getCount());
                }
                if (cursor.getCount() == 0) return event;
                cursor.moveToPosition(0);
                for (int col = 0; col < cursor.getColumnCount(); ++col) {
                    event.put(cursor.getColumnName(col), cursor.getString(col));
                }
                Timber.d("Event found: %s", event);
                cursor.close();
                return event;
            }

            @Override
            protected void onPostExecute(Event event) {
                unregisterContentObserver();
                setValue(event);
                context.getContentResolver().registerContentObserver(
                        Event.Data.CONTENT_URI.buildUpon().appendPath(event.get(Event.Data._ID)).build(),
                        true, calendarObserver);
            }
        }.execute();
    }

}
