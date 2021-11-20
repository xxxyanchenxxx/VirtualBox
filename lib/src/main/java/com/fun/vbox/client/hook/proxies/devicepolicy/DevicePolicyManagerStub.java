package com.fun.vbox.client.hook.proxies.devicepolicy;

import android.content.ComponentName;
import android.content.Context;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.MethodProxy;

import java.lang.reflect.Method;

import mirror.vbox.app.admin.IDevicePolicyManager;

/**
 * Created by wy on 2017/10/20.
 */

public class DevicePolicyManagerStub extends BinderInvocationProxy {
    public DevicePolicyManagerStub() {
        super(IDevicePolicyManager.Stub.asInterface, Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new GetStorageEncryptionStatus());
        addMethodProxy(new GetDeviceOwnerComponent());
    }

    private static class GetDeviceOwnerComponent extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getDeviceOwnerComponent";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return new ComponentName(getAppPkg(), "");
        }
    }

    private static class GetStorageEncryptionStatus extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getStorageEncryptionStatus";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            args[0] = VCore.get().getHostPkg();
            return method.invoke(who, args);
        }
    }
}
