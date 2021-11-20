// IRequestPermissionsResult.aidl
package com.fun.vbox.server;

// Declare any non-default types here with import statements

interface IRequestPermissionsResult {
    boolean onResult(int requestCode,in String[] permissions,in int[] grantResults);
}
