package com.fun.vbox.client.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.fun.vbox.helper.Keep;

@Keep
public interface AppCallback {

    @Keep
    AppCallback EMPTY = new AppCallback() {

        @Override
        public void beforeStartApplication(String packageName, String processName, Context context) {
            // Empty
        }

        @Override
        public void beforeApplicationCreate(String packageName, String processName, Application application) {
            // Empty
        }

        @Override
        public void afterApplicationCreate(String packageName, String processName, Application application) {
            // Empty
        }

        @Override
        public void beforeActivityCreate(Activity activity) {

        }

        @Override
        public void beforeActivityResume(Activity activity) {

        }

        @Override
        public void beforeActivityPause(Activity activity) {

        }

        @Override
        public void beforeActivityDestroy(Activity activity) {

        }

        @Override
        public void afterActivityCreate(Activity activity) {

        }

        @Override
        public void afterActivityResume(Activity activity) {

        }

        @Override
        public void afterActivityPause(Activity activity) {

        }

        @Override
        public void afterActivityDestroy(Activity activity) {

        }

        @Override
        public Bundle invokeFromAnyWhere(Bundle param) {
            return null;
        }
    };

    void beforeStartApplication(String packageName, String processName, Context context);

    void beforeApplicationCreate(String packageName, String processName, Application application);

    void afterApplicationCreate(String packageName, String processName, Application application);

    void beforeActivityCreate(Activity activity);

    void beforeActivityResume(Activity activity);

    void beforeActivityPause(Activity activity);

    void beforeActivityDestroy(Activity activity);

    void afterActivityCreate(Activity activity);

    void afterActivityResume(Activity activity);

    void afterActivityPause(Activity activity);

    void afterActivityDestroy(Activity activity);

    Bundle invokeFromAnyWhere(Bundle param);

}
