package de.dlyt.yanndroid.dualwallpaper.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;
import java.util.Date;

import de.dlyt.yanndroid.dualwallpaper.Preferences;
import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.trigger.ThemeTrigger;
import de.dlyt.yanndroid.dualwallpaper.ui.adapter.ViewPagerAdapter;
import de.dlyt.yanndroid.dualwallpaper.utils.TriggerUtil;
import de.dlyt.yanndroid.dualwallpaper.utils.WallpaperUtil;
import dev.oneuiproject.oneui.dialog.StartEndTimePickerDialog;
import dev.oneuiproject.oneui.preference.LayoutPreference;
import dev.oneuiproject.oneui.preference.SwitchBarPreference;
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard;
import dev.oneuiproject.oneui.utils.PreferenceUtils;

public class PreferencesFragment extends PreferenceFragmentCompat {

    private Context mContext;
    private Preferences mPreferences;
    private WallpaperUtil mWallpaperUtil;
    private ViewPagerAdapter mAdapter;
    private PreferenceRelatedCard mRelativeLinkCard;

    public void initFields(ViewPagerAdapter adapter, WallpaperUtil wallpaperUtil) {
        this.mAdapter = adapter;
        this.mWallpaperUtil = wallpaperUtil;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mPreferences = new Preferences(mContext);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        SwitchBarPreference switchBarPreference = findPreference("service_enabled");
        LayoutPreference previewPreference = findPreference("preview");
        DropDownPreference modePreference = findPreference("service_mode");
        Preference schedulePreference = findPreference("schedule");

        switchBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                if (mPreferences.changeWithTheme()) {
                    TriggerUtil.startThemeTrigger(mContext);
                } else {
                    TriggerUtil.startTimeTrigger(mContext);
                }
            } else {
                TriggerUtil.stopAll(mContext);
            }
            return true;
        });

        if (mAdapter != null && mWallpaperUtil != null) {
            ViewPager2 viewPager = previewPreference.findViewById(R.id.viewPager);
            viewPager.seslGetListView().setNestedScrollingEnabled(false);
            viewPager.setAdapter(mAdapter);
            viewPager.setOffscreenPageLimit(1);

            TabLayout tabLayout = previewPreference.findViewById(R.id.tabLayout);
            tabLayout.seslSetSubTabStyle();

            TypedValue colorPrimaryDark = new TypedValue();
            mContext.getTheme().resolveAttribute(dev.oneuiproject.oneui.design.R.attr.colorPrimaryDark, colorPrimaryDark, true);
            tabLayout.seslSetSubTabSelectedIndicatorColor(colorPrimaryDark.data);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position)
                    -> tab.setText(((ViewPagerAdapter) viewPager.getAdapter()).getTitle(position))).attach();
        } else {
            getPreferenceScreen().removePreference(previewPreference);
        }

        modePreference.seslSetSummaryColor(getColoredSummaryColor());
        modePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            schedulePreference.setVisible(newValue.equals("1"));
            if (mPreferences.isEnabled()) {
                if (newValue.equals("0")) {
                    TriggerUtil.startThemeTrigger(mContext);
                } else {
                    TriggerUtil.startTimeTrigger(mContext);
                }
            }
            return true;
        });

        schedulePreference.setVisible(!mPreferences.changeWithTheme());
        schedulePreference.seslSetSummaryColor(getColoredSummaryColor());
        schedulePreference.setSummary(getScheduleSummary(mContext, mPreferences.getScheduleStart(), mPreferences.getScheduleEnd()));
        schedulePreference.setOnPreferenceClickListener(preference -> {
            new StartEndTimePickerDialog(mContext,
                    mPreferences.getScheduleStart(),
                    mPreferences.getScheduleEnd(),
                    DateFormat.is24HourFormat(mContext),
                    (startTime, endTime) -> {
                        schedulePreference.setSummary(getScheduleSummary(mContext, startTime, endTime));
                        mPreferences.setScheduleTime(startTime, endTime);
                        if (mPreferences.isEnabled() && !mPreferences.changeWithTheme())
                            TriggerUtil.startTimeTrigger(mContext);
                    }).show();
            return false;
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setBackgroundColor(mContext.getColor(dev.oneuiproject.oneui.design.R.color.oui_background_color));
        getListView().seslSetLastRoundedCorner(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        setRelativeLinkCard();
        super.onResume();
    }

    private String getScheduleSummary(Context context, int startTime, int endTime) {
        int startHour = startTime / 60;
        int startMinute = startTime % 60;
        int endHour = endTime / 60;
        int endMinute = endTime % 60;
        StringBuilder sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(DateFormat.is24HourFormat(context) ? 11 : 10, startHour);
        calendar.set(12, startMinute);
        sb.append(DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis())));
        sb.append(" - ");
        calendar.clear();
        calendar.set(DateFormat.is24HourFormat(context) ? 11 : 10, endHour);
        calendar.set(12, endMinute);
        if (startTime >= endTime) {
            sb.append(context.getResources().getString(R.string.s_next_day, DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()))));
        } else {
            sb.append(DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis())));
        }
        return sb.toString();
    }

    private ColorStateList getColoredSummaryColor() {
        TypedValue colorPrimaryDark = new TypedValue();
        mContext.getTheme().resolveAttribute(dev.oneuiproject.oneui.design.R.attr.colorPrimaryDark, colorPrimaryDark, true);
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
            mRelativeLinkCard.addButton(mContext.getString(R.string.service_notification), v -> startActivity(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, mContext.getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID, ThemeTrigger.CHANNEL_ID)));
            mRelativeLinkCard.addButton(mContext.getString(R.string.display_settings), v -> startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS)));

            Intent samsungWallpaperIntent = new Intent("com.samsung.intent.action.WALLPAPER_SETTING");
            if (samsungWallpaperIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mRelativeLinkCard.addButton(mContext.getString(R.string.wallpaper_and_style), v -> startActivity(samsungWallpaperIntent));
            }

            mRelativeLinkCard.show(this);
        }
    }
}
