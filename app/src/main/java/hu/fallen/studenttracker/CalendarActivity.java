package hu.fallen.studenttracker;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

public class CalendarActivity extends BaseActivity {

    private WeekView mWeekView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Get a reference for the week view in the layout.
        mWeekView = prepareWeekView(R.id.weekView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private WeekView prepareWeekView(int layoutId) {
        WeekView weekView = findViewById(layoutId);

        weekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                Timber.d("Event clicked...");
            }
        });

        weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            class MyWeekViewEvent extends WeekViewEvent {
                @Override
                public String toString() {
                    return String.format("Event<%1$tF %1$tT - %2$tF %2$tT : %3$s (%4$x)>", getStartTime(), getEndTime(), getName(), getColor());
                }
            }

            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                Timber.d("Month changed to: %d-%d", newYear, newMonth);
                Random rnd = new Random();
                String[] names = {"Alice Apple", "Ben Banana", "Charlie Cherry", "Daniel Damson"};
                List<WeekViewEvent> monthlyList = new ArrayList<>();
                for (int day = 1; day < 28; ++day) {
                    int numEvents = rnd.nextInt(5);
                    for (int i = 0; i < numEvents; ++i) {
                        WeekViewEvent event = new MyWeekViewEvent();
                        event.setStartTime(Calendar.getInstance());
                        event.getStartTime().set(newYear, newMonth-1, day, rnd.nextInt(24), rnd.nextInt(4) * 15);
                        event.setEndTime((Calendar) event.getStartTime().clone());
                        event.getEndTime().add(Calendar.MINUTE, (rnd.nextInt(11) + 2) * 15);
                        event.setName(names[rnd.nextInt(names.length)]);
                        if (event.getEndTime().compareTo(Calendar.getInstance()) > 0) {
                            event.setColor(getResources().getColor(R.color.futureEventColor));
                        } else {
                            event.setColor(getResources().getColor(R.color.pastEventColor));
                        }
                        Timber.d("Event created: %s", event);
                        monthlyList.add(event);
                    }
                }
                return monthlyList;
            }
        });

        weekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                Timber.d("Long event press...");
            }
        });

        weekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
            @Override
            public void onEmptyViewClicked(Calendar time) {
                Timber.d("User clicked this time: %tc", time);
            }
        });

        weekView.setEmptyViewLongPressListener(new WeekView.EmptyViewLongPressListener() {
            @Override
            public void onEmptyViewLongPress(Calendar time) {
                Timber.d("User pressed this time: %tc", time);
            }
        });

        weekView.goToHour(Math.max(0, Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 1));

        return weekView;
    }

}
