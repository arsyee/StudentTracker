package hu.fallen.studenttracker.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class GoogleCalendarModel extends AndroidViewModel {
    private GoogleCalendarLiveList calendars;

    public GoogleCalendarModel(@NonNull Application application) {
        super(application);
        calendars = new GoogleCalendarLiveList(application);
    }

    @Override
    protected void onCleared() {
        calendars.unregisterContentObserver();
        super.onCleared();
    }

    public GoogleCalendarLiveList getCalendars() {
        return calendars;
    }

}
