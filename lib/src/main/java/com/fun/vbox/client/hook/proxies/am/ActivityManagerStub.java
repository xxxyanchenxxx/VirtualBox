package com.fun.vbox.client.hook.proxies.am;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.annotations.LogInvocation;
import com.fun.vbox.client.hook.base.BinderInvocationStub;
import com.fun.vbox.client.hook.base.MethodInvocationProxy;
import com.fun.vbox.client.hook.base.MethodInvocationStub;
import com.fun.vbox.client.hook.base.MethodProxy;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.fun.vbox.client.hook.base.ReplaceLastPkgMethodProxy;
import com.fun.vbox.client.hook.base.ResultStaticMethodProxy;
import com.fun.vbox.client.hook.base.StaticMethodProxy;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.helper.compat.BuildCompat;
import com.fun.vbox.helper.utils.ArrayUtils;
import com.fun.vbox.helper.utils.ComponentUtils;
import com.fun.vbox.os.VUserHandle;
import com.fun.vbox.remote.IntentSenderData;

import java.lang.reflect.Method;

import mirror.vbox.app.ActivityManagerNative;
import mirror.vbox.app.ActivityManagerOreo;
import mirror.vbox.app.IActivityManager;
import mirror.vbox.os.ServiceManager;
import mirror.vbox.util.Singleton;

/**
 *
 * @see IActivityManager
 * @see android.app.ActivityManager
 */
//@LogInvocation
@Inject(MethodProxies.class)
public class ActivityManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public ActivityManagerStub() {
        super(new MethodInvocationStub<>(ActivityManagerNative.getDefault.call()));
    }

    @Override
    public void inject() {
        if (BuildCompat.isOreo()) {
            //Android Oreo(8.X)
            Object singleton = ActivityManagerOreo.IActivityManagerSingleton.get();
            Singleton.mInstance.set(singleton, getInvocationStub().getProxyInterface());
        } else {
            if (ActivityManagerNative.gDefault.type() == IActivityManager.TYPE) {
                ActivityManagerNative.gDefault.set(getInvocationStub().getProxyInterface());
            } else if (ActivityManagerNative.gDefault.type() == Singleton.TYPE) {
                Object gDefault = ActivityManagerNative.gDefault.get();
                Singleton.mInstance.set(gDefault, getInvocationStub().getProxyInterface());
            }
        }
        BinderInvocationStub hookAMBinder = new BinderInvocationStub(getInvocationStub().getBaseInterface());
        hookAMBinder.copyMethodProxies(getInvocationStub());
        ServiceManager.sCache.get().put(Context.ACTIVITY_SERVICE, hookAMBinder);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        if (VCore.get().isVAppProcess()) {
            addMethodProxy(new StaticMethodProxy("setRequestedOrientation") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    try {
                        return super.call(who, method, args);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
            addMethodProxy(new ResultStaticMethodProxy("registerUidObserver", 0));
            addMethodProxy(new ResultStaticMethodProxy("unregisterUidObserver", 0));
            addMethodProxy(new ReplaceLastPkgMethodProxy("getAppStartMode"));
            addMethodProxy(new ResultStaticMethodProxy("updateConfiguration", 0));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setAppLockedVerifying"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("reportJunkFromApp"));
            addMethodProxy(new StaticMethodProxy("activityResumed") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    IBinder token = (IBinder) args[0];
                    VActivityManager.get().onActivityResumed(token);
                    return super.call(who, method, args);
                }
            });
            addMethodProxy(new StaticMethodProxy("activityDestroyed") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    IBinder token = (IBinder) args[0];
                    VActivityManager.get().onActivityDestroy(token);
                    return super.call(who, method, args);
                }
            });
            addMethodProxy(new StaticMethodProxy("checkUriPermission") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    return PackageManager.PERMISSION_GRANTED;
                }
            });
            addMethodProxy(new StaticMethodProxy("finishActivity") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    IBinder token = (IBinder) args[0];
                    VActivityManager.get().onFinishActivity(token);
                    return super.call(who, method, args);
                }

                @Override
                public boolean isEnable() {
                    return isAppProcess();
                }
            });
            addMethodProxy(new StaticMethodProxy("finishActivityAffinity") {
                @Override
                public Object call(Object who, Method method, Object... args) {
                    IBinder token = (IBinder) args[0];
                    return VActivityManager.get().finishActivityAffinity(getAppUserId(), token);
                }

                @Override
                public boolean isEnable() {
                    return isAppProcess();
                }
            });

            if (Build.VERSION.SDK_INT >= 29) {
                addMethodProxy(new bd());
            }
            if (BuildCompat.isR()) {
                addMethodProxy("getIntentSenderWithFeature", new bd());
                addMethodProxy("registerReceiverWithFeature", new MethodProxies.RegisterReceiver());
                addMethodProxy("broadcastIntentWithFeature", new ba());
                addMethodProxy(new ReplaceCallingPkgMethodProxy("isTopActivityInFreeform"));
            }
        }
    }

    @Override
    public boolean isEnvBad() {
        return ActivityManagerNative.getDefault.call() != getInvocationStub().getProxyInterface();
    }

    static final class ba extends MethodProxies.BroadcastIntent {
        @Override
        public final String getMethodName() {
            return "broadcastIntentWithFeature";
        }

        @Override
        public final Object call(Object who, Method method, Object[] args) throws Throwable {
            Intent v1 = (Intent)args[2];
            v1.setDataAndType(v1.getData(), ((String)args[3]));
            Intent v1_1 = handleIntent(v1);
            if(v1_1 != null) {
                args[2] = v1_1;
                if(((args[8] instanceof String)) || ((args[8] instanceof String[]))) {
                    args[8] = null;
                }

                return method.invoke(who, args);
            }

            return 0;
        }

        @Override
        public final boolean isEnable() {
            return isAppProcess();
        }
    }

    static class bd extends MethodProxy {
        @Override
        public final String getMethodName() {
            return "getIntentSenderWithSourceToken";
        }

        @Override
        public final Object call(Object who, Method method, Object[] args) throws Throwable {
            String creator = (String) args[1];
            args[1] = getHostPkg();
            String[] v5 = (String[])args[7];
            ArrayUtils.indexOfFirst(args, IBinder.class);
            Intent[] v12 = (Intent[])args[6];
            if(v12.length > 0) {
                Intent v14 = new Intent(v12[v12.length - 1]);
                if(v5 != null && v5.length >= v12.length) {
                    v14.setDataAndType(v14.getData(), v5[v12.length - 1]);
                }

                Intent v5_1 = ComponentUtils.redirectIntentSender((Integer) args[0], creator, v14);
                if(v5_1 == null) {
                    return null;
                }

                args[6] = new Intent[]{v5_1};
                args[7] = new String[]{null};
                args[8] = ((Integer)args[8]) & 0xFFFFFF00;
                IInterface v0 = (IInterface)method.invoke(who, args);
                if(v0 != null) {
                    IBinder token = v0.asBinder();
                    int type = (int) args[0];
                    int flags = (int) args[8];
                    IntentSenderData
                            v1 = new IntentSenderData(creator, token, v14, flags, type, VUserHandle.myUserId());
                    VActivityManager.get().addOrUpdateIntentSender(v1);
                }

                return v0;
            }

            return method.invoke(who, args);
        }
    }
}
