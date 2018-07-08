package hu.fallen.studenttracker.model;

import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student {

    public static final String MIMETYPE = "vnd.android.cursor.item/vnd.hu.fallen.studenttracker.student";

    private final Map<String, String> data;
    final Map<String, String> changeList;

    Student() {
        data = new HashMap<>();
        changeList = new HashMap<>();
    }

    void put(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public void setStatus(STATUS s) {
        changeList.put(Data.STATUS, s.toString());
    }

    public void set(String key, String value) {
        if (Data.FREETEXT_PARAM.contains(key)) {
            changeList.put(key, value);
        }
    }

    @Override
    public String toString() {
        return String.format("%s (changes: %s)", data, changeList);
    }

    public enum STATUS {
        ACTIVE("active"),
        INACTIVE("inactive");
        private final String str;
        STATUS(String status) {
            str = status;
        }
        @Override
        public String toString() {
            return str;
        }
    }

    public static class Data {
        public static final Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

        public static final String _ID = ContactsContract.Data._ID;
        public static final String DISPLAY_NAME_PRIMARY = ContactsContract.Data.DISPLAY_NAME_PRIMARY;
        public static final String RAW_CONTACT_ID = ContactsContract.Data.RAW_CONTACT_ID;
        public static final String MIMETYPE = ContactsContract.Data.MIMETYPE;

        // public static final String  = ContactsContract.Data.;

        public static final String STATUS = ContactsContract.Data.DATA1;
        public static final String LEGAL_NAME = ContactsContract.Data.DATA2;
        public static final String AUTHORITY_ID = ContactsContract.Data.DATA3;

        static final List<String> FREETEXT_PARAM = new ArrayList<>();
        static {
            FREETEXT_PARAM.add(LEGAL_NAME);
            FREETEXT_PARAM.add(AUTHORITY_ID);
        }

        public static final String[] PROJECTION = {
                _ID,
                DISPLAY_NAME_PRIMARY,
                RAW_CONTACT_ID,
                MIMETYPE,

                STATUS,
                LEGAL_NAME,
                AUTHORITY_ID,
        };
    }
}
