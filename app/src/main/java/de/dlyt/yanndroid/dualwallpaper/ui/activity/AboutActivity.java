package de.dlyt.yanndroid.dualwallpaper.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.utils.PlayUpdater;
import dev.oneuiproject.oneui.layout.AppInfoLayout;

public class AboutActivity extends AppCompatActivity implements AppInfoLayout.OnClickListener {

    private static final String GITHUB_URL = "https://github.com/Yanndroid/DualWallpaper";
    private static final String POEDITOR_URL = "https://poeditor.com/join/project/is9K6CJAaL";
    private static final String DONATE_URL = "https://paypal.me/YanndroidDev";
    private AppInfoLayout mAppInfoLayout;
    private PlayUpdater mPlayUpdater;
    private AppUpdateInfo mAppUpdateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mPlayUpdater = new PlayUpdater(this);

        mAppInfoLayout = findViewById(R.id.appInfoLayout);
        mAppInfoLayout.setMainButtonClickListener(this);

        findViewById(R.id.about_source_code).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))));
        findViewById(R.id.about_translations).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(POEDITOR_URL))));
        findViewById(R.id.about_donate).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_URL))));

        checkForUpdate();
    }

    private void checkForUpdate() {
        mAppInfoLayout.setStatus(AppInfoLayout.LOADING);

        mPlayUpdater.checkUpdate(new PlayUpdater.Callback() {
            @Override
            public void updateAvailable(AppUpdateInfo appUpdateInfo) {
                mAppInfoLayout.setStatus(AppInfoLayout.UPDATE_AVAILABLE);
                mAppUpdateInfo = appUpdateInfo;
            }

            @Override
            public void noConnection() {
                mAppInfoLayout.setStatus(AppInfoLayout.NO_CONNECTION);
            }

            @Override
            public void noUpdate() {
                mAppInfoLayout.setStatus(AppInfoLayout.NO_UPDATE);
            }

            @Override
            public void error() {
                mAppInfoLayout.setStatus(AppInfoLayout.NOT_UPDATEABLE);
            }
        });
    }

    @Override
    public void onUpdateClicked(View v) {
        if (mAppUpdateInfo == null) {
            mAppInfoLayout.setStatus(AppInfoLayout.NO_CONNECTION);
        } else {
            mPlayUpdater.startUpdate(this, mAppUpdateInfo);
        }
    }

    @Override
    public void onRetryClicked(View v) {
        checkForUpdate();
    }
}
