package hu.fallen.studenttracker.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class ScheduleModel extends AndroidViewModel {
    private ScheduleLiveList schedule;

    public ScheduleModel(@NonNull Application application) {
        super(application);
        schedule = new ScheduleLiveList(application);
    }

    public ScheduleLiveList getSchedule() {
        return schedule;
    }

}
