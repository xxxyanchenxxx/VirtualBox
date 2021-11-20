package com.zb.vv.client.stub;

import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import com.fun.vbox.client.NativeEngine;
import com.fun.vbox.client.VClient;
import com.fun.vbox.client.core.InvocationStubManager;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.hook.proxies.am.ActivityManagerStub;
import com.fun.vbox.helper.compat.ApplicationThreadCompat;
import com.fun.vbox.helper.compat.BundleCompat;
import com.fun.vbox.helper.utils.ArrayUtils;
import com.fun.vbox.helper.utils.ClassUtils;
import com.fun.vbox.remote.ClientConfig;

import mirror.vbox.app.IApplicationThread;

/**
 * @author Lody
 */
public class ShadowContentProvider extends ContentProvider {
    ActivityManager mAM;

    @Override
    public boolean onCreate() {
        mAM = (ActivityManager) VCore.get().getContext().getSystemService(Context.ACTIVITY_SERVICE);
        return true;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if ("_VBOX_|_init_process_".equals(method)) {
            return initProcess(extras);
        } else if ("_VBOX_|_invoke_".equals(method)) {
            if (VCore.get().isAppCallbackEmpty()) {
                VCore.get().setInitBundle(extras);
                return null;
            }
            return VCore.get().getAppCallback().invokeFromAnyWhere(extras);
        } else if ("_VBOX_|_startActivity_".equals(method)) {
            return startActivityInner(extras);
        } else if ("_VBOX_|_moveTaskToFront_".equals(method)) {
            return moveTaskToFront(extras);
        } else if ("_VBOX_|_startActivity2_".equals(method)) {
            return startActivityInner2(extras);
        }
        return null;
    }

    private Bundle moveTaskToFront(Bundle extras) {
        Bundle res = new Bundle();
        int taskId = extras.getInt("taskId");
        int flags = extras.getInt("flags");
        mAM.moveTaskToFront(taskId, flags);
        res.putBoolean("res", true);
        return res;
    }

    private Bundle startActivityInner(Bundle extras) {
        Bundle res = new Bundle();
        Intent destIntent = extras.getParcelable("intent");
        if (destIntent != null) {
            Bundle options = extras.getBundle("options");
            if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                VCore.get().getContext().startActivity(destIntent, options);
            } else {
                VCore.get().getContext().startActivity(destIntent);
            }
            res.putBoolean("res", true);
        } else {
            res.putBoolean("res", false);
        }
        return res;
    }

    private Bundle startActivityInner2(Bundle extras) {
        Bundle res = new Bundle();
        Class<?>[] types = mirror.vbox.app.IActivityManager.startActivity.paramList();
        Object[] args = new Object[types.length];

        IBinder applicationThread = BundleCompat.getBinder(extras, "applicationThread");
        Intent intent = extras.getParcelable("intent");
        IBinder resultTo = BundleCompat.getBinder(extras, "resultTo");
        String resultWho = extras.getString("resultWho");
        int requestCode = extras.getInt("requestCode");
        Bundle options = extras.getBundle("options");
        String pkg = extras.getString("pkg");

        if (types[0] == IApplicationThread.TYPE) {
            args[0] = ApplicationThreadCompat.asInterface(applicationThread);
        }

        int intentIndex = ArrayUtils.protoIndexOf(types, Intent.class);
        int resultToIndex = ArrayUtils.protoIndexOf(types, IBinder.class, 2);
        int optionsIndex = ArrayUtils.protoIndexOf(types, Bundle.class);
        int resolvedTypeIndex = intentIndex + 1;
        int resultWhoIndex = resultToIndex + 1;
        int requestCodeIndex = resultToIndex + 2;

        if (!NativeEngine.nativeTraceProcessEx(Build.VERSION.SDK_INT)) {
            intentIndex += 2;
        }
        args[intentIndex] = intent;
        args[resultToIndex] = resultTo;
        args[resultWhoIndex] = resultWho;
        args[requestCodeIndex] = requestCode;
        if (optionsIndex != -1) {
            args[optionsIndex] = options;
        }
        if (intent != null) {
            args[resolvedTypeIndex] = intent.getType();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            args[intentIndex - 1] = pkg;
        }
        ClassUtils.fixArgs(types, args);
        try {
            ActivityManagerStub stub =
                    InvocationStubManager.getInstance().findInjector(ActivityManagerStub.class);
            Object who = stub.getInvocationStub().getBaseInterface();
            mirror.vbox.app.IActivityManager.startActivity
                    .call(who,
                            (Object[]) args);
            res.putBoolean("res", true);
            return res;
        } catch (Throwable e) {
            Log.e("VActivityManager", "", e);
        }
        res.putBoolean("res", false);
        return res;
    }

    private Bundle initProcess(Bundle extras) {
        VCore.get().waitStartup();
        extras.setClassLoader(ClientConfig.class.getClassLoader());
        ClientConfig clientConfig = extras.getParcelable("_VBOX_|_client_config_");
        VClient client = VClient.get();
        client.initProcess(clientConfig);
        Bundle res = new Bundle();
        BundleCompat.putBinder(res, "_VBOX_|_client_", client.asBinder());
        res.putInt("_VBOX_|_pid_", Process.myPid());
        return res;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static class P0 extends ShadowContentProvider {
    }

    public static class P1 extends ShadowContentProvider {
    }

    public static class P2 extends ShadowContentProvider {
    }

    public static class P3 extends ShadowContentProvider {
    }

    public static class P4 extends ShadowContentProvider {
    }

    public static class P5 extends ShadowContentProvider {
    }

    public static class P6 extends ShadowContentProvider {
    }

    public static class P7 extends ShadowContentProvider {
    }

    public static class P8 extends ShadowContentProvider {
    }

    public static class P9 extends ShadowContentProvider {
    }

    public static class P10 extends ShadowContentProvider {
    }

    public static class P11 extends ShadowContentProvider {
    }

    public static class P12 extends ShadowContentProvider {
    }

    public static class P13 extends ShadowContentProvider {
    }

    public static class P14 extends ShadowContentProvider {
    }

    public static class P15 extends ShadowContentProvider {
    }

    public static class P16 extends ShadowContentProvider {
    }

    public static class P17 extends ShadowContentProvider {
    }

    public static class P18 extends ShadowContentProvider {
    }

    public static class P19 extends ShadowContentProvider {
    }

    public static class P20 extends ShadowContentProvider {
    }

    public static class P21 extends ShadowContentProvider {
    }

    public static class P22 extends ShadowContentProvider {
    }

    public static class P23 extends ShadowContentProvider {
    }

    public static class P24 extends ShadowContentProvider {
    }

    public static class P25 extends ShadowContentProvider {
    }

    public static class P26 extends ShadowContentProvider {
    }

    public static class P27 extends ShadowContentProvider {
    }

    public static class P28 extends ShadowContentProvider {
    }

    public static class P29 extends ShadowContentProvider {
    }

    public static class P30 extends ShadowContentProvider {
    }

    public static class P31 extends ShadowContentProvider {
    }

    public static class P32 extends ShadowContentProvider {
    }

    public static class P33 extends ShadowContentProvider {
    }

    public static class P34 extends ShadowContentProvider {
    }

    public static class P35 extends ShadowContentProvider {
    }

    public static class P36 extends ShadowContentProvider {
    }

    public static class P37 extends ShadowContentProvider {
    }

    public static class P38 extends ShadowContentProvider {
    }

    public static class P39 extends ShadowContentProvider {
    }

    public static class P40 extends ShadowContentProvider {
    }

    public static class P41 extends ShadowContentProvider {
    }

    public static class P42 extends ShadowContentProvider {
    }

    public static class P43 extends ShadowContentProvider {
    }

    public static class P44 extends ShadowContentProvider {
    }

    public static class P45 extends ShadowContentProvider {
    }

    public static class P46 extends ShadowContentProvider {
    }

    public static class P47 extends ShadowContentProvider {
    }

    public static class P48 extends ShadowContentProvider {
    }

    public static class P49 extends ShadowContentProvider {
    }

    public static class P50 extends ShadowContentProvider {
    }

    public static class P51 extends ShadowContentProvider {
    }

    public static class P52 extends ShadowContentProvider {
    }

    public static class P53 extends ShadowContentProvider {
    }

    public static class P54 extends ShadowContentProvider {
    }

    public static class P55 extends ShadowContentProvider {
    }

    public static class P56 extends ShadowContentProvider {
    }

    public static class P57 extends ShadowContentProvider {
    }

    public static class P58 extends ShadowContentProvider {
    }

    public static class P59 extends ShadowContentProvider {
    }

    public static class P60 extends ShadowContentProvider {
    }

    public static class P61 extends ShadowContentProvider {
    }

    public static class P62 extends ShadowContentProvider {
    }

    public static class P63 extends ShadowContentProvider {
    }

    public static class P64 extends ShadowContentProvider {
    }

    public static class P65 extends ShadowContentProvider {
    }

    public static class P66 extends ShadowContentProvider {
    }

    public static class P67 extends ShadowContentProvider {
    }

    public static class P68 extends ShadowContentProvider {
    }

    public static class P69 extends ShadowContentProvider {
    }

    public static class P70 extends ShadowContentProvider {
    }

    public static class P71 extends ShadowContentProvider {
    }

    public static class P72 extends ShadowContentProvider {
    }

    public static class P73 extends ShadowContentProvider {
    }

    public static class P74 extends ShadowContentProvider {
    }

    public static class P75 extends ShadowContentProvider {
    }

    public static class P76 extends ShadowContentProvider {
    }

    public static class P77 extends ShadowContentProvider {
    }

    public static class P78 extends ShadowContentProvider {
    }

    public static class P79 extends ShadowContentProvider {
    }

    public static class P80 extends ShadowContentProvider {
    }

    public static class P81 extends ShadowContentProvider {
    }

    public static class P82 extends ShadowContentProvider {
    }

    public static class P83 extends ShadowContentProvider {
    }

    public static class P84 extends ShadowContentProvider {
    }

    public static class P85 extends ShadowContentProvider {
    }

    public static class P86 extends ShadowContentProvider {
    }

    public static class P87 extends ShadowContentProvider {
    }

    public static class P88 extends ShadowContentProvider {
    }

    public static class P89 extends ShadowContentProvider {
    }

    public static class P90 extends ShadowContentProvider {
    }

    public static class P91 extends ShadowContentProvider {
    }

    public static class P92 extends ShadowContentProvider {
    }

    public static class P93 extends ShadowContentProvider {
    }

    public static class P94 extends ShadowContentProvider {
    }

    public static class P95 extends ShadowContentProvider {
    }

    public static class P96 extends ShadowContentProvider {
    }

    public static class P97 extends ShadowContentProvider {
    }

    public static class P98 extends ShadowContentProvider {
    }

    public static class P99 extends ShadowContentProvider {
    }


}
