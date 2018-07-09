package hu.fallen.studenttracker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hu.fallen.studenttracker.model.ScheduleModel;
import timber.log.Timber;

public class CalendarActivity extends BaseActivity {

    private WeekView mWeekView;
    private ScheduleModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Get a reference for the week view in the layout.
        mWeekView = prepareWeekView(R.id.weekView);
        mModel = ViewModelProviders.of(this).get(ScheduleModel.class);
        mModel.getSchedule().observe(this, new Observer<List<WeekViewEvent>>() {
            @Override
            public void onChanged(@Nullable List<WeekViewEvent> weekViewEvents) {
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
        WeekView weekView = findViewById(layoutId);

        weekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                Timber.d("Event clicked...");
            }
        });

        weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                Timber.d("Month changed to: %d-%d", newYear, newMonth);
                List<WeekViewEvent> monthlyList = new ArrayList<>();
                for (WeekViewEvent event : mModel.getSchedule().getValue()) {
                    if (event.getStartTime().get(Calendar.YEAR) == newYear && event.getStartTime().get(Calendar.MONTH) + 1 == newMonth) {
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
                mModel.getSchedule().remove(event);
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
                mModel.getSchedule().add(time);
            }
        });

        weekView.goToHour(Math.max(0, Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 1));

        return weekView;
    }

}
