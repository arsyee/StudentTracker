package hu.fallen.studenttracker.misc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import hu.fallen.studenttracker.SettingsActivity;
import timber.log.Timber;

// utilities to check and ensure everything is set up for this app
public class Config {

    // checkPermissions is only used by SettingsActivity, so I don't disable settings when only those are missing
    public static boolean checkPermissions(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Timber.i("READ_CONTACTS not granted!");
            return false;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Timber.i("WRITE_CONTACTS not granted!");
            return false;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Timber.i("READ_CALENDAR not granted!");
            return false;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Timber.i("WRITE_CALENDAR not granted!");
            return false;
        }
        return true;
    }

    private static boolean check(Context context, View view) {
        if (!checkPermissions(context)) {
            configSnack(view);
            return false;
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getString("group", null) == null) {
            Timber.i("Configuration missing: group");
            configSnack(view);
            return false;
        }
        return true;
    }

    private static void configSnack(final View view) {
        String message = "Please configure app first!";
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Settings", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(view.getContext(), SettingsActivity.class);
                    view.getContext().startActivity(intent);
                }
            });
            snackbar.show();
        }
    }

    public static boolean check(Context context) {
        View view = null;
        if (context instanceof Activity) {
            view = ((Activity) context).findViewById(android.R.id.content);
        }
        return check(context, view);
    }

    public static boolean check(View view) {
        return check(view.getContext(), view);
    }

}
