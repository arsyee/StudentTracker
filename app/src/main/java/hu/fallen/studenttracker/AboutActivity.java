package hu.fallen.studenttracker;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.Map;

import timber.log.Timber;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        printPreferences();
    }

    public void clearPreferences(View view) {
        Timber.d("Clearing preferences");
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        printPreferences();
    }

    private void printPreferences() {
        Timber.d("Preference list:");
        Map<String, ?> prefs = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        for(Map.Entry<String,?> entry : prefs.entrySet()) {
            Timber.d("map values: %s - %s",entry.getKey(),  entry.getValue());
        }
    }
}
