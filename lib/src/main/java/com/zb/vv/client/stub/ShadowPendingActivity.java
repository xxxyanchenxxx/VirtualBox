package com.zb.vv.client.stub;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.zb.vv.BuildConfig;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.helper.compat.IntentCompat;
import com.fun.vbox.remote.IntentSenderData;
import com.fun.vbox.remote.IntentSenderExtData;

/**
 * @author Lody
 */

public class ShadowPendingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        Intent intent = getIntent();
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
            ActivityInfo info = VCore.get().resolveActivityInfo(intent, data.userId);
            int res = VActivityManager.get().startActivity(finalIntent, info, ext.resultTo, ext.options, ext.resultWho, ext.requestCode, data.userId);
            if (res != 0 && ext.resultTo != null && ext.requestCode > 0) {
                VActivityManager.get().sendCancelActivityResult(ext.resultTo, ext.resultWho, ext.requestCode);
            }
        } else {
            VActivityManager.get().startActivity(finalIntent, userId);
        }
    }
}
