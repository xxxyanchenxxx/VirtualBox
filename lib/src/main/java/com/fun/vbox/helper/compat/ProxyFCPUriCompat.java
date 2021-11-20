package com.fun.vbox.helper.compat;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.ServiceManagerNative;
import com.fun.vbox.client.ipc.VPackageManager;
import com.fun.vbox.helper.utils.Reflect;
import com.fun.vbox.helper.utils.VLog;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProxyFCPUriCompat {
    private static final boolean DEBUG = true;
    private static final String SUFFIX = "@_outside";
    private static final String TAG = "UriCompat";
    private static ProxyFCPUriCompat sInstance = new ProxyFCPUriCompat();

    public static ProxyFCPUriCompat get() {
        return sInstance;
    }

    private boolean isInsideuthority(String str) {
        return VPackageManager.get().isVirtualAuthority(str);
    }

    public boolean isOutSide(String str) {
        return str != null && str.endsWith(SUFFIX);
    }

    public String wrapperOutSide(String str) {
        if (str.lastIndexOf(SUFFIX) < 0) {
            return str + SUFFIX;
        }
        return str;
    }

    public String unWrapperOutSide(String str) {
        int lastIndexOf = str.lastIndexOf(SUFFIX);
        if (lastIndexOf > 0) {
            return str.substring(0, lastIndexOf);
        }
        return str;
    }

    public boolean needFake(Intent intent) {
        String str = intent.getPackage();
        if (str != null && VCore.get().isAppInstalled(str)) {
            return false;
        }
        ComponentName component = intent.getComponent();
        if (component == null || !VCore.get().isAppInstalled(component.getPackageName())) {
            return true;
        }
        return false;
    }

    public String getAuthority() {
        return VCore.getConfig().getProxyFileContentProviderAuthority();
    }

    private String encode(String str) throws UnsupportedEncodingException {
        return new String(Base64.encode(str.getBytes("utf-8"), 10), "US-ASCII");
    }

    private String decode(String str) throws UnsupportedEncodingException {
        return new String(Base64.decode(str.getBytes("US-ASCII"), 10), "utf-8");
    }

    public Uri wrapperUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        String authority = uri.getAuthority();
        if (getAuthority().equals(authority) || !isInsideuthority(authority)) {
            return uri;
        }
        if (!ServiceManagerNative.CONTENT.equals(uri.getScheme())) {
            return null;
        }
        Builder builder = new Builder();
        builder.scheme(uri.getScheme());
        builder.authority(getAuthority()).appendPath("out");
        try {
            builder.appendQueryParameter("uri", encode(uri.toString()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Uri build = builder.build();
        Log.i(TAG, "fake uri:" + uri + "->" + build);
        return build;
    }

    public Uri unWrapperUri(Uri uri) {
        Map<String, String> map = null;
        if (uri == null) {
            return null;
        }
        if (!TextUtils.equals(uri.getAuthority(), getAuthority())) {
            return uri;
        }
        String queryParameter = uri.getQueryParameter("uri");
        if (TextUtils.isEmpty(queryParameter)) {
            queryParameter = uri.getQueryParameter("__UR_auth");
            if (TextUtils.isEmpty(queryParameter)) {
                return null;
            }
            Builder authority = uri.buildUpon().authority(queryParameter);
            Set<String> queryParameterNames = uri.getQueryParameterNames();
            if (queryParameterNames != null && queryParameterNames.size() > 0) {
                Map<String, String> hashMap = new HashMap<>();
                for (String str : queryParameterNames) {
                    hashMap.put(str, uri.getQueryParameter(str));
                }
                map = hashMap;
            }
            authority.clearQuery();
            for (Entry<String, String> entry : map.entrySet()) {
                if (!"__UR_auth".equals(entry.getKey())) {
                    authority.appendQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            Uri build = authority.build();
            Log.i(TAG, "unWrapperUri uri:" + uri + "->" + build);
            return build;
        }
        Uri parse = null;
        try {
            parse = Uri.parse(decode(queryParameter));
            Log.i(TAG, "wrapperUri uri:" + uri + "->" + parse);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return parse;
    }

    public Intent fakeFileUri(Intent intent) {
        if (needFake(intent)) {
            Uri data = intent.getData();
            if (data != null) {
                Uri wrapperUri = wrapperUri(data);
                if (wrapperUri != null) {
                    Log.i(TAG, "fake data uri:" + data + "->" + wrapperUri);
                    intent.setDataAndType(wrapperUri, intent.getType());
                }
            }
            if (VERSION.SDK_INT >= 16) {
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    int itemCount = clipData.getItemCount();
                    for (int i = 0; i < itemCount; i++) {
                        ClipData.Item itemAt = clipData.getItemAt(i);
                        Uri wrapperUri2 = wrapperUri(itemAt.getUri());
                        if (wrapperUri2 != null) {
                            Reflect.on(itemAt).set("mUri", wrapperUri2);
                        }
                    }
                }
            }
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int i2 = 0;
                for (String str : extras.keySet()) {
                    Object obj = extras.get(str);
                    Parcelable fakeFileUri;
                    int i3;
                    if (obj instanceof Intent) {
                        fakeFileUri = fakeFileUri((Intent) obj);
                        if (fakeFileUri != null) {
                            extras.putParcelable(str, fakeFileUri);
                            i3 = 1;
                        } else {
                            i3 = i2;
                        }
                        i2 = i3;
                    } else {
                        if (obj instanceof Uri) {
                            fakeFileUri = wrapperUri((Uri) obj);
                            if (fakeFileUri != null) {
                                extras.putParcelable(str, fakeFileUri);
                                i3 = 1;
                                i2 = i3;
                            }
                        }
                        i3 = i2;
                        i2 = i3;
                    }
                }
                if (i2 != 0) {
                    intent.putExtras(extras);
                }
            }
        } else {
            VLog.i(TAG, "don't need fake intent", new Object[0]);
        }
        return intent;
    }
}