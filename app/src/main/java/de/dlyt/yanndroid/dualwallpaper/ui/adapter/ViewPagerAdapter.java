package de.dlyt.yanndroid.dualwallpaper.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.ui.dialog.WallpaperActionsDialog;
import de.dlyt.yanndroid.dualwallpaper.utils.DeviceUtil;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil.WallpaperType;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

    private Context mContext;
    private WallpaperUtil mWallpaperUtil;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView lock_screen_preview;
        ImageView home_screen_preview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lock_screen_preview = itemView.findViewById(R.id.lock_screen_preview);
            home_screen_preview = itemView.findViewById(R.id.home_screen_preview);
        }
    }

    public ViewPagerAdapter(Context context, WallpaperUtil wallpaperUtil) {
        super();
        this.mContext = context;
        this.mWallpaperUtil = wallpaperUtil;
    }

    public String getTitle(int position) {
        int[] stringRes = {R.string.theme_light, R.string.theme_dark};
        return mContext.getString(stringRes[position]);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.viewpager_page_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boolean isLightModeTab = position == 0;
        initImageViewSize(holder.lock_screen_preview);
        initImageViewSize(holder.home_screen_preview);

        WallpaperType lock_type = isLightModeTab ? WallpaperType.LOCK_LIGHT : WallpaperType.LOCK_DARK;
        WallpaperType home_type = isLightModeTab ? WallpaperType.HOME_LIGHT : WallpaperType.HOME_DARK;

        holder.lock_screen_preview.setOnClickListener(v -> showActionsDialog((ImageView) v, lock_type));
        holder.home_screen_preview.setOnClickListener(v -> showActionsDialog((ImageView) v, home_type));
        updateImages(holder.lock_screen_preview, lock_type);
        updateImages(holder.home_screen_preview, home_type);
    }

    private void updateImages(ImageView imageView, WallpaperType type) {
        new Thread(() -> {
            Bitmap image = BitmapFactory.decodeFile(mWallpaperUtil.getWallpaperTypePath(type));
            if (image == null) {
                imageView.post(() -> {
                    imageView.setBackgroundColor(Color.TRANSPARENT);
                    imageView.setImageBitmap(null);
                });
            } else {
                double scale = Math.max((double) imageView.getWidth() / (double) image.getWidth(), (double) imageView.getHeight() / (double) image.getHeight());
                Bitmap scaledImage = Bitmap.createScaledBitmap(image, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), true);
                imageView.post(() -> {
                    imageView.setImageBitmap(scaledImage);
                    imageView.setBackgroundColor(Color.BLACK);
                });
            }
        }).start();
    }

    private void initImageViewSize(ImageView imageView) {
        Point size = DeviceUtil.getDisplaySize(mContext);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) (size.x / 2.8);
        params.height = (int) (size.y / 2.8);
    }

    private void showActionsDialog(ImageView imageView, WallpaperType type) {
        File wallpaperFile = new File(mWallpaperUtil.getWallpaperTypePath(type));

        new WallpaperActionsDialog(mContext, mWallpaperUtil, type, wallpaperFile.exists(), new WallpaperActionsDialog.Callback() {
            @Override
            public void onDone() {
                updateImages(imageView, type);
            }

            @Override
            public void onDelete() {
                if (wallpaperFile.delete()) updateImages(imageView, type);
            }
        }).show();
    }

}
