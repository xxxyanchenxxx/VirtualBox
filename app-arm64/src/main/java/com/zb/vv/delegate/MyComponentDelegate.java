package com.zb.vv.delegate;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.fun.vbox.client.hook.delegate.ComponentDelegate;

public class MyComponentDelegate implements ComponentDelegate {

    @Override
    public void beforeStartApplication(String packageName, String processName, Context context) {

    }

    @Override
    public void beforeApplicationCreate(String packageName, String processName,
                                        Application application) {

    }

    @Override
    public void afterApplicationCreate(String packageName, String processName,
                                       Application application) {
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
    public Bundle invokeFromAnyWhere(Bundle bundle) {
        return null;
    }
}
