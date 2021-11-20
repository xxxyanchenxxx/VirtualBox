package com.ft.mapp.home.models;

import android.content.Context;

import com.fun.vbox.remote.InstalledAppInfo;

public class AppLaunchData extends SettingsData {
    public int monopoly;

    public AppLaunchData(Context context, InstalledAppInfo installedAppInfo, int userId) {
        super(context, installedAppInfo, userId);
    }
}
