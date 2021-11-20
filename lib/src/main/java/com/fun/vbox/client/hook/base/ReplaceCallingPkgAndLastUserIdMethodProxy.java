package com.fun.vbox.client.hook.base;

import com.fun.vbox.client.hook.utils.MethodParameterUtils;

import java.lang.reflect.Method;

public class ReplaceCallingPkgAndLastUserIdMethodProxy extends StaticMethodProxy {
    public ReplaceCallingPkgAndLastUserIdMethodProxy(String str) {
        super(str);
    }

    public boolean beforeCall(Object obj, Method method, Object... objArr) {
        MethodProxy.replaceLastUserId(objArr);
        MethodParameterUtils.replaceFirstAppPkg(objArr);
        return super.beforeCall(obj, method, objArr);
    }
}