package hu.fallen.studenttracker.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class CalendarModel extends AndroidViewModel {
    private CalendarLiveList calendars;

    public CalendarModel(@NonNull Application application) {
        super(application);
        calendars = new CalendarLiveList(application);
    }

    @Override
    protected void onCleared() {
        calendars.unregisterContentObserver();
        super.onCleared();
    }

    public CalendarLiveList getCalendars() {
        return calendars;
    }

}
