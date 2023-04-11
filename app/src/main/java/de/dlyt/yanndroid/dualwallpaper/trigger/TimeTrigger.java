package de.dlyt.yanndroid.dualwallpaper.trigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
}