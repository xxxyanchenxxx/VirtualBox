package com.fun.vbox.client.hook.base;

import android.content.Context;

import com.fun.vbox.client.core.InvocationStubManager;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.annotations.LogInvocation;
import com.fun.vbox.client.hook.annotations.SkipInject;
import com.fun.vbox.client.interfaces.IInjector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *
 * <p>
 * This class is responsible with:
 * - Instantiating a {@link MethodInvocationStub.HookInvocationHandler} on {@link #getInvocationStub()} ()}
 * - Install a bunch of {@link MethodProxy}s, either with a @{@link Inject} annotation or manually
 * calling {@link #addMethodProxy(MethodProxy)} from {@link #onBindMethods()}
 * - Install the hooked object on the Runtime via {@link #inject()}
 * <p>
 * All {@link MethodInvocationProxy}s (plus a couple of other @{@link IInjector}s are installed by
 * {@link InvocationStubManager}
 * @see Inject
 */
public abstract class MethodInvocationProxy<T extends MethodInvocationStub> implements IInjector {

    protected T mInvocationStub;

    public MethodInvocationProxy(T invocationStub) {
        this.mInvocationStub = invocationStub;
        onBindMethods();
        afterHookApply(invocationStub);

        LogInvocation loggingAnnotation = getClass().getAnnotation(LogInvocation.class);
        if (loggingAnnotation != null) {
            invocationStub.setInvocationLoggingCondition(loggingAnnotation.value());
        }
    }

    protected void onBindMethods() {

        if (mInvocationStub == null) {
            return;
        }
        Class<? extends MethodInvocationProxy> clazz = getClass();
        Inject inject = clazz.getAnnotation(Inject.class);
        if (inject != null) {
            Class<?> proxiesClass = inject.value();
            Class<?>[] innerClasses = proxiesClass.getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                if (!Modifier.isAbstract(innerClass.getModifiers())
                        && MethodProxy.class.isAssignableFrom(innerClass)
                        && innerClass.getAnnotation(SkipInject.class) == null) {
                    addMethodProxy(innerClass);
                }
            }
            for (Method method : proxiesClass.getMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (method.getAnnotation(SkipInject.class) != null) {
                    continue;
                }
                addMethodProxy(new DirectCallMethodProxy(method));

            }
        }
    }

    private static final class DirectCallMethodProxy extends StaticMethodProxy {

        private Method directCallMethod;

        public DirectCallMethodProxy(Method method) {
            super(method.getName());
            this.directCallMethod = method;
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return directCallMethod.invoke(null, who, method, args);
        }
    }

    private void addMethodProxy(Class<?> hookType) {
        try {
            Constructor<?> constructor = hookType.getDeclaredConstructors()[0];
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            MethodProxy methodProxy;
            if (constructor.getParameterTypes().length == 0) {
                methodProxy = (MethodProxy) constructor.newInstance();
            } else {
                methodProxy = (MethodProxy) constructor.newInstance(this);
            }
            mInvocationStub.addMethodProxy(methodProxy);
        } catch (Throwable e) {
            throw new RuntimeException("Unable to instance Hook : " + hookType + " : " + e.getMessage());
        }
    }

    public MethodProxy addMethodProxy(MethodProxy methodProxy) {
        return mInvocationStub.addMethodProxy(methodProxy);
    }

    public MethodProxy addMethodProxy(String methodName, MethodProxy methodProxy) {
        return mInvocationStub.addMethodProxy(methodName, methodProxy);
    }

    public void setDefaultMethodProxy(MethodProxy methodProxy) {
        mInvocationStub.setDefaultMethodProxy(methodProxy);
    }

    protected void afterHookApply(T delegate) {
    }

    @Override
    public abstract void inject() throws Throwable;

    public Context getContext() {
        return VCore.get().getContext();
    }

    public T getInvocationStub() {
        return mInvocationStub;
    }
}
