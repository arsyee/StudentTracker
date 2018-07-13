package hu.fallen.studenttracker.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// Store all IDs here to ensure uniqueness
public class IDs {
    // activity request codes
    public static final int REQUEST_CODE_ADD_STUDENT = 201;

    // permission request IDs
    public static final int PERMISSION_REQUEST_CONTACTS_ID = 701;

    // preference keys
    public enum PREFERENCE {
        CALENDAR("calendar", String.class, null),
        LESSON_LENGTH("lessonLength", Integer.class, 60),
        NUM_LESSONS("numLessons", Integer.class, 2);

        private final String key;
        private final Class type;
        private final Object defaultValue;

        PREFERENCE(String key, Class type, Object defaultValue) {
            this.key = key;
            this.type = type;
            this.defaultValue = defaultValue;
        }
        @Override
        public String toString() {
            return key;
        }

        public Object get(Context context) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (type.equals(String.class)) {
                return preferences.getString(key, (String) defaultValue);
            }
            if (type.equals(Integer.class)) {
                return preferences.getInt(key, (Integer) defaultValue);
            }
            return null;
        }

        public int getInt(Context context) {
            Object o = get(context);
            if (o instanceof Integer) return (Integer) o;
            return 0;
        }
    }

}
