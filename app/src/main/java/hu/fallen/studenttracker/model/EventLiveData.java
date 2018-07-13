package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import timber.log.Timber;

public class EventLiveData extends MutableLiveData<StudentEvent> {
    private final Context context;
    private final ContentObserver calendarObserver;

    EventLiveData(Context context) {
        this.context = context;
        setValue(new StudentEvent(context));

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
        new AsyncTask<Void, Void, StudentEvent>() {
            @Override
            protected StudentEvent doInBackground(Void... voids) {
                @SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(
                        Uri.parse(uriString),
                        Event.Data.PROJECTION,
                        null,
                        null,
                        null
                );

                if (cursor == null) return new StudentEvent(context);
                if (cursor.getCount() != 1) {
                    Timber.e("Number of events found: %d", cursor.getCount());
                }
                if (cursor.getCount() == 0) return new StudentEvent(context);
                cursor.moveToPosition(0);
                StudentEvent studentEvent = new StudentEvent(cursor, context);
                Timber.d("Event found: %s", studentEvent);
                cursor.close();
                return studentEvent;
            }

            @Override
            protected void onPostExecute(StudentEvent studentEvent) {
                unregisterContentObserver();
                setValue(studentEvent);
                context.getContentResolver().registerContentObserver(
                        Event.Data.CONTENT_URI.buildUpon().appendPath(studentEvent.get(Event.Data._ID)).build(),
                        true, calendarObserver);
            }
        }.execute();
    }

}
