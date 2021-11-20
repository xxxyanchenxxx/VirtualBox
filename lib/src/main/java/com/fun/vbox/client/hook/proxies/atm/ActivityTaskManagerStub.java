package com.fun.vbox.client.hook.proxies.atm;

import android.annotation.TargetApi;
import android.os.IBinder;

import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.annotations.LogInvocation;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.MethodProxy;
import com.fun.vbox.client.hook.base.StaticMethodProxy;
import com.fun.vbox.client.ipc.VActivityManager;

import java.lang.reflect.Method;

import mirror.vbox.app.IActivityTaskManager;

@TargetApi(29)
@Inject(MethodProxies.class)
@LogInvocation
public class ActivityTaskManagerStub extends BinderInvocationProxy {
    public ActivityTaskManagerStub() {
        super(IActivityTaskManager.Stub.asInterface, "activity_task");
    }

    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new StaticMethodProxy("activityDestroyed") {
            public Object call(Object obj, Method method, Object... objArr) throws Throwable {
                VActivityManager.get().onActivityDestroy((IBinder) objArr[0]);
                return super.call(obj, method, objArr);
            }
        });
        addMethodProxy(new StaticMethodProxy("activityResumed") {
            public Object call(Object obj, Method method, Object... objArr) throws Throwable {
                VActivityManager.get().onActivityResumed((IBinder) objArr[0]);
                return super.call(obj, method, objArr);
            }
        });
        addMethodProxy(new StaticMethodProxy("finishActivity") {
            public Object call(Object obj, Method method, Object... objArr) throws Throwable {
                VActivityManager.get().onFinishActivity((IBinder) objArr[0]);
                return super.call(obj, method, objArr);
            }

            public boolean isEnable() {
                return MethodProxy.isAppProcess();
            }
        });
        addMethodProxy(new StaticMethodProxy("finishActivityAffinity") {
            public Object call(Object obj, Method method, Object... objArr) {
                return VActivityManager.get()
                        .finishActivityAffinity(MethodProxy.getAppUserId(),
                                (IBinder) objArr[Integer.parseInt(null)]);
            }

            public boolean isEnable() {
                return MethodProxy.isAppProcess();
            }
        });
        addMethodProxy(new StaticMethodProxy("requestAutofillData"));
    }
}
