package com.ft.mapp.home.models;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.ft.mapp.utils.AppDataUtils;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.remote.InstalledAppInfo;

/**
 * @author Lody
 */

public class MultiplePackageAppData implements AppData {

    public InstalledAppInfo appInfo;
    public int userId;
    public boolean isFirstOpen;
    public boolean isLoading;
    public Drawable icon;
    public String name;

    public MultiplePackageAppData(PackageAppData target, int userId) {
        this.userId = userId;
        this.appInfo = VCore.get().getInstalledAppInfo(target.packageName, 0);
        this.isFirstOpen = !appInfo.isLaunched(userId);
        if (target.icon != null) {
            Drawable.ConstantState state = target.icon.getConstantState();
            if (state != null) {
                icon = state.newDrawable();
            }
        }
        String appName = AppDataUtils.getAppName(target.packageName, userId);
        if (TextUtils.isEmpty(appName)) {
            name = target.name;
        } else {
            name = appName;
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
        String appName = AppDataUtils.getAppName(getPackageName(), userId);
        if (!TextUtils.isEmpty(appName)) {
            return appName;
        }
        return name;
    }

    @Override
    public String getPackageName() {
        return appInfo.packageName;
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
