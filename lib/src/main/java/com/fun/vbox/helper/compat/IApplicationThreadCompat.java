package com.fun.vbox.helper.compat;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import mirror.vbox.app.IApplicationThread;
import mirror.vbox.app.IApplicationThreadICSMR1;
import mirror.vbox.app.IApplicationThreadKitkat;
import mirror.vbox.app.IApplicationThreadOreo;
import mirror.vbox.app.ServiceStartArgs;
import mirror.vbox.content.res.CompatibilityInfo;

/**
 * @author Lody
 */

public class IApplicationThreadCompat {

    public static void scheduleCreateService(IInterface appThread, IBinder token, ServiceInfo info) throws RemoteException {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            IApplicationThreadKitkat.scheduleCreateService.call(appThread, token, info, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO.get(),
                    0);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            IApplicationThreadICSMR1.scheduleCreateService.call(appThread, token, info, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO.get());
        } else {
            IApplicationThread.scheduleCreateService.call(appThread, token, info);
        }

    }

    public static void scheduleBindService(IInterface appThread, IBinder token, Intent intent, boolean rebind) throws RemoteException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            IApplicationThreadKitkat.scheduleBindService.call(appThread, token, intent, rebind, 0);
        } else {
            IApplicationThread.scheduleBindService.call(appThread, token, intent, rebind);
        }
    }

    public static void scheduleUnbindService(IInterface appThread, IBinder token, Intent intent) throws RemoteException {
        IApplicationThread.scheduleUnbindService.call(appThread, token, intent);
    }

    public static void scheduleServiceArgs(IInterface appThread, IBinder token,
                                           int startId, Intent args) throws RemoteException {

        if (BuildCompat.isOreo()) {
            List<Object> list = new ArrayList<>(1);
            Object serviceStartArg = ServiceStartArgs.ctor.newInstance(false, startId, 0, args);
            list.add(serviceStartArg);
            IApplicationThreadOreo.scheduleServiceArgs.call(appThread, token, ParceledListSliceCompat.create(list));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            IApplicationThreadICSMR1.scheduleServiceArgs.call(appThread, token, false, startId, 0, args);
        } else {
            IApplicationThread.scheduleServiceArgs.call(appThread, token, startId, 0, args);
        }
    }


    public static void scheduleStopService(IInterface appThread, IBinder token) throws RemoteException {
        IApplicationThread.scheduleStopService.call(appThread, token);
    }

}
