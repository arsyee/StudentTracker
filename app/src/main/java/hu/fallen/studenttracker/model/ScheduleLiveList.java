package hu.fallen.studenttracker.model;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import hu.fallen.studenttracker.R;
import timber.log.Timber;

public class ScheduleLiveList extends MutableLiveData<List<WeekViewEvent>> {
    private final Context context;

    ScheduleLiveList(Context context) {
        this.context = context;
        setValue(new ArrayList<WeekViewEvent>());
        loadSchedule();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadSchedule() {
        new AsyncTask<Void, Void, List<WeekViewEvent>>() {
            @Override
            protected List<WeekViewEvent> doInBackground(Void... voids) {
                Random rnd = new Random();
                List<WeekViewEvent> monthlyList = new ArrayList<>();
                for (int day = 5; day < 12; ++day) {
                    int numEvents = rnd.nextInt(5);
                    for (int newYear = 2018; newYear <= 2018; ++newYear) {
                        for (int newMonth = 7 - 0; newMonth <= 7 + 0; ++newMonth) {
                            for (int i = 0; i < numEvents; ++i) {
                                Calendar startTime = Calendar.getInstance();
                                startTime.set(newYear, newMonth - 1, day, rnd.nextInt(24), rnd.nextInt(4) * 15);
                                monthlyList.add(createEvent(startTime, (rnd.nextInt(11) + 2) * 15));
                            }
                        }
                    }
                }
                return monthlyList;
            }

            @Override
            protected void onPostExecute(List<WeekViewEvent> students) {
                setValue(students);
            }
        }.execute();
    }

    private static int counter = 0;
    private WeekViewEvent createEvent(Calendar start, int length) {
        Random rnd = new Random();
        String[] names = {"Alice Apple", "Ben Banana", "Charlie Cherry", "Daniel Damson"};
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MINUTE, length);
        WeekViewEvent event = new WeekViewEvent(counter++, names[rnd.nextInt(names.length)], start, end);
        if (event.getEndTime().compareTo(Calendar.getInstance()) > 0) {
            event.setColor(context.getResources().getColor(R.color.futureEventColor));
        } else {
            event.setColor(context.getResources().getColor(R.color.pastEventColor));
        }
        Timber.d("Creating: %s", str(event));
        return event;
    }

    public void add(Calendar time) {
        List<WeekViewEvent> value = getValue();
        value.add(createEvent(time, 120));
        setValue(value);
        Timber.d("Number of events: %d", value.size());
    }

    public void remove(WeekViewEvent event) {
        List<WeekViewEvent> value = getValue();
        Timber.d("Removing %s (%s)", str(event), value.contains(event));
        value.remove(event);
        setValue(value);
        Timber.d("Number of events: %d", value.size());
    }

    private String str(WeekViewEvent e) {
        return String.format("Event<%5$d: %1$tF %1$tT - %2$tF %2$tT : %3$s (%4$x)>", e.getStartTime(), e.getEndTime(), e.getName(), e.getColor(), e.getId());
    }
}
