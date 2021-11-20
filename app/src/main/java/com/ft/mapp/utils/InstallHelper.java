package com.ft.mapp.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.ft.mapp.R;
import com.ft.mapp.widgets.CommonDialog;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.remote.InstallOptions;
import com.fun.vbox.server.bit64.Bit64Utils;

import io.reactivex.android.schedulers.AndroidSchedulers;


public class InstallHelper {


    public static boolean installPackage(Activity activity, String packageName) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(packageName, 0);

        String apkPath = packageInfo.applicationInfo.publicSourceDir;
        if (apkPath == null || apkPath.isEmpty()) {
            apkPath = packageInfo.applicationInfo.sourceDir;
        }

        if (!checkInstall64Bit(activity, packageName, apkPath)) {
            return false;
        }

        return installPackage(apkPath);
    }

    private static boolean installPackage(String apkPath) {
        InstallOptions options = InstallOptions.makeOptions(true, false,
                InstallOptions.UpdateStrategy.COMPARE_VERSION);
        return VCore.get().installPackageSync(apkPath, options).isSuccess;
    }

    private static boolean checkInstall64Bit(Activity activity, String pkgName, String apkPath) {
        boolean isBit64 = Bit64Utils.isRunOn64BitProcess(pkgName, apkPath);
        if (isBit64) {
            if (!VCore.get().is64BitEngineInstalled()) {
                install64Bit(activity);
                return false;
            } else if (CommonUtil.shouldUpdate64BitApk()) {
                install64Bit(activity);
                return false;
            }
        }
        return true;
    }

    public static void install64Bit(Activity activity) {
        AndroidSchedulers.mainThread().scheduleDirect(() -> new CommonDialog(activity)
                .setTitleId(R.string.notice)
                .setMessage(R.string.tip_64)
                .setPositiveButton(R.string.OK, (dialogInterface, i) -> {
                    CommonUtil.install64Bit();
                }).show());
    }


}
