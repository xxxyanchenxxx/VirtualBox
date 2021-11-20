package com.fun.vbox.server.accounts;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;

import com.fun.vbox.server.pm.PackageCacheManager;
import com.fun.vbox.server.pm.PackageSetting;

public class RegisteredServicesParser {

    public XmlResourceParser getParser(Context context, ServiceInfo serviceInfo, String name) {
        Bundle meta = serviceInfo.metaData;
        if (meta != null) {
            int xmlId = meta.getInt(name);
            if (xmlId != 0) {
                try {
                    return getResources(context, serviceInfo.applicationInfo).getXml(xmlId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Resources getResources(Context context, ApplicationInfo appInfo) {
        PackageSetting ps = PackageCacheManager.getSetting(appInfo.packageName);
        if (ps != null) {
            AssetManager assets = mirror.vbox.content.res.AssetManager.ctor.newInstance();
            mirror.vbox.content.res.AssetManager.addAssetPath.call(assets, ps.getApkPath(false));
            Resources hostRes = context.getResources();
            return new Resources(assets, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
        }
        return null;
    }
}