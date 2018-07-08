package hu.fallen.studenttracker.model;

import android.provider.ContactsContract;

public class Student {

    public static final String MIMETYPE = "vnd.android.cursor.item/vnd.hu.fallen.studenttracker.student";

    public static class Data {
        public static final String RAW_CONTACT_ID = ContactsContract.Data.RAW_CONTACT_ID;
        public static final String MIMETYPE = ContactsContract.Data.MIMETYPE;

        public static final String STATUS = ContactsContract.Data.DATA1;
    }
}