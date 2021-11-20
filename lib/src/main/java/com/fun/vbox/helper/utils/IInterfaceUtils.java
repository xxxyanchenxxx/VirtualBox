package com.fun.vbox.helper.utils;

import android.os.IInterface;

public class IInterfaceUtils {
    public static boolean isAlive(IInterface binder) {
        if (binder == null) {
            return false;
        }
        return binder.asBinder().isBinderAlive();
    }
}