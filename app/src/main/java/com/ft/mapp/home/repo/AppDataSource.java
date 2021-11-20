package com.ft.mapp.home.repo;

import android.content.Context;

import com.ft.mapp.home.models.AppData;
import com.ft.mapp.home.models.AppInfo;
import com.ft.mapp.home.models.AppInfoLite;
import com.fun.vbox.remote.InstallResult;

import org.jdeferred2.Promise;

import java.io.File;
import java.util.List;

/**
 *
 * @version 1.0
 */
public interface AppDataSource {

    /**
     * @return All the Applications we Virtual.
     */
    Promise<List<AppData>, Throwable, Void> getVirtualApps();

    /**
     * @param context Context
     * @return All the Applications we Installed.
     */
    Promise<List<AppInfo>, Throwable, Void> getInstalledApps(Context context);

    Promise<List<AppInfo>, Throwable, Void> getStorageApps(Context context, File rootDir);

    InstallResult addVirtualApp(AppInfoLite info);

    boolean removeVirtualApp(String packageName, int userId);
}
