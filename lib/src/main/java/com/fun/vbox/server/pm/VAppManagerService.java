package com.fun.vbox.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;

import com.fun.vbox.client.NativeEngine;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.env.SpecialComponentList;
import com.fun.vbox.client.stub.StubManifest;
import com.fun.vbox.helper.DexOptimizer;
import com.fun.vbox.helper.collection.IntArray;
import com.fun.vbox.helper.compat.BuildCompat;
import com.fun.vbox.helper.compat.NativeLibraryHelperCompat;
import com.fun.vbox.helper.utils.ArrayUtils;
import com.fun.vbox.helper.utils.FileUtils;
import com.fun.vbox.helper.utils.Singleton;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.os.VEnvironment;
import com.fun.vbox.os.VUserHandle;
import com.fun.vbox.os.VUserInfo;
import com.fun.vbox.os.VUserManager;
import com.fun.vbox.remote.InstallOptions;
import com.fun.vbox.remote.InstallResult;
import com.fun.vbox.remote.InstalledAppInfo;
import com.fun.vbox.server.accounts.VAccountManagerService;
import com.fun.vbox.server.am.UidSystem;
import com.fun.vbox.server.am.VActivityManagerService;
import com.fun.vbox.server.bit64.V64BitHelper;
import com.fun.vbox.server.interfaces.IAppManager;
import com.fun.vbox.server.interfaces.IPackageObserver;
import com.fun.vbox.server.notification.VNotificationManagerService;
import com.fun.vbox.server.pm.parser.PackageLite;
import com.fun.vbox.server.pm.parser.PackageParserEx;
import com.fun.vbox.server.pm.parser.VPackage;
import com.fun.vbox.server.bit64.Bit64Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fun.vbox.remote.InstalledAppInfo.MODE_APP_COPY_APK;
import static com.fun.vbox.remote.InstalledAppInfo.MODE_APP_USE_OUTSIDE_APK;


/**
 * @author Lody
 */
public class VAppManagerService extends IAppManager.Stub {

    private static final String TAG = VAppManagerService.class.getSimpleName();
    private static final Singleton<VAppManagerService> sService = new Singleton<VAppManagerService>() {
        @Override
        protected VAppManagerService create() {
            return new VAppManagerService();
        }
    };
    private final UidSystem mUidSystem = new UidSystem();
    private final PackagePersistenceLayer mPersistenceLayer = new PackagePersistenceLayer(this);
    private final Set<String> mVisibleOutsidePackages = new HashSet<>();
    private boolean mBooting;
    private RemoteCallbackList<IPackageObserver> mRemoteCallbackList = new RemoteCallbackList<>();
    private BroadcastReceiver appEventReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBooting) {
                return;
            }
            PendingResult result = goAsync();
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            Uri data = intent.getData();
            if (data == null) {
                return;
            }
            String pkg = data.getSchemeSpecificPart();
            if (pkg == null) {
                return;
            }
            PackageSetting ps = PackageCacheManager.getSetting(pkg);
            if (ps == null || ps.appMode != InstalledAppInfo.MODE_APP_USE_OUTSIDE_APK) {
                return;
            }
            VActivityManagerService.get().killAppByPkg(pkg, VUserHandle.USER_ALL);
            if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                ApplicationInfo outInfo = null;
                try {
                    outInfo = VCore.getPM().getApplicationInfo(pkg, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (outInfo == null) {
                    return;
                }
                InstallOptions options = InstallOptions.makeOptions(true, false, InstallOptions.UpdateStrategy.FORCE_UPDATE);
                InstallResult res = installPackageImpl(outInfo.publicSourceDir, options);
                VLog.e(TAG, "Update package %s %s", res.packageName, res.isSuccess ? "success" : "failed");
            } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                if (intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false)) {
                    VLog.e(TAG, "Removing package %s...", ps.packageName);
                    uninstallPackageFully(ps, true);
                }
            }
            result.finish();
        }
    };


    public static VAppManagerService get() {
        return sService.get();
    }

    public static void systemReady() {
        VEnvironment.systemReady();
        if (!BuildCompat.isPie()) {
            get().extractRequiredFrameworks();
        }
        get().startup();
    }

    private void startup() {
        mVisibleOutsidePackages.add("com.android.providers.downloads");
        mUidSystem.initUidList();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        VCore.get().getContext().registerReceiver(appEventReciever, filter);
    }

    public boolean isBooting() {
        return mBooting;
    }

    private void extractRequiredFrameworks() {
        for (String framework : StubManifest.REQUIRED_FRAMEWORK) {
            File zipFile = VEnvironment.getFrameworkFile32(framework);
            File odexFile = VEnvironment.getOptimizedFrameworkFile32(framework);
            if (!odexFile.exists()) {
                OatHelper.extractFrameworkFor32Bit(framework, zipFile, odexFile);
            }
        }
    }

    @Override
    public void scanApps() {
        if (mBooting) {
            return;
        }
        synchronized (this) {
            mBooting = true;
            mPersistenceLayer.read();
            if (mPersistenceLayer.changed) {
                mPersistenceLayer.changed = false;
                mPersistenceLayer.save();
                VLog.w(TAG, "Package PersistenceLayer updated.");
            }
            for (String preInstallPkg : SpecialComponentList.getPreInstallPackages()) {
                if (!isAppInstalled(preInstallPkg)) {
                    try {
                        ApplicationInfo outInfo = VCore.get().getUnHookPackageManager().getApplicationInfo(preInstallPkg, 0);
                        InstallOptions options = InstallOptions.makeOptions(true, false, InstallOptions.UpdateStrategy.FORCE_UPDATE);
                        installPackageImpl(outInfo.publicSourceDir, options);
                    } catch (PackageManager.NameNotFoundException e) {
                        // ignore
                    }
                }
            }
            PrivilegeAppOptimizer.get().performOptimizeAllApps();
            mBooting = false;
        }
    }

    private void cleanUpResidualFiles(PackageSetting ps) {
        VLog.e(TAG, "cleanup residual files for : %s", ps.packageName);
        uninstallPackageFully(ps, false);
    }


    public void onUserCreated(VUserInfo userInfo) {
        VEnvironment.getUserDataDirectory(userInfo.id).mkdirs();
    }


    synchronized boolean loadPackage(PackageSetting setting) {
        if (!loadPackageInnerLocked(setting)) {
            cleanUpResidualFiles(setting);
            return false;
        }
        return true;
    }

    private boolean loadPackageInnerLocked(PackageSetting ps) {
        boolean modeUseOutsideApk = ps.appMode == InstalledAppInfo.MODE_APP_USE_OUTSIDE_APK;
        if (modeUseOutsideApk) {
            if (!VCore.get().isOutsideInstalled(ps.packageName)) {
                return false;
            }
        }
        File cacheFile = VEnvironment.getPackageCacheFile(ps.packageName);
        VPackage pkg = null;
        try {
            pkg = PackageParserEx.readPackageCache(ps.packageName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (pkg == null || pkg.packageName == null) {
            return false;
        }
        VEnvironment.chmodPackageDictionary(cacheFile);
        PackageCacheManager.put(pkg, ps);
        if (modeUseOutsideApk) {
            try {
                PackageInfo outInfo = VCore.get().getUnHookPackageManager().getPackageInfo(ps.packageName, 0);
                if (pkg.mVersionCode != outInfo.versionCode) {
                    VLog.d(TAG, "app (" + ps.packageName + ") has changed version, update it.");
                    InstallOptions options = InstallOptions.makeOptions(true, false, InstallOptions.UpdateStrategy.FORCE_UPDATE);
                    installPackageImpl(outInfo.applicationInfo.publicSourceDir, options);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return false;
            }

        }
        return true;
    }

    @Override
    public boolean isOutsidePackageVisible(String pkg) {
        return pkg != null && mVisibleOutsidePackages.contains(pkg);
    }

    @Override
    public int getUidForSharedUser(String sharedUserName) {
        if (sharedUserName == null) {
            return -1;
        }
        return mUidSystem.getUid(sharedUserName);
    }

    @Override
    public void addVisibleOutsidePackage(String pkg) {
        if (pkg != null) {
            mVisibleOutsidePackages.add(pkg);
        }
    }

    @Override
    public void removeVisibleOutsidePackage(String pkg) {
        if (pkg != null) {
            mVisibleOutsidePackages.remove(pkg);
        }
    }

    @Override
    public void installPackage(String path, InstallOptions options, ResultReceiver receiver) {
        InstallResult res;
        synchronized (this) {
            res = installPackageImpl(path, options);
        }
        if (receiver != null) {
            android.os.Bundle data = new Bundle();
            data.putParcelable("result", res);
            receiver.send(0, data);
        }
    }

    @Override
    public void requestCopyPackage64(String packageName) {
        /**
         * Lock VAMS avoid two process invoke this method Simultaneously.
         */
        synchronized (VActivityManagerService.get()) {
            PackageSetting ps = PackageCacheManager.getSetting(packageName);
            if (ps != null && ps.appMode == MODE_APP_USE_OUTSIDE_APK) {
                V64BitHelper.copyPackage64(ps.getApkPath(false), packageName);
            }
        }
    }

    public InstallResult installPackage(String path, InstallOptions options) {
        synchronized (this) {
            return installPackageImpl(path, options);
        }
    }


    private InstallResult installPackageImpl(String path, InstallOptions options) {
        long installTime = System.currentTimeMillis();
        if (path == null) {
            return InstallResult.makeFailure("path = NULL");
        }
        File packageFile = new File(path);
        if (!packageFile.exists() || !packageFile.isFile()) {
            return InstallResult.makeFailure("Package File is not exist.");
        }
        PackageLite packageLite = null;
        try {
            long begin = System.currentTimeMillis();
            packageLite = PackageLite.parse(packageFile);
            VLog.i(TAG,
                    "parse:" + packageFile.getAbsolutePath()
                            + " pkg:" + packageLite.packageName
                            + " verCode:" + packageLite.versionCode
                            + " cost:" + (System.currentTimeMillis() - begin) + "ms");
        } catch (Throwable ignore) {
            //
        }
        if (packageLite == null || packageLite.packageName == null) {
            return InstallResult.makeFailure("Unable to parse the package.");
        }

        boolean nativeTraceProcess = NativeEngine.nativeTraceProcessEx(Build.VERSION.SDK_INT);

        File libDir = VEnvironment.getAppLibDirectory(packageLite.packageName);
        InstallResult res = new InstallResult();
        res.packageName = packageLite.packageName;
        // PackageCache holds all packages, try to check if we need to update.
        VPackage existOne = PackageCacheManager.get(packageLite.packageName);
        PackageSetting existSetting = existOne != null ? (PackageSetting) existOne.mExtras : null;
        if (existOne != null) {
            if (options.updateStrategy == InstallOptions.UpdateStrategy.IGNORE_NEW_VERSION) {
                res.isUpdate = true;
                return res;
            }
            if (!isAllowedUpdate(existOne.mVersionCode, packageLite.versionCode, options.updateStrategy)) {
                VLog.e(TAG, "Not allowed to update the package.");

                if (existSetting.isRunOn64BitProcess() && nativeTraceProcess) {
                    if (!VCore.get().is64BitEngineInstalled() || !VCore.get().has64BitEngineStartPermission()) {
                        VLog.e(TAG, "64bit engine not installed.");
                        return InstallResult.makeFailure("32bit engine not installed.");
                    }
                    boolean result = V64BitHelper.copyPackageLib(packageLite.packageName);
                    if (!result) {
                        VLog.e(TAG, "copyPackageLib failed!!!");
                    }
                }
                return InstallResult.makeFailure("Not allowed to update the package.");
            }
            res.isUpdate = true;
            VActivityManagerService.get().killAppByPkg(res.packageName, VUserHandle.USER_ALL);
        }
        if (res.isUpdate) {
            FileUtils.deleteDir(libDir);
            VEnvironment.getOdexFile(packageLite.packageName).delete();
        }
        if (!libDir.exists() && !libDir.mkdirs()) {
            return InstallResult.makeFailure("Unable to create lib dir.");
        }

        VPackage pkg = null;
        try {
            pkg = PackageParserEx.parsePackage(packageFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (pkg == null || pkg.packageName == null) {
            return InstallResult.makeFailure("Unable to parse the package.");
        }

        boolean useSourceLocationApk = options.useSourceLocationApk;
        if (existOne != null) {
            PackageCacheManager.remove(pkg.packageName);
        }
        PackageSetting ps;
        if (existSetting != null) {
            ps = existSetting;
        } else {
            ps = new PackageSetting();
        }
        Bit64Utils.Result supportAbi = Bit64Utils.getSupportAbi(pkg.packageName, packageFile.getPath());
        ps.packageName = pkg.packageName;
        ps.flag = supportAbi.flag;

        if (ps.isRunOn64BitProcess() && nativeTraceProcess) {
            if (!VCore.get().is64BitEngineInstalled() || !VCore.get().has64BitEngineStartPermission()) {
                VLog.e(TAG, "64bit engine not installed.");
                return InstallResult.makeFailure("64bit engine not installed.");
            }
            boolean result = V64BitHelper.copyPackageLib(pkg.packageName);
            if (!result) {
                VLog.e(TAG, "copyPackageLib " + pkg.packageName + " failed!!!");
            }
        }

        if (nativeTraceProcess) {
            NativeLibraryHelperCompat.copyNativeBinaries(packageFile, libDir);
            NativeLibraryHelperCompat.copyNativeBinaries2(ps.packageName, libDir);
        }

        if (!useSourceLocationApk) {
            File privatePackageFile = VEnvironment.getPackageResourcePath(pkg.packageName);
            try {
                FileUtils.copyFile(packageFile, privatePackageFile);
            } catch (IOException e) {
                privatePackageFile.delete();
                return InstallResult.makeFailure("Unable to copy the package file.");
            }
            packageFile = privatePackageFile;
            VEnvironment.chmodPackageDictionary(packageFile);
        }

        if (supportAbi.support64bit && !useSourceLocationApk) {
            V64BitHelper.copyPackage64(packageFile.getPath(), pkg.packageName);
        }

        ps.appMode = useSourceLocationApk ? MODE_APP_USE_OUTSIDE_APK : MODE_APP_COPY_APK;
        ps.packageName = pkg.packageName;
        ps.appId = VUserHandle.getAppId(mUidSystem.getOrCreateUid(pkg));
        if (res.isUpdate) {
            ps.lastUpdateTime = installTime;
        } else {
            ps.firstInstallTime = installTime;
            ps.lastUpdateTime = installTime;
            for (int userId : VUserManagerService.get().getUserIds()) {
                boolean installed = userId == 0;
                ps.setUserState(userId, false/*launched*/, false/*hidden*/, installed);
            }
        }
        PackageParserEx.savePackageCache(pkg);
        PackageCacheManager.put(pkg, ps);
        mPersistenceLayer.save();
        if (supportAbi.support32bit && !useSourceLocationApk) {
            try {
                DexOptimizer.optimizeDex(packageFile.getPath(), VEnvironment.getOdexFile(ps.packageName).getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (options.notify) {
            notifyAppInstalled(ps, -1);
        }
        res.isSuccess = true;
        return res;
    }

    @Override
    public synchronized boolean installPackageAsUser(int userId, String packageName) {
        if (VUserManagerService.get().exists(userId)) {
            PackageSetting ps = PackageCacheManager.getSetting(packageName);
            if (ps != null) {
                if (!ps.isInstalled(userId)) {
                    ps.setInstalled(userId, true);
                    notifyAppInstalled(ps, userId);
                    mPersistenceLayer.save();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAllowedUpdate(
            int existOneVersionCode,
            int newOneVersionCode,
            InstallOptions.UpdateStrategy strategy
    ) {
        switch (strategy) {
            case FORCE_UPDATE:
                return true;
            case COMPARE_VERSION:
                return existOneVersionCode != newOneVersionCode;
            case TERMINATE_IF_EXIST:
                return false;
        }
        return true;
    }



    @Override
    public synchronized boolean uninstallPackage(String packageName) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null) {
            uninstallPackageFully(ps, true);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean uninstallPackageAsUser(String packageName, int userId) {
        if (!VUserManagerService.get().exists(userId)) {
            return false;
        }
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null) {
            int[] userIds = getPackageInstalledUsers(packageName);
            if (!ArrayUtils.contains(userIds, userId)) {
                return false;
            }
            if (userIds.length == 1) {
                uninstallPackageFully(ps, true);
            } else {
                // Just hidden it
                VActivityManagerService.get().killAppByPkg(packageName, userId);
                ps.setInstalled(userId, false);
                mPersistenceLayer.save();
                deletePackageDataAsUser(userId, ps);
                notifyAppUninstalled(ps, userId);
            }
            return true;
        }
        return false;
    }

    private boolean isPackageSupport32Bit(PackageSetting ps) {
        return ps.flag == PackageSetting.FLAG_RUN_32BIT
                || ps.flag == PackageSetting.FLAG_RUN_BOTH_32BIT_64BIT;
    }

    private boolean isPackageSupport64Bit(PackageSetting ps) {
        return ps.flag == PackageSetting.FLAG_RUN_64BIT
                || ps.flag == PackageSetting.FLAG_RUN_BOTH_32BIT_64BIT;
    }

    private void deletePackageDataAsUser(int userId, PackageSetting ps) {
        if (isPackageSupport32Bit(ps)) {
            if (userId == -1) {
                List<VUserInfo> userInfos = VUserManager.get().getUsers();
                if (userInfos != null) {
                    for (VUserInfo info : userInfos) {
                        FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(info.id, ps.packageName));
                    }
                }
            } else {
                FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(userId, ps.packageName));
            }
        }
        if (isPackageSupport64Bit(ps)) {
            V64BitHelper.cleanPackageData64(userId, ps.packageName);
        }
        VNotificationManagerService.get().cancelAllNotification(ps.packageName, userId);
    }

    public boolean cleanPackageData(String pkg, int userId) {
        PackageSetting ps = PackageCacheManager.getSetting(pkg);
        if (ps == null) {
            return false;
        }
        VActivityManagerService.get().killAppByPkg(pkg, userId);
        deletePackageDataAsUser(userId, ps);
        return true;
    }

    private void uninstallPackageFully(PackageSetting ps, boolean notify) {
        String packageName = ps.packageName;
        try {
            VActivityManagerService.get().killAppByPkg(packageName, VUserHandle.USER_ALL);
            if (isPackageSupport32Bit(ps)) {
                VEnvironment.getPackageResourcePath(packageName).delete();
                FileUtils.deleteDir(VEnvironment.getDataAppPackageDirectory(packageName));
                VEnvironment.getOdexFile(packageName).delete();
                for (int id : VUserManagerService.get().getUserIds()) {
                    deletePackageDataAsUser(id, ps);
                }
            }
            if (isPackageSupport64Bit(ps)) {
                V64BitHelper.uninstallPackage64(-1, packageName);
            }
            PackageCacheManager.remove(packageName);
            File cacheFile = VEnvironment.getPackageCacheFile(packageName);
            cacheFile.delete();
            File signatureFile = VEnvironment.getSignatureFile(packageName);
            signatureFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (notify) {
                notifyAppUninstalled(ps, -1);
            }
        }
    }

    @Override
    public int[] getPackageInstalledUsers(String packageName) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null) {
            IntArray installedUsers = new IntArray(5);
            int[] userIds = VUserManagerService.get().getUserIds();
            for (int userId : userIds) {
                if (ps.readUserState(userId).installed) {
                    installedUsers.add(userId);
                }
            }
            return installedUsers.getAll();
        }
        return new int[0];
    }

    @Override
    public List<InstalledAppInfo> getInstalledApps(int flags) {
        List<InstalledAppInfo> infoList = new ArrayList<>(getInstalledAppCount());
        for (VPackage p : PackageCacheManager.PACKAGE_CACHE.values()) {
            PackageSetting setting = (PackageSetting) p.mExtras;
            infoList.add(setting.getAppInfo());
        }
        return infoList;
    }

    @Override
    public List<InstalledAppInfo> getInstalledAppsAsUser(int userId, int flags) {
        List<InstalledAppInfo> infoList = new ArrayList<>(getInstalledAppCount());
        for (VPackage p : PackageCacheManager.PACKAGE_CACHE.values()) {
            PackageSetting setting = (PackageSetting) p.mExtras;
            boolean visible = setting.isInstalled(userId);
            if ((flags & VCore.GET_HIDDEN_APP) == 0 && setting.isHidden(userId)) {
                visible = false;
            }
            if (visible) {
                infoList.add(setting.getAppInfo());
            }
        }
        return infoList;
    }

    @Override
    public int getInstalledAppCount() {
        return PackageCacheManager.PACKAGE_CACHE.size();
    }

    @Override
    public boolean isAppInstalled(String packageName) {
        return packageName != null && PackageCacheManager.PACKAGE_CACHE.containsKey(packageName);
    }

    @Override
    public boolean isAppInstalledAsUser(int userId, String packageName) {
        if (packageName == null || !VUserManagerService.get().exists(userId)) {
            return false;
        }
        PackageSetting setting = PackageCacheManager.getSetting(packageName);
        if (setting == null) {
            return false;
        }
        return setting.isInstalled(userId);
    }

    private void notifyAppInstalled(PackageSetting setting, int userId) {
        final String pkg = setting.packageName;
        int N = mRemoteCallbackList.beginBroadcast();
        while (N-- > 0) {
            try {
                if (userId == -1) {
                    mRemoteCallbackList.getBroadcastItem(N).onPackageInstalled(pkg);
                    mRemoteCallbackList.getBroadcastItem(N).onPackageInstalledAsUser(0, pkg);

                } else {
                    mRemoteCallbackList.getBroadcastItem(N).onPackageInstalledAsUser(userId, pkg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        sendInstalledBroadcast(pkg, new VUserHandle(userId));
        mRemoteCallbackList.finishBroadcast();
        VAccountManagerService.get().refreshAuthenticatorCache(null);
    }

    private void notifyAppUninstalled(PackageSetting setting, int userId) {
        final String pkg = setting.packageName;
        int N = mRemoteCallbackList.beginBroadcast();
        while (N-- > 0) {
            try {
                if (userId == -1) {
                    mRemoteCallbackList.getBroadcastItem(N).onPackageUninstalled(pkg);
                    mRemoteCallbackList.getBroadcastItem(N).onPackageUninstalledAsUser(0, pkg);
                } else {
                    mRemoteCallbackList.getBroadcastItem(N).onPackageUninstalledAsUser(userId, pkg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        sendUninstalledBroadcast(pkg, new VUserHandle(userId));
        mRemoteCallbackList.finishBroadcast();
        VAccountManagerService.get().refreshAuthenticatorCache(null);
    }


    private void sendInstalledBroadcast(String packageName, VUserHandle user) {
        Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + packageName));
        VActivityManagerService.get().sendBroadcastAsUser(intent, user);
    }

    private void sendUninstalledBroadcast(String packageName, VUserHandle user) {
        Intent intent = new Intent(Intent.ACTION_PACKAGE_REMOVED);
        intent.setData(Uri.parse("package:" + packageName));
        VActivityManagerService.get().sendBroadcastAsUser(intent, user);
    }

    @Override
    public void registerObserver(IPackageObserver observer) {
        try {
            mRemoteCallbackList.register(observer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregisterObserver(IPackageObserver observer) {
        try {
            mRemoteCallbackList.unregister(observer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public InstalledAppInfo getInstalledAppInfo(String packageName, int flags) {
        synchronized (PackageCacheManager.class) {
            if (packageName != null) {
                PackageSetting setting = PackageCacheManager.getSetting(packageName);
                if (setting != null) {
                    return setting.getAppInfo();
                }
            }
            return null;
        }
    }

    @Override
    public boolean isRun64BitProcess(String packageName) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        return ps != null && ps.isRunOn64BitProcess();
    }

    @Override
    public synchronized boolean isIORelocateWork() {
        return true;
    }

    public boolean isPackageLaunched(int userId, String packageName) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        return ps != null && ps.isLaunched(userId);
    }

    public void setPackageHidden(int userId, String packageName, boolean hidden) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null && VUserManagerService.get().exists(userId)) {
            ps.setHidden(userId, hidden);
            mPersistenceLayer.save();
        }
    }

    public int getAppId(String packageName) {
        PackageSetting setting = PackageCacheManager.getSetting(packageName);
        return setting != null ? setting.appId : -1;
    }

    void restoreFactoryState() {
        VLog.w(TAG, "Warning: Restore the factory state...");
        VEnvironment.getDalvikCacheDirectory().delete();
        VEnvironment.getUserSystemDirectory().delete();
        VEnvironment.getUserDeSystemDirectory().delete();
        VEnvironment.getDataAppDirectory().delete();
    }

    public void savePersistenceData() {
        mPersistenceLayer.save();
    }

    public boolean is64BitUid(int uid) throws PackageManager.NameNotFoundException {
        int appId = VUserHandle.getAppId(uid);
        synchronized (PackageCacheManager.PACKAGE_CACHE) {
            for (VPackage p : PackageCacheManager.PACKAGE_CACHE.values()) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                if (ps.appId == appId) {
                    return ps.isRunOn64BitProcess();
                }
            }
        }
        throw new PackageManager.NameNotFoundException();
    }

    @Override
    public boolean cleanUselessApp() {
        final HashSet<String> setPkg = new HashSet<>(getInstalledAppCount());
        for (VPackage p : PackageCacheManager.PACKAGE_CACHE.values()) {
            setPkg.add(p.packageName);
        }
        setPkg.add("system");

        File dataAppDir = VEnvironment.getDataAppDirectory();
        File[] listFile = dataAppDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean isUse = setPkg.contains(name);
                return !isUse;
            }
        });
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                FileUtils.deleteDir(file);
            }
        }

        int[] userIds = VUserManagerService.get().getUserIds();
        for (int userId : userIds) {
            File userDataDir = VEnvironment.getUserDataDirectory(userId);
            if (userDataDir.exists()) {
                File[] listUserData = userDataDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        boolean isUse = setPkg.contains(name);
                        return !isUse;
                    }
                });
                if (listFile != null && listFile.length > 0) {
                    for (File file : listUserData) {
                        FileUtils.deleteDir(file);
                    }
                }
            }

            File userDeDataDir = VEnvironment.getUserDeDataDirectory(userId);
            if (userDeDataDir.exists()) {
                File[] listUserDeData = userDeDataDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        boolean isUse = setPkg.contains(name);
                        return !isUse;
                    }
                });
                if (listFile != null && listFile.length > 0) {
                    for (File file : listUserDeData) {
                        FileUtils.deleteDir(file);
                    }
                }
            }
        }
        return true;
    }
}
