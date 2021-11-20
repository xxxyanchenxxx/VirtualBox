package com.fun.vbox.client.hook.proxies.libcore;

import com.fun.vbox.client.NativeEngine;
import com.fun.vbox.client.VClient;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.base.MethodProxy;
import com.fun.vbox.helper.utils.Reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import mirror.libcore.io.Os;

/**
 * @author Lody
 */

class MethodProxies {

    static class Lstat extends Stat {

        @Override
        public String getMethodName() {
            return "lstat";
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            if (result != null) {
                Reflect pwd = Reflect.on(result);
                int uid = pwd.get("st_uid");
                if (uid == VCore.get().myUid()) {
                    pwd.set("st_uid", VClient.get().getVUid());
                }
            }
            return result;
        }
    }

    static class Fstat extends Stat {

        @Override
        public String getMethodName() {
            return "fstat";
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            if (result != null) {
                Reflect pwd = Reflect.on(result);
                int uid = pwd.get("st_uid");
                if (uid == VCore.get().myUid()) {
                    pwd.set("st_uid", VClient.get().getVUid());
                }
            }
            return result;
        }
    }
    static class Getpwnam extends MethodProxy {
            @Override
            public String getMethodName() {
                return "getpwnam";
            }

            @Override
            public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
                if (result != null) {
                    Reflect pwd = Reflect.on(result);
                    int uid = pwd.get("pw_uid");
                    if (uid == VCore.get().myUid()) {
                        pwd.set("pw_uid", VClient.get().getVUid());
                    }
                }
                return result;
            }
        }

    static class GetUid extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getuid";
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            int uid = (int) result;
            return NativeEngine.onGetUid(uid);
        }
    }

    static class GetsockoptUcred extends MethodProxy {
            @Override
            public String getMethodName() {
                return "getsockoptUcred";
            }

            @Override
            public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
                if (result != null) {
                    Reflect ucred = Reflect.on(result);
                    int uid = ucred.get("uid");
                    if (uid == VCore.get().myUid()) {
                        ucred.set("uid", getBaseVUid());
                    }
                }
                return result;
            }
        }

    static class Stat extends MethodProxy {

        private static Field st_uid;

        static {
            try {
                Method stat = Os.TYPE.getMethod("stat", String.class);
                Class<?> StructStat = stat.getReturnType();
                st_uid = StructStat.getDeclaredField("st_uid");
                st_uid.setAccessible(true);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            int uid = (int) st_uid.get(result);
            if (uid == VCore.get().myUid()) {
                st_uid.set(result, getBaseVUid());
            }
            return result;
        }

        @Override
        public String getMethodName() {
            return "stat";
        }
    }
}
