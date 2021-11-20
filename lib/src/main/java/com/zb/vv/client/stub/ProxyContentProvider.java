package com.zb.vv.client.stub;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.fun.vbox.helper.compat.ContentProviderCompat;
import com.fun.vbox.helper.compat.ProxyFCPUriCompat;

public class ProxyContentProvider extends ContentProvider {
    private static final boolean DEBUG = false;

    public boolean onCreate() {
        return true;
    }

    private Uri unWrapperUri(String str, Uri uri) {
        return ProxyFCPUriCompat.get().unWrapperUri(uri);
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        Uri unWrapperUri = unWrapperUri("query", uri);
        if (unWrapperUri == null) {
            return null;
        }
        try {
            return ContentProviderCompat
                    .acquireContentProviderClientRetry(getContext(), unWrapperUri, 5)
                    .query(unWrapperUri, strArr, str, strArr2, str2);
        } catch (Exception e) {
            return new MatrixCursor(new String[0]);
        }
    }

    public String getType(Uri uri) {
        String str = null;
        Uri unWrapperUri = unWrapperUri("getType", uri);
        if (unWrapperUri != null) {
            try {
                str = ContentProviderCompat.acquireContentProviderClientRetry(getContext(), unWrapperUri, 5)
                                .getType(unWrapperUri);
            } catch (Exception e) {
            }
        }
        return str;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri uri2 = null;
        Uri unWrapperUri = unWrapperUri("insert", uri);
        if (unWrapperUri != null) {
            try {
                uri2 = ContentProviderCompat.acquireContentProviderClientRetry(getContext(), unWrapperUri, 5)
                                .insert(unWrapperUri, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uri2;
    }

    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        int i = 0;
        Uri unWrapperUri = unWrapperUri("insert", uri);
        if (unWrapperUri != null) {
            try {
                i = ContentProviderCompat.acquireContentProviderClientRetry(getContext(), unWrapperUri, 5)
                                .bulkInsert(unWrapperUri, contentValuesArr);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return i;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        int i = 0;
        Uri unWrapperUri = unWrapperUri("delete", uri);
        if (unWrapperUri != null) {
            try {
                i = ContentProviderCompat.acquireContentProviderClientRetry(getContext(), unWrapperUri, 5)
                                .delete(unWrapperUri, str, strArr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int i = 0;
        Uri unWrapperUri = unWrapperUri("update", uri);
        if (unWrapperUri != null) {
            try {
                i = ContentProviderCompat.acquireContentProviderClientRetry(getContext(), unWrapperUri, 5)
                                .update(unWrapperUri, contentValues, str, strArr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String str) {
        AssetFileDescriptor assetFileDescriptor = null;
        Uri unWrapperUri = unWrapperUri("openAssetFile", uri);
        if (unWrapperUri != null) {
            try {
                assetFileDescriptor = ContentProviderCompat.acquireContentProviderClientRetry(getContext(),
                        unWrapperUri, 5).openAssetFile(unWrapperUri, str);
            } catch (Throwable th) {
            }
        }
        return assetFileDescriptor;
    }

    @TargetApi(19)
    public AssetFileDescriptor openAssetFile(Uri uri, String str, CancellationSignal cancellationSignal) {
        AssetFileDescriptor assetFileDescriptor = null;
        Uri unWrapperUri = unWrapperUri("openAssetFile2", uri);
        if (unWrapperUri != null) {
            try {
                assetFileDescriptor = ContentProviderCompat.acquireContentProviderClientRetry(getContext(),
                        unWrapperUri, 5).openAssetFile(unWrapperUri, str, cancellationSignal);
            } catch (Throwable th) {
            }
        }
        return assetFileDescriptor;
    }

    public ParcelFileDescriptor openFile(Uri uri, String str) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        Uri unWrapperUri = unWrapperUri("openFile", uri);
        if (unWrapperUri != null) {
            try {
                parcelFileDescriptor = ContentProviderCompat.acquireContentProviderClientRetry(getContext(),
                        unWrapperUri, 5).openFile(unWrapperUri, str);
            } catch (Exception e) {
            }
        }
        return parcelFileDescriptor;
    }

    @TargetApi(19)
    public ParcelFileDescriptor openFile(Uri uri, String str, CancellationSignal cancellationSignal) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        Uri unWrapperUri = unWrapperUri("openFile2", uri);
        if (unWrapperUri != null) {
            try {
                parcelFileDescriptor = ContentProviderCompat.acquireContentProviderClientRetry(getContext(),
                        unWrapperUri, 5).openFile(unWrapperUri, str, cancellationSignal);
            } catch (Exception e) {
            }
        }
        return parcelFileDescriptor;
    }

    public Bundle call(String str, String str2, Bundle bundle) {
        return super.call(str, str2, bundle);
    }
}