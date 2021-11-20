package com.fun.vbox.client.core;

import com.fun.vbox.helper.Keep;

@Keep
public interface CrashHandler {

    void handleUncaughtException(Thread t, Throwable e);

}
