package com.fun.vbox.client.hook.proxies.mount;

import android.os.IInterface;

import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.helper.compat.BuildCompat;

import mirror.RefStaticMethod;
import mirror.vbox.os.mount.IMountService;
import mirror.vbox.os.storage.IStorageManager;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class MountServiceStub extends BinderInvocationProxy {

    public MountServiceStub() {
        super(getInterfaceMethod(), "mount");
    }

    private static RefStaticMethod<IInterface> getInterfaceMethod() {
        if (BuildCompat.isOreo()) {
            return IStorageManager.Stub.asInterface;
        } else {
            return IMountService.Stub.asInterface;
        }
    }
}
