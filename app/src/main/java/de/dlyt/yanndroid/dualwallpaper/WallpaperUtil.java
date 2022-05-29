package de.dlyt.yanndroid.dualwallpaper;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WallpaperUtil {

    private Context context;
    private WallpaperManager wallpaperManager;

    public WallpaperUtil(Context context) {
        this.context = context;
        this.wallpaperManager = WallpaperManager.getInstance(context);
    }

    public void saveWallpaper(boolean homeScreen, boolean lightMode) {
        if (!hasPermission()) return;
        saveInputStream(new ParcelFileDescriptor.AutoCloseInputStream(wallpaperManager.getWallpaperFile(homeScreen ? WallpaperManager.FLAG_SYSTEM : WallpaperManager.FLAG_LOCK)), new File(getWallpaperPath(homeScreen, lightMode)));
    }

    public void loadWallpaper(boolean homeScreen, boolean lightMode) {
        try {
            InputStream inputStream = loadInputStream(new File(getWallpaperPath(homeScreen, lightMode)));
            if (inputStream != null)
                wallpaperManager.setStream(inputStream, null, true, homeScreen ? WallpaperManager.FLAG_SYSTEM : WallpaperManager.FLAG_LOCK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWallpaperPath(boolean homeScreen, boolean lightMode) {
        return context.getFilesDir().getPath() + "/wallpaper_" + (homeScreen ? "home" : "lock") + "_" + (lightMode ? "light" : "dark") + ".jpg";
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private InputStream loadInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveInputStream(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
