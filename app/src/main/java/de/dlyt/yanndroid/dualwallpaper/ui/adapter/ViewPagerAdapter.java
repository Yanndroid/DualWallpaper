package de.dlyt.yanndroid.dualwallpaper.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.ui.dialog.WallpaperOptionsDialog;
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
        int[] stringRes = {R.string.light, R.string.dark};
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
        setImageViewSize(holder.lock_screen_preview);
        setImageViewSize(holder.home_screen_preview);
        if (isLightModeTab) {
            holder.lock_screen_preview.setOnClickListener(v -> wallpaperOptionsDialog((ImageView) v, WallpaperType.LOCK_LIGHT));
            holder.home_screen_preview.setOnClickListener(v -> wallpaperOptionsDialog((ImageView) v, WallpaperType.HOME_LIGHT));
            updateImages(holder.lock_screen_preview, WallpaperType.LOCK_LIGHT);
            updateImages(holder.home_screen_preview, WallpaperType.HOME_LIGHT);
        } else {
            holder.lock_screen_preview.setOnClickListener(v -> wallpaperOptionsDialog((ImageView) v, WallpaperType.LOCK_DARK));
            holder.home_screen_preview.setOnClickListener(v -> wallpaperOptionsDialog((ImageView) v, WallpaperType.HOME_DARK));
            updateImages(holder.lock_screen_preview, WallpaperType.LOCK_DARK);
            updateImages(holder.home_screen_preview, WallpaperType.HOME_DARK);
        }
    }

    private void updateImages(ImageView imageView, WallpaperType type) {
        new Thread(() -> {
            Bitmap image = BitmapFactory.decodeFile(mWallpaperUtil.getPathForWallpaper(type));
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

    private void setImageViewSize(ImageView imageView) {
        Point size = new Point();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(size);
        ViewGroup.LayoutParams lph = imageView.getLayoutParams();
        lph.width = (int) (size.x / 2.8);
        lph.height = (int) (size.y / 2.8);
    }

    private void wallpaperOptionsDialog(ImageView imageView, WallpaperType type) {
        File wallpaperFile = new File(mWallpaperUtil.getPathForWallpaper(type));

        new WallpaperOptionsDialog(mContext, mWallpaperUtil, type, wallpaperFile.exists(), new WallpaperOptionsDialog.Callback() {
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
