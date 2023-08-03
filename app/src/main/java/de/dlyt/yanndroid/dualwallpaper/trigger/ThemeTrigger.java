package de.dlyt.yanndroid.dualwallpaper.trigger;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil;

public class ThemeTrigger extends Service {

    public static final String CHANNEL_ID = "4000";

    private WallpaperUtil mWallpaperUtil;
    private boolean mIsDarkMode = false;

    @Override
    public void onCreate() {
        mWallpaperUtil = new WallpaperUtil(this);
        updateDarkMode(getResources().getConfiguration());
        mWallpaperUtil.loadWallpapers(mIsDarkMode);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(3000, new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_oui_wallpaper_outline)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_description))
                .setContentIntent(PendingIntent.getActivity(
                        ThemeTrigger.this,
                        0,
                        new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID, "4000"),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build());
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (updateDarkMode(newConfig)) mWallpaperUtil.loadWallpapers(mIsDarkMode);
    }

    private boolean updateDarkMode(Configuration newConfig) {
        boolean newIsDarkMode = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (mIsDarkMode != newIsDarkMode) {
            mIsDarkMode = newIsDarkMode;
            return true;
        }
        return false;
    }
}