package de.dlyt.yanndroid.dualwallpaper.ui.activity;

import static de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil.WallpaperType;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;

import java.io.FileNotFoundException;

import de.dlyt.yanndroid.dualwallpaper.Preferences;
import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.trigger.ThemeTrigger;
import de.dlyt.yanndroid.dualwallpaper.ui.adapter.ViewPagerAdapter;
import de.dlyt.yanndroid.dualwallpaper.ui.fragment.PreferencesFragment;
import de.dlyt.yanndroid.dualwallpaper.utils.DeviceUtil;
import de.dlyt.yanndroid.dualwallpaper.utils.TriggerUtil;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil;
import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.utils.ActivityUtils;

public class MainActivity extends AppCompatActivity {

    public static final int PICKER_REQUEST_CODE = 5000;

    private WallpaperUtil wallpaperUtil;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wallpaperUtil = new WallpaperUtil(this);
        adapter = new ViewPagerAdapter(this, wallpaperUtil);

        if (!DeviceUtil.hasStoragePermission(this)) DeviceUtil.requestStoragePermission(this);
        NotificationManagerCompat.from(this).createNotificationChannel(new NotificationChannel(ThemeTrigger.CHANNEL_ID, getString(R.string.wallpaper_service), NotificationManager.IMPORTANCE_MIN));

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbarLayout);
        toolbarLayout.setNavigationButtonAsBack();

        Preferences preferences = new Preferences(this);
        if (preferences.isEnabled()) {
            if (preferences.changeWithTheme()) {
                TriggerUtil.startThemeTrigger(this);
            } else {
                TriggerUtil.startTimeTrigger(this);
            }
        }

        PreferencesFragment fragment = new PreferencesFragment();
        fragment.initFields(adapter, wallpaperUtil);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_preferences, fragment).commit();
        fragmentManager.executePendingTransactions();

        if (getIntent().getAction().equals(Intent.ACTION_ATTACH_DATA))
            setWallpaperIntent(getIntent());
    }

    private void setWallpaperIntent(Intent intent) {
        WallpaperType[] types = WallpaperType.values();
        CharSequence[] items = new CharSequence[types.length];
        for (int i = 0; i < types.length; i++) {
            WallpaperType type = types[i];
            items[i] = getString(R.string.type_name_separator,
                    type.home ? getString(R.string.home_screen) : getString(R.string.lock_screen),
                    type.light ? getString(R.string.light) : getString(R.string.dark));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.set_wallpaper_as)
                .setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, null)
                .setItems(items, (dialog1, which) -> {
                    /*try {
                        wallpaperUtil.saveFromUri(intent.getData(), types[which]);
                        adapter.notifyItemChanged(types[which].light ? 0 : 1);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }*/
                    Handler handler = new Handler();
                    new Thread(() -> {
                        try {
                            wallpaperUtil.saveFromUri(intent.getData(), types[which]);
                            handler.post(() -> adapter.notifyItemChanged(types[which].light ? 0 : 1));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }).start();
                })
                .create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO convert, crop, scale
        if (resultCode == RESULT_OK && requestCode >> 2 == PICKER_REQUEST_CODE >> 2) {
            /*try {
                WallpaperType type = WallpaperType.values()[requestCode - PICKER_REQUEST_CODE];
                wallpaperUtil.saveFromUri(data.getData(), type);
                adapter.notifyItemChanged(type.light ? 0 : 1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
            Handler handler = new Handler();
            new Thread(() -> {
                try {
                    WallpaperType type = WallpaperType.values()[requestCode - PICKER_REQUEST_CODE];
                    wallpaperUtil.saveFromUri(data.getData(), type);
                    handler.post(() -> adapter.notifyItemChanged(type.light ? 0 : 1));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(dev.oneuiproject.oneui.design.R.menu.app_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == dev.oneuiproject.oneui.design.R.id.menu_app_info) {
            ActivityUtils.startPopOverActivity(this,
                    new Intent(this, AboutActivity.class),
                    null,
                    ActivityUtils.POP_OVER_POSITION_RIGHT | ActivityUtils.POP_OVER_POSITION_TOP);
            return true;
        }
        return false;
    }
}