package com.fun.vbox.client.core;

import android.content.ComponentName;
import android.os.Bundle;

import com.fun.vbox.helper.Keep;

@Keep
public interface VirtualEngineCallback {
    VirtualEngineCallback EMPTY = new EmptyDelegate();

    @Keep
    class EmptyDelegate implements VirtualEngineCallback {

        @Override
        public Bundle invokeFromAnyWhere(Bundle param) {
            return null;
        }

        @Override public void onProcessDied(String pkg, String processName) {

        }

        @Override public void onProcessCreate(String pkg, String processName) {

        }

        @Override public void onActivityCreated(ComponentName componentName) {

        }

        @Override public void onActivityDestroyed(ComponentName componentName) {

        }
    }

    Bundle invokeFromAnyWhere(Bundle param);
    void onProcessDied(String pkg, String processName);
    void onProcessCreate(String pkg, String processName);
    void onActivityCreated(ComponentName componentName);
    void onActivityDestroyed(ComponentName componentName);
}
