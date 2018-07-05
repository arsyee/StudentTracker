package hu.fallen.studenttracker;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Student detail screen.
 * This fragment is either contained in a {@link StudentListActivity}
 * in two-pane mode (on tablets) or a {@link StudentDetailActivity}
 * on handsets.
 */
public class StudentDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
        getLoaderManager().initLoader(DETAILS_QUERY_ID, null, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.student_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            // ((TextView) mRootView.findViewById(R.id.student_detail)).setText(mItem);
        }

        return mRootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DETAILS_QUERY_ID:
                final String[] PROJECTION =
                        {
                                ContactsContract.Data._ID,
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.Data.DATA1,
                                ContactsContract.Data.DATA2,
                                ContactsContract.Data.DATA3,
                                ContactsContract.Data.DATA4,
                                ContactsContract.Data.DATA5,
                                ContactsContract.Data.DATA6,
                                ContactsContract.Data.DATA7,
                                ContactsContract.Data.DATA8,
                                ContactsContract.Data.DATA9,
                                ContactsContract.Data.DATA10,
                                ContactsContract.Data.DATA11,
                                ContactsContract.Data.DATA12,
                                ContactsContract.Data.DATA13,
                                ContactsContract.Data.DATA14,
                                ContactsContract.Data.DATA15
                        };
                final String SELECTION = ContactsContract.Data.LOOKUP_KEY + " = ?";
                // Assigns the selection parameter
                String[] selectionArgs = {mItem};
                // Starts the query
                return new CursorLoader(
                        getActivity(),
                        ContactsContract.Data.CONTENT_URI,
                        PROJECTION,
                        SELECTION,
                        selectionArgs,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == DETAILS_QUERY_ID) {
            StringBuilder builder = new StringBuilder();
            for (int n = 0; n < data.getCount(); ++n) {
                data.moveToPosition(n);
                for (int i = 0; i < data.getColumnCount(); ++i) {
                    if (data.getString(i) == null) continue;
                    builder.append(data.getColumnName(i))
                            .append("(" + i + ")")
                            .append(" - ")
                            .append(data.getString(i))
                            .append("\n");
                }
                builder.append("\n");
            }

            ((TextView) mRootView.findViewById(R.id.student_detail)).setText(builder.toString());
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        if (loader.getId() == DETAILS_QUERY_ID) {
            ((TextView) mRootView.findViewById(R.id.student_detail)).setText("");
        }
    }
}
