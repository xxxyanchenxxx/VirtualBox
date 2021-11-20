package com.fun.vbox.server.bit64;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.Process;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.ProviderCall;
import com.fun.vbox.helper.DexOptimizer;
import com.fun.vbox.helper.NativeLibraryFixer;
import com.fun.vbox.helper.compat.NativeLibraryHelperCompat;
import com.fun.vbox.helper.utils.FileUtils;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.os.VEnvironment;
import com.fun.vbox.os.VUserInfo;
import com.fun.vbox.os.VUserManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lody
 */
public class V64BitHelper extends ContentProvider {
    private static final String TAG = "V64BitHelper";

    private static final String[] METHODS = {
            "getRunningAppProcess",
            "getRunningTasks",
            "getRecentTasks",
            "forceStop",
            "copyPackage",
            "uninstallPackage",
            "cleanPackageData",
            "startActivity",
            "invoke"
    };

    private static String getAuthority() {
        return VCore.getConfig().get64bitHelperAuthority();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    private static String sProcessName;

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (METHODS[0].equals(method)) {
            return getRunningAppProcess64(extras);
        } else if (METHODS[1].equals(method)) {
            return getRunningTasks64(extras);
        } else if (METHODS[2].equals(method)) {
            return getRecentTasks64(extras);
        } else if (METHODS[3].equals(method)) {
            return forceStop64(extras);
        } else if (METHODS[4].equals(method)) {
            return copyPackage64(extras);
        } else if (METHODS[5].equals(method)) {
            return uninstallPackage64(extras);
        } else if (METHODS[6].equals(method)) {
            return cleanPackageData64(extras);
        } else if (METHODS[7].equals(method)) {
            return startActivityInner(extras);
        } else if (METHODS[8].equals(method)) {
            return invokeInner(extras);
        }
        return null;
    }

    private Bundle invokeInner(Bundle extras) {
        Bundle param = extras.getBundle("param_bundle");
        return VCore.get().get64BitHelperCallback().invokeFromAnyWhere(param);
    }

    private Bundle startActivityInner(Bundle extras) {
        Bundle res = new Bundle();
        Intent destIntent = extras.getParcelable("intent");
        if (destIntent != null) {
            Bundle options = extras.getBundle("options");
            if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                VCore.get().getContext().startActivity(destIntent, options);
            } else {
                VCore.get().getContext().startActivity(destIntent);
            }
            res.putBoolean("res", true);
        } else {
            res.putBoolean("res", false);
        }
        return res;
    }

    private Bundle cleanPackageData64(Bundle extras) {
        int[] userIds = extras.getIntArray("user_ids");
        String packageName = extras.getString("package_name");
        if (packageName == null) {
            return null;
        }
        if (userIds == null) {
            return null;
        }
        for (int userId : userIds) {
            FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory64(userId, packageName));
        }
        return null;
    }

    private Bundle uninstallPackage64(Bundle extras) {
        int[] userIds = extras.getIntArray("user_ids");
        String packageName = extras.getString("package_name");
        boolean fullRemove = extras.getBoolean("full_remove", false);
        if (packageName == null) {
            return null;
        }
        if (userIds == null) {
            return null;
        }
        if (fullRemove) {
            VEnvironment.getPackageResourcePath64(packageName).delete();
            FileUtils.deleteDir(VEnvironment.getDataAppPackageDirectory64(packageName));
            VEnvironment.getOdexFile64(packageName).delete();
        }
        for (int userId : userIds) {
            FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory64(userId, packageName));
        }
        return null;
    }

    private Bundle getRunningAppProcess64(Bundle extras) {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningAppProcessInfo> processes = new ArrayList<>(am.getRunningAppProcesses());
        Bundle res = new Bundle();
        res.putParcelableArrayList("running_processes", processes);
        return res;
    }

    private Bundle getRunningTasks64(Bundle extras) {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int maxNum = extras.getInt("max_num", Integer.MAX_VALUE);
        ArrayList<ActivityManager.RunningTaskInfo> tasks = new ArrayList<>(am.getRunningTasks(maxNum));
        Bundle res = new Bundle();
        res.putParcelableArrayList("running_tasks", tasks);
        return res;
    }

    private Bundle getRecentTasks64(Bundle extras) {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int maxNum = extras.getInt("max_num", Integer.MAX_VALUE);
        int flags = extras.getInt("flags", 0);
        ArrayList<ActivityManager.RecentTaskInfo> tasks = new ArrayList<>(am.getRecentTasks(maxNum, flags));
        Bundle res = new Bundle();
        res.putParcelableArrayList("recent_tasks", tasks);
        return res;
    }

    private Bundle forceStop64(Bundle extras) {
        Object pidOrPids = extras.get("target");
        if (pidOrPids instanceof Integer) {
            int pid = (int) pidOrPids;
            Process.killProcess(pid);
        } else if (pidOrPids instanceof int[]) {
            int[] pids = (int[]) pidOrPids;
            for (int pid : pids) {
                Process.killProcess(pid);
            }
        }
        return null;
    }

    private Bundle copyPackage64(Bundle extras) {
        boolean success = false;
        ParcelFileDescriptor fd = extras.getParcelable("fd");
        String packageName = extras.getString("package_name");
        if (fd != null && packageName != null) {
            File targetPath = VEnvironment.getPackageResourcePath64(packageName);
            try {
                FileInputStream is = new FileInputStream(fd.getFileDescriptor());
                FileUtils.writeToFile(is, targetPath);
                FileUtils.closeQuietly(is);
                VEnvironment.chmodPackageDictionary(targetPath);
                File libDir = VEnvironment.getAppLibDirectory64(packageName);
                NativeLibraryHelperCompat.copyNativeBinaries(targetPath, libDir);
                NativeLibraryHelperCompat.copyNativeBinaries2(packageName, libDir);
                try {
                    DexOptimizer.optimizeDex(targetPath.getPath(), VEnvironment.getOdexFile64(packageName).getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (packageName != null) {
            File libDir = VEnvironment.getAppLibDirectory64(packageName);

            //判断App更新后删除So路径重新拷贝
            NativeLibraryFixer.checkAndCopySo(packageName, libDir);

            File[] libDirFiles = libDir.listFiles();
            if (libDirFiles == null || libDirFiles.length == 0) {
                VLog.e(TAG, "copyNativeBinaries failed!!! for" + packageName);
                success = false;
            } else {
                success = true;
            }
        }
        Bundle res = new Bundle();
        res.putBoolean("res", success);
        return res;
    }

    public static boolean has64BitEngineStartPermission() {
        try {
            new ProviderCall.Builder(VCore.get().getContext(), getAuthority()).methodName("@").retry(1).call();
            return true;
        } catch (IllegalAccessException e) {
            // ignore
        }
        return false;
    }

    private static ProviderCall.Builder getHelper() {
        return new ProviderCall.Builder(VCore.get().getContext(), getAuthority()).retry(1);
    }

    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcess64() {
        if (VCore.get().is64BitEngineInstalled()) {
            Bundle res = callSafely(getHelper().methodName(METHODS[0]));
            if (res != null) {
                return res.getParcelableArrayList("running_processes");
            }
        }
        return Collections.emptyList();
    }

    public static List<ActivityManager.RunningTaskInfo> getRunningTasks64(int maxNum) {
        if (VCore.get().is64BitEngineInstalled()) {
            Bundle res = callSafely(getHelper().methodName(METHODS[1]).addArg("max_num", maxNum).retry(1));
            if (res != null) {
                return res.getParcelableArrayList("running_tasks");
            }
        }
        return Collections.emptyList();
    }


    public static List<ActivityManager.RecentTaskInfo> getRecentTasks64(int maxNum, int flags) {
        if (VCore.get().is64BitEngineInstalled()) {
            Bundle res = callSafely(getHelper()
                    .methodName(METHODS[2])
                    .addArg("max_num", maxNum)
                    .addArg("flags", flags));
            if (res != null) {
                return res.getParcelableArrayList("recent_tasks");
            }
        }
        return Collections.emptyList();
    }

    public static void forceStop64(int pid) {
        if (VCore.get().is64BitEngineInstalled()) {
            callSafely(getHelper().methodName(METHODS[3]).addArg("target", pid).retry(1));
        }
    }

    public static void forceStop64(int[] pids) {
        if (VCore.get().is64BitEngineInstalled()) {
            callSafely(getHelper().methodName(METHODS[3]).addArg("target", pids).retry(1));
        }
    }


    public static void uninstallPackage64(int userId, String packageName) {
        if (VCore.get().is64BitEngineInstalled()) {
            boolean fullRemove = userId == -1;
            int[] userIds;
            if (fullRemove) {
                List<VUserInfo> userInfos = VUserManager.get().getUsers();
                userIds = new int[userInfos.size()];
                for (int i = 0; i < userIds.length; i++) {
                    VUserInfo info = userInfos.get(i);
                    userIds[i] = info.id;
                }
            } else {
                userIds = new int[]{userId};
            }
            callSafely(getHelper()
                    .methodName(METHODS[5])
                    .addArg("user_ids", userIds)
                    .addArg("full_remove", fullRemove)
                    .addArg("package_name", packageName));
        }
    }

    public static void cleanPackageData64(int userId, String packageName) {
        if (VCore.get().is64BitEngineInstalled()) {
            int[] userIds;
            if (userId == -1) {
                List<VUserInfo> userInfos = VUserManager.get().getUsers();
                userIds = new int[userInfos.size()];
                for (int i = 0; i < userIds.length; i++) {
                    VUserInfo info = userInfos.get(i);
                    userIds[i] = info.id;
                }
            } else {
                userIds = new int[]{userId};
            }
            callSafely(getHelper()
                    .methodName(METHODS[6])
                    .addArg("user_ids", userIds)
                    .addArg("package_name", packageName));
        }
    }

    public static boolean copyPackage64(String packagePath, String packageName) {
        if (VCore.get().is64BitEngineInstalled()) {
            try {
                FileInputStream is = new FileInputStream(packagePath);
                byte[] content = FileUtils.toByteArray(is);
                FileUtils.closeQuietly(is);
                final MemoryFile memoryFile = new MemoryFile("file_" + packageName, content.length);
                memoryFile.allowPurging(false);
                memoryFile.getOutputStream().write(content);
                FileDescriptor fd = mirror.vbox.os.MemoryFile.getFileDescriptor.call(memoryFile);
                ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(fd);
                Bundle res = callSafely(getHelper()
                        .methodName(METHODS[4])
                        .addArg("fd", pfd)
                        .addArg("package_name", packageName));
                memoryFile.close();
                if (res != null) {
                    return res.getBoolean("res");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean copyPackageLib(String packageName) {
        if (VCore.get().is64BitEngineInstalled()) {
            try {
                Bundle res = callSafely(getHelper()
                        .methodName(METHODS[4])
                        .addArg("package_name", packageName));
                if (res != null) {
                    return res.getBoolean("res");
                }
            } catch (Throwable e) {
                VLog.e(TAG, e);
            }
        }
        return false;
    }

    public static Bundle startActivity(Intent intent, Bundle options) {
        if (VCore.get().is64BitEngineInstalled()) {
            try {
                Bundle res = callSafely(getHelper()
                        .methodName(METHODS[7])
                        .addArg("intent", intent)
                        .addArg("options", options)
                );
                return res;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bundle invoke(Bundle param) {
        if (VCore.get().is64BitEngineInstalled()) {
            try {
                Bundle res = callSafely(getHelper()
                        .methodName(METHODS[8])
                        .addArg("param_bundle", param)
                );
                return res;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Bundle callSafely(ProviderCall.Builder builder) {
        try {
            return builder.call();
        } catch (IllegalAccessException e) {
            /**
             * Fuck oppo PACM00手机，64位应用中的，ContentProvider无法启动，
             * 需要先启动进程对应的Activity，才能拉起进程
             * see {@link com.lody.virtual.server.am.VActivityManagerService} initProcess()
             */
            ensureProcessStarted();
            try {
                return builder.call();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static void ensureProcessStarted() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(
                VCore.getConfig().get64bitEnginePackageName(),
                VCore.getConfig().get64bitEngineLaunchActivityName()
        );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        try {
            VCore.get().getContext().startActivity(intent);
        } catch (Throwable ignore) {
            //
        }
    }
}

