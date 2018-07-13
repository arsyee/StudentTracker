package hu.fallen.studenttracker.misc;

// Store all IDs here to ensure uniqueness
public class IDs {
    // activity request codes
    public static final int REQUEST_CODE_ADD_STUDENT = 201;

    // permission request IDs
    public static final int PERMISSION_REQUEST_CONTACTS_ID = 701;

    // preference keys
    public enum PREFERENCE {
        CALENDAR("calendar"),
        LESSON_LENGTH("lessonLength"),
        NUM_LESSONS("numLessons");
        private final String str;
        PREFERENCE(String status) {
            str = status;
        }
        @Override
        public String toString() {
            return str;
        }
    }

}
