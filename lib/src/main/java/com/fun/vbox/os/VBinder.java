package com.fun.vbox.os;

import android.os.Binder;

import com.fun.vbox.client.ipc.VActivityManager;

/**
 * @author Lody
 */

public class VBinder {

    public static int getCallingUid() {
        return VActivityManager.get().getUidByPid(Binder.getCallingPid());
    }

    public static int getBaseCallingUid() {
        return VUserHandle.getAppId(getCallingUid());
    }

    public static int getCallingPid() {
        return Binder.getCallingPid();
    }

    /**
     * @see com.fun.vbox.os.VUserHandle#getCallingUserHandle
     * @deprecated
     */
    public static VUserHandle getCallingUserHandle() {
        return VUserHandle.getCallingUserHandle();
    }
}
