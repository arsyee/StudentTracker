package hu.fallen.studenttracker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import hu.fallen.studenttracker.model.Event;
import hu.fallen.studenttracker.model.EventModel;
import timber.log.Timber;

public class EventActivity extends BaseActivity {

    public enum EXTRA_KEY {
        URI("uri"),
        START_TIME("startTime"),
        END_TIME("endTime");
        private final String str;
        EXTRA_KEY(String status) {
            str = status;
        }
        @Override
        public String toString() {
            return str;
        }
    }

    private EventModel mEventModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mEventModel = ViewModelProviders.of(this).get(EventModel.class);
        mEventModel.getEvent().observe(this, new Observer<Event>() {
            @Override
            public void onChanged(@Nullable Event event) {
                updateUI();
            }
        });

        Intent intent = getIntent();
        for (EXTRA_KEY key : EXTRA_KEY.values()) {
            Timber.d("EventActivity started: %s: %s", key, intent.getSerializableExtra(key.toString()));
        }
        if (intent.hasExtra(EXTRA_KEY.URI.toString())) {
            mEventModel.getEvent().replaceWith(intent.getStringExtra(EXTRA_KEY.URI.toString()));
        } else {
            updateEventFromExtras(intent);
        }
    }

    private void updateEventFromExtras(Intent intent) {
        if (intent.hasExtra(EXTRA_KEY.START_TIME.toString())) {
            Calendar startTime = (Calendar) intent.getSerializableExtra(EXTRA_KEY.START_TIME.toString());
            mEventModel.getEvent().getValue().setStartTime(startTime);
            mEventModel.getEvent().notifyClientChange();
        }
    }

    private void updateUI() {
        Event event = mEventModel.getEvent().getValue();
        if (event == null) return; // TODO: this is where I should clear the UI, but I think here I'd have more issues than that
        if (event.getStartTime() != null) {
            ((TextView) findViewById(R.id.start_date)).setText(String.format("%1$tF", event.getStartTime()));
            ((TextView) findViewById(R.id.start_time)).setText(String.format("%1$tT", event.getStartTime()));
        }
        if (event.getEndTime() != null) {
            ((TextView) findViewById(R.id.end_date)).setText(String.format("%1$tF", event.getEndTime()));
            ((TextView) findViewById(R.id.end_time)).setText(String.format("%1$tT", event.getEndTime()));
        }
    }
}
