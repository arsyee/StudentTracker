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
        context.getContentResolver().registerContentObserver(Student.Data.CONTENT_URI, true, contactObserver);
    }

    void unregisterContentObserver() {
        context.getContentResolver().unregisterContentObserver(contactObserver);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadStudents() {
        new AsyncTask<Void, Void, List<Student>>() {
            @Override
            protected List<Student> doInBackground(Void... voids) {
                String SELECTION = Student.Data.MIMETYPE + " LIKE ?";
                String[] selectionArgs = { Student.MIMETYPE };
                Cursor cursor = context.getContentResolver().query(
                        Student.Data.CONTENT_URI,
                        Student.Data.PROJECTION,
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
                    Timber.d("Student found: %s", student);
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

                if (contactId == null) return null;

                List<Student> students = getValue();
                if (students != null) for (Student student : students) {
                    if (contactId.equals(student.get(Student.Data.RAW_CONTACT_ID))) {
                        Timber.d("Student already exists.");
                        return null;
                    }
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(Student.Data.RAW_CONTACT_ID, contactId);
                contentValues.put(Student.Data.MIMETYPE, Student.MIMETYPE);
                Uri result = context.getContentResolver().insert(Student.Data.CONTENT_URI, contentValues);
                Timber.d("Insert result is: %s", result);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void update(final Student student) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Timber.d("Updating %s", student);
                ContentValues values = new ContentValues();
                for (String key : student.changeList.keySet()) {
                    values.put(key, student.changeList.get(key));
                }
                String SELECTION = Student.Data.MIMETYPE + " LIKE ? AND " + Student.Data.RAW_CONTACT_ID + " = ?";
                String[] selectionArgs = { Student.MIMETYPE, student.get(Student.Data.RAW_CONTACT_ID) };
                int result = context.getContentResolver().update(Student.Data.CONTENT_URI, values, SELECTION, selectionArgs);
                Timber.d("Number of rows updated: %d", result);
                return null;
            }
        }.execute();
    }

    public Student getStudentById(String contactId) {
        if (contactId == null) return null;
        List<Student> students = getValue();
        for (Student student : students) {
            if (contactId.equals(student.get(Student.Data.RAW_CONTACT_ID))) {
                return student;
            }
        }
        return null;
    }
}
