package de.dlyt.yanndroid.dualwallpaper;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.File;

public class LiveWallpaper extends WallpaperService {

    private WallpaperUtil wallpaperUtil;
    private DynamicThemeEngine dynamicThemeEngine;

    @Override
    public Engine onCreateEngine() {
        wallpaperUtil = new WallpaperUtil(this);
        return dynamicThemeEngine = new DynamicThemeEngine(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (wallpaperUtil.updateDarkMode(newConfig)) {
            wallpaperUtil.loadWallpaper(false);
        }
        dynamicThemeEngine.update(newConfig);
    }

    private class DynamicThemeEngine extends Engine {
        private final Handler handler = new Handler();
        private boolean portrait;
        private boolean visible = true;        private final Runnable runnable = this::draw;
        public DynamicThemeEngine(Context context) {
            update(context.getResources().getConfiguration());
        }

        public void update(Configuration config) {
            this.portrait = config.orientation == Configuration.ORIENTATION_PORTRAIT;
            handler.post(runnable);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            String wallpaperPath = wallpaperUtil.getWallpaperPath(true);
            if (!new File(wallpaperPath).exists()) return;
            try {
                canvas = holder.lockCanvas();
                Bitmap bMap = BitmapFactory.decodeFile(wallpaperPath);
                Rect surfaceFrame = holder.getSurfaceFrame();

                int cropH = !portrait ? 0 : (bMap.getWidth() - ((bMap.getHeight() / surfaceFrame.height()) * surfaceFrame.width())) / 2;
                int cropV = portrait ? 0 : (bMap.getHeight() - ((bMap.getWidth() / surfaceFrame.width()) * surfaceFrame.height())) / 2;

                BitmapDrawable d = new BitmapDrawable(bMap);
                d.setBounds(-cropH, -cropV, surfaceFrame.width() + cropH, surfaceFrame.height() + cropV);
                d.draw(canvas);
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
            handler.removeCallbacks(runnable);
            if (visible) {
                handler.post(runnable);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(runnable);
            } else {
                handler.removeCallbacks(runnable);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(runnable);
        }




    }

}