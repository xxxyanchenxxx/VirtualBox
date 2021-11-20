package com.ft.mapp.delegate;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.core.VirtualEngineCallback;

public class VirtualEngineDelegate extends VirtualEngineCallback.EmptyDelegate {

    private static final String TAG = "VirtualEngineDelegate";

    @Override public void onProcessDied(String pkg, String processName) {
        super.onProcessDied(pkg, processName);
        if ("com.tencent.mm:appbrand1".equals(processName)) {
            VCore.get().killAllApps();
        }
    }
}