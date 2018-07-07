package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import timber.log.Timber;

public class StudentModel extends AndroidViewModel {
    private StudentsLiveCursor students;

    public StudentModel(@NonNull Application application) {
        super(application);
        students = new StudentsLiveCursor(application);
    }

    public LiveData<Cursor> getStudents() {
        return students;
    }

    private static class StudentsLiveCursor extends MutableLiveData<Cursor> {
        private final Context context;

        StudentsLiveCursor(Context context) {
            this.context = context;
            loadStudents();
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
                    String group = PreferenceManager.getDefaultSharedPreferences(context).getString("group", null);
                    Timber.d("Querying group %s", group);
                    if (group == null) {
                        return null;
                    }
                    String[] selectionArgs = { group };
                    return context.getContentResolver().query(
                            ContactsContract.Data.CONTENT_URI,
                            PROJECTION,
                            SELECTION,
                            selectionArgs,
                            null);
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    setValue(cursor);
                }
            }.execute();
        }
    }
}
