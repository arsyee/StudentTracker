package hu.fallen.studenttracker;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.fallen.studenttracker.misc.Config;
import hu.fallen.studenttracker.misc.IDs;
import timber.log.Timber;

/**
 * An activity representing a list of Students. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StudentDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StudentListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private CursorRecyclerViewAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Config.check(view)) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, IDs.REQUEST_CODE_ADD_STUDENT);
                }
            }
        });

        if (findViewById(R.id.student_detail_container) != null) {
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.student_list);
        assert recyclerView != null;
        if (Config.check(recyclerView)) {
            setupRecyclerView((RecyclerView) recyclerView);
            getLoaderManager().initLoader(IDs.LOADER_ID_STUDENT_LIST, null, this);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        int CONTACT_ID_INDEX = 0;
        int LOOKUP_KEY_INDEX = 1;
        mCursorAdapter = new CursorRecyclerViewAdapter(this, null, mTwoPane);
        recyclerView.setAdapter(mCursorAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IDs.REQUEST_CODE_ADD_STUDENT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Timber.d("Querying...");
                Cursor cursor =  getContentResolver().query(contactData, null, null, null, null);
                Timber.d("Cursor length: %d (%d columns)", cursor.getCount(), cursor.getColumnCount());
                cursor.moveToFirst();
                String message = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                Snackbar.make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                cursor.close();

                String groupId = PreferenceManager.getDefaultSharedPreferences(this).getString("group", null);
                if (groupId == null) return;

                ContentValues contentValues = new ContentValues();
                contentValues.put(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, lookupKey);
                contentValues.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId);
                contentValues.put(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
                Uri result = getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                Timber.d("Insert result is: %s", result);
            } else {
                Timber.d("onActivityResult reports failure: %d", resultCode);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] PROJECTION = {
                ContactsContract.Data._ID,
                ContactsContract.Data.LOOKUP_KEY,
                ContactsContract.Data.DISPLAY_NAME_PRIMARY,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DATA1
        };
        String SELECTION = ContactsContract.Data.DATA1 + " LIKE ?";
        String searchString = PreferenceManager.getDefaultSharedPreferences(this).getString("group", null);
        Timber.d("Querying group %s", searchString);
        if (searchString == null) {
            return null;
        }
        String[] selectionArgs = { searchString };
        return new CursorLoader(
                this,
                ContactsContract.Data.CONTENT_URI,
                PROJECTION,
                SELECTION,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public static class CursorRecyclerViewAdapter
            extends RecyclerView.Adapter<CursorRecyclerViewAdapter.ViewHolder> {

        private final StudentListActivity mParentActivity;
        private Cursor mCursor;
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

        CursorRecyclerViewAdapter(StudentListActivity parent,
                                  Cursor cursor,
                                  boolean twoPane) {
            mCursor = cursor;
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
            if (mCursor == null) {
                throw new IllegalStateException("Cursor is null.");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException(String.format("Cannot move cursor to position %d.", position));
            }
            int CONTACT_ID_INDEX = 3;
            int LOOKUP_KEY_INDEX = 1;
            // deal with Cursor data
            holder.mIdView.setText(mCursor.getString(CONTACT_ID_INDEX));
            holder.mContentView.setText(mCursor.getString(2) + " " + mCursor.getString(4)
            + "(" + mCursor.getColumnCount() + ")");

            holder.itemView.setTag(mCursor.getString(LOOKUP_KEY_INDEX));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        public void swapCursor(Cursor data) {
            mCursor = data;
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
