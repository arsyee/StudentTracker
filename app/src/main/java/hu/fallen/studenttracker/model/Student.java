package hu.fallen.studenttracker.model;

import android.net.Uri;
import android.provider.ContactsContract;

import java.util.HashMap;

public class Student extends HashMap<String, String> {

    public static final String MIMETYPE = "vnd.android.cursor.item/vnd.hu.fallen.studenttracker.student";

    public static class Data {
        public static final Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

        public static final String _ID = ContactsContract.Data._ID;
        public static final String DISPLAY_NAME_PRIMARY = ContactsContract.Data.DISPLAY_NAME_PRIMARY;
        public static final String RAW_CONTACT_ID = ContactsContract.Data.RAW_CONTACT_ID;
        public static final String MIMETYPE = ContactsContract.Data.MIMETYPE;

        // public static final String  = ContactsContract.Data.;

        public static final String STATUS = ContactsContract.Data.DATA1;

        public static final String[] PROJECTION = {
                _ID,
                DISPLAY_NAME_PRIMARY,
                RAW_CONTACT_ID,
                MIMETYPE,
                STATUS
        };
    }
}
