package com.fun.vbox.client.hook.proxies.permission;

import android.annotation.TargetApi;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.vbox.permission.IPermissionManager;

@TargetApi(30)
public final class PermissionManagerStub extends BinderInvocationProxy {
    public PermissionManagerStub() {
        super(IPermissionManager.Stub.asInterface, "permissionmgr");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("checkDeviceIdentifierAccess"));
    }
}