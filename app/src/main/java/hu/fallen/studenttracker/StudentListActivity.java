package hu.fallen.studenttracker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hu.fallen.studenttracker.misc.Config;
import hu.fallen.studenttracker.misc.IDs;
import hu.fallen.studenttracker.model.Student;
import hu.fallen.studenttracker.model.StudentModel;
import timber.log.Timber;

/**
 * An activity representing a list of Students. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StudentDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StudentListActivity extends BaseActivity {

    private boolean mTwoPane;
    private StudentModel mModel;
    private StudentListRecyclerViewAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());

        if (Config.check(this)) {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, IDs.REQUEST_CODE_ADD_STUDENT);
                }
            });
        }

        if (findViewById(R.id.student_detail_container) != null) {
            mTwoPane = true;
        }

        mModel = ViewModelProviders.of(this).get(StudentModel.class);

        View recyclerView = findViewById(R.id.student_list);
        assert recyclerView != null;
        if (Config.check(recyclerView)) {
            setupRecyclerView((RecyclerView) recyclerView);
            mModel.getStudents().observe(this, new Observer<List<Student>>() {
                @Override
                public void onChanged(@Nullable List<Student> students) {
                    mCursorAdapter.swapList(students);
                }
            });
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mCursorAdapter = new StudentListRecyclerViewAdapter(this, null, mTwoPane);
        recyclerView.setAdapter(mCursorAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IDs.REQUEST_CODE_ADD_STUDENT) {
            if (resultCode == RESULT_OK) {
                mModel.getStudents().createFromContact(data.getData());
            } else {
                Timber.d("onActivityResult reports failure: %d", resultCode);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static class StudentListRecyclerViewAdapter
            extends RecyclerView.Adapter<StudentListRecyclerViewAdapter.ViewHolder> {

        private final StudentListActivity mParentActivity;
        private List<Student> mStudentList;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactId = (String) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(StudentDetailFragment.ARG_ITEM_ID, contactId);
                    StudentDetailFragment fragment = new StudentDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.student_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, StudentDetailActivity.class);
                    intent.putExtra(StudentDetailFragment.ARG_ITEM_ID, contactId);

                    context.startActivity(intent);
                }
            }
        };

        StudentListRecyclerViewAdapter(StudentListActivity parent,
                                       List<Student> studentList,
                                       boolean twoPane) {
            mStudentList = studentList;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.student_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (mStudentList == null) {
                throw new IllegalStateException("Cursor is null.");
            }
            if (mStudentList.size() <= position) {
                throw new IllegalStateException(String.format("Cannot move cursor to position %d.", position));
            }
            Student student = mStudentList.get(position);
            // deal with Cursor data
            holder.mIdView.setText(student.get(Student.Data._ID));
            holder.mContentView.setText(student.get(Student.Data.DISPLAY_NAME_PRIMARY));

            holder.itemView.setTag(student.get(Student.Data.RAW_CONTACT_ID));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mStudentList == null ? 0 : mStudentList.size();
        }

        public void swapList(List<Student> data) {
            mStudentList = data;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}
