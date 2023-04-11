package de.dlyt.yanndroid.dualwallpaper.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import de.dlyt.yanndroid.dualwallpaper.Preferences;
import de.dlyt.yanndroid.dualwallpaper.trigger.ThemeTrigger;
import de.dlyt.yanndroid.dualwallpaper.trigger.TimeTrigger;

public class TriggerUtil {

    public static void startThemeTrigger(Context context) {
        stopTimeTrigger(context);

        context.startForegroundService(new Intent(context, ThemeTrigger.class));
    }

    public static void startTimeTrigger(Context context) {
        stopAll(context);

        //TODO AlarmManager | JobScheduler | WorkManager ?

        Preferences preferences = new Preferences(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getNextTimeMillis(preferences.getScheduleStart()), AlarmManager.INTERVAL_DAY, getPendingIntent(context, true));
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getNextTimeMillis(preferences.getScheduleEnd()), AlarmManager.INTERVAL_DAY, getPendingIntent(context, false));

        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int startTime = preferences.getScheduleStart();
        int endTime = preferences.getScheduleEnd();
        new WallpaperUtil(context).loadWallpapers((timeOfDay < endTime && startTime < timeOfDay) || (endTime < startTime && (startTime < timeOfDay || timeOfDay < endTime)));
    }

    public static void stopAll(Context context) {
        stopThemeTrigger(context);
        stopTimeTrigger(context);
    }

    private static void stopThemeTrigger(Context context) {
        context.stopService(new Intent(context, ThemeTrigger.class));
    }

    private static void stopTimeTrigger(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context, true));
        alarmManager.cancel(getPendingIntent(context, false));
    }

    private static long getNextTimeMillis(int time) {
        Calendar calendar = Calendar.getInstance();
        long timeNow = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, time / 60);
        calendar.set(Calendar.MINUTE, time % 60);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() - timeNow <= 0) calendar.add(Calendar.DATE, 1);
        return calendar.getTimeInMillis();
    }

    private static PendingIntent getPendingIntent(Context context, boolean night) {
        Intent intent = new Intent(context, TimeTrigger.class);
        intent.setAction(night ? TimeTrigger.ACTION_SET_NIGHT : TimeTrigger.ACTION_SET_DAY);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

}
