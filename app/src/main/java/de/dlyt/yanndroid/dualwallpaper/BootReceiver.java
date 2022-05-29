package de.dlyt.yanndroid.dualwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BootReceiver", intent.getAction());
        if (context.getSharedPreferences("sp", Context.MODE_PRIVATE).getBoolean("use_service_switch", false))
            context.startForegroundService(new Intent(context, WallpaperService.class));
    }
}
