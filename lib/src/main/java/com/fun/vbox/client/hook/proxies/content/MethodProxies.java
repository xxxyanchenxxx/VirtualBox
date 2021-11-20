package com.fun.vbox.client.hook.proxies.content;


import android.content.ContentResolver;
import android.content.pm.ProviderInfo;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Build;

import com.fun.vbox.client.hook.base.MethodProxy;
import com.fun.vbox.client.hook.utils.MethodParameterUtils;
import com.fun.vbox.client.ipc.VContentManager;
import com.fun.vbox.client.ipc.VPackageManager;
import com.fun.vbox.helper.Keep;
import com.fun.vbox.helper.compat.BuildCompat;
import com.fun.vbox.os.VUserHandle;
import com.fun.vbox.server.content.VContentService;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
@Keep
public class MethodProxies {

    private static boolean isAppUri(Uri uri) {
        ProviderInfo info = VPackageManager.get().resolveContentProvider(uri.getAuthority(), 0, VUserHandle.myUserId());
        return info != null;
    }

    public static Object registerContentObserver(Object who, Method method, Object[] args) throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (args.length >= 5) {
                args[4] = Build.VERSION_CODES.LOLLIPOP_MR1;
            }
        }
        Uri uri = (Uri) args[0];
        boolean notifyForDescendents = (boolean) args[1];
        IContentObserver observer = (IContentObserver) args[2];
        if (isAppUri(uri)) {
            VContentService.get().registerContentObserver(uri, notifyForDescendents, observer);
            return 0;
        } else {
            MethodProxy.replaceFirstUserId(args);
            return method.invoke(who, args);
        }
    }

    public static Object unregisterContentObserver(Object who, Method method, Object[] args) throws Throwable {
        IContentObserver observer = (IContentObserver) args[0];
        VContentService.get().unregisterContentObserver(observer);
        return method.invoke(who, args);
    }

    public static Object notifyChange(Object who, Method method, Object[] args) throws Throwable {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (args.length >= 6) {
                args[5] = Build.VERSION_CODES.LOLLIPOP_MR1;
            }
        }
        if (BuildCompat.isR()) {
            Uri[] uriArr = (Uri[]) args[0];
            uri = (uriArr == null || uriArr.length <= 0) ? null : uriArr[0];
        } else {
            uri = (Uri) args[0];
        }
        if (uri != null && isAppUri(uri)) {
            IContentObserver observer = (IContentObserver) args[1];
            boolean observerWantsSelfNotifications = (boolean) args[2];
            boolean syncToNetwork;
            if (args[3] instanceof Integer) {
                int flags = (int) args[3];
                syncToNetwork = (flags & ContentResolver.NOTIFY_SYNC_TO_NETWORK) != 0;
            } else {
                syncToNetwork = (boolean) args[3];
            }
            VContentManager.get().notifyChange(uri, observer, observerWantsSelfNotifications, syncToNetwork, VUserHandle.myUserId());
            return 0;
        } else {
            if (BuildCompat.isR()) {
                MethodParameterUtils.replaceLastAppPkg(args);
            }
            MethodProxy.replaceLastUserId(args);
            return method.invoke(who, args);
        }
    }
}
