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
    private final EventLiveData event;

    public EventModel(@NonNull Application application) {
        super(application);
        event = new EventLiveData(application);
    }

    @Override
    protected void onCleared() {
        event.unregisterContentObserver();
        super.onCleared();
    }

    public @NonNull
    EventLiveData getEvent() {
        return event;
    }

}
