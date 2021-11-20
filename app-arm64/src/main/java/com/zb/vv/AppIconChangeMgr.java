package com.zb.vv;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppIconChangeMgr {
    private static final String ALIAS_64BIT_PLUGIN = ".vbox64";
    private static final String TAG = "AppIconChangeMgr";
    private static volatile AppIconChangeMgr instance = null;
    ArrayList<ComponentName> mComponentNames = new ArrayList();
    private PackageManager mPkgMgr;

    public static AppIconChangeMgr getInstance() {
        if (instance == null) {
            synchronized (AppIconChangeMgr.class) {
                if (instance == null) {
                    instance = new AppIconChangeMgr();
                }
            }
        }
        return instance;
    }

    AppIconChangeMgr() {
        initComponent();
    }

    private void initComponent() {
        this.mPkgMgr = App.getApp().getApplicationContext().getPackageManager();
        String pkg = App.getApp().getPackageName();
        this.mComponentNames.add(new ComponentName(pkg, pkg + ALIAS_64BIT_PLUGIN));
    }

    @SuppressLint("WrongConstant")
    public void enableComponent(ComponentName componentName) {
        Log.d(TAG, "enableComponent =" + componentName.getClassName());
        this.mPkgMgr.setComponentEnabledSetting(componentName, 1, 1);
    }

    @SuppressLint("WrongConstant")
    public void disableComponent(ComponentName componentName) {
        Log.d(TAG, "disableComponent =" + componentName.getClassName());
        this.mPkgMgr.setComponentEnabledSetting(componentName, 2, 1);
    }

    @SuppressLint("WrongConstant")
    public void restartSystemLauncher() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        for (ResolveInfo res : this.mPkgMgr.queryIntentActivities(intent, 0)) {
            if (res.activityInfo != null) {
                ActivityManager am = (ActivityManager) App.getApp().getSystemService("activity");
                if (am != null) {
                    am.killBackgroundProcesses(res.activityInfo.packageName);
                }
            }
        }
    }

    public List<ComponentName> getComponentNames() {
        return this.mComponentNames;
    }

    public void hideAppLockIcon() {
        Iterator it = this.mComponentNames.iterator();
        while (it.hasNext()) {
            disableComponent((ComponentName) it.next());
        }
        restartSystemLauncher();
    }
}