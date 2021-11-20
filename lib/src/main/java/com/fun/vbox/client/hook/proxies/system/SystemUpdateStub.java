package com.fun.vbox.client.hook.proxies.system;

import android.os.Bundle;
import android.os.ISystemUpdateManager;
import android.os.PersistableBundle;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;

import mirror.vbox.os.ServiceManager;

/**
 * @author Lody
 */
public class SystemUpdateStub extends BinderInvocationProxy {

    private static final String SERVICE_NAME = "system_update";

    public SystemUpdateStub() {
        super(new EmptySystemUpdateManagerImpl(), SERVICE_NAME);
    }

    @Override
    public void inject() throws Throwable {
        if (ServiceManager.checkService.call(SERVICE_NAME) == null) {
            super.inject();
        }
    }

    static class EmptySystemUpdateManagerImpl extends ISystemUpdateManager.Stub {

        @Override
        public Bundle retrieveSystemUpdateInfo() {
            Bundle info = new Bundle();
            info.putInt("status", 0);
            return info;
        }

        @Override
        public void updateSystemUpdateInfo(PersistableBundle data) {

        }
    }
}