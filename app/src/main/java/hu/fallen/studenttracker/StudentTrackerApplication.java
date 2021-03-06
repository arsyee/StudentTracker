package hu.fallen.studenttracker;

import android.app.Application;
import android.support.annotation.NonNull;

import timber.log.Timber;

public class StudentTrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new NotLoggingTree());
        }
    }

    private class NotLoggingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {

        }
    }
}
