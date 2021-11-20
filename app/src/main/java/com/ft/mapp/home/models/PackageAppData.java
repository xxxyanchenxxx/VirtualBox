package com.ft.mapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.ft.mapp.utils.AppDataUtils;
import com.fun.vbox.remote.InstalledAppInfo;

/**
 * @author Lody
 */
public class PackageAppData implements AppData {

    public String packageName;
    public String name;
    public Drawable icon;
    public boolean fastOpen;
    public boolean isFirstOpen;
    public boolean isLoading;

    public PackageAppData(Context context, InstalledAppInfo installedAppInfo) {
        this.packageName = installedAppInfo.packageName;
        this.isFirstOpen = !installedAppInfo.isLaunched(0);
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            loadData(context, info);
        } catch (PackageManager.NameNotFoundException e) {

        }
    }

    private void loadData(Context context, ApplicationInfo appInfo) {
        if (appInfo == null) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        try {
            name = AppDataUtils.getAppName(appInfo, 0, pm);
            icon = appInfo.loadIcon(pm);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public boolean isFirstOpen() {
        return isFirstOpen;
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        String appName = AppDataUtils.getAppName(getPackageName(), 0);
        if (!TextUtils.isEmpty(appName)) {
            return appName;
        }
        return name;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public boolean canReorder() {
        return true;
    }

    @Override
    public boolean canLaunch() {
        return true;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public boolean canCreateShortcut() {
        return true;
    }
}
