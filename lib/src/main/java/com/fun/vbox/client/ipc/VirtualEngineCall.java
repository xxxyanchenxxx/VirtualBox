package com.fun.vbox.client.ipc;

import android.os.Bundle;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.utils.VLog;

public class VirtualEngineCall {
    private static final String TAG = "VirtualEngineCall";

    private static String getAuthority() {
        return VCore.getConfig().getBinderProviderAuthority();
    }

    public static Bundle invoke(Bundle param) {
        try {
            return ProviderCall.callSafely(
                    getAuthority(),
                    "_VBOX_|_invoke_",
                    null,
                    param
            );
        } catch (Throwable e) {
            VLog.e(TAG, e);
        }
        return null;
    }
}
