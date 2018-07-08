package hu.fallen.studenttracker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import hu.fallen.studenttracker.model.Student;
import hu.fallen.studenttracker.model.StudentModel;

/**
 * A fragment representing a single Student detail screen.
 * This fragment is either contained in a {@link StudentListActivity}
 * in two-pane mode (on tablets) or a {@link StudentDetailActivity}
 * on handsets.
 */
public class StudentDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private static final int DETAILS_QUERY_ID = 1;

    /**
     * The dummy content this fragment is presenting.
     */
    private String mItem;
    private View mRootView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StudentDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = getArguments().getString(ARG_ITEM_ID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.student_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            final StudentModel model;
            if (getActivity() == null) {
                model = ViewModelProviders.of(this).get(StudentModel.class);
            } else {
                model = ViewModelProviders.of(getActivity()).get(StudentModel.class);
            }
            model.getStudents().observe(this, new Observer<List<Student>>() {
                @Override
                public void onChanged(@Nullable List<Student> students) {
                    if (students == null) return;
                    Student student = model.getStudents().getStudentById(mItem);
                    if (student == null) return;

                    StringBuilder builder = new StringBuilder();
                    for (String key : Student.Data.PROJECTION) {
                        builder.append(key).append(" ").append(student.get(key)).append("\n");
                    }
                    ((TextView) mRootView.findViewById(R.id.student_detail)).setText(builder.toString());

                    ((TextView) mRootView.findViewById(R.id.display_name)).setText(student.get(Student.Data.DISPLAY_NAME_PRIMARY));
                    if (student.get(Student.Data.LEGAL_NAME) == null) {
                        ((EditText) mRootView.findViewById(R.id.legal_name)).setText(student.get(Student.Data.DISPLAY_NAME_PRIMARY));
                    } else {
                        ((EditText) mRootView.findViewById(R.id.legal_name)).setText(student.get(Student.Data.LEGAL_NAME));
                    }
                    if (Student.STATUS.INACTIVE.toString().equals(student.get(Student.Data.STATUS))) {
                        ((Switch) mRootView.findViewById(R.id.student_activated)).setChecked(false);
                    } else {
                        ((Switch) mRootView.findViewById(R.id.student_activated)).setChecked(true);
                    }
                    ((TextView) mRootView.findViewById(R.id.auth_id)).setText(student.get(Student.Data.AUTHORITY_ID));
                    // ((TextView) mRootView.findViewById(R.id.student_detail)).setText(student.get());
                }
            });
        }
        return mRootView;
    }

}
