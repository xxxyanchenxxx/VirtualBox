package com.fun.vbox.client.hook.proxies.connectivity;

import android.content.Context;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceLastPkgMethodProxy;
import com.fun.vbox.client.hook.base.ResultStaticMethodProxy;
import com.fun.vbox.helper.compat.BuildCompat;

import mirror.vbox.net.IConnectivityManager;

/**
 * @author legency
 * @see android.net.ConnectivityManager
 */
public class ConnectivityStub extends BinderInvocationProxy {

    public ConnectivityStub() {
        super(IConnectivityManager.Stub.asInterface, Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("isTetheringSupported", true));
        if (BuildCompat.isR()) {
            addMethodProxy(new ReplaceLastPkgMethodProxy("getNetworkCapabilities"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("listenForNetwork"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("requestNetwork"));
        }
    }
}
