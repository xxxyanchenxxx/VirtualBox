package com.fun.vbox.helper.utils;

import android.os.Build;
import android.util.Log;

import com.fun.vbox.helper.compat.BuildCompat;
import com.fun.vbox.helper.compat.SystemPropertiesCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {
    static Method addWhiteListMethod;
    public static Method forNameMethod;
    public static Method getMethodMethod;
    static Object vmRuntime;
    static Class vmRuntimeClass;

    static {
        try {
            ReflectionUtils.getMethodMethod =
                    Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            Class[] v3 = new Class[]{String.class};
            ReflectionUtils.forNameMethod = Class.class.getDeclaredMethod("forName", v3);
            ReflectionUtils.vmRuntimeClass = (Class) Class.class.getDeclaredMethod("forName", v3)
                    .invoke(null, "dalvik.system.VMRuntime");
            Method v0_1 = ReflectionUtils.getMethodMethod;
            Class v1 = ReflectionUtils.vmRuntimeClass;
            Object[] v4 = new Object[]{"setHiddenApiExemptions", null};
            v4[1] = new Class[]{String[].class};
            ReflectionUtils.addWhiteListMethod = (Method) v0_1.invoke(v1, v4);
            ReflectionUtils.vmRuntime = ((Method) ReflectionUtils.getMethodMethod
                    .invoke(ReflectionUtils.vmRuntimeClass, "getRuntime", null)).invoke(null);
        } catch (Exception v0) {
            Log.e("ReflectionUtils", "error get methods", v0);
        }
    }

    public static void addReflectionWhiteList(String[] arg4)
            throws InvocationTargetException, IllegalAccessException {
        ReflectionUtils.addWhiteListMethod.invoke(ReflectionUtils.vmRuntime, (Object) arg4);
    }

    public static int getPreviewSDKInt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Build.VERSION.PREVIEW_SDK_INT;
            } catch (Throwable e) {
                // ignore
            }
        }
        return 0;
    }

    public static boolean isR() {
        return Build.VERSION.SDK_INT > 29 || (Build.VERSION.SDK_INT == 29 && getPreviewSDKInt() > 0);
    }

    public static boolean isEMUI() {
        if (Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
            return true;
        }
        String property = SystemPropertiesCompat.get("ro.build.version.emui");
        return property != null && property.contains("EmotionUI");
    }

    public static int passApiCheck() {
        if (!BuildCompat.isR()) {
            return 1;
        }
        try {
            if (BuildCompat.isEMUI()) {
                ReflectionUtils.addReflectionWhiteList(
                        new String[]{"Landroid/", "Lcom/android/", "Ljava/lang/", "Ldalvik/system/",
                                "Llibcore/", "Lhuawei/", "Lsun/security/"});
            } else {
                ReflectionUtils.addReflectionWhiteList(
                        new String[]{"Landroid/", "Lcom/android/", "Ljava/lang/", "Ldalvik/system/",
                                "Llibcore/", "Lsun/security/"});
            }
            return 0;
        } catch (Throwable v0) {
            v0.printStackTrace();
            return 0;
        }
     }
}