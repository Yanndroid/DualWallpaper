package de.dlyt.yanndroid.dualwallpaper.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.ui.activity.MainActivity;
import de.dlyt.yanndroid.dualwallpaper.utils.BitmapUtil;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil;
import dev.oneuiproject.oneui.utils.DialogUtils;

public class WallpaperOptionsDialog {

    public interface Callback {
        void onDone();

        void onDelete();
    }

    private final Context mContext;
    private final WallpaperUtil mWallpaperUtil;
    private final WallpaperUtil.WallpaperType mType;
    private final AlertDialog mDialog;
    private final Callback mCallback;

    public static Point getDisplaySize(Context context) {
        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(size);
        return size;
    }

    public WallpaperOptionsDialog(Context context, WallpaperUtil wallpaperUtil, WallpaperUtil.WallpaperType type, boolean deleteButton, Callback callback) {
        this.mContext = context;
        this.mWallpaperUtil = wallpaperUtil;
        this.mType = type;
        this.mCallback = callback;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.type_name_separator,
                        type.home ? mContext.getString(R.string.home_screen) : mContext.getString(R.string.lock_screen),
                        type.light ? mContext.getString(R.string.light) : mContext.getString(R.string.dark)))
                .setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, null)
                .setItems(R.array.select_dialog_options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            optionPickNew();
                            break;
                        case 1:
                            optionUseCurrent();
                            break;
                        case 2:
                            optionPlainColor();
                            break;
                        case 3:
                            optionGradientColor();
                            break;
                    }
                });
        if (deleteButton) {
            dialogBuilder.setPositiveButton(R.string.delete, (dialog, which) -> mCallback.onDelete());
        }
        mDialog = dialogBuilder.create();
    }

    public void show() {
        mDialog.show();
        if (mDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) {
            DialogUtils.setDialogButtonTextColor(mDialog, AlertDialog.BUTTON_POSITIVE, mContext.getColor(dev.oneuiproject.oneui.design.R.color.oui_functional_red_color));
        }
    }

    private void optionUseCurrent() {
        mWallpaperUtil.saveFromCurrent(mType);
        mCallback.onDone();
    }

    private void optionPickNew() {
        //TODO crop stuff (in MainActivity)
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        ((MainActivity) mContext).startActivityForResult(intent, MainActivity.PICKER_REQUEST_CODE + mType.index);
    }

    private void optionPlainColor() {
        int lastColor = Color.BLACK;
        Bitmap image = BitmapFactory.decodeFile(mWallpaperUtil.getPathForWallpaper(mType));
        if (image != null) lastColor = image.getPixel(0, 0);
        new ColorPickerDialog(mContext, color -> {
            mWallpaperUtil.saveFromBitmap(mType, BitmapUtil.plainColor(getDisplaySize(mContext), color));
            mCallback.onDone();
        }, lastColor).show();
    }

    private void optionGradientColor() {
        new GradientPickerDialog(mContext, (startColor, endColor) -> new Thread(() -> {
            mWallpaperUtil.saveFromBitmap(mType, BitmapUtil.gradientColor(getDisplaySize(mContext), startColor, endColor));
            mCallback.onDone();
        }).start()).show();
    }
}
