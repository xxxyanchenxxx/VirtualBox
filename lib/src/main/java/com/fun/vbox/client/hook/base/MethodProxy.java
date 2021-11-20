package com.fun.vbox.client.hook.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.fun.vbox.client.VClient;
import com.fun.vbox.client.core.SettingConfig;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.annotations.LogInvocation;
import com.fun.vbox.client.ipc.VirtualLocationManager;
import com.fun.vbox.helper.utils.ComponentUtils;
import com.fun.vbox.os.VUserHandle;
import com.fun.vbox.remote.VDeviceConfig;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
public abstract class MethodProxy {

    private boolean enable = true;
    private LogInvocation.Condition mInvocationLoggingCondition = LogInvocation.Condition.NEVER; // Inherit

    public MethodProxy() {
        LogInvocation loggingAnnotation = getClass().getAnnotation(LogInvocation.class);
        if (loggingAnnotation != null) {
            this.mInvocationLoggingCondition = loggingAnnotation.value();
        }
    }

    public static String getHostPkg() {
        return VCore.get().getHostPkg();
    }

    public static String getAppPkg() {
        return VClient.get().getCurrentPackage();
    }

    protected static Context getHostContext() {
        return VCore.get().getContext();
    }

    protected static boolean isAppProcess() {
        return VCore.get().isVAppProcess();
    }

    protected static boolean isServerProcess() {
        return VCore.get().isServerProcess();
    }

    protected static boolean isMainProcess() {
        return VCore.get().isMainProcess();
    }

    protected static int getVUid() {
        return VClient.get().getVUid();
    }

    public static int getAppUserId() {
        return VUserHandle.getUserId(getVUid());
    }

    protected static int getBaseVUid() {
        return VClient.get().getBaseVUid();
    }

    protected static int getRealUid() {
        return VCore.get().myUid();
    }

    protected static SettingConfig getConfig() {
        return VCore.getConfig();
    }

    protected static VDeviceConfig getDeviceConfig() {
        return VClient.get().getDeviceConfig();
    }

    protected static boolean isFakeLocationEnable() {
        return VirtualLocationManager.get().getMode(VUserHandle.myUserId(), VClient.get().getCurrentPackage()) != 0;
    }

    public static boolean isVisiblePackage(ApplicationInfo info) {
        return getHostPkg().equals(info.packageName)
                || ComponentUtils.isSystemApp(info)
                || VCore.get().isOutsidePackageVisible(info.packageName);
    }

    public static boolean isVisiblePackage(String packageName) {
        try {
            ApplicationInfo info = VCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0);
            return isVisiblePackage(info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getRealUserId() {
        return VUserHandle.realUserId();
    }

    public static void replaceFirstUserId(Object[] objArr) {
        if (getRealUserId() != 0) {
            for (int i = 0; i < objArr.length; i++) {
                if (objArr[i] == Integer.valueOf(0)) {
                    objArr[i] = getRealUserId();
                    return;
                }
            }
        }
    }

    public static void replaceLastUserId(Object[] objArr) {
        if (getRealUserId() != 0) {
            int i = -1;
            for (int i2 = 0; i2 < objArr.length; i2++) {
                if (objArr[i2] == Integer.valueOf(0)) {
                    i = i2;
                }
            }
            if (i >= 0) {
                objArr[i] = getRealUserId();
            }
        }
    }

    public static boolean isHostIntent(Intent intent) {
        ComponentName component = intent.getComponent();
        if (component != null) {
            String pkg = component.getPackageName();
            SettingConfig config = VCore.getConfig();
            return pkg.equals(config.getHostPackageName())
                    || pkg.equals(config.get64bitEnginePackageName())
                    || config.isHostIntent(intent);
        }
        return false;
    }

    public abstract String getMethodName();

    public boolean beforeCall(Object who, Method method, Object... args) {
        return true;
    }

    public Object call(Object who, Method method, Object... args) throws Throwable {
        return method.invoke(who, args);
    }

    public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        return result;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public LogInvocation.Condition getInvocationLoggingCondition() {
        return mInvocationLoggingCondition;
    }

    public void setInvocationloggingCondition(LogInvocation.Condition invocationLoggingCondition) {
        mInvocationLoggingCondition = invocationLoggingCondition;
    }

    public boolean isAppPkg(String pkg) {
        return VCore.get().isAppInstalled(pkg);
    }

    protected PackageManager getPM() {
        return VCore.getPM();
    }

    @Override
    public String toString() {
        return "Method : " + getMethodName();
    }
}
