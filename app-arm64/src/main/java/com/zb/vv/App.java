package com.zb.vv;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.WebView;

import com.fun.vbox.client.core.HostApp;
import com.fun.vbox.client.core.SettingConfig;
import com.fun.vbox.client.core.VCore;
import com.zb.vv.delegate.MyComponentDelegate;
import com.zb.vv.delegate.MyTaskDescriptionDelegate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.weishu.reflection.Reflection;
import com.xyz.vbox64.BuildConfig;

public class App extends Application {

    private static App gApp;
    private SharedPreferences mPreferences;

    private SettingConfig mConfig = new SettingConfig() {
        @Override
        public String getHostPackageName() {
            return BuildConfig.PACKAGE_NAME_32BIT;
        }

        @Override
        public String get64bitEnginePackageName() {
            return BuildConfig.APPLICATION_ID;
        }

        @Override
        public String get64bitEngineLaunchActivityName() {
            return "com.zb.vv.EmptyActivity";
        }

        @Override
        public String getShortcutProxyActivityName() {
            return "com.fun.vapp.open.ShortcutHandleActivity";
        }

        @Override
        public String getShortcutProxyActionName() {
            return BuildConfig.PACKAGE_NAME_32BIT + ".vbox.action.shortcut";
        }

        @Override
        public boolean isEnableIORedirect() {
            return true;
        }

        @Override
        public Intent onHandleLauncherIntent(Intent originIntent) {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.PACKAGE_NAME_32BIT, "com.fun.vapp.home" +
                    ".BackHomeActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }

        @Override
        public boolean isUseRealDataDir(String packageName) {
            if ("com.eg.android.AlipayGphone".equals(packageName)) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public AppLibConfig getAppLibConfig(String packageName) {
            return AppLibConfig.UseRealLib;
        }

        @Override
        public boolean isAllowCreateShortcut() {
            return false;
        }

        /**
         * 如果返回[true], 则认为将要启动的Activity在宿主之中。
         * @param intent
         * @return
         */
        @Override
        public boolean isHostIntent(Intent intent) {
            return intent.getData() != null && "market".equals(intent.getData().getScheme());
        }
    };

    public static App getApp() {
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
        if (Build.VERSION.SDK_INT >= 28) {
            closeAndroidPDialog();
        }
    }

    @Override
    public void onCreate() {
        gApp = this;
        super.onCreate();

        HostApp.setApplication(this);

        init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = VCore.get().getProcessName();
            if (!getPackageName().equals(processName)) {
                int nameIndex = processName.indexOf(":");
                String name = processName.substring(nameIndex + 1);
                WebView.setDataDirectorySuffix(name + "_vbox");
            }
        }
    }

    private void init() {
        final VCore virtualCore = VCore.get();
        virtualCore.initialize(new VCore.VirtualInitializer() {

            @Override
            public void onMainProcess() {

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

            }
        });
    }

    public static SharedPreferences getPreferences() {
        return getApp().mPreferences;
    }

    private void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
