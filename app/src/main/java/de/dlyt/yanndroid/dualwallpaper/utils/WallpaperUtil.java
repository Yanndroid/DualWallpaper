package de.dlyt.yanndroid.dualwallpaper.utils;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import de.dlyt.yanndroid.dualwallpaper.Preferences;
import de.dlyt.yanndroid.dualwallpaper.R;

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

    private Context mContext;
    private WallpaperManager mWallpaperManager;

    public WallpaperUtil(Context context) {
        this.mContext = context;
        this.mWallpaperManager = WallpaperManager.getInstance(context);
    }

    public String getPathForWallpaper(WallpaperType type) {
        return mContext.getFilesDir().getPath() + type.fileName;
    }

    public void loadWallpapers(boolean darkMode) {
        if (darkMode) {
            loadWallpaper(WallpaperUtil.WallpaperType.HOME_DARK);
            loadWallpaper(WallpaperUtil.WallpaperType.LOCK_DARK);
        } else {
            loadWallpaper(WallpaperUtil.WallpaperType.HOME_LIGHT);
            loadWallpaper(WallpaperUtil.WallpaperType.LOCK_LIGHT);
        }
    }

    public void loadWallpaper(WallpaperType type) {
        try {
            InputStream inputStream = new FileInputStream(getPathForWallpaper(type));
            mWallpaperManager.setStream(inputStream, null, false, type.home ? WallpaperManager.FLAG_SYSTEM : WallpaperManager.FLAG_LOCK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFromCurrent(WallpaperType type) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ParcelFileDescriptor fileDescriptor = mWallpaperManager.getWallpaperFile(type.home ? WallpaperManager.FLAG_SYSTEM : WallpaperManager.FLAG_LOCK);
            if (fileDescriptor == null) {
                Toast.makeText(mContext, R.string.wallpaper_not_supported, Toast.LENGTH_SHORT).show();
                return;
            }
            //saveToFile(new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor), new File(getPathForWallpaper(type)));
            saveFromBitmap(streamToBitmap(new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor)), type, true, false);
        }
    }

    public void saveFromUri(Uri wallpaperUri, WallpaperType type) throws FileNotFoundException {
        //saveToFile(context.getContentResolver().openInputStream(wallpaperUri), new File(getPathForWallpaper(type)));
        //onFileChanged(type);
        saveFromBitmap(streamToBitmap(mContext.getContentResolver().openInputStream(wallpaperUri)), type, true, false);
    }

    private Bitmap streamToBitmap(InputStream in) {
        return BitmapFactory.decodeStream(in);
    }

    private Bitmap scaleToScreenSize(Bitmap bitmap) {
        Point size = DeviceUtil.getDisplaySize(mContext);
        double scale = Math.max((double) size.x / (double) bitmap.getWidth(), (double) size.y / (double) bitmap.getHeight());
        return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), true);
    }

    public void saveFromBitmap(Bitmap bitmap, WallpaperType type, boolean crop, boolean png) {
        bitmap = scaleToScreenSize(bitmap);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getPathForWallpaper(type));
            bitmap.compress(png ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        onFileChanged(type);
    }

    /*private void saveToFile(InputStream in, File file) {
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
                    out.flush();
                    out.close();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    private void onFileChanged(WallpaperType type) {
        Preferences preferences = new Preferences(mContext);
        if (preferences.isEnabled()) {
            if (preferences.changeWithTheme()) {
                if (((mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) == type.light) {
                    loadWallpaper(type);
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                int startTime = preferences.getScheduleStart();
                int endTime = preferences.getScheduleEnd();
                if (((endTime < timeOfDay && timeOfDay < startTime) || (startTime < endTime && (timeOfDay < startTime || endTime < timeOfDay))) == type.light) {
                    loadWallpaper(type);
                }
            }
        }
    }
}
