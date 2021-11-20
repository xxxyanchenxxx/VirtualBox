package com.fun.vbox.client.hook.providers;

import com.fun.vbox.client.core.VCore;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class ExternalProviderHook extends ProviderHook {

    public ExternalProviderHook(Object base) {
        super(base);
    }

    @Override
    protected void processArgs(Method method, Object... args) {
        if (args != null && args.length > 0 && args[0] instanceof String) {
            String pkg = (String) args[0];
            if (VCore.get().isAppInstalled(pkg)) {
                args[0] = VCore.get().getHostPkg();
            }
        }
    }
}
