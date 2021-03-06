package de.dlyt.yanndroid.dualwallpaper.ui.fragment;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import de.dlyt.yanndroid.dualwallpaper.LiveWallpaper;
import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.WallpaperService;
import de.dlyt.yanndroid.dualwallpaper.WallpaperUtil;
import de.dlyt.yanndroid.dualwallpaper.ui.adapter.ViewPagerAdapter;
import dev.oneuiproject.oneui.preference.LayoutPreference;
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard;
import dev.oneuiproject.oneui.utils.PreferenceUtils;

public class PreferencesFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {
    private Context mContext;
    private WallpaperUtil wallpaperUtil;
    private ViewPagerAdapter adapter;
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

        LayoutPreference layoutPreference = findPreference("preview_pref");
        if (adapter != null && wallpaperUtil != null) {
            ViewPager2 viewPager = layoutPreference.findViewById(R.id.viewPager);
            viewPager.seslGetListView().setNestedScrollingEnabled(false);
            viewPager.setAdapter(adapter);

            TabLayout tabLayout = layoutPreference.findViewById(R.id.tabLayout);
            tabLayout.seslSetSubTabStyle();

            new TabLayoutMediator(tabLayout, viewPager, (tab, position)
                    -> tab.setText(((ViewPagerAdapter) viewPager.getAdapter()).getTitle(position))).attach();
        } else {
            getPreferenceScreen().removePreference(layoutPreference);
        }

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (adapter != null) {
            adapter.notifyItemRangeChanged(0, 2);
        }
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
                    if (wallpaperUtil != null) {
                        wallpaperUtil.loadWallpaper(true, lightMode);
                        wallpaperUtil.loadWallpaper(false, lightMode);
                    }
                    break;
            }
            return true;
        }
        return false;
    }

    public void initFields(ViewPagerAdapter adapter, WallpaperUtil wallpaperUtil) {
        this.adapter = adapter;
        this.wallpaperUtil = wallpaperUtil;
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

            Intent samsungWallpaperIntent = new Intent("com.samsung.intent.action.WALLPAPER_SETTING");
            if (samsungWallpaperIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mRelativeLinkCard.addButton(mContext.getString(R.string.wallpaper_and_style), v -> startActivity(samsungWallpaperIntent));
            }

            mRelativeLinkCard.show(this);
        }
    }
}
