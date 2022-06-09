package de.dlyt.yanndroid.dualwallpaper.ui;

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
import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard;
import dev.oneuiproject.oneui.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity {

    private static WallpaperUtil wallpaperUtil;
    private ViewPagerAdapter adapter;

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
        toolbarLayout.setNavigationButtonAsBack();

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter = new ViewPagerAdapter(this, wallpaperUtil));
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.seslSetSubTabStyle();
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(((ViewPagerAdapter) viewPager.getAdapter()).getTitle(position))).attach();

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("main_pref", "off").equals("wps"))
            startForegroundService(new Intent(MainActivity.this, WallpaperService.class));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.main_preferences, new PreferencesFragment()).commit();
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

    public static class PreferencesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        private Context mContext;
        private PreferenceRelatedCard mRelativeLinkCard;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            mContext = context;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            DropDownPreference main_pref = findPreference("main_pref");
            main_pref.seslSetSummaryColor(getColoredSummaryColor());
            main_pref.setOnPreferenceChangeListener(this);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getView().setBackgroundColor(mContext.getColor(dev.oneuiproject.oneui.R.color.oui_background_color));
            getListView().seslSetLastRoundedCorner(false);
        }

        @Override
        public void onResume() {
            setRelativeLinkCard();
            super.onResume();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals("main_pref")) {
                switch ((String) newValue) {
                    case "wps":
                        mContext.startForegroundService(new Intent(mContext, WallpaperService.class));
                        break;
                    case "lwp":
                        mContext.stopService(new Intent(mContext, WallpaperService.class));
                        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(mContext, LiveWallpaper.class));
                        startActivity(intent);
                        break;
                    case "off":
                        mContext.stopService(new Intent(mContext, WallpaperService.class));
                        boolean lightMode = getResources().getConfiguration().uiMode != 33;
                        wallpaperUtil.loadWallpaper(true, lightMode);
                        wallpaperUtil.loadWallpaper(false, lightMode);
                        break;
                }
                return true;
            }
            return false;
        }

        private ColorStateList getColoredSummaryColor() {
            TypedValue colorPrimaryDark = new TypedValue();
            mContext.getTheme().resolveAttribute(dev.oneuiproject.oneui.R.attr.colorPrimaryDark, colorPrimaryDark, true);
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_enabled},
                    new int[]{-android.R.attr.state_enabled}
            };
            int[] colors = new int[]{
                    Color.argb(0xff,
                            Color.red(colorPrimaryDark.data),
                            Color.green(colorPrimaryDark.data),
                            Color.blue(colorPrimaryDark.data)),
                    Color.argb(0x4d,
                            Color.red(colorPrimaryDark.data),
                            Color.green(colorPrimaryDark.data),
                            Color.blue(colorPrimaryDark.data))
            };
            return new ColorStateList(states, colors);
        }

        private void setRelativeLinkCard() {
            if (mRelativeLinkCard == null) {
                mRelativeLinkCard = PreferenceUtils.createRelatedCard(mContext);
                mRelativeLinkCard.addButton(mContext.getString(R.string.service_notification), v -> startActivity(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, mContext.getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID, "4000")));
                mRelativeLinkCard.addButton(mContext.getString(R.string.live_wallpaper), v -> startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)));
                if (Build.MANUFACTURER.equals("samsung")) {
                    mRelativeLinkCard.addButton(mContext.getString(R.string.wallpaper_and_style), v -> startActivity(new Intent("com.samsung.intent.action.WALLPAPER_SETTING")));
                }
                mRelativeLinkCard.show(this);
            }
        }
    }
}