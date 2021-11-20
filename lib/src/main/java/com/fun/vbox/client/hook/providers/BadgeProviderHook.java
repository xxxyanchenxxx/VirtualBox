package com.fun.vbox.client.hook.providers;

import android.os.Bundle;

import com.fun.vbox.client.VClient;
import com.fun.vbox.client.hook.base.MethodBox;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.os.VUserHandle;
import com.fun.vbox.remote.BadgerInfo;

import java.lang.reflect.InvocationTargetException;

public class BadgeProviderHook extends ExternalProviderHook {
    public BadgeProviderHook(Object base) {
        super(base);
    }

    @Override
    public Bundle call(MethodBox methodBox, String method, String arg, Bundle extras) throws InvocationTargetException {
        if("change_badge".equals(method)){
            BadgerInfo info = new BadgerInfo();
            info.userId = VUserHandle.myUserId();
            info.packageName = extras.getString("package");
            info.className = extras.getString("class");
            info.badgerCount = extras.getInt("badgenumber");
            VActivityManager.get().notifyBadgerChange(info);
            Bundle out = new Bundle();
            out.putBoolean("success", true);
            return out;
        } else if ("setAppBadgeCount".equals(method)) {
            BadgerInfo info = new BadgerInfo();
            info.userId = VUserHandle.myUserId();
            info.packageName = VClient.get().getCurrentPackage();
            info.badgerCount = extras.getInt("app_badge_count");
            VActivityManager.get().notifyBadgerChange(info);
            Bundle out = new Bundle();
            out.putBoolean("success", true);
        }
        return super.call(methodBox, method, arg, extras);
    }
}
