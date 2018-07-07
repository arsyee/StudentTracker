package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import timber.log.Timber;

public class StudentModel extends AndroidViewModel {
    private MutableLiveData<Cursor> students;

    public StudentModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Cursor> getStudents() {
        if (students == null) {
            students = new MutableLiveData<>();
            loadStudents();
        }
        return students;
    }

    @SuppressLint("StaticFieldLeak")
    private void loadStudents() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                String[] PROJECTION = {
                        ContactsContract.Data._ID,
                        ContactsContract.Data.LOOKUP_KEY,
                        ContactsContract.Data.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.Data.DATA1
                };
                String SELECTION = ContactsContract.Data.DATA1 + " LIKE ?";
                String group = PreferenceManager.getDefaultSharedPreferences(getApplication()).getString("group", null);
                Timber.d("Querying group %s", group);
                if (group == null) {
                    return null;
                }
                String[] selectionArgs = { group };
                return getApplication().getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        PROJECTION,
                        SELECTION,
                        selectionArgs,
                        null);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                students.setValue(cursor);
            }
        }.execute();
    }
}
