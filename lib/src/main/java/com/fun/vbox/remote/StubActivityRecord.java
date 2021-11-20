package com.fun.vbox.remote;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import com.fun.vbox.helper.compat.BundleCompat;

/**
 * @author Lody
 */

public class StubActivityRecord {
    public Intent intent;
    public ActivityInfo info;
    public int userId;
    public IBinder virtualToken;

    public StubActivityRecord(Intent intent, ActivityInfo info, int userId, IBinder virtualToken) {
        this.intent = intent;
        this.info = info;
        this.userId = userId;
        this.virtualToken = virtualToken;
    }

    public StubActivityRecord(Intent stub) {
        this.intent = stub.getParcelableExtra("_VBOX_|_intent_");
        this.info = stub.getParcelableExtra("_VBOX_|_info_");
        this.userId = stub.getIntExtra("_VBOX_|_user_id_", 0);
        this.virtualToken = BundleCompat.getBinder(stub, "_VBOX_|_token_");

    }

    public void saveToIntent(Intent stub) {
        stub.putExtra("_VBOX_|_intent_", intent);
        stub.putExtra("_VBOX_|_info_", info);
        stub.putExtra("_VBOX_|_user_id_", userId);
        BundleCompat.putBinder(stub, "_VBOX_|_token_", virtualToken);
    }
}
