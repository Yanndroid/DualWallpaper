package de.dlyt.yanndroid.dualwallpaper.ui.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.Random;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.utils.BitmapUtil;

public class GradientPickerDialog {

    public interface Callback {
        void onDone(int startColor, int endColor);
    }

    private final Context mContext;
    private final AlertDialog mDialog;
    private final Callback mCallback;

    private ImageView mPreviewImage;
    private AppCompatImageView mStartPick;
    private AppCompatImageView mEndPick;

    private int mStartColor;
    private int mEndColor;

    public GradientPickerDialog(Context context, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        randomColor();

        LinearLayout content = (LinearLayout) LayoutInflater.from(this.mContext).inflate(R.layout.dialog_gradient_picker, (ViewGroup) null);
        mPreviewImage = content.findViewById(R.id.gradient_preview);
        mStartPick = content.findViewById(R.id.gradient_start_pick);
        mEndPick = content.findViewById(R.id.gradient_end_pick);

        mStartPick.setOnClickListener(v -> new ColorPickerDialog(mContext, color -> {
            mStartColor = color;
            updatePreview();
        }, mStartColor).show());
        mEndPick.setOnClickListener(v -> new ColorPickerDialog(mContext, color -> {
            mEndColor = color;
            updatePreview();
        }, mEndColor).show());
        content.findViewById(R.id.gradient_random).setOnClickListener(v -> {
            randomColor();
            updatePreview();
        });


        this.mDialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.gradient_color)
                .setView(content)
                .setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, null)
                .setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, (dialog, which) -> mCallback.onDone(mStartColor, mEndColor))
                .create();
    }

    public void show() {
        mDialog.show();
        updatePreview();
    }

    private void randomColor() {
        Random random = new Random();
        mStartColor = -(random.nextInt(-Color.BLACK - 1) + 1);
        mEndColor = -(random.nextInt(-Color.BLACK - 1) + 1);
    }

    private void updatePreview() {
        new Thread(() -> {
            Bitmap preview = BitmapUtil.gradientColor(new Point(300, 500), mStartColor, mEndColor);
            mPreviewImage.post(() -> {
                mPreviewImage.setImageBitmap(preview);
                mStartPick.setImageDrawable(getPickIcon(mStartColor));
                mEndPick.setImageDrawable(getPickIcon(mEndColor));
            });
        }).start();
    }

    private GradientDrawable getPickIcon(int color) {
        GradientDrawable drawable = (GradientDrawable) mContext.getDrawable(dev.oneuiproject.oneui.design.R.drawable.oui_preference_color_picker_preview).mutate();
        drawable.setColor(color);
        return drawable;
    }
}
