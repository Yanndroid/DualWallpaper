package de.dlyt.yanndroid.dualwallpaper.utils;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.HashMap;

import de.dlyt.yanndroid.dualwallpaper.R;


public class Updater {

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.REQUEST_INSTALL_PACKAGES})
    public static void downloadAndInstall(Context context, String url, String versionName) {
        String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + context.getString(R.string.app_name) + "_" + versionName + ".apk";
        Uri fileUri = Uri.parse("file://" + destination);

        File file = new File(destination);
        if (file.exists()) file.delete();

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.setMimeType("application/vnd.android.package-archive");
        request.setTitle(context.getString(R.string.app_name));
        request.setDescription(versionName);
        request.setDestinationUri(fileUri);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                Uri apkFileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(destination));
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                install.setDataAndType(apkFileUri, "application/vnd.android.package-archive");
                context.startActivity(install);

                context.unregisterReceiver(this);
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager.enqueue(request);
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    public static void checkForUpdate(Context context, UpdateChecker updateChecker) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected())) {
            updateChecker.noConnection();
            return;
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(context.getString(R.string.app_name));
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    HashMap<String, String> hashMap = new HashMap<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        hashMap.put(child.getKey(), child.getValue().toString());
                    }

                    updateChecker.updateAvailable(Integer.parseInt(hashMap.get("versionCode")) > context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode, hashMap.get("apk"), hashMap.get("versionName"));

                    if (hashMap.get("github") != null) {
                        updateChecker.githubAvailable(hashMap.get("github"));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("Updater.checkForUpdate", e.getMessage());
                    updateChecker.updateAvailable(false, null, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Updater.checkForUpdate", error.getMessage());
                updateChecker.updateAvailable(false, null, null);
            }
        });
    }

    public interface UpdateChecker {
        void updateAvailable(boolean available, String url, String versionName);

        void githubAvailable(String url);

        void noConnection();
    }
}
