package com.fun.vbox.client.hook.proxies.power;

import android.content.Context;
import android.os.Build;
import android.os.WorkSource;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceLastPkgMethodProxy;
import com.fun.vbox.client.hook.base.ReplaceSequencePkgMethodProxy;
import com.fun.vbox.client.hook.base.ResultStaticMethodProxy;
import com.fun.vbox.client.hook.base.StaticMethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mirror.vbox.os.IPowerManager;

/**
 *
 * @see android.os.PowerManager
 */
public class PowerManagerStub extends BinderInvocationProxy {

    public PowerManagerStub() {
        super(IPowerManager.Stub.asInterface, Context.POWER_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceLastPkgMethodProxy("wakeUp"));
        addMethodProxy(new ReplaceSequencePkgMethodProxy("acquireWakeLock", 2) {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                replaceWorkSource(args);
                try {
                    return super.call(who, method, args);
                } catch (InvocationTargetException e) {
                    return onHandleError(e);
                }
            }
        });
        addMethodProxy(new ReplaceLastPkgMethodProxy("acquireWakeLockWithUid") {

            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                replaceWorkSource(args);
                try {
                    return super.call(who, method, args);
                } catch (InvocationTargetException e) {
                    return onHandleError(e);
                }
            }
        });
        addMethodProxy(new ResultStaticMethodProxy("updateWakeLockWorkSource", 0) {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                replaceWorkSource(args);
                return super.call(who, method, args);
            }
        });
        if (Build.MANUFACTURER.equalsIgnoreCase("FUJITSU")) {
            addMethodProxy(new StaticMethodProxy("acquireWakeLockWithLogging") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    if (args[3] instanceof String && isAppPkg((String) args[3])) {
                        args[3] = getHostPkg();
                    }
                    replaceWorkSource(args);
                    return super.call(who, method, args);
                }
            });
        }
    }

    private void replaceWorkSource(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof WorkSource) {
                args[i] = null;
                break;
            }
        }
    }

    private Object onHandleError(InvocationTargetException e) throws Throwable {
        if (e.getCause() instanceof SecurityException) {
            return 0;
        }
        throw e.getCause();
    }
}
