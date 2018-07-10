package hu.fallen.studenttracker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.List;

import hu.fallen.studenttracker.model.CalendarModel;
import hu.fallen.studenttracker.model.Event;
import hu.fallen.studenttracker.model.Calendar;
import hu.fallen.studenttracker.model.EventModel;
import timber.log.Timber;

public class CalendarActivity extends BaseActivity {

    private WeekView mWeekView;
    private EventModel mEventModel;
    private CalendarModel mCalendarModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Get a reference for the week view in the layout.
        mWeekView = prepareWeekView(R.id.weekView);
        mEventModel = ViewModelProviders.of(this).get(EventModel.class);
        mCalendarModel = ViewModelProviders.of(this).get(CalendarModel.class);
        mCalendarModel.getCalendars().observe(this, new Observer<List<Calendar>>() {
            @Override
            public void onChanged(@Nullable List<Calendar> calendars) {
                mWeekView.notifyDatasetChanged();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWeekView.notifyDatasetChanged();
            }
        });
    }

    private WeekView prepareWeekView(int layoutId) {
        final WeekView weekView = findViewById(layoutId);

        weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                Timber.d("Month changed to: %d-%d", newYear, newMonth);
                List<WeekViewEvent> monthlyList = new ArrayList<>();
                Observer<List<Event>> observer = new Observer<List<Event>>() {
                    @Override
                    public void onChanged(@Nullable List<Event> events) {
                        mWeekView.notifyDatasetChanged();
                    }
                };
                for (Event event : mEventModel.getSchedule(newYear, newMonth, CalendarActivity.this, observer).getValue()) {
                    monthlyList.add(createWeekViewEvent(event));
                }
                return monthlyList;
            }

            private WeekViewEvent createWeekViewEvent(Event event) {
                WeekViewEvent weekViewEvent = new WeekViewEvent(
                        Integer.parseInt(event.get(Event.Data._ID)),
                        event.get(Event.Data.TITLE),
                        event.getStartTime(),
                        event.getEndTime()
                );
                for (Calendar calendar : mCalendarModel.getCalendars().getValue()) {
                    if (calendar.get(Calendar.Data._ID).equals(event.get(Event.Data.CALENDAR_ID))) {
                        weekViewEvent.setColor(Integer.parseInt(calendar.get(Calendar.Data.COLOR)));
                    }
                }
                return weekViewEvent;
            }
        });

        weekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent weekViewEvent, RectF eventRect) {
                Uri eventUri = CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(Long.toString(weekViewEvent.getId())).build();
                String studentCalendarId = PreferenceManager.getDefaultSharedPreferences(CalendarActivity.this).getString("calendar", null);
                Event event = mEventModel.getEventById(Long.toString(weekViewEvent.getId()));
                Timber.d("Event clicked: %s (%s - %s)", eventUri, event.get(Event.Data.CALENDAR_ID), studentCalendarId);
                Intent intent;
                if (studentCalendarId != null && studentCalendarId.equals(event.get(Event.Data.CALENDAR_ID))) {
                    intent = new Intent(CalendarActivity.this, EventActivity.class);
                    intent.putExtra("uri", eventUri.toString());
                } else {
                    intent = new Intent(Intent.ACTION_VIEW, eventUri);
                }
                startActivity(intent);
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
            public void onEmptyViewClicked(java.util.Calendar time) {
                Timber.d("User clicked this time: %tc", time);
            }
        });

        weekView.setEmptyViewLongPressListener(new WeekView.EmptyViewLongPressListener() {
            @Override
            public void onEmptyViewLongPress(java.util.Calendar time) {
                Timber.d("User pressed this time: %tc", time);
            }
        });

        weekView.goToHour(Math.max(0, java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) - 1));

        return weekView;
    }

}
