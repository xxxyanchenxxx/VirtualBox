package com.ft.mapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.ft.mapp.VApp;
import com.ft.mapp.utils.AppDataUtils;

public class RcmdAppData implements AppData {
    public String packageName;
    public String name;
    public Drawable icon;

    public RcmdAppData(String packageName) {
        this.packageName = packageName;
        try {
            ApplicationInfo info = VApp.getApp().getPackageManager().getApplicationInfo(getPackageName(), 0);
            loadData(VApp.getApp(), info);
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
        return false;
    }

    @Override
    public boolean isFirstOpen() {
        return true;
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
        return false;
    }
}
