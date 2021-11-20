package com.zb.vv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.xyz.vbox64.BuildConfig;

public class SplashActivity extends Activity {

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = queryIntentActivity(this, BuildConfig.PACKAGE_NAME_32BIT);
        if (intent == null) {
            return;
        }
        intent.addFlags(268435456);
        startActivity(intent);
        AppIconChangeMgr.getInstance().hideAppLockIcon();
        finish();
    }

    private boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    private Intent queryIntentActivity(Context context, String packageName) {
        if (isAppInstalled(context, packageName)) {
            return context.getPackageManager().getLaunchIntentForPackage(packageName);
        }
        return null;
    }

}
