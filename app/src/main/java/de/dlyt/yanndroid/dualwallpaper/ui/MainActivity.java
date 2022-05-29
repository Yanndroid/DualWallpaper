package de.dlyt.yanndroid.dualwallpaper.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import de.dlyt.yanndroid.dualwallpaper.LiveWallpaper;
import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.WallpaperService;
import de.dlyt.yanndroid.dualwallpaper.WallpaperUtil;
import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class MainActivity extends AppCompatActivity {

    private WallpaperUtil wallpaperUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wallpaperUtil = new WallpaperUtil(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        NotificationManagerCompat.from(this).createNotificationChannel(new NotificationChannel("4000", getString(R.string.wallpaper_service), NotificationManager.IMPORTANCE_LOW));

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbarLayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarLayout.setNavigationButtonOnClickListener(v -> onBackPressed());

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(this, wallpaperUtil));
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.seslSetSubTabStyle();
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(((ViewPagerAdapter) viewPager.getAdapter()).getTitle(position))).attach();


        SwitchCompat use_service_switch = findViewById(R.id.use_service_switch);
        use_service_switch.setChecked(getSharedPreferences("sp", MODE_PRIVATE).getBoolean("use_service_switch", false));
        use_service_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("sp", MODE_PRIVATE).edit().putBoolean("use_service_switch", isChecked).apply();
            if (isChecked) {
                startForegroundService(new Intent(MainActivity.this, WallpaperService.class));
            } else {
                stopService(new Intent(MainActivity.this, WallpaperService.class));
            }
        });
    }

    public void setLW(View view) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, LiveWallpaper.class));
        startActivity(intent);
    }
}