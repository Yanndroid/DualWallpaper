package de.dlyt.yanndroid.dualwallpaper.ui.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.FileNotFoundException;

import de.dlyt.yanndroid.dualwallpaper.LiveWallpaper;
import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.WallpaperService;
import de.dlyt.yanndroid.dualwallpaper.WallpaperUtil;
import de.dlyt.yanndroid.dualwallpaper.ui.adapter.ViewPagerAdapter;
import de.dlyt.yanndroid.dualwallpaper.ui.fragment.PreferencesFragment;
import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.preference.LayoutPreference;
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard;
import dev.oneuiproject.oneui.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity {
    private WallpaperUtil wallpaperUtil;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wallpaperUtil = new WallpaperUtil(this);
        adapter = new ViewPagerAdapter(this, wallpaperUtil);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        NotificationManagerCompat.from(this).createNotificationChannel(new NotificationChannel("4000", getString(R.string.wallpaper_service), NotificationManager.IMPORTANCE_LOW));

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbarLayout);
        toolbarLayout.setNavigationButtonAsBack();

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("main_pref", "off").equals("wps"))
            startForegroundService(new Intent(MainActivity.this, WallpaperService.class));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.main_preferences, PreferencesFragment.newInstance(adapter, wallpaperUtil)).commit();
        fragmentManager.executePendingTransactions();

        if (getIntent().getAction().equals(Intent.ACTION_ATTACH_DATA))
            setWallpaperIntent(getIntent());
    }

    private void setWallpaperIntent(Intent intent) {
        CharSequence[] items = new CharSequence[4];
        items[0] = getString(R.string.light) + " " + getString(R.string.lock_screen);
        items[1] = getString(R.string.light) + " " + getString(R.string.home_screen);
        items[2] = getString(R.string.dark) + " " + getString(R.string.lock_screen);
        items[3] = getString(R.string.dark) + " " + getString(R.string.home_screen);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.set_wallpaper_as)
                .setItems(items, (dialog1, which) -> {
                    try {
                        wallpaperUtil.saveUriWallpaper(intent.getData(), (which & 1) == 1, ((which >> 1) & 1) == 0);
                        adapter.notifyItemChanged(((which >> 1) & 1));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode >> 3) == 625) {
            try {
                wallpaperUtil.saveUriWallpaper(data.getData(), (((requestCode >> 1) & 1) == 1), ((requestCode & 1) == 1));
                adapter.notifyItemChanged(1 - (requestCode & 1));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(dev.oneuiproject.oneui.R.menu.app_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == dev.oneuiproject.oneui.R.id.menu_app_info) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return false;
    }
}