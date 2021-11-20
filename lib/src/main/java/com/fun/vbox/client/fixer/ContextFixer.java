package com.fun.vbox.client.fixer;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import com.fun.vbox.client.core.InvocationStubManager;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.proxies.graphics.GraphicsStatsStub;

import mirror.vbox.app.ContextImpl;
import mirror.vbox.app.ContextImplKitkat;
import mirror.vbox.content.ContentResolverJBMR2;

/**
 * @author Lody
 */
public class ContextFixer {

    /**
     * Fuck AppOps
     *
     * @param context Context
     */
    public static void fixContext(Context context) {
        try {
            context.getPackageName();
        } catch (Throwable e) {
            return;
        }
        InvocationStubManager.getInstance().checkEnv(GraphicsStatsStub.class);
        int deep = 0;
        while (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
            deep++;
            if (deep >= 10) {
                return;
            }
        }
        ContextImpl.mPackageManager.set(context, null);
        try {
            context.getPackageManager();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (!VCore.get().isVAppProcess()) {
            return;
        }

        String hostPkg = VCore.get().getHostPkg();
        ContextImpl.mBasePackageName.set(context, hostPkg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ContextImplKitkat.mOpPackageName.set(context, hostPkg);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ContentResolverJBMR2.mPackageName.set(context.getContentResolver(), hostPkg);
        }
    }
}
