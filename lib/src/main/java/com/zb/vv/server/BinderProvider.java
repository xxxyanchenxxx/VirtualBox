package com.zb.vv.server;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.ServiceManagerNative;
import com.fun.vbox.server.ServiceCache;
import com.zb.vv.client.stub.KeepAliveService;
import com.fun.vbox.helper.compat.BundleCompat;
import com.fun.vbox.helper.compat.NotificationChannelCompat;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.server.accounts.VAccountManagerService;
import com.fun.vbox.server.am.VActivityManagerService;
import com.fun.vbox.server.content.VContentService;
import com.fun.vbox.server.device.VDeviceManagerService;
import com.fun.vbox.server.interfaces.IServiceFetcher;
import com.fun.vbox.server.job.VJobSchedulerService;
import com.fun.vbox.server.location.VirtualLocationService;
import com.fun.vbox.server.notification.VNotificationManagerService;
import com.fun.vbox.server.pm.VAppManagerService;
import com.fun.vbox.server.pm.VPackageManagerService;
import com.fun.vbox.server.pm.VUserManagerService;
import com.fun.vbox.server.vs.VirtualStorageService;

/**
 * @author Lody
 */
public final class BinderProvider extends ContentProvider {

    private final ServiceFetcher mServiceFetcher = new ServiceFetcher();
    private static boolean sInitialized = false;

    @Override
    public boolean onCreate() {
        return init();
    }

    public static boolean isInitialized() {
        return sInitialized;
    }

    private boolean init() {
        if (sInitialized) {
            return false;
        }
        Context context = getContext();
        if (context != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannelCompat.checkOrCreateChannel(context, NotificationChannelCompat.DAEMON_ID, "daemon");
                NotificationChannelCompat.checkOrCreateChannel(context, NotificationChannelCompat.DEFAULT_ID, "default");
            }
        }

        if (!VCore.get().isStartup()) {
            return false;
        }
        VPackageManagerService.systemReady();
        addService(ServiceManagerNative.PACKAGE, VPackageManagerService.get());
        addService(ServiceManagerNative.ACTIVITY, VActivityManagerService.get());
        addService(ServiceManagerNative.USER, VUserManagerService.get());
        VAppManagerService.systemReady();
        addService(ServiceManagerNative.APP, VAppManagerService.get());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addService(ServiceManagerNative.JOB, VJobSchedulerService.get());
        }
        VNotificationManagerService.systemReady(context);
        addService(ServiceManagerNative.NOTIFICATION, VNotificationManagerService.get());
        VAppManagerService.get().scanApps();
        VAccountManagerService.systemReady();
        VContentService.systemReady();
        addService(ServiceManagerNative.ACCOUNT, VAccountManagerService.get());
        addService(ServiceManagerNative.CONTENT, VContentService.get());
        addService(ServiceManagerNative.VS, VirtualStorageService.get());
        addService(ServiceManagerNative.DEVICE, VDeviceManagerService.get());
        addService(ServiceManagerNative.VIRTUAL_LOC, VirtualLocationService.get());
        sInitialized = true;
        return true;
    }

    private void startKeepAliveService() {
        try {
            Context context = getContext();
            if (context != null) {
                context.startService(new Intent(context, KeepAliveService.class));
            }
        } catch (Throwable e) {
            VLog.e("BinderProvider", e);
        }
    }

    private void addService(String name, IBinder service) {
        ServiceCache.addService(name, service);
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        startKeepAliveService();
        if (!sInitialized) {
            init();
        }
        if ("@".equals(method)) {
            Bundle bundle = new Bundle();
            BundleCompat.putBinder(bundle, "_VBOX_|_binder_", mServiceFetcher);
            return bundle;
        } else if ("_VBOX_|_invoke_".equals(method)) {
            if (VCore.get().isVirtualEngineCallbackEmpty()) {
                VCore.get().setInitBundle(extras);
                return null;
            }
            return VCore.get().getVirtualEngineCallback().invokeFromAnyWhere(extras);
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private class ServiceFetcher extends IServiceFetcher.Stub {
        @Override
        public IBinder getService(String name) throws RemoteException {
            if (name != null) {
                return ServiceCache.getService(name);
            }
            return null;
        }

        @Override
        public void addService(String name, IBinder service) throws RemoteException {
            if (name != null && service != null) {
                ServiceCache.addService(name, service);
            }
        }

        @Override
        public void removeService(String name) throws RemoteException {
            if (name != null) {
                ServiceCache.removeService(name);
            }
        }
    }
}
