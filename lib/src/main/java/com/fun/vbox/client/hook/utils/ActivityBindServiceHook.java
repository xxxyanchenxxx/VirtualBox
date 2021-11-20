package com.fun.vbox.client.hook.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.fun.vbox.client.core.VCore;
import com.zb.vv.client.stub.KeepAliveService;
import com.fun.vbox.helper.utils.VLog;

import java.util.HashMap;

public class ActivityBindServiceHook {

    private static final String TAG = ActivityBindServiceHook.class.getSimpleName();

    private static final HashMap<String, Boolean> sIsBoundedMap = new HashMap<>();

    private static final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public static void hookOnCreate(Activity activity) {
        String activityName = activity.getClass().getCanonicalName();
        bindService(activityName);
    }

    public static void hookOnDestroy(Activity activity) {
        String activityName = activity.getClass().getCanonicalName();
        unBindService(activityName);
    }

    private static void bindService(String activityName) {
        if (sIsBoundedMap.get(activityName) != null && sIsBoundedMap.get(activityName)) {
            return;
        }
        Intent intent = new Intent(VCore.get().getContext(), KeepAliveService.class);
        boolean isBound =
                VCore.get().getContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        sIsBoundedMap.put(activityName, isBound);
    }

    private static void unBindService(String activityName) {
        if (sIsBoundedMap.get(activityName) != null && sIsBoundedMap.get(activityName)) {
            VCore.get().getContext().unbindService(conn);
            sIsBoundedMap.put(activityName, false);
        } else {
            VLog.e(TAG,
                    "  warning: service not bounded, but trying to unbound service !!!!!, activityName = " +
                            activityName);
        }
    }
}
