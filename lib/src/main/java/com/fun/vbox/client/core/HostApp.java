package com.fun.vbox.client.core;

import android.app.Application;

public class HostApp {
    private static Application sApplication;
    public static void setApplication(Application application) {
        sApplication = application;
    }

    public static Application getApplication() {
        return sApplication;
    }
}
