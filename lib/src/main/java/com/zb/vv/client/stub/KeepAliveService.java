package com.zb.vv.client.stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.fun.vbox.client.core.VCore;

public class KeepAliveService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!VCore.getConfig().isHideForegroundNotification()) {
            HiddenForeNotification.bindForeground(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
