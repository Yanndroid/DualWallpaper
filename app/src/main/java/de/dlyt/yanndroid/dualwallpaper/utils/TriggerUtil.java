package de.dlyt.yanndroid.dualwallpaper.utils;

import android.content.Context;

import de.dlyt.yanndroid.dualwallpaper.trigger.ThemeTrigger;
import de.dlyt.yanndroid.dualwallpaper.trigger.TimeTrigger;

public class TriggerUtil {

    public static void startThemeTrigger(Context context) {
        stopTimeTrigger(context);

        ThemeTrigger.start(context);
    }

    public static void startTimeTrigger(Context context) {
        stopAll(context);

        TimeTrigger.start(context);
    }

    public static void stopAll(Context context) {
        stopThemeTrigger(context);
        stopTimeTrigger(context);
    }

    private static void stopThemeTrigger(Context context) {
        ThemeTrigger.stop(context);
    }

    private static void stopTimeTrigger(Context context) {
        TimeTrigger.stop(context);
    }


}
