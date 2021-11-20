package com.fun.vbox.client.hook.proxies.appops;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.os.Build;

import com.fun.vbox.GmsSupport;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.Keep;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
@Keep
public class MethodProxies {

    private static void replaceUidAndPackage(Object[] args, int pkgIndex) {
        args[pkgIndex] = VCore.get().getHostPkg();
        int uidIndex = pkgIndex - 1;
        if (args[pkgIndex - 1] instanceof Integer) {
            args[uidIndex] = VCore.get().myUid();
        }
    }

    public static Object checkAudioOperation(Object who, Method method, Object[] args) throws Throwable {
        replaceUidAndPackage(args, 3);
        return method.invoke(who, args);
    }

    public static Object checkOperation(Object who, Method method, Object[] args) throws Throwable {
        replaceUidAndPackage(args, 2);
        return method.invoke(who, args);
    }

    public static Object checkPackage(Object who, Method method, Object[] args) throws Throwable {
        String pkg = (String) args[1];
        if (GmsSupport.isGoogleAppOrService(pkg)) {
            return AppOpsManager.MODE_ALLOWED;
        }
        replaceUidAndPackage(args, 1);
        return method.invoke(who, args);
    }


    public static Object getOpsForPackage(Object who, Method method, Object[] args) throws Throwable {
        replaceUidAndPackage(args, 1);
        return method.invoke(who, args);
    }

    public static Object getPackagesForOps(Object who, Method method, Object[] args) throws Throwable {
        return method.invoke(who, args);
    }

    public static Object noteOperation(Object who, Method method, Object[] args) throws Throwable {
        replaceUidAndPackage(args, 2);
        return method.invoke(who, args);
    }


    public static Object noteProxyOperation(Object who, Method method, Object[] args) throws Throwable {
        return 0;
    }

    public static Object resetAllModes(Object who, Method method, Object[] args) throws Throwable {
        // force userId to 0
        args[0] = 0;
        args[1] = VCore.get().getHostPkg();
        return method.invoke(who, args);
    }

    public static Object startOperation(Object who, Method method, Object[] args) throws Throwable {
        replaceUidAndPackage(args, 3);
        return method.invoke(who, args);
    }

    public static Object finishOperation(Object who, Method method, Object[] args) throws Throwable {
        replaceUidAndPackage(args, 3);
        return method.invoke(who, args);
    }

}
