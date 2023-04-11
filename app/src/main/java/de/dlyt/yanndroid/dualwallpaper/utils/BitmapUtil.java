package de.dlyt.yanndroid.dualwallpaper.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;

public class BitmapUtil {

    public static Bitmap plainColor(Point size, int color) {
        Bitmap bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);
        return bitmap;
    }

    public static Bitmap gradientColor(Point size, int colorStart, int colorEnd) {
        Bitmap bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        LinearGradient gradient = new LinearGradient(0, 0, 0, size.y, colorStart, colorEnd, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setShader(gradient);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPaint(paint);
        return bitmap;
    }

}
