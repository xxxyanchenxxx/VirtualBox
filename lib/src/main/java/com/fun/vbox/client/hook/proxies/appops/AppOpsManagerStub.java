package com.fun.vbox.client.hook.proxies.appops;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.fun.vbox.helper.compat.BuildCompat;

import mirror.com.android.internal.app.IAppOpsService;

/**
 *
 * <p>
 * Fuck the AppOpsService.
 * @see android.app.AppOpsManager
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
@Inject(MethodProxies.class)
public class AppOpsManagerStub extends BinderInvocationProxy {

    public AppOpsManagerStub() {
        super(IAppOpsService.Stub.asInterface, Context.APP_OPS_SERVICE);
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        if (mirror.vbox.app.AppOpsManager.mService != null) {
            AppOpsManager appOpsManager = (AppOpsManager) VCore.get().getContext().getSystemService(Context.APP_OPS_SERVICE);
            try {
                mirror.vbox.app.AppOpsManager.mService.set(appOpsManager, getInvocationStub().getProxyInterface());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();

        if (BuildCompat.isR()) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("reportRuntimeAppOpAccessMessageAndGetConfig"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("startWatchingAsyncNoted"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("extractAsyncOps"));
        }
    }

}
