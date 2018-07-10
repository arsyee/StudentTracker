package hu.fallen.studenttracker;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
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
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                Timber.d("Event clicked: %d", event.getId());
                showEvent(event);
            }
        });

        weekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(final WeekViewEvent event, RectF eventRect) {
                Timber.d("Event long clicked on %d", event.getId());
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                builder.setTitle(R.string.event_action);
                StringBuilder sb = new StringBuilder(getString(R.string.select_action_event));
                sb.append("\nTitle: ").append(event.getName());
                sb.append("\nStart: ").append(String.format("%1$tF %1$tT", event.getStartTime()));
                sb.append("\nEnd: ").append(String.format("%1$tF %1$tT", event.getEndTime()));
                builder.setMessage(sb.toString());
                builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showEvent(event);
                    }
                });
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEvent(event);
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // pass
                    }
                });
                builder.create().show();
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
            public void onEmptyViewLongPress(final java.util.Calendar time) {
                Timber.d("User pressed this time: %1$tF %1$tT", time);
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                builder.setTitle(R.string.create_event_action);
                builder.setMessage(R.string.create_event_text);
                builder.setPositiveButton("Lesson", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createLesson(time);
                    }
                });
                builder.setNegativeButton("Calendar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createRegular(time);
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // pass
                    }
                });
                builder.create().show();
            }
        });

        weekView.goToHour(Math.max(0, java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) - 1));

        return weekView;
    }

    private void createLesson(java.util.Calendar startTime) {

    }

    private void createRegular(java.util.Calendar selectedTime) {
        Intent intent = new Intent(Intent.ACTION_EDIT, CalendarContract.Events.CONTENT_URI);
        java.util.Calendar startTime = (java.util.Calendar) selectedTime.clone();
        startTime.set(java.util.Calendar.MINUTE, 15 * (startTime.get(java.util.Calendar.MINUTE) / 15));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTimeInMillis());
        java.util.Calendar endTime = (java.util.Calendar) startTime.clone();
        endTime.add(java.util.Calendar.HOUR, 1);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
        Timber.d("Firing create new event: %s -> %s", String.format("%1$tF %1$tT", startTime), String.format("%1$tF %1$tT", endTime));
        startActivity(intent);
    }

    private void showEvent(WeekViewEvent weekViewEvent) {
        Uri eventUri = CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(Long.toString(weekViewEvent.getId())).build();
        String studentCalendarId = PreferenceManager.getDefaultSharedPreferences(CalendarActivity.this).getString("calendar", null);
        Event event = mEventModel.getEventById(Long.toString(weekViewEvent.getId()));
        Timber.d("Opening event: %s (%s - %s)", eventUri, event.get(Event.Data.CALENDAR_ID), studentCalendarId);
        Intent intent;
        if (studentCalendarId != null && studentCalendarId.equals(event.get(Event.Data.CALENDAR_ID))) {
            intent = new Intent(CalendarActivity.this, EventActivity.class);
            intent.putExtra("uri", eventUri.toString());
        } else {
            intent = new Intent(Intent.ACTION_VIEW, eventUri);
        }
        startActivity(intent);
    }

    private void deleteEvent(WeekViewEvent weekViewEvent) {
        Uri eventUri = CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(Long.toString(weekViewEvent.getId())).build();
        Timber.d("Now I should delete this event: %s", eventUri);
        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(R.string.confirm_delete_event);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Timber.d("No I will really delete it...");
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // pass
            }
        });
        builder.create().show();
    }
}
