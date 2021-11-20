package com.fun.vbox.client.hook.proxies.os;

import android.annotation.TargetApi;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.vbox.os.IDeviceIdentifiersPolicyService;

@TargetApi(29)
public class DeviceIdentifiersPolicyServiceHub extends BinderInvocationProxy {
    public DeviceIdentifiersPolicyServiceHub() {
        super(IDeviceIdentifiersPolicyService.Stub.asInterface, "device_identifiers");
    }

    @Override protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getSerialForPackage"));
    }
}