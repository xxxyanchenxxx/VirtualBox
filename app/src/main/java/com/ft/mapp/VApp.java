package com.ft.mapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import com.ft.mapp.delegate.MyAppRequestListener;
import com.ft.mapp.delegate.MyComponentDelegate;
import com.ft.mapp.delegate.MyTaskDescriptionDelegate;
import com.ft.mapp.delegate.VirtualEngineDelegate;
import com.ft.mapp.home.BackHomeActivity;
import com.ft.mapp.open.ShortcutHandleActivity;
import com.fun.vbox.client.core.HostApp;
import com.fun.vbox.client.core.SettingConfig;
import com.fun.vbox.client.core.VCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jonathanfinerty.once.Once;
import me.weishu.reflection.Reflection;

public class VApp extends Application {

    @SuppressLint("StaticFieldLeak")
    private static VApp gApp;
    private SharedPreferences mPreferences;

    private final SettingConfig mConfig = new SettingConfig() {
        @Override
        public String getHostPackageName() {
            return BuildConfig.APPLICATION_ID;
        }

        @Override
        public String get64bitEnginePackageName() {
            return BuildConfig.PACKAGE_NAME_ARM64;
        }

        @Override
        public String get64bitEngineLaunchActivityName() {
            return "com.zb.vv.EmptyActivity";
        }

        @Override
        public String getShortcutProxyActivityName() {
            return ShortcutHandleActivity.class.getName();
        }

        @Override
        public String getShortcutProxyActionName() {
            return BuildConfig.APPLICATION_ID + ".vbox.action.shortcut";
        }

        @Override
        public boolean isEnableIORedirect() {
            return true;
        }

        @Override
        public Intent onHandleLauncherIntent(Intent originIntent) {
            Intent intent = new Intent(VCore.get().getContext(), BackHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }

        @Override
        public boolean isUseRealDataDir(String packageName) {
            return false;
        }

        @Override
        public AppLibConfig getAppLibConfig(String packageName) {
            return AppLibConfig.UseRealLib;
        }

        @Override
        public boolean isAllowCreateShortcut() {
            return false;
        }

        @Override
        public boolean isHostIntent(Intent intent) {
            return intent.getData() != null && "market".equals(intent.getData().getScheme());
        }
    };

    public static VApp getApp() {
        return gApp;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        mPreferences = base.getSharedPreferences("va", Context.MODE_MULTI_PROCESS);

        if (Build.VERSION.SDK_INT < 30) {
            Reflection.unseal(base);
        }
        HostApp.setApplication(this);

        try {
            VCore.get().startup(base, mConfig);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            closeAndroidPDialog();
        }
    }

    @Override
    public void onCreate() {
        gApp = this;
        super.onCreate();
        HostApp.setApplication(this);
        BasicConfig.getInstance().setAppContext(this);

        String processName = VCore.get().getProcessName();
        if (!getPackageName().equals(processName)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                int nameIndex = processName.indexOf(":");
                String name = processName.substring(nameIndex + 1);
                WebView.setDataDirectorySuffix(name + "_ft");
            }
            return;
        }

        init();
    }

    private void init() {
        VCore virtualCore = VCore.get();
        virtualCore.initialize(new VCore.VirtualInitializer() {

            @Override
            public void onMainProcess() {
                Once.initialise(VApp.this);
            }

            @Override
            public void onVirtualProcess() {
                //listener components
                virtualCore.setAppCallback(new MyComponentDelegate());
                //fake task description's icon and title
                virtualCore.setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
            }

            @Override
            public void onServerProcess() {
                virtualCore.setAppRequestListener(new MyAppRequestListener(VApp.this));
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqq");
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqqi");
                virtualCore.addVisibleOutsidePackage("com.tencent.minihd.qq");
                virtualCore.addVisibleOutsidePackage("com.tencent.qqlite");
                //virtualCore.addVisibleOutsidePackage("com.facebook.katana");
                virtualCore.addVisibleOutsidePackage("com.whatsapp");
                virtualCore.addVisibleOutsidePackage("com.tencent.mm");
                virtualCore.addVisibleOutsidePackage("com.immomo.momo");

                virtualCore.setVirtualEngineCallback(new VirtualEngineDelegate());
                Bundle initBundle = virtualCore.getInitBundle();
                if (initBundle != null)  {
                    virtualCore.getVirtualEngineCallback().invokeFromAnyWhere(initBundle);
                }
            }
        });
    }

    public static SharedPreferences getPreferences() {
        return getApp().mPreferences;
    }

    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    private void closeAndroidPDialog() {
        try {
            Class<?> aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception ignored) {
            //
        }
        try {
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception ignored) {
            //
        }
    }
}
