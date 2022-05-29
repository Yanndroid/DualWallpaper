package de.dlyt.yanndroid.dualwallpaper.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.WallpaperUtil;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

    private Context context;
    private WallpaperUtil wallpaperUtil;
    private int imageWidth, imageHeight;

    public ViewPagerAdapter(Context context, WallpaperUtil wallpaperUtil) {
        super();
        this.context = context;
        this.wallpaperUtil = wallpaperUtil;

        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(size);
        imageWidth = (int) (size.x / 2.8);
        imageHeight = (int) (size.y / 2.8);
    }

    public String getTitle(int position) {
        int[] stringRes = {R.string.light, R.string.dark};
        return context.getString(stringRes[position]);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boolean isLightModeTab = position == 0;
        setImageViewSize(holder.lock_screen_preview);
        setImageViewSize(holder.home_screen_preview);
        holder.lock_screen_preview.setOnClickListener(v -> wallpaperOptionsDialog((ImageView) v, false, isLightModeTab));
        holder.home_screen_preview.setOnClickListener(v -> wallpaperOptionsDialog((ImageView) v, true, isLightModeTab));

        updateImages(holder.lock_screen_preview, false, isLightModeTab);
        updateImages(holder.home_screen_preview, true, isLightModeTab);
    }

    private void updateImages(ImageView imageView, boolean homeScreen, boolean lightMode) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(wallpaperUtil.getWallpaperPath(homeScreen, lightMode)));
    }

    private void setImageViewSize(ImageView imageView) {
        ViewGroup.LayoutParams lph = imageView.getLayoutParams();
        lph.width = imageWidth;
        lph.height = imageHeight;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.viewpager_page_layout, parent, false));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView lock_screen_preview;
        ImageView home_screen_preview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lock_screen_preview = itemView.findViewById(R.id.lock_screen_preview);
            home_screen_preview = itemView.findViewById(R.id.home_screen_preview);
        }
    }

    private void wallpaperOptionsDialog(ImageView imageView, boolean homeScreen, boolean lightMode) {
        File wallpaperFile = new File(wallpaperUtil.getWallpaperPath(homeScreen, lightMode));
        CharSequence[] dialogOptions = wallpaperFile.exists() ?
                new String[]{context.getString(R.string.use_current), context.getString(R.string.pick_new), context.getString(R.string.delete)} :
                new String[]{context.getString(R.string.use_current), context.getString(R.string.pick_new)};

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setItems(dialogOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            wallpaperUtil.saveWallpaper(homeScreen, lightMode);
                            updateImages(imageView, homeScreen, lightMode);
                            break;
                        case 1:
                            //todo
                            break;
                        case 2:
                            wallpaperFile.delete();
                            updateImages(imageView, homeScreen, lightMode);
                            break;
                    }
                }).create();
        alertDialog.show();
    }
}
