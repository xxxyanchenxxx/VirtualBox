package com.fun.vbox.client.hook.base;

import java.lang.reflect.Method;

public class ReplaceLastUserIdMethodProxy extends StaticMethodProxy {
    public ReplaceLastUserIdMethodProxy(String str) {
        super(str);
    }

    public boolean beforeCall(Object obj, Method method, Object... objArr) {
        MethodProxy.replaceLastUserId(objArr);
        return super.beforeCall(obj, method, objArr);
    }
}