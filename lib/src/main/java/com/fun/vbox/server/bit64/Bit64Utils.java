package com.fun.vbox.server.bit64;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.Keep;
import com.fun.vbox.helper.compat.NativeLibraryHelperCompat;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.server.pm.PackageSetting;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

@Keep
public class Bit64Utils {
    public static class Result {
        public boolean support64bit = false;
        public boolean support32bit = false;
        public int flag;
    }

    public static Result getSupportAbi(String packageName, String apkPath) {
        String appLibPath = null;
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo =
                    VCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0);
            appLibPath = applicationInfo.nativeLibraryDir;
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        Result result = new Result();
        if (applicationInfo != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (applicationInfo.splitNames != null && applicationInfo.splitNames.length != 0) {
                    String splitNames = Arrays.toString(applicationInfo.splitNames);
                    if (!TextUtils.isEmpty(splitNames)) {
                        if (splitNames.contains("config.arm64_v8a")) {
                            result.support64bit = true;
                            result.flag = PackageSetting.FLAG_RUN_64BIT;
                        } else {
                            result.support32bit = true;
                            result.flag = PackageSetting.FLAG_RUN_32BIT;
                        }
                        return result;
                    }
                }
            }
        }

        Set<String> abiList = NativeLibraryHelperCompat.getSupportAbiList(apkPath);
        if (abiList.isEmpty()) {
            result.support64bit = true;
            result.support32bit = true;
        } else {
            if (NativeLibraryHelperCompat.contain64bitAbi(abiList)) {
                result.support64bit = true;
            }
            if (NativeLibraryHelperCompat.contain32bitAbi(abiList)) {
                result.support32bit = true;
            }
        }
        if (!abiList.isEmpty()) {
            if (result.support32bit) {
                if (result.support64bit) {
                    result.flag = PackageSetting.FLAG_RUN_BOTH_32BIT_64BIT;
                } else {
                    result.flag = PackageSetting.FLAG_RUN_32BIT;
                }
            } else {
                result.flag = PackageSetting.FLAG_RUN_64BIT;
            }
        } else {
            result.flag = PackageSetting.FLAG_RUN_32BIT;
            if (!TextUtils.isEmpty(appLibPath)) {
                String[] files = new File(appLibPath).list();
                if (files != null && files.length > 0) {
                    if (appLibPath.contains("arm64")) {
                        result.flag = PackageSetting.FLAG_RUN_64BIT;
                    }
                }
            }

        }
        return result;
    }

    public static boolean isRunOn64BitProcess(String packageName, String apkPath) {
        int flag = getSupportAbi(packageName, apkPath).flag;
        return PackageSetting.isRunOn64BitProcess(flag);
    }

    public static boolean isRunOn64BitProcess(String packageName) {
        try {
            PackageInfo packageInfo = VCore.get().getUnHookPackageManager().getPackageInfo(packageName, 0);
            String apkPath = packageInfo.applicationInfo.publicSourceDir;
            if (TextUtils.isEmpty(apkPath)) {
                apkPath = packageInfo.applicationInfo.sourceDir;
            }
            boolean isBit64 = isRunOn64BitProcess(packageName, apkPath);
            VLog.i("Bit64", packageName + " isBit64:" + isBit64);
            return isBit64;
        } catch (Exception e) {
            VLog.w("Bit64", "", e);
            return false;
        }
    }
}
