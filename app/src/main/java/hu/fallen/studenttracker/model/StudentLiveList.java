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

public class StudentLiveList extends MutableLiveData<List<Student>> {
    private final Context context;
    private final ContentObserver contactObserver;

    StudentLiveList(Context context) {
        this.context = context;
        loadStudents();

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
                loadStudents();
            }
        };
        context.getContentResolver().registerContentObserver(ContactsContract.Data.CONTENT_URI, true, contactObserver);
    }

    void unregisterContentObserver() {
        context.getContentResolver().unregisterContentObserver(contactObserver);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadStudents() {
        new AsyncTask<Void, Void, List<Student>>() {
            @Override
            protected List<Student> doInBackground(Void... voids) {
                String[] PROJECTION = {
                        ContactsContract.Data._ID,
                        ContactsContract.Data.LOOKUP_KEY,
                        ContactsContract.Data.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.Data.DATA1,
                        ContactsContract.Data.MIMETYPE
                };
                String SELECTION = ContactsContract.Data.MIMETYPE + " LIKE ?";
                String[] selectionArgs = { Student.MIMETYPE };
                Cursor cursor = context.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        PROJECTION,
                        SELECTION,
                        selectionArgs,
                        null);

                List<Student> students = new ArrayList<>();
                if (cursor == null) return students;
                for (int i = 0; i < cursor.getCount(); ++i) {
                    cursor.moveToPosition(i);
                    Student student = new Student();
                    for (int col = 0; col < cursor.getColumnCount(); ++col) {
                        student.put(cursor.getColumnName(col), cursor.getString(col));
                    }
                    students.add(student);
                }
                cursor.close();
                return students;
            }

            @Override
            protected void onPostExecute(List<Student> students) {
                setValue(students);
            }
        }.execute();
    }

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

                ContentValues contentValues = new ContentValues();
                contentValues.put(Student.Data.RAW_CONTACT_ID, contactId);
                contentValues.put(Student.Data.MIMETYPE, Student.MIMETYPE);
                Uri result = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                Timber.d("Insert result is: %s", result);
                return null;
            }
        }.execute();
    }

}
