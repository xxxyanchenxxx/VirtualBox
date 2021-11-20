package com.fun.vbox.client.ipc;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.compat.BundleCompat;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.server.ServiceCache;
import com.fun.vbox.server.interfaces.IServiceFetcher;

/**
 * @author Lody
 */
public class ServiceManagerNative {
    public static final String PACKAGE = "package";
    public static final String ACTIVITY = "activity";
    public static final String USER = "user";
    public static final String APP = "app";
    public static final String ACCOUNT = "account";
    public static final String CONTENT = "content";
    public static final String JOB = "job";
    public static final String NOTIFICATION = "notification";
    public static final String VS = "vs";
    public static final String DEVICE = "device";
    public static final String VIRTUAL_LOC = "virtual-loc";

    private static final String TAG = ServiceManagerNative.class.getSimpleName();

    private static IServiceFetcher sFetcher;

    private static String getAuthority() {
        return VCore.getConfig().getBinderProviderAuthority();
    }

    private static IServiceFetcher getServiceFetcher() {
        if (sFetcher == null || !sFetcher.asBinder().isBinderAlive()) {
            synchronized (ServiceManagerNative.class) {
                Context context = VCore.get().getContext();
                Bundle response = new ProviderCall.Builder(context, getAuthority()).methodName("@").callSafely();
                if (response != null) {
                    IBinder binder = BundleCompat.getBinder(response, "_VBOX_|_binder_");
                    linkBinderDied(binder);
                    sFetcher = IServiceFetcher.Stub.asInterface(binder);
                }
            }
        }
        return sFetcher;
    }

    public static void ensureServerStarted() {
        new ProviderCall.Builder(VCore.get().getContext(), getAuthority()).methodName("ensure_created").callSafely();
    }

    public static void clearServerFetcher() {
        sFetcher = null;
    }

    private static void linkBinderDied(final IBinder binder) {
        IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                binder.unlinkToDeath(this, 0);
            }
        };
        try {
            binder.linkToDeath(deathRecipient, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static IBinder getService(String name) {
        if (VCore.get().isServerProcess()) {
            return ServiceCache.getService(name);
        }
        IServiceFetcher fetcher = getServiceFetcher();
        if (fetcher != null) {
            try {
                return fetcher.getService(name);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        VLog.e(TAG, "GetService(%s) return null.", name);
        return null;
    }

    public static void addService(String name, IBinder service) {
        IServiceFetcher fetcher = getServiceFetcher();
        if (fetcher != null) {
            try {
                fetcher.addService(name, service);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    public static void removeService(String name) {
        IServiceFetcher fetcher = getServiceFetcher();
        if (fetcher != null) {
            try {
                fetcher.removeService(name);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
