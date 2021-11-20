package com.fun.vbox.client.hook.proxies.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.fun.vbox.client.VClient;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.interfaces.IInjector;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.helper.AvoidRecursive;
import com.fun.vbox.helper.compat.BuildCompat;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.remote.InstalledAppInfo;
import com.fun.vbox.remote.StubActivityRecord;

import java.util.List;

import mirror.vbox.app.ActivityManagerNative;
import mirror.vbox.app.ActivityThread;
import mirror.vbox.app.ActivityThreadQ;
import mirror.vbox.app.ClientTransactionHandler;
import mirror.vbox.app.IActivityManager;
import mirror.vbox.app.servertransaction.ClientTransaction;
import mirror.vbox.app.servertransaction.LaunchActivityItem;
import mirror.vbox.app.servertransaction.TopResumedActivityChangeItem;

/**
 *
 * @see Handler.Callback
 */
public class HCallbackStub implements Handler.Callback, IInjector {


    private static final int LAUNCH_ACTIVITY;
    private static final int EXECUTE_TRANSACTION;
    private static final int SCHEDULE_CRASH = ActivityThread.H.SCHEDULE_CRASH.get();

    static {
        LAUNCH_ACTIVITY = BuildCompat.isPie() ? -1 : ActivityThread.H.LAUNCH_ACTIVITY.get();
        EXECUTE_TRANSACTION = BuildCompat.isPie() ? ActivityThread.H.EXECUTE_TRANSACTION.get() : -1;
    }

    private static final String TAG = HCallbackStub.class.getSimpleName();
    private static final HCallbackStub sCallback = new HCallbackStub();

    private final AvoidRecursive mAvoidRecurisve = new AvoidRecursive();


    private Handler.Callback otherCallback;

    private HCallbackStub() {
    }

    public static HCallbackStub getDefault() {
        return sCallback;
    }

    private static Handler getH() {
        return ActivityThread.mH.get(VCore.mainThread());
    }

    private static Handler.Callback getHCallback() {
        try {
            Handler handler = getH();
            return mirror.vbox.os.Handler.mCallback.get(handler);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (mAvoidRecurisve.beginCall()) {
            try {
                if (LAUNCH_ACTIVITY == msg.what) {
                    if (!handleLaunchActivity(msg, msg.obj)) {
                        return true;
                    }
                } else if (BuildCompat.isPie() && EXECUTE_TRANSACTION == msg.what) {
                    if (!handleExecuteTransaction(msg)) {
                        return true;
                    }
                } else if (SCHEDULE_CRASH == msg.what) {
                    String crashReason = (String) msg.obj;
                    new RemoteException(crashReason).printStackTrace();
                    return true;
                }
                if (otherCallback != null) {
                    return otherCallback.handleMessage(msg);
                }
            } finally {
                mAvoidRecurisve.finishCall();
            }
        }
        return false;
    }

    private boolean handleExecuteTransaction(Message msg) {
        Object transaction = msg.obj;
        IBinder token = ClientTransaction.mActivityToken.get(transaction);

        List<Object> activityCallbacks = ClientTransaction.mActivityCallbacks.get(transaction);
        if (activityCallbacks == null || activityCallbacks.isEmpty()) {
            return true;
        }

        Object item = activityCallbacks.get(0);
        if (BuildCompat.isQ() && item.getClass() == TopResumedActivityChangeItem.TYPE) {
            Object activityClientRecord =
                    ActivityThread.mActivities.get(VCore.mainThread()).get(token);
            if (activityClientRecord != null && TopResumedActivityChangeItem.mOnTop.get(item) ==
                    ActivityThreadQ.ActivityClientRecord.isTopResumedActivity.get(activityClientRecord)) {
                return false;
            }
        }

        if (ClientTransactionHandler.getActivityClient.call(VCore.mainThread(), token) != null) {
            return true;
        }
        if (item.getClass() != LaunchActivityItem.TYPE) {
            return true;
        }

        return handleLaunchActivity(msg, item);
    }

    private boolean handleLaunchActivity(Message msg, Object r) {
        Intent stubIntent;
        if (BuildCompat.isPie()) {
            stubIntent = LaunchActivityItem.mIntent.get(r);
        } else {
            stubIntent = ActivityThread.ActivityClientRecord.intent.get(r);
        }
        StubActivityRecord saveInstance = new StubActivityRecord(stubIntent);
        if (saveInstance.intent == null) {
            return true;
        }
        Intent intent = saveInstance.intent;
        IBinder token;
        if (BuildCompat.isPie()) {
            token = ClientTransaction.mActivityToken.get(msg.obj);
        } else {
            token = ActivityThread.ActivityClientRecord.token.get(r);
        }
        ActivityInfo info = saveInstance.info;
        if (info == null) {
            return true;
        }
        if (VClient.get().getClientConfig() == null) {
            InstalledAppInfo installedAppInfo = VCore.get().getInstalledAppInfo(info.packageName, 0);
            if (installedAppInfo == null) {
                return true;
            }
            boolean restart = VActivityManager.get().processRestarted(info.packageName,
                    info.processName,
                    saveInstance.userId);
            if (restart) {
                getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
                return false;
            } else {
                return true;
            }
        }
        if (!VClient.get().isAppRunning()) {
            VClient.get().bindApplication(info.packageName, info.processName);
            getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
            return false;
        }
        int taskId = IActivityManager.getTaskForActivity.call(
                ActivityManagerNative.getDefault.call(),
                token,
                false
        );
        if (info.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            try {
                IActivityManager.setRequestedOrientation.call(ActivityManagerNative.getDefault.call(),
                        token, info.screenOrientation);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        VActivityManager.get().onActivityCreate(saveInstance.virtualToken, token, taskId);
        ClassLoader appClassLoader = VClient.get().getClassLoader(info.applicationInfo);
        intent.setExtrasClassLoader(appClassLoader);
        if (BuildCompat.isPie()) {
            LaunchActivityItem.mIntent.set(r, intent);
            LaunchActivityItem.mInfo.set(r, info);
        } else {
            ActivityThread.ActivityClientRecord.intent.set(r, intent);
            ActivityThread.ActivityClientRecord.activityInfo.set(r, info);
        }
        return true;
    }

    @Override
    public void inject() {
        otherCallback = getHCallback();
        mirror.vbox.os.Handler.mCallback.set(getH(), this);
    }

    @Override
    public boolean isEnvBad() {
        Handler.Callback callback = getHCallback();
        boolean envBad = callback != this;
        if (callback != null && envBad) {
            VLog.d(TAG, "HCallback has bad, other callback = " + callback);
        }
        return envBad;
    }

}
