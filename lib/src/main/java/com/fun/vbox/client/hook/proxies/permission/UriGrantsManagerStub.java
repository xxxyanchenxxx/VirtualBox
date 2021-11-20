package com.fun.vbox.client.hook.proxies.permission;

import android.os.IInterface;

import com.fun.vbox.client.hook.base.BinderInvocationStub;
import com.fun.vbox.client.hook.base.MethodInvocationProxy;
import com.fun.vbox.client.hook.base.MethodInvocationStub;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.fun.vbox.client.hook.base.ResultStaticMethodProxy;

import mirror.vbox.app.UriGrantsManager;
import mirror.vbox.os.ServiceManager;
import mirror.vbox.util.Singleton;

public final class UriGrantsManagerStub extends
        MethodInvocationProxy<MethodInvocationStub<IInterface>> {
    public UriGrantsManagerStub() {
        super(new MethodInvocationStub(UriGrantsManager.getService.call()));
    }

    @Override
    public void inject() {
        if (UriGrantsManager.IUriGrantsManagerSingleton != null) {
            Singleton.mInstance.set(UriGrantsManager.IUriGrantsManagerSingleton.get(), getInvocationStub().getProxyInterface());
        }
        BinderInvocationStub cVar = new BinderInvocationStub(getInvocationStub().getBaseInterface());
        cVar.replaceService("uri_grants");
        ServiceManager.sCache.get().put("uri_grants", cVar);
    }

    @Override
    public boolean isEnvBad() {
        return false;
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getUriPermissions"));
        addMethodProxy(new ResultStaticMethodProxy("takePersistableUriPermission", null));
    }
}