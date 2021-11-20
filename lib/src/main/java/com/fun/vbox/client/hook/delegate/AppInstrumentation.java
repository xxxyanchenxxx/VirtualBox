package com.fun.vbox.client.hook.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import com.fun.vbox.client.core.InvocationStubManager;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.fixer.ActivityFixer;
import com.fun.vbox.client.fixer.ContextFixer;
import com.fun.vbox.client.hook.proxies.am.HCallbackStub;
import com.fun.vbox.client.hook.utils.ActivityBindServiceHook;
import com.fun.vbox.client.interfaces.IInjector;
import com.fun.vbox.helper.compat.ActivityManagerCompat;

import java.lang.reflect.Field;

import mirror.vbox.app.ActivityThread;

/**
 * @author Lody
 */
public final class AppInstrumentation extends InstrumentationDelegate implements IInjector {

    private static final String TAG = AppInstrumentation.class.getSimpleName();

    private static AppInstrumentation gDefault;

    private AppInstrumentation(Instrumentation base) {
        super(base);
    }

    public static AppInstrumentation getDefault() {
        if (gDefault == null) {
            synchronized (AppInstrumentation.class) {
                if (gDefault == null) {
                    gDefault = create();
                }
            }
        }
        return gDefault;
    }

    private static AppInstrumentation create() {
        Instrumentation instrumentation = ActivityThread.mInstrumentation.get(VCore.mainThread());
        if (instrumentation instanceof AppInstrumentation) {
            return (AppInstrumentation) instrumentation;
        }
        return new AppInstrumentation(instrumentation);
    }


    @Override
    public void inject() {
        base = ActivityThread.mInstrumentation.get(VCore.mainThread());
        ActivityThread.mInstrumentation.set(VCore.mainThread(), this);
    }

    @Override
    public boolean isEnvBad() {
        return !(ActivityThread.mInstrumentation.get(VCore.mainThread()) instanceof AppInstrumentation);
        //return !checkInstrumentation(ActivityThread.mInstrumentation.get(VCore.mainThread()));
    }

    private boolean checkInstrumentation(Instrumentation instrumentation) {
        if (instrumentation instanceof AppInstrumentation) {
            return true;
        }
        Class<?> clazz = instrumentation.getClass();
        if (Instrumentation.class.equals(clazz)) {
            return false;
        }
        do {
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (Instrumentation.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object obj;
                        try {
                            obj = field.get(instrumentation);
                        } catch (IllegalAccessException e) {
                            return false;
                        }
                        if ((obj instanceof AppInstrumentation)) {
                            return true;
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Instrumentation.class.equals(clazz));
        return false;
    }

    private void checkActivityCallback() {
        InvocationStubManager.getInstance().checkEnv(HCallbackStub.class);
        InvocationStubManager.getInstance().checkEnv(AppInstrumentation.class);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        VCore.get().getAppCallback().beforeActivityCreate(activity);
        checkActivityCallback();
        ContextFixer.fixContext(activity);
        ActivityFixer.fixActivity(activity);
        ActivityInfo info = mirror.vbox.app.Activity.mActivityInfo.get(activity);
        if (info != null) {
            if (info.theme != 0) {
                activity.setTheme(info.theme);
            }
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    && info.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                if (activity.getRequestedOrientation() != info.screenOrientation) {
                    ActivityManagerCompat.setActivityOrientation(activity, info.screenOrientation);
                    boolean needWait;
                    //set orientation
                    Configuration configuration = activity.getResources().getConfiguration();
                    if (isOrientationLandscape(info.screenOrientation)) {
                        needWait = configuration.orientation != Configuration.ORIENTATION_LANDSCAPE;
                        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
                    } else {
                        needWait = configuration.orientation != Configuration.ORIENTATION_PORTRAIT;
                        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
                    }
                    if (needWait) {
                        try {
                            Thread.sleep(800);
                        } catch (Exception e) {
                            //ignore
                        }
                    }
                }
            }
        }
        super.callActivityOnCreate(activity, icicle);
        ActivityBindServiceHook.hookOnCreate(activity);
        VCore.get().getAppCallback().afterActivityCreate(activity);
    }

    private boolean isOrientationLandscape(int requestedOrientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    || (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                    || (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                    || (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        } else {
            return (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    || (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                    || (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        VCore.get().getAppCallback().beforeActivityDestroy(activity);
        super.callActivityOnDestroy(activity);
        VCore.get().getAppCallback().afterActivityDestroy(activity);
    }

    @Override
    public void callActivityOnPause(Activity activity) {
        VCore.get().getAppCallback().beforeActivityPause(activity);
        super.callActivityOnPause(activity);
        VCore.get().getAppCallback().afterActivityPause(activity);
    }


    @Override
    public void callApplicationOnCreate(Application app) {
        checkActivityCallback();
        super.callApplicationOnCreate(app);
    }
}
