package com.zb.vv.client.stub;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.fun.vbox.helper.utils.VLog;

/**
 * This is used to protect server process from being killed by system in low memory phone ,such as hongmi 4A, oppo A71,
 * Bind Forground activity to this service can promote its priority,
 * So it is not easily killed by system.
 */
public class DaemonBindActivityService extends Service {

    private static final String TAG = DaemonBindActivityService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        VLog.e(TAG, " onBind ");
        return new Binder();
    }

    @Override
    public void onCreate() {
        VLog.e(TAG, " onCreate ");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        VLog.e(TAG, " onDestroy ");
        super.onDestroy();
    }
}
