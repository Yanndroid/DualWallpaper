package de.dlyt.yanndroid.dualwallpaper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Preferences {

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public Preferences(Context context) {
        this.mContext = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public boolean isEnabled() {
        return mSharedPreferences.getBoolean("service_enabled", false);
    }

    public boolean changeWithTheme() {
        return mSharedPreferences.getString("service_mode", "0").equals("0");
    }

    public int getScheduleStart() {
        return mSharedPreferences.getInt("schedule_start", 1140);
    }

    public int getScheduleEnd() {
        return mSharedPreferences.getInt("schedule_end", 420);
    }

    public void setScheduleTime(int startTime, int endTime) {
        mSharedPreferences.edit().putInt("schedule_start", startTime).putInt("schedule_end", endTime).apply();
    }
}
