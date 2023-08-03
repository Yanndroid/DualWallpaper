package de.dlyt.yanndroid.dualwallpaper.trigger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil;

public class ThemeTrigger extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "4000";

    private WallpaperUtil mWallpaperUtil;
    private boolean mIsDarkMode = false;

    public static void start(Context context) {
        context.startForegroundService(new Intent(context, ThemeTrigger.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ThemeTrigger.class));
    }

    @Override
    public void onCreate() {
        mWallpaperUtil = new WallpaperUtil(this);
        mIsDarkMode = isNowDark(getResources().getConfiguration());

        mWallpaperUtil.loadWallpapers(mIsDarkMode);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(3000, buildForegroundNotification());
        return START_STICKY;
    }

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_oui_wallpaper_outline)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_description))
                .setContentIntent(getNotificationSettingsIntent())
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build();
    }

    private PendingIntent getNotificationSettingsIntent() {
        return PendingIntent.getActivity(
                ThemeTrigger.this,
                0,
                new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                        .putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (updateDarkMode(newConfig)) mWallpaperUtil.loadWallpapers(mIsDarkMode);
    }

    private boolean updateDarkMode(Configuration newConfig) {
        boolean newIsDarkMode = isNowDark(newConfig);
        if (mIsDarkMode != newIsDarkMode) {
            mIsDarkMode = newIsDarkMode;
            return true;
        }
        return false;
    }

    public static boolean isNowDark(Configuration configuration) {
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}