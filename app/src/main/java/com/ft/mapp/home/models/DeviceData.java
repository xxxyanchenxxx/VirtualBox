package com.ft.mapp.home.models;

import android.content.Context;

import com.fun.vbox.client.ipc.VDeviceManager;
import com.fun.vbox.remote.InstalledAppInfo;

public class DeviceData extends SettingsData {
    public DeviceData(Context context, InstalledAppInfo installedAppInfo, int userId) {
        super(context, installedAppInfo, userId);
    }

    public boolean isMocking() {
        return VDeviceManager.get().isEnable(userId);
    }
}
