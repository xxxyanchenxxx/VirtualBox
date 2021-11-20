package com.fun.vbox.helper.compat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.fun.vbox.client.core.VCore;
import com.zb.vv.client.stub.RequestPermissionsActivity;
import com.fun.vbox.helper.Keep;
import com.fun.vbox.server.IRequestPermissionsResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Keep
public class PermissionCompat {
    public static Set<String> DANGEROUS_PERMISSION = new HashSet<String>() {{
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= 16) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }};

    public static String[] findDangerousPermissions(List<String> permissions) {
        if (permissions == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (String per : permissions) {
            if (DANGEROUS_PERMISSION.contains(per)) {
                list.add(per);
            }
        }
        return list.toArray(new String[0]);
    }

    public static String[] findDangrousPermissions(String[] permissions) {
        if (permissions == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (String permission : permissions) {
            if (DANGEROUS_PERMISSION.contains(permission)) {
                list.add(permission);
            }
        }
        return list.toArray(new String[0]);
    }

    public static boolean isCheckPermissionRequired(int targetSdkVersion) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M
                || VCore.get().getTargetSdkVersion() < android.os.Build.VERSION_CODES.M) {
            return false;
        }
        return true;
    }

    public static boolean checkPermissions(String[] permissions, boolean is64Bit) {
        if (permissions == null) {
            return true;
        }
        for (String per : permissions) {
            if (!VCore.get().checkSelfPermission(per, is64Bit)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRequestGranted(int[] grantResults) {
        boolean allGranted = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                allGranted = false;
                break;
            }
        }
        return allGranted;
    }

    public interface CallBack {
        boolean onResult(int requestCode, String[] permissions, int[] grantResults);
    }

    public static void startRequestPermissions(Context context, boolean is64bit,
                                               String[] permissions, final CallBack callBack) {
        RequestPermissionsActivity.request(context, is64bit, permissions, new IRequestPermissionsResult.Stub() {
            @Override
            public boolean onResult(int requestCode, String[] permissions, int[] grantResults) {
                if (callBack != null) {
                    return callBack.onResult(requestCode, permissions, grantResults);
                }
                return false;
            }
        });

    }
}
