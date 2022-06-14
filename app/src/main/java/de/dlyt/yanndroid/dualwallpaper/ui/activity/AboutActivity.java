package de.dlyt.yanndroid.dualwallpaper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import de.dlyt.yanndroid.dualwallpaper.R;
import dev.oneuiproject.oneui.layout.AppInfoLayout;

public class AboutActivity extends AppCompatActivity implements AppInfoLayout.OnClickListener {

    private static final String GITHUB_URL = "https://github.com/Yanndroid/DualWallpaper";
    private AppInfoLayout appInfoLayout;
    private AppUpdateManager appUpdateManager;
    private AppUpdateInfo appUpdateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appInfoLayout = findViewById(R.id.appInfoLayout);
        AppCompatButton about_github = findViewById(R.id.about_github);

        appInfoLayout.setMainButtonClickListener(this);
        about_github.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))));

        checkForUpdate();
    }

    private void checkForUpdate() {
        appInfoLayout.setStatus(AppInfoLayout.LOADING);
        NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected())) {
            appInfoLayout.setStatus(AppInfoLayout.NO_CONNECTION);
            return;
        }

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appInfoLayout.setStatus(AppInfoLayout.UPDATE_AVAILABLE);
                this.appUpdateInfo = appUpdateInfo;
            } else {
                appInfoLayout.setStatus(AppInfoLayout.NO_UPDATE);
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            appInfoLayout.setStatus(AppInfoLayout.NOT_UPDATEABLE);
        });
    }

    @Override
    public void onUpdateClicked(View v) {
        if (appUpdateInfo == null) return;
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 6000);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRetryClicked(View v) {
        checkForUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(dev.oneuiproject.oneui.R.menu.app_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == dev.oneuiproject.oneui.R.id.menu_app_info) {
            appInfoLayout.openSettingsAppInfo();
            return true;
        }
        return false;
    }
}
