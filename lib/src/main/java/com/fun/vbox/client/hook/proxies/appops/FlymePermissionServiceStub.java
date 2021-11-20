package com.fun.vbox.client.hook.proxies.appops;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.oem.IFlymePermissionService;

/**
 * @author kenan
 */
public class FlymePermissionServiceStub extends BinderInvocationProxy {
    public FlymePermissionServiceStub() {
        super(IFlymePermissionService.Stub.TYPE, "flyme_permission");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("noteIntentOperation"));
    }
}
