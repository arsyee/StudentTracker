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

public class EventListModel extends AndroidViewModel {
    private Map<String, EventLiveList> monthlyEventList;

    public EventListModel(@NonNull Application application) {
        super(application);
        monthlyEventList = new HashMap<>();
    }

    @Override
    protected void onCleared() {
        for (EventLiveList eventLiveList : monthlyEventList.values()) {
            eventLiveList.unregisterContentObserver();
        }
        super.onCleared();
    }

    public @NonNull
    EventLiveList getEventList(int year, int month, LifecycleOwner owner, Observer<List<Event>> observer) {
        String key = getKey(year, month);
        if (!monthlyEventList.containsKey(key)) {
            EventLiveList eventLiveList = new EventLiveList(getApplication(), year, month);
            eventLiveList.observe(owner, observer);
            monthlyEventList.put(key, eventLiveList);
        }
        return monthlyEventList.get(key);
    }

    @SuppressLint("DefaultLocale")
    private String getKey(int year, int month) {
        return String.format("%d-%d", year, month);
    }

    public Event getEventById(String id) {
        if (id == null) return null;
        for (EventLiveList eventLiveList : monthlyEventList.values()) {
            for (Event event : eventLiveList.getValue()) {
                if (id.equals(event.get(Event.Data._ID))) return event;
            }
        }
        return null;
    }
}
