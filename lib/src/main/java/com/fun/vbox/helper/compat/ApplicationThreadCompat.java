package com.fun.vbox.helper.compat;

import android.os.IBinder;
import android.os.IInterface;

import mirror.vbox.app.ApplicationThreadNative;
import mirror.vbox.app.IApplicationThreadOreo;

/**
 * @author Lody
 */

public class ApplicationThreadCompat {

    public static IInterface asInterface(IBinder binder) {
        if (BuildCompat.isOreo()) {
            return IApplicationThreadOreo.Stub.asInterface.call(binder);
        }
        return ApplicationThreadNative.asInterface.call(binder);
    }
}
