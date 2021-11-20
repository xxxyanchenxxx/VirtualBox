package com.fun.vbox.client.hook.proxies.window;

import android.content.Context;
import android.os.Build;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.base.StaticMethodProxy;

import mirror.vbox.view.Display;
import mirror.vbox.view.IWindowManager;
import mirror.vbox.view.WindowManagerGlobal;
import mirror.com.android.internal.policy.PhoneWindow;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class WindowManagerStub extends BinderInvocationProxy {

    public WindowManagerStub() {
        super(IWindowManager.Stub.asInterface, Context.WINDOW_SERVICE);
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (WindowManagerGlobal.sWindowManagerService != null) {
                WindowManagerGlobal.sWindowManagerService.set(getInvocationStub().getProxyInterface());
            }
        } else {
            if (Display.sWindowManager != null) {
                Display.sWindowManager.set(getInvocationStub().getProxyInterface());
            }
        }
        if (PhoneWindow.TYPE != null && PhoneWindow.sWindowManager != null) {
            PhoneWindow.sWindowManager.set(getInvocationStub().getProxyInterface());
        }
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new StaticMethodProxy("addAppToken"));
        addMethodProxy(new StaticMethodProxy("setScreenCaptureDisabled"));
    }
}
