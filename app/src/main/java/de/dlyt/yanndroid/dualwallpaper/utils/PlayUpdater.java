package de.dlyt.yanndroid.dualwallpaper.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class PlayUpdater {

    public static class Callback {
        public void updateAvailable(AppUpdateInfo appUpdateInfo) {
        }

        public void noConnection() {
        }

        public void noUpdate() {
        }

        public void error() {
        }
    }

    private Context mContext;
    private AppUpdateManager mAppUpdateManager;

    public PlayUpdater(Context context) {
        mContext = context;
        mAppUpdateManager = AppUpdateManagerFactory.create(context);
    }

    public void checkUpdate(Callback callback) {
        NetworkInfo networkInfo = ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected())) {
            callback.noConnection();
            return;
        }

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                callback.updateAvailable(appUpdateInfo);
            } else {
                callback.noUpdate();
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            callback.error();
        });
    }

    public void startUpdate(Activity activity, AppUpdateInfo appUpdateInfo) {
        mAppUpdateManager.startUpdateFlow(appUpdateInfo, activity, AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE));
    }
}
