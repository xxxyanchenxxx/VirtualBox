package com.zb.vv.client.stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.zb.vv.BuildConfig;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.helper.compat.IntentCompat;
import com.fun.vbox.remote.IntentSenderData;
import com.fun.vbox.remote.IntentSenderExtData;


/**
 * @author Lody
 */

public class ShadowPendingService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent selector = intent.getSelector();
        if (selector == null) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("selector = null");
            }
            return START_NOT_STICKY;
        }
        selector.setExtrasClassLoader(IntentSenderExtData.class.getClassLoader());
        Intent finalIntent = selector.getParcelableExtra("_VBOX_|_intent_");
        int userId = selector.getIntExtra("_VBOX_|_userId_", -1);
        if (finalIntent == null || userId == -1) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("targetIntent = null");
            }
            return START_NOT_STICKY;
        }
        IntentSenderExtData ext = intent.getParcelableExtra("_VBOX_|_ext_");
        if (ext != null && ext.sender != null) {
            IntentSenderData data = VActivityManager.get().getIntentSender(ext.sender);
            Intent fillIn = ext.fillIn;
            if (fillIn != null) {
                finalIntent.fillIn(fillIn, data.flags);
            }
            int flagsMask = ext.flagsMask;
            int flagsValues = ext.flagsValues;
            flagsMask &= ~IntentCompat.IMMUTABLE_FLAGS;
            flagsValues &= flagsMask;
            finalIntent.setFlags((finalIntent.getFlags() & ~flagsMask) | flagsValues);
        }
        VActivityManager.get().startService(finalIntent, null, userId);
        stopSelf();
        return START_NOT_STICKY;
    }
}
