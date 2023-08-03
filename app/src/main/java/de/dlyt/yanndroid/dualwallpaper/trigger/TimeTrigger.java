package de.dlyt.yanndroid.dualwallpaper.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import de.dlyt.yanndroid.dualwallpaper.Preferences;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil;

public class TimeTrigger extends BroadcastReceiver {

    public static final String ACTION_SET_NIGHT = "de.dlyt.yanndroid.dualwallpaper.SET_NIGHT";
    public static final String ACTION_SET_DAY = "de.dlyt.yanndroid.dualwallpaper.SET_DAY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;

        WallpaperUtil wallpaperUtil = new WallpaperUtil(context);

        if (intent.getAction().equals(ACTION_SET_NIGHT)) {
            wallpaperUtil.loadWallpapers(true);
        } else if (intent.getAction().equals(ACTION_SET_DAY)) {
            wallpaperUtil.loadWallpapers(false);
        }
    }

    public static void start(Context context) {
        //TODO AlarmManager || JobScheduler || WorkManager ?

        Preferences preferences = new Preferences(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getNextTimeMillis(preferences.getScheduleStart()), AlarmManager.INTERVAL_DAY, getTriggerIntent(context, true));
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getNextTimeMillis(preferences.getScheduleEnd()), AlarmManager.INTERVAL_DAY, getTriggerIntent(context, false));

        //TODO fix activity reload loop
        new WallpaperUtil(context).loadWallpapers(isNowDark(preferences));
    }

    public static void stop(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getTriggerIntent(context, true));
        alarmManager.cancel(getTriggerIntent(context, false));
    }

    public static boolean isNowDark(Preferences preferences) {
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int startTime = preferences.getScheduleStart();
        int endTime = preferences.getScheduleEnd();
        return (timeOfDay < endTime && startTime < timeOfDay) || (endTime < startTime && (startTime < timeOfDay || timeOfDay < endTime));
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

    private static PendingIntent getTriggerIntent(Context context, boolean night) {
        Intent intent = new Intent(context, TimeTrigger.class);
        intent.setAction(night ? TimeTrigger.ACTION_SET_NIGHT : TimeTrigger.ACTION_SET_DAY);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}