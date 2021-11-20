package com.fun.vbox.client.hook.proxies.dropbox;

import android.content.Context;
import android.os.DropBoxManager;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ResultStaticMethodProxy;

import mirror.com.android.internal.os.IDropBoxManagerService;

/**
 * @author Lody
 */
public class DropBoxManagerStub extends BinderInvocationProxy {
    public DropBoxManagerStub() {
        super(IDropBoxManagerService.Stub.asInterface, Context.DROPBOX_SERVICE);
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        DropBoxManager dm = (DropBoxManager) VCore.get().getContext().getSystemService(Context.DROPBOX_SERVICE);
        try {
            mirror.vbox.os.DropBoxManager.mService.set(dm, getInvocationStub().getProxyInterface());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("getNextEntry", null));
    }
}
