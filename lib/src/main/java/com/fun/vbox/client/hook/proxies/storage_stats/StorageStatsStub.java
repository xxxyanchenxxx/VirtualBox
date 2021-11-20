package com.fun.vbox.client.hook.proxies.storage_stats;

import android.annotation.TargetApi;
import android.app.usage.StorageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelableException;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceLastPkgMethodProxy;
import com.fun.vbox.client.hook.base.StaticMethodProxy;
import com.fun.vbox.client.ipc.VPackageManager;
import com.fun.vbox.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

import mirror.vbox.app.usage.IStorageStatsManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.O)
public class StorageStatsStub extends BinderInvocationProxy {

    public StorageStatsStub() {
        super(IStorageStatsManager.Stub.TYPE, Context.STORAGE_STATS_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceLastPkgMethodProxy("getTotalBytes"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getCacheBytes"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getCacheQuotaBytes"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryStatsForUser"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryExternalStatsForUser"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryStatsForUid"));
        addMethodProxy(new StaticMethodProxy("queryStatsForPackage") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                int packageNameIndex = ArrayUtils.indexOfFirst(args, String.class);
                int userIdIndex = ArrayUtils.indexOfLast(args, Integer.class);
                if (packageNameIndex != -1 && userIdIndex != -1) {
                    String packageName = (String) args[packageNameIndex];
                    int userId = (int) args[userIdIndex];
                    return queryStatsForPackage(packageName, userId);
                }
                return super.call(who, method, args);
            }
        });
    }

    private StorageStats queryStatsForPackage(String packageName, int userId) {
        ApplicationInfo appInfo = VPackageManager.get().getApplicationInfo(packageName, 0, userId);
        if (appInfo == null) {
            throw new ParcelableException(new PackageManager.NameNotFoundException(packageName));
        }
        StorageStats stats = mirror.vbox.app.usage.StorageStats.ctor.newInstance();
        mirror.vbox.app.usage.StorageStats.cacheBytes.set(stats, 0);
        mirror.vbox.app.usage.StorageStats.codeBytes.set(stats, 0);
        mirror.vbox.app.usage.StorageStats.dataBytes.set(stats, 0);
        return stats;
    }


}
