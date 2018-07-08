package hu.fallen.studenttracker;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.List;
import java.util.Map;

import hu.fallen.studenttracker.model.Student;
import hu.fallen.studenttracker.model.StudentModel;
import timber.log.Timber;

public class AboutActivity extends BaseActivity {

    StudentModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        printPreferences();
        model = ViewModelProviders.of(this).get(StudentModel.class);
    }

    public void clearPreferences(View view) {
        Timber.d("Clearing preferences");
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        printPreferences();
    }

    private void printPreferences() {
        Timber.d("Preference list:");
        Map<String, ?> prefs = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        for(Map.Entry<String,?> entry : prefs.entrySet()) {
            Timber.d("map values: %s - %s",entry.getKey(),  entry.getValue());
        }
    }

    public void updateStudents(View view) {
        List<Student> students = model.getStudents().getValue();
        if (students == null) return;
        for (Student student : students) {
            // TODO: I think this is not thread-safe, but won't be part of the final app anyway
            student.setStatus(Student.STATUS.ACTIVE);
            model.getStudents().update(student);
        }
    }
}
