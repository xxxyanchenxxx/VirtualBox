package com.ft.mapp.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.ft.mapp.VApp;

public class AppDataUtils {
    public static String getAppName(ApplicationInfo appInfo, int userId, PackageManager pm) {
        String name = "";
        String appName = getAppName(appInfo.packageName, userId);
        if (TextUtils.isEmpty(appName)) {
            CharSequence sequence = appInfo.loadLabel(pm);
            if (sequence != null) {
                name = sequence.toString();
            }
        } else {
            name = appName;
        }
        return name;
    }

    public static String getAppName(String pkg, int userId) {
        return VApp.getPreferences().getString(pkg +
                "_" + userId + "_app_name", "");
    }

    public static void setAppName(String pkg, int userId, String name) {
        VApp.getPreferences().edit().putString(pkg +
                "_" + userId + "_app_name", name).apply();
    }
}
