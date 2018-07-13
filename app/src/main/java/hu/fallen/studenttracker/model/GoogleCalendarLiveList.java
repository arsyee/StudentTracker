package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class GoogleCalendarLiveList extends MutableLiveData<List<GoogleCalendar>> {
    private final Context context;
    private final ContentObserver contactObserver;

    GoogleCalendarLiveList(Context context) {
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
        context.getContentResolver().registerContentObserver(GoogleCalendar.Data.CONTENT_URI, true, contactObserver);
    }

    void unregisterContentObserver() {
        context.getContentResolver().unregisterContentObserver(contactObserver);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadCalendars() {
        new AsyncTask<Void, Void, List<GoogleCalendar>>() {
            @Override
            protected List<GoogleCalendar> doInBackground(Void... voids) {
                String SELECTION = GoogleCalendar.Data.VISIBLE + " = ?";
                String[] selectionArgs = { "1" };
                @SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(
                        GoogleCalendar.Data.CONTENT_URI,
                        GoogleCalendar.Data.PROJECTION,
                        SELECTION,
                        selectionArgs,
                        null);

                List<GoogleCalendar> googleCalendars = new ArrayList<>();
                if (cursor == null) return googleCalendars;
                for (int i = 0; i < cursor.getCount(); ++i) {
                    cursor.moveToPosition(i);
                    GoogleCalendar googleCalendar = new GoogleCalendar();
                    for (int col = 0; col < cursor.getColumnCount(); ++col) {
                        googleCalendar.put(cursor.getColumnName(col), cursor.getString(col));
                    }
                    if (googleCalendar.get(GoogleCalendar.Data.CALENDAR_DISPLAY_NAME).equals(googleCalendar.get(GoogleCalendar.Data.ACCOUNT_NAME))) {
                        googleCalendar.put(GoogleCalendar.Data.CALENDAR_DISPLAY_NAME, context.getResources().getString(R.string.default_calendar));
                    }
                    Timber.d("GoogleCalendar found: %s", googleCalendar);
                    googleCalendars.add(googleCalendar);
                }
                cursor.close();
                return googleCalendars;
            }

            @Override
            protected void onPostExecute(List<GoogleCalendar> googleCalendars) {
                setValue(googleCalendars);
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

                List<GoogleCalendar> calendars = getValue();
                if (calendars != null) for (GoogleCalendar calendar : calendars) {
                    if (contactId.equals(calendar.get(GoogleCalendar.Data.RAW_CONTACT_ID))) {
                        Timber.d("GoogleCalendar already exists.");
                        return null;
                    }
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(GoogleCalendar.Data.RAW_CONTACT_ID, contactId);
                contentValues.put(GoogleCalendar.Data.MIMETYPE, GoogleCalendar.MIMETYPE);
                Uri result = context.getContentResolver().insert(GoogleCalendar.Data.CONTENT_URI, contentValues);
                Timber.d("Insert result is: %s", result);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void update(final GoogleCalendar calendar) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Timber.d("Updating %s", calendar);
                ContentValues values = new ContentValues();
                for (String key : calendar.changeList.keySet()) {
                    values.put(key, calendar.changeList.get(key));
                }
                String SELECTION = GoogleCalendar.Data.MIMETYPE + " LIKE ? AND " + GoogleCalendar.Data.RAW_CONTACT_ID + " = ?";
                String[] selectionArgs = { GoogleCalendar.MIMETYPE, calendar.get(GoogleCalendar.Data.RAW_CONTACT_ID) };
                int result = context.getContentResolver().update(GoogleCalendar.Data.CONTENT_URI, values, SELECTION, selectionArgs);
                Timber.d("Number of rows updated: %d", result);
                return null;
            }
        }.execute();
    }

    public GoogleCalendar getCalendarById(String contactId) {
        if (contactId == null) return null;
        List<GoogleCalendar> calendars = getValue();
        for (GoogleCalendar calendar : calendars) {
            if (contactId.equals(calendar.get(GoogleCalendar.Data.RAW_CONTACT_ID))) {
                return calendar;
            }
        }
        return null;
    }
    */
}
