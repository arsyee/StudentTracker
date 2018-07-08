package hu.fallen.studenttracker.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class StudentModel extends AndroidViewModel {
    private StudentLiveList students;

    public StudentModel(@NonNull Application application) {
        super(application);
        students = new StudentLiveList(application);
    }

    @Override
    protected void onCleared() {
        students.unregisterContentObserver();
        super.onCleared();
    }

    public StudentLiveList getStudents() {
        return students;
    }

}
