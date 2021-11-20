package com.zb.vv.client.stub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zb.vv.BuildConfig;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.helper.compat.IntentCompat;
import com.fun.vbox.helper.utils.ComponentUtils;
import com.fun.vbox.remote.IntentSenderData;
import com.fun.vbox.remote.IntentSenderExtData;

/**
 * @author Lody
 */

public class ShadowPendingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent selector = intent.getSelector();
        if (selector == null) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("selector = null");
            }
            return;
        }
        selector.setExtrasClassLoader(IntentSenderExtData.class.getClassLoader());
        Intent finalIntent = selector.getParcelableExtra("_VBOX_|_intent_");
        int userId = selector.getIntExtra("_VBOX_|_userId_", -1);
        if (finalIntent == null || userId == -1) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("targetIntent = null");
            }
            return;
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
        Intent redirectIntent = ComponentUtils.redirectBroadcastIntent(finalIntent, userId);
        context.sendBroadcast(redirectIntent);
    }
}
