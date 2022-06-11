package de.dlyt.yanndroid.dualwallpaper.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import de.dlyt.yanndroid.dualwallpaper.R;
import de.dlyt.yanndroid.dualwallpaper.utils.Updater;
import dev.oneuiproject.oneui.layout.AppInfoLayout;

public class AboutActivity extends AppCompatActivity implements AppInfoLayout.OnClickListener, Updater.UpdateChecker {

    private AppInfoLayout appInfoLayout;
    private String update_url, update_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        appInfoLayout = findViewById(R.id.appInfoLayout);
        appInfoLayout.setMainButtonClickListener(this);
        Updater.checkForUpdate(this, this);
    }

    @Override
    public void updateAvailable(boolean available, String url, String versionName) {
        appInfoLayout.setStatus(available ? AppInfoLayout.UPDATE_AVAILABLE : AppInfoLayout.NO_UPDATE);
        update_url = url;
        update_version = versionName;
    }

    @Override
    public void githubAvailable(String url) {
        AppCompatButton about_github = findViewById(R.id.about_github);
        about_github.setVisibility(View.VISIBLE);
        about_github.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
    }

    @Override
    public void noConnection() {
        appInfoLayout.setStatus(AppInfoLayout.NO_CONNECTION);
    }

    @Override
    public void onUpdateClicked(View v) {
        if (update_url != null) Updater.downloadAndInstall(this, update_url, update_version);
    }

    @Override
    public void onRetryClicked(View v) {
        Updater.checkForUpdate(this, this);
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
