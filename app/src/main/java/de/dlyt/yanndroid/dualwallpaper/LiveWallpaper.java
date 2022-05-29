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

public class LiveWallpaper extends WallpaperService {

    private WallpaperUtil wallpaperUtil;
    private DynamicThemeEngine dynamicThemeEngine;
    private int uiMode = -1;

    @Override
    public Engine onCreateEngine() {
        wallpaperUtil = new WallpaperUtil(this);
        return dynamicThemeEngine = new DynamicThemeEngine(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (uiMode != newConfig.uiMode) {
            //dynamicThemeEngine.update(newConfig);
            wallpaperUtil.loadWallpaper(false, newConfig.uiMode != 33);
            uiMode = newConfig.uiMode;
        }
        dynamicThemeEngine.update(newConfig);
        //wallpaperUtil.loadWallpaper(false, newConfig.uiMode != 33);
    }

    private class DynamicThemeEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable runnable = this::draw;
        private boolean lightMode, portrait;

        private boolean visible = true;

        public DynamicThemeEngine(Context context) {
            update(context.getResources().getConfiguration());
        }

        public void update(Configuration config) {
            this.lightMode = config.uiMode != 33;
            this.portrait = config.orientation == Configuration.ORIENTATION_PORTRAIT;
            handler.post(runnable);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                Bitmap bMap = BitmapFactory.decodeFile(wallpaperUtil.getWallpaperPath(true, lightMode));
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