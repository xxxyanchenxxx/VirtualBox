package com.ft.mapp.utils;

import android.text.TextUtils;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.utils.VLog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AppPackageCompat {
    private static final String TAG = "packageCompat";
    private static final String SPLIT_TAG = ",";
    private static final ConcurrentHashMap<String, PluginPackageGroup> sMapPkgGroup = new ConcurrentHashMap<>();

    public static synchronized void putPackageGroup(String packageName, String rpkgs) {
        if (TextUtils.isEmpty(rpkgs)) {
            return;
        }
        String[] arrPkg = rpkgs.split(SPLIT_TAG);
        if (arrPkg.length == 0) {
            return;
        }
        Set<String> lstPkg = new HashSet<>(Arrays.asList(arrPkg));
        PluginPackageGroup group = new PluginPackageGroup(packageName, lstPkg);
        putPackageGroup(packageName, group);
    }

    private static void putPackageGroup(String pkg, PluginPackageGroup group) {
        sMapPkgGroup.put(pkg, group);
    }

    public static String getPackageName(String pkgName) {
        String selectPackageName = pkgName;

        do {
            PluginPackageGroup group = sMapPkgGroup.get(pkgName);
            if (group == null || group.getRpkgs() == null) {
                break;
            }

            boolean isInstall = VCore.get().isOutsideInstalled(pkgName);
            if (isInstall) {
                break;
            }

            for (String pkg : group.getRpkgs()) {
                isInstall = VCore.get().isOutsideInstalled(pkg);
                if (isInstall) {
                    selectPackageName = pkg;
                    break;
                }
            }
        } while (false);

        VLog.i(TAG, "getPackageName:" + pkgName + "=>" + selectPackageName);
        return selectPackageName;
    }

    public static class PluginPackageGroup {
        private String packageName;
        private Set<String> rpkgs;

        public PluginPackageGroup(String packageName, Set<String> rpkgs) {
            this.packageName = packageName;
            this.rpkgs = rpkgs;
        }

        public String getPackageName() {
            return packageName;
        }

        public Set<String> getRpkgs() {
            return rpkgs;
        }
    }
}
