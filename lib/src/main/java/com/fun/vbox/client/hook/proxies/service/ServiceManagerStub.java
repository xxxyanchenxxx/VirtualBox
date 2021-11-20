package com.fun.vbox.client.hook.proxies.service;

import android.os.IInterface;

import com.fun.vbox.client.core.ServiceLocalManager;
import com.fun.vbox.client.hook.base.BinderInvocationStub;
import com.fun.vbox.client.hook.base.MethodInvocationProxy;
import com.fun.vbox.client.hook.base.MethodInvocationStub;
import com.fun.vbox.client.hook.base.StaticMethodProxy;
import com.fun.vbox.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.vbox.os.ServiceManager;

public class ServiceManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public ServiceManagerStub() {
        super(new MethodInvocationStub<>(ServiceManager.getIServiceManager.call()));
    }

    @Override
    public void inject() {
        ServiceManager.sServiceManager.set(getInvocationStub().getProxyInterface());
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new StaticMethodProxy("getService") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                String name = (String) args[0];
                BinderInvocationStub proxy = ServiceLocalManager.getService(name);
                if(proxy != null){
                    //getInterfaceDescriptor,queryLocalInterface
                    VLog.d("kk", "ServiceLocalManager.getService:%s->%s", name, proxy);
                    return proxy;
                }
                VLog.d("kk", "ServiceLocalManager.getService:%s no find", name);
                return super.call(who, method, args);
            }
        });
        addMethodProxy(new StaticMethodProxy("checkService"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                String name = (String) args[0];
                BinderInvocationStub proxy = ServiceLocalManager.getService(name);
                if(proxy != null){
                    //getInterfaceDescriptor,queryLocalInterface
                    VLog.d("kk", "ServiceLocalManager.checkService:%s->%s", name, proxy);
                    return proxy;
                }
                VLog.d("kk", "ServiceLocalManager.checkService:%s no find", name);
                return super.call(who, method, args);
            }
        });
    }

    @Override
    public boolean isEnvBad() {
        return ServiceManager.sServiceManager.get() != getInvocationStub().getProxyInterface();
    }
}