package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventModel extends AndroidViewModel {
    private Map<String, EventLiveList> schedule;

    public EventModel(@NonNull Application application) {
        super(application);
        schedule = new HashMap<>();
    }

    public @NonNull
    EventLiveList getSchedule(int year, int month, LifecycleOwner owner, Observer<List<Event>> observer) {
        String key = getKey(year, month);
        if (!schedule.containsKey(key)) {
            EventLiveList scheduleLiveList = new EventLiveList(getApplication(), year, month);
            scheduleLiveList.observe(owner, observer);
            schedule.put(key, scheduleLiveList);
        }
        return schedule.get(key);
    }

    @SuppressLint("DefaultLocale")
    private String getKey(int year, int month) {
        return String.format("%d-%d", year, month);
    }
}
