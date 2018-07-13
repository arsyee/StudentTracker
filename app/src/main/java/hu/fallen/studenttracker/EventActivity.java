package hu.fallen.studenttracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;

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
    }

}
