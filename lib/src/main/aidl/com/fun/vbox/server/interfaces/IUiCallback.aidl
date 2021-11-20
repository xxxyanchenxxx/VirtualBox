// IUiCallback.aidl
package com.fun.vbox.server.interfaces;

interface IUiCallback {
    void onAppOpened(in String packageName, in int userId);
}
