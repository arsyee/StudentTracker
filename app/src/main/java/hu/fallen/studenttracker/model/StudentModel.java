package hu.fallen.studenttracker.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import timber.log.Timber;

public class StudentModel extends AndroidViewModel {
    private StudentsLiveCursor students;

    public StudentModel(@NonNull Application application) {
        super(application);
        students = new StudentsLiveCursor(application);
    }

    @Override
    protected void onCleared() {
        students.unregisterContentObserver();
        super.onCleared();
    }

    public StudentsLiveCursor getStudents() {
        return students;
    }

}
