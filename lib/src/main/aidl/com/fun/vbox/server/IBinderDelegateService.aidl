// IBinderDelegateService.aidl
package com.fun.vbox.server;

import android.content.ComponentName;

interface IBinderDelegateService {

   ComponentName getComponent();

   IBinder getService();

}
