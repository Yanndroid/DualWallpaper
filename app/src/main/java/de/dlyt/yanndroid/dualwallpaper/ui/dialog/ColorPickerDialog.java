package de.dlyt.yanndroid.dualwallpaper.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.picker3.app.SeslColorPickerDialog;

public class ColorPickerDialog {

    private SeslColorPickerDialog mDialog;

    public ColorPickerDialog(Context context, SeslColorPickerDialog.OnColorSetListener listener, int startColor) {
        mDialog = new SeslColorPickerDialog(context, listener, startColor, null, false);
    }

    public void show() {
        mDialog.show();
        LinearLayout container = mDialog.findViewById(androidx.picker.R.id.sesl_color_picker_main_content_container);
        container.getChildAt(container.getChildCount() - 1).setVisibility(View.GONE);
        container.getChildAt(container.getChildCount() - 2).setVisibility(View.GONE);
    }

}
