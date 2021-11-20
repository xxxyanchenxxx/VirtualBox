package com.fun.vbox.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.compat.NativeLibraryHelperCompat;
import com.fun.vbox.helper.utils.FileUtils;
import com.fun.vbox.helper.utils.VLog;

import java.io.File;

public class NativeLibraryFixer {
    public static void checkAndCopySo(String packageName, File libDir) {
        int verCode = getSharedPreferences(packageName).getInt(packageName + "_last_ver", 0);
        try {
            PackageInfo packageInfo = VCore.get().getUnHookPackageManager().getPackageInfo(packageName, 0);
            String apkPath = packageInfo.applicationInfo.publicSourceDir;
            if (TextUtils.isEmpty(apkPath)) {
                apkPath = packageInfo.applicationInfo.sourceDir;
            }

            int curVerCode = packageInfo.versionCode;
            if (curVerCode != verCode) {
                FileUtils.deleteDir(libDir);

                if (!libDir.exists()) {
                    libDir.mkdir();
                }

                NativeLibraryHelperCompat.copyNativeBinaries(new File(apkPath), libDir);
                NativeLibraryHelperCompat.copyNativeBinaries2(packageName, libDir);

                getSharedPreferences(packageName).edit().putInt(packageName + "_last_ver", curVerCode).apply();
            }
        } catch (Throwable e) {
            VLog.e("NativeLibraryFixer", "checkAndCopySo", e);
        }
    }

    private static SharedPreferences getSharedPreferences(String pkg) {
        return VCore.get().getContext().getSharedPreferences(pkg + "_plugin", Context.MODE_PRIVATE);
    }
}
