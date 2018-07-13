package hu.fallen.studenttracker.model;

import android.content.Context;
import android.database.Cursor;

import java.util.Calendar;

import hu.fallen.studenttracker.misc.IDs;

public class StudentEvent extends Event {
    private final Context mContext;
    // TODO: make sure there is always a start time! (empty constructor is used 3 times)
    private int mLessonCount = 0; // only used if I set the count earlier than the start time

    StudentEvent(Context context) {
        super();
        this.mContext = context.getApplicationContext();
    }

    StudentEvent(Cursor cursor, Context context) {
        super(cursor);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void setStartTime(Calendar startTime) {
        super.setStartTime(startTime);
        setEndTime(startTime, getLessonCount());
    }

    public void setLessonCount(int lessonCount) {
        mLessonCount = lessonCount;
        setEndTime(getStartTime(), lessonCount);
    }

    private void setEndTime(Calendar startTime, int lessonCount) {
        if (startTime == null) return;
        Calendar endTime = (Calendar) startTime.clone();
        int lessonLength = IDs.PREFERENCE.LESSON_LENGTH.getInt(mContext);
        int breakLength = IDs.PREFERENCE.BREAK_LENGTH.getInt(mContext);
        int duration = lessonLength + (lessonCount - 1) * (lessonLength + breakLength);
        endTime.add(Calendar.MINUTE, duration);
        data.put(Data.DTEND, Long.toString(endTime.getTimeInMillis()));
    }

    public int getLessonCount() {
        if (data.get(Data.DTSTART) == null || data.get(Data.DTEND) == null) {
            if (mLessonCount == 0) {
                return IDs.PREFERENCE.NUM_LESSONS.getInt(mContext);
            } else {
                return mLessonCount;
            }
        }
        long startTime = Long.parseLong(data.get(Data.DTSTART));
        long endTime = Long.parseLong(data.get(Data.DTEND));
        long duration = (endTime - startTime) / 60 / 1000;
        int lessonLength = IDs.PREFERENCE.LESSON_LENGTH.getInt(mContext);
        int breakLength = IDs.PREFERENCE.BREAK_LENGTH.getInt(mContext);
        return Math.max(1, (int) ((duration - lessonLength ) / (lessonLength + breakLength)) + 1);
    }

}
