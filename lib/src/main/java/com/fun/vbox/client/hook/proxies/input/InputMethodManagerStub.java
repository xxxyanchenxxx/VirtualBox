package com.fun.vbox.client.hook.proxies.input;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.base.ReplaceLastUserIdMethodProxy;

import mirror.com.android.internal.view.inputmethod.InputMethodManager;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class InputMethodManagerStub extends BinderInvocationProxy {

    public InputMethodManagerStub() {
        super(
                InputMethodManager.mService.get(
                        VCore.get().getContext().getSystemService(Context.INPUT_METHOD_SERVICE)),
                Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void inject() throws Throwable {
        Object inputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodManager.mService
                .set(inputMethodManager, getInvocationStub().getProxyInterface());
        getInvocationStub().replaceService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceLastUserIdMethodProxy("getInputMethodList"));
        addMethodProxy(new ReplaceLastUserIdMethodProxy("getEnabledInputMethodList"));
    }

    @Override
    public boolean isEnvBad() {
        Object inputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return InputMethodManager
                .mService.get(inputMethodManager) != getInvocationStub().getBaseInterface();
    }

}