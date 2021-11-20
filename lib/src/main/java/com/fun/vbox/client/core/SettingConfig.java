package com.fun.vbox.client.core;

import android.content.Intent;

import com.fun.vbox.client.env.Constants;
import com.fun.vbox.helper.Keep;

@Keep
public abstract class SettingConfig {

    public abstract String getHostPackageName();

    public abstract String get64bitEnginePackageName();

    public abstract String get64bitEngineLaunchActivityName();

    public String getBinderProviderAuthority() {
        return getHostPackageName() + ".virtual.service.BinderProvider";
    }

    public String get64bitHelperAuthority() {
        return get64bitEnginePackageName() + ".virtual.service.64bit_helper";
    }

    public String getProxyFileContentProviderAuthority() {
        return getHostPackageName() + ".virtual.fileprovider";
    }

    public String getShortcutProxyActivityName() {
        return Constants.SHORTCUT_PROXY_ACTIVITY_NAME;
    }

    public String getShortcutProxyActionName() {
        return Constants.ACTION_SHORTCUT;
    }

    public boolean isEnableIORedirect() {
        return true;
    }

    public boolean isAllowCreateShortcut() {
        return true;
    }

    public boolean isUseRealDataDir(String packageName) {
        return false;
    }

    public boolean isUseRealLibDir(String packageName) {
        return false;
    }

    /**
     *
     * 当app请求回到桌面时调用此方法
     *
     * @return intent or null
     */
    public Intent onHandleLauncherIntent(Intent originIntent) {
        return null;
    }

    @Keep
    public enum AppLibConfig {
        UseRealLib,
        UseOwnLib,
    }

    public AppLibConfig getAppLibConfig(String packageName) {
        return AppLibConfig.UseRealLib;
    }

    public boolean isAllowServiceStartForeground() {
        return false;
    }

    public boolean isEnableAppFileSystemIsolation() {
        return false;
    }

    public boolean isHideForegroundNotification() {
        return false;
    }

    public FakeWifiStatus getFakeWifiStatus() {
        return null;
    }

    public boolean isHostIntent(Intent intent) {
        return false;
    }


    /**
     * 是否禁止悬浮窗
     */
    public boolean isDisableDrawOverlays(String packageName){
        return false;
    }

    public static class FakeWifiStatus {

        public static String DEFAULT_BSSID = "66:55:44:33:22:11";
        public static String DEFAULT_MAC = "11:22:33:44:55:66";
        public static String DEFAULT_SSID = "VA_SSID";

        public String getSSID() {
            return DEFAULT_SSID;
        }

        public String getBSSID() {
            return DEFAULT_BSSID;
        }

        public String getMAC() {
            return DEFAULT_MAC;
        }

    }

}
