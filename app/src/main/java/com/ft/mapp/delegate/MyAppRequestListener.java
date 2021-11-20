package com.ft.mapp.delegate;

import android.content.Context;
import android.widget.Toast;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.remote.InstallOptions;

/**
 * @author Lody
 */

public class MyAppRequestListener implements VCore.AppRequestListener {

    private final Context context;

    public MyAppRequestListener(Context context) {
        this.context = context;
    }

    @Override
    public void onRequestInstall(String path) {
        info("Start installing: " + path);
        InstallOptions options = InstallOptions.makeOptions(false);
        VCore.get().installPackage(path, options, res -> {
            if (res.isSuccess) {
                info("Install " + res.packageName + " success.");
                boolean success = VActivityManager.get().launchApp(0, res.packageName);
                info("launch app " + (success ? "success." : "fail."));
            } else {
                info("Install " + res.packageName + " fail, reason: " + res.error);
            }
        });
    }

    private static void info(String msg) {
        VLog.e("AppInstaller", msg);
    }

    @Override
    public void onRequestUninstall(String pkg) {
        Toast.makeText(context, "Intercept uninstall request: " + pkg, Toast.LENGTH_SHORT).show();

    }
}
