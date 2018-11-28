package dev.eder.oreogeofence;

import android.content.Context;

import dev.eder.oreogeofence.model.Reminder;

public class ReminderRepository {

    private Context context;

    public ReminderRepository(Context context) {
        this.context = context;
    }

    private static String PREFS_NAME = "ReminderRepository";

    private static String REMINDERS = "REMINDERS";

}
