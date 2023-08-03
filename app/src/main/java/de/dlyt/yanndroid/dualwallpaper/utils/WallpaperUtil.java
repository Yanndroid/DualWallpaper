package de.dlyt.yanndroid.dualwallpaper.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.dlyt.yanndroid.dualwallpaper.Preferences;
import de.dlyt.yanndroid.dualwallpaper.trigger.ThemeTrigger;
import de.dlyt.yanndroid.dualwallpaper.trigger.TimeTrigger;

public class WallpaperUtil {

    public enum WallpaperType {
        HOME_LIGHT(true, true, 0), HOME_DARK(true, false, 1), LOCK_LIGHT(false, true, 2), LOCK_DARK(false, false, 3);
        public final boolean home;
        public final boolean light;
        public final String fileName;
        public final int index;

        WallpaperType(boolean home, boolean light, int index) {
            this.index = index;
            this.home = home;
            this.light = light;
            this.fileName = String.format("/wallpaper_%s_%s", home ? "home" : "lock", light ? "light" : "dark");
        }
    }

    public interface SaveCallback {
        void onSuccess(WallpaperType type);

        void onError(Exception e);
    }

    private Context mContext;
    private WallpaperManager mWallpaperManager;

    public WallpaperUtil(Context context) {
        this.mContext = context;
        this.mWallpaperManager = WallpaperManager.getInstance(context);
    }

    public String getWallpaperTypePath(WallpaperType type) {
        return mContext.getFilesDir().getPath() + type.fileName;
    }

    public void loadWallpapers(boolean darkMode) {
        for (WallpaperType wallpaperType : WallpaperType.values()) {
            if (wallpaperType.light != darkMode) loadWallpaper(wallpaperType);
        }
    }

    private void loadWallpaper(WallpaperType type) {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(getWallpaperTypePath(type)));
            mWallpaperManager.setStream(inputStream, null, false, type.home ? WallpaperManager.FLAG_SYSTEM : WallpaperManager.FLAG_LOCK);
            //TODO notify system ?
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveFromCurrent(WallpaperType type, SaveCallback callback) {
        try {
            ParcelFileDescriptor fileDescriptor = mWallpaperManager.getWallpaperFile(type.home ? WallpaperManager.FLAG_SYSTEM : WallpaperManager.FLAG_LOCK);
            if (fileDescriptor == null) throw new FileNotFoundException();

            saveFromBitmap(streamToBitmap(new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor)), type, true, false, callback);
        } catch (SecurityException | FileNotFoundException e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    public void saveFromIntent(Intent intent, WallpaperType type, SaveCallback callback) {
        try {
            Uri data = intent.getData();
            if (data == null) throw new FileNotFoundException();
            saveFromBitmap(streamToBitmap(mContext.getContentResolver().openInputStream(data)), type, true, false, callback);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    public void saveFromBitmap(Bitmap bitmap, WallpaperType type, boolean scaleToScreen, boolean png, SaveCallback callback) {
        Handler handler = new Handler();
        new Thread(() -> {

            Bitmap input = scaleToScreen ? scaleToScreenSize(bitmap) : bitmap;
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(getWallpaperTypePath(type));
                input.compress(png ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, output);
                handler.post(() -> callback.onSuccess(type));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                handler.post(() -> callback.onError(e));
            } finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            onFileChanged(type);

        }).start();
    }

    private Bitmap streamToBitmap(InputStream in) {
        return BitmapFactory.decodeStream(in);
    }

    private Bitmap scaleToScreenSize(Bitmap bitmap) {
        Point size = DeviceUtil.getDisplaySize(mContext);
        double scale = Math.max((double) size.x / (double) bitmap.getWidth(), (double) size.y / (double) bitmap.getHeight());
        return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), true);
    }

    private void onFileChanged(WallpaperType type) {
        Preferences preferences = new Preferences(mContext);
        if (preferences.isEnabled()) {
            boolean isNowDark = preferences.changeWithTheme() ? ThemeTrigger.isNowDark(mContext.getResources().getConfiguration()) : TimeTrigger.isNowDark(preferences);

            if (type.light != isNowDark) {
                loadWallpaper(type);
            }
        }
    }
}
