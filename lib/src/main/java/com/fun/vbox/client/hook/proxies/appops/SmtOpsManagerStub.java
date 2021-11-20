package com.fun.vbox.client.hook.proxies.appops;

import android.annotation.TargetApi;
import android.os.Build;

import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;

import mirror.com.android.internal.app.ISmtOpsService;

/**
 *
 * <p>
 * Fuck the AppOpsService.
 * @see android.app.AppOpsManager
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
@Inject(MethodProxies.class)
public class SmtOpsManagerStub extends BinderInvocationProxy {

    public SmtOpsManagerStub() {
        super(ISmtOpsService.Stub.asInterface, "smtops");
    }

}
