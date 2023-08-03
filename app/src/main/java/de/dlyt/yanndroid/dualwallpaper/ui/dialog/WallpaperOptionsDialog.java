package de.dlyt.yanndroid.dualwallpaper.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.ui.activity.MainActivity;
import de.dlyt.yanndroid.dualwallpaper.utils.BitmapUtil;
import de.dlyt.yanndroid.dualwallpaper.utils.DeviceUtil;
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

    public WallpaperOptionsDialog(Context context, WallpaperUtil wallpaperUtil, WallpaperUtil.WallpaperType type, boolean deleteButton, Callback callback) {
        this.mContext = context;
        this.mWallpaperUtil = wallpaperUtil;
        this.mType = type;
        this.mCallback = callback;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.dialog_type_name_separator,
                        type.home ? mContext.getString(R.string.home_screen) : mContext.getString(R.string.lock_screen),
                        type.light ? mContext.getString(R.string.theme_light) : mContext.getString(R.string.theme_dark)))
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
            dialogBuilder.setPositiveButton(R.string.dialog_delete, (dialog, which) -> mCallback.onDelete());
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
        if (DeviceUtil.hasStoragePermission(mContext)) {
            Handler handler = new Handler();
            new Thread(() -> {
                if (mWallpaperUtil.saveFromCurrent(mType)) {
                    mCallback.onDone();
                } else {
                    handler.post(() -> Toast.makeText(mContext, R.string.toast_wallpaper_not_supported, Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    private void optionPickNew() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        ((MainActivity) mContext).startActivityForResult(intent, MainActivity.PICKER_REQUEST_CODE + mType.index);
    }

    private void optionPlainColor() {
        int lastColor = Color.BLACK;
        Bitmap image = BitmapFactory.decodeFile(mWallpaperUtil.getPathForWallpaper(mType));
        if (image != null) lastColor = image.getPixel(0, 0);
        new ColorPickerDialog(mContext, color -> new Thread(() -> {
            mWallpaperUtil.saveFromBitmap(BitmapUtil.plainColor(DeviceUtil.getDisplaySize(mContext), color), mType, false, false);
            mCallback.onDone();
        }).start(), lastColor).show();
    }

    private void optionGradientColor() {
        new GradientPickerDialog(mContext, (startColor, endColor) -> new Thread(() -> {
            mWallpaperUtil.saveFromBitmap(BitmapUtil.gradientColor(DeviceUtil.getDisplaySize(mContext), startColor, endColor), mType, false, true);
            mCallback.onDone();
        }).start()).show();
    }
}
