package com.ft.mapp.delegate;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

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

    public static int getScreenWidth(Context context) {
        return getScreenSize(context, null).x;
    }

    public static int getScreenHeight(Context context) {
        return getScreenSize(context, null).y;
    }

    public static Point getScreenSize(Context context, Point outSize) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point ret = outSize == null ? new Point() : outSize;
        final Display defaultDisplay = wm.getDefaultDisplay();
        defaultDisplay.getSize(ret);
        return ret;
    }
}
