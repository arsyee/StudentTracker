package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CalendarLiveList extends MutableLiveData<List<Calendar>> {
    private final Context context;
    private final ContentObserver contactObserver;

    CalendarLiveList(Context context) {
        this.context = context;
        loadCalendars();

        contactObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadCalendars();
            }
        };
        context.getContentResolver().registerContentObserver(Calendar.Data.CONTENT_URI, true, contactObserver);
    }

    void unregisterContentObserver() {
        context.getContentResolver().unregisterContentObserver(contactObserver);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadCalendars() {
        new AsyncTask<Void, Void, List<Calendar>>() {
            @Override
            protected List<Calendar> doInBackground(Void... voids) {
                @SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(
                        Calendar.Data.CONTENT_URI,
                        Calendar.Data.PROJECTION,
                        null,
                        null,
                        null);

                List<Calendar> calendars = new ArrayList<>();
                if (cursor == null) return calendars;
                for (int i = 0; i < cursor.getCount(); ++i) {
                    cursor.moveToPosition(i);
                    Calendar calendar = new Calendar();
                    for (int col = 0; col < cursor.getColumnCount(); ++col) {
                        calendar.put(cursor.getColumnName(col), cursor.getString(col));
                    }
                    Timber.d("Calendar found: %s", calendar);
                    calendars.add(calendar);
                }
                cursor.close();
                return calendars;
            }

            @Override
            protected void onPostExecute(List<Calendar> calendars) {
                setValue(calendars);
            }
        }.execute();
    }

    /*
    @SuppressLint("StaticFieldLeak")
    public void createFromContact(final Uri contactData) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor =  context.getContentResolver().query(contactData, null, null, null, null);
                if (cursor == null) return null;
                cursor.moveToFirst();
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                cursor.close();

                if (contactId == null) return null;

                List<Calendar> calendars = getValue();
                if (calendars != null) for (Calendar calendar : calendars) {
                    if (contactId.equals(calendar.get(Calendar.Data.RAW_CONTACT_ID))) {
                        Timber.d("Calendar already exists.");
                        return null;
                    }
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(Calendar.Data.RAW_CONTACT_ID, contactId);
                contentValues.put(Calendar.Data.MIMETYPE, Calendar.MIMETYPE);
                Uri result = context.getContentResolver().insert(Calendar.Data.CONTENT_URI, contentValues);
                Timber.d("Insert result is: %s", result);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void update(final Calendar calendar) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Timber.d("Updating %s", calendar);
                ContentValues values = new ContentValues();
                for (String key : calendar.changeList.keySet()) {
                    values.put(key, calendar.changeList.get(key));
                }
                String SELECTION = Calendar.Data.MIMETYPE + " LIKE ? AND " + Calendar.Data.RAW_CONTACT_ID + " = ?";
                String[] selectionArgs = { Calendar.MIMETYPE, calendar.get(Calendar.Data.RAW_CONTACT_ID) };
                int result = context.getContentResolver().update(Calendar.Data.CONTENT_URI, values, SELECTION, selectionArgs);
                Timber.d("Number of rows updated: %d", result);
                return null;
            }
        }.execute();
    }

    public Calendar getCalendarById(String contactId) {
        if (contactId == null) return null;
        List<Calendar> calendars = getValue();
        for (Calendar calendar : calendars) {
            if (contactId.equals(calendar.get(Calendar.Data.RAW_CONTACT_ID))) {
                return calendar;
            }
        }
        return null;
    }
    */
}
