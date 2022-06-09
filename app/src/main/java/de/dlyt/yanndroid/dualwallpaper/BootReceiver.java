package de.dlyt.yanndroid.dualwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BootReceiver", intent.getAction());
        if (PreferenceManager.getDefaultSharedPreferences(context).getString("main_pref", "off").equals("wps"))
            context.startForegroundService(new Intent(context, WallpaperService.class));
    }
}
