package hu.fallen.studenttracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import hu.fallen.studenttracker.model.Calendar;
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

        Intent intent = getIntent();
        for (EXTRA_KEY key : EXTRA_KEY.values()) {
            Timber.d("EventActivity started: %s: %s", key, intent.getSerializableExtra(key.toString()));
        }
        if (intent.hasExtra(EXTRA_KEY.URI.toString())) {
            updateFromUri(intent);
        } else {
            updateEventFromExtras(intent);
        }
    }

    private void updateFromUri(Intent intent) {
        // intent.getStringExtra(EXTRA_KEY.URI.toString());
        updateEventFromExtras(intent); // call this after update
    }

    private void updateEventFromExtras(Intent intent) {
        if (intent.hasExtra(EXTRA_KEY.START_TIME.toString())) {
            java.util.Calendar startTime = (java.util.Calendar) intent.getSerializableExtra(EXTRA_KEY.START_TIME.toString());
            ((TextView) findViewById(R.id.start_date)).setText(String.format("%1$tF", startTime));
            ((TextView) findViewById(R.id.start_time)).setText(String.format("%1$tT", startTime));
        }
        if (intent.hasExtra(EXTRA_KEY.END_TIME.toString())) {
            java.util.Calendar endTime = (java.util.Calendar) intent.getSerializableExtra(EXTRA_KEY.END_TIME.toString());
            ((TextView) findViewById(R.id.end_date)).setText(String.format("%1$tF", endTime));
            ((TextView) findViewById(R.id.end_time)).setText(String.format("%1$tT", endTime));
        }
    }

}
