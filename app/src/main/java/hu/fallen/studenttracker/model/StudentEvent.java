package hu.fallen.studenttracker.model;

import android.content.Context;
import android.database.Cursor;

import hu.fallen.studenttracker.misc.IDs;

public class StudentEvent extends Event {
    private final Context mContext;

    StudentEvent(Context context) {
        super();
        this.mContext = context.getApplicationContext();
    }

    StudentEvent(Cursor cursor, Context context) {
        super(cursor);
        this.mContext = context.getApplicationContext();
    }

    public int getLessonCount() {
        if (data.get(Data.DTSTART) == null || data.get(Data.DTEND) == null) return 0;
        long startTime = Long.parseLong(data.get(Data.DTSTART));
        long endTime = Long.parseLong(data.get(Data.DTEND));
        long duration = (endTime - startTime) / 60 / 1000;
        long lessonLength = IDs.PREFERENCE.LESSON_LENGTH.getInt(mContext);
        return (int) (duration / lessonLength);
    }

}
