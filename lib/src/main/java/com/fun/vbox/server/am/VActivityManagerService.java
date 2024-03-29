package com.fun.vbox.server.am;

import android.Manifest;
import android.app.ActivityManager;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;

import com.fun.vbox.client.IVClient;
import com.fun.vbox.client.NativeEngine;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.env.Constants;
import com.fun.vbox.client.env.SpecialComponentList;
import com.fun.vbox.client.ipc.ProviderCall;
import com.fun.vbox.client.ipc.VNotificationManager;
import com.fun.vbox.client.stub.StubManifest;
import com.fun.vbox.helper.PersistenceLayer;
import com.fun.vbox.helper.collection.ArrayMap;
import com.fun.vbox.helper.collection.SparseArray;
import com.fun.vbox.helper.compat.ActivityManagerCompat;
import com.fun.vbox.helper.compat.ApplicationThreadCompat;
import com.fun.vbox.helper.compat.BuildCompat;
import com.fun.vbox.helper.compat.BundleCompat;
import com.fun.vbox.helper.compat.PermissionCompat;
import com.fun.vbox.helper.utils.ComponentUtils;
import com.fun.vbox.helper.utils.Singleton;
import com.fun.vbox.helper.utils.VLog;
import com.fun.vbox.os.VBinder;
import com.fun.vbox.os.VEnvironment;
import com.fun.vbox.os.VUserHandle;
import com.fun.vbox.remote.AppTaskInfo;
import com.fun.vbox.remote.BadgerInfo;
import com.fun.vbox.remote.ClientConfig;
import com.fun.vbox.remote.IntentSenderData;
import com.fun.vbox.remote.VParceledListSlice;
import com.fun.vbox.server.bit64.V64BitHelper;
import com.fun.vbox.server.interfaces.IActivityManager;
import com.fun.vbox.server.pm.PackageCacheManager;
import com.fun.vbox.server.pm.PackageSetting;
import com.fun.vbox.server.pm.VAppManagerService;
import com.fun.vbox.server.pm.VPackageManagerService;
import com.fun.vbox.server.secondary.BinderDelegateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mirror.vbox.app.IServiceConnectionO;

/**
 * @author Lody
 */
public class VActivityManagerService extends IActivityManager.Stub {

    private static final Singleton<VActivityManagerService> sService = new Singleton<VActivityManagerService>() {
        @Override
        protected VActivityManagerService create() {
            return new VActivityManagerService();
        }
    };
    private static final String TAG = VActivityManagerService.class.getSimpleName();
    private final Object mProcessLock = new Object();
    private final List<ProcessRecord> mPidsSelfLocked = new ArrayList<>();
    private final ActivityStack mActivityStack = new ActivityStack(this);
    private final ProcessMap<ProcessRecord> mProcessNames = new ProcessMap<>();
    private final Map<IBinder, IntentSenderData> mIntentSenderMap = new HashMap<>();
    private NotificationManager nm = (NotificationManager) VCore.get().getContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
    private final Map<String, Boolean> sIdeMap = new HashMap<>();
    private final Map<String, Integer> mRestartProcessCount = new HashMap<>();
    private final Set<ServiceRecord> mHistory = new HashSet<>();
    private boolean mResult;

    private final HashMap<String, VAppLaunchConfig> mAppLaunchConfigs = new HashMap<>();

    private static class VAppLaunchConfig implements Parcelable {
        int monopoly;

        public void set(VAppLaunchConfig other) {
            this.monopoly = other.monopoly;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.monopoly);
        }

        VAppLaunchConfig(Parcel in) {
            this.monopoly = in.readInt();
        }

        VAppLaunchConfig() {

        }

        public static final Creator<VAppLaunchConfig> CREATOR = new Creator<VAppLaunchConfig>() {
            @Override
            public VAppLaunchConfig createFromParcel(Parcel source) {
                return new VAppLaunchConfig(source);
            }

            @Override
            public VAppLaunchConfig[] newArray(int size) {
                return new VAppLaunchConfig[size];
            }
        };
    }

    private final PersistenceLayer mPersistenceLayer = new PersistenceLayer(VEnvironment.getSettingRuleFile()) {
        @Override
        public int getCurrentVersion() {
            return 1;
        }

        @Override
        public void writePersistenceData(Parcel p) {
            p.writeMap(mAppLaunchConfigs);
        }

        @Override
        public void readPersistenceData(Parcel p, int version) {
            mAppLaunchConfigs.clear();
            HashMap hashMap = p.readHashMap(getClass().getClassLoader());
            mAppLaunchConfigs.putAll(hashMap);
        }
    };

    private VActivityManagerService() {
        mPersistenceLayer.read();
    }

    public static VActivityManagerService get() {
        return sService.get();
    }

    @Override
    public int isLaunchMonopoly(String pkg) {
        synchronized (mAppLaunchConfigs) {
            VAppLaunchConfig config = getOrCreateConfig(pkg);
            mPersistenceLayer.save();
            return config.monopoly;
        }
    }

    @Override
    public void setLaunchMonopoly(String pkg, int monopoly) {
        synchronized (mAppLaunchConfigs) {
            getOrCreateConfig(pkg).monopoly = monopoly;
            mPersistenceLayer.save();
        }
    }

    private VAppLaunchConfig getOrCreateConfig(String pkg) {
        VAppLaunchConfig config = mAppLaunchConfigs.get(pkg);
        if (config == null) {
            config = new VAppLaunchConfig();
            config.monopoly = 0;
            mAppLaunchConfigs.put(pkg, config);
        }
        return config;
    }

    @Override
    public int startActivity(Intent intent, ActivityInfo info, IBinder resultTo, Bundle options,
                             String resultWho, int requestCode, int userId, boolean appMultiTask) {
        synchronized (this) {
            try {
                if (isLaunchIntent(intent)) {
                    VAppLaunchConfig config = getOrCreateConfig(info.packageName);
                    if (config.monopoly == 1) {
                        killAppByPkgExcludeUserId(info.packageName, userId);
                    }
                }
            } catch (Throwable ignore) {
                //
            }
            return mActivityStack.startActivityLocked(userId, intent, info, resultTo, options,
                    resultWho, requestCode, VBinder.getCallingUid(), appMultiTask);
        }
    }

    private boolean isLaunchIntent(Intent intent) {
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && intent.getCategories() != null
                && intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean finishActivityAffinity(int userId, IBinder token) {
        synchronized (this) {
            return mActivityStack.finishActivityAffinity(userId, token);
        }
    }

    @Override
    public int startActivities(Intent[] intents, String[] resolvedTypes, IBinder token, Bundle options, int userId) {
        synchronized (this) {
            ActivityInfo[] infos = new ActivityInfo[intents.length];
            for (int i = 0; i < intents.length; i++) {
                ActivityInfo ai = VCore.get().resolveActivityInfo(intents[i], userId);
                if (ai == null) {
                    return ActivityManagerCompat.START_INTENT_NOT_RESOLVED;
                }
                infos[i] = ai;
            }
            return mActivityStack.startActivitiesLocked(userId, intents, infos, resolvedTypes, token, options, VBinder.getCallingUid());
        }
    }


    @Override
    public int getSystemPid() {
        return Process.myPid();
    }

    @Override
    public int getSystemUid() {
        return Process.myUid();
    }

    @Override
    public void onActivityCreated(IBinder record, IBinder token, int taskId) {
        int pid = Binder.getCallingPid();
        ProcessRecord targetApp;
        synchronized (mProcessLock) {
            targetApp = findProcessLocked(pid);
        }
        if (targetApp != null) {
            mActivityStack.onActivityCreated(targetApp, token, taskId, (ActivityRecord) record);
        }
        ActivityRecord activityRecord = (ActivityRecord) record;
        if (activityRecord != null) {
            VCore.get().getVirtualEngineCallback().onActivityCreated(activityRecord.component);
        }
    }

    @Override
    public void onActivityResumed(int userId, IBinder token) {
        mActivityStack.onActivityResumed(userId, token);
    }

    @Override
    public boolean onActivityDestroyed(int userId, IBinder token) {
        ActivityRecord r = mActivityStack.onActivityDestroyed(userId, token);
        if (r != null) {
            VCore.get().getVirtualEngineCallback().onActivityDestroyed(r.component);
        }
        return r != null;
    }

    @Override
    public void onActivityFinish(int userId, IBinder token) {
        mActivityStack.onActivityFinish(userId, token);
    }

    @Override
    public AppTaskInfo getTaskInfo(int taskId) {
        return mActivityStack.getTaskInfo(taskId);
    }

    @Override
    public String getPackageForToken(int userId, IBinder token) {
        return mActivityStack.getPackageForToken(userId, token);
    }

    @Override
    public ComponentName getActivityClassForToken(int userId, IBinder token) {
        return mActivityStack.getActivityClassForToken(userId, token);
    }


    private void processDied(ProcessRecord record) {
        synchronized (mHistory) {
            Iterator<ServiceRecord> iterator = mHistory.iterator();
            while (iterator.hasNext()) {
                ServiceRecord r = iterator.next();
                if (r.process != null && r.process.pid == record.pid) {
                    iterator.remove();
                }
            }
        }
        mActivityStack.processDied(record);

        VCore.get().getVirtualEngineCallback().onProcessDied(record.info.packageName,
                record.processName);
    }

    @Override
    public IBinder acquireProviderClient(int userId, ProviderInfo info) {
        String processName = info.processName;
        ProcessRecord r;
        synchronized (this) {
            r = startProcessIfNeedLocked(processName, userId, info.packageName, -1, VBinder.getCallingUid());
        }
        if (r != null) {
            try {
                return r.client.acquireProviderClient(info);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void addOrUpdateIntentSender(IntentSenderData sender, int userId) {
        if (sender == null || sender.token == null) {
            return;
        }

        synchronized (mIntentSenderMap) {
            IntentSenderData data = mIntentSenderMap.get(sender.token);
            if (data == null) {
                mIntentSenderMap.put(sender.token, sender);
            } else {
                data.replace(sender);
            }
        }
    }

    @Override
    public void removeIntentSender(IBinder token) {
        if (token != null) {
            synchronized (mIntentSenderMap) {
                mIntentSenderMap.remove(token);
            }
        }
    }

    @Override
    public IntentSenderData getIntentSender(IBinder token) {
        if (token != null) {
            synchronized (mIntentSenderMap) {
                return mIntentSenderMap.get(token);
            }
        }
        return null;
    }

    @Override
    public ComponentName getCallingActivity(int userId, IBinder token) {
        return mActivityStack.getCallingActivity(userId, token);
    }

    @Override
    public String getCallingPackage(int userId, IBinder token) {
        return mActivityStack.getCallingPackage(userId, token);
    }


    private void addRecord(ServiceRecord r) {
        synchronized (mHistory) {
            mHistory.add(r);
        }
    }

    private ServiceRecord findRecordLocked(int userId, ServiceInfo serviceInfo) {
        for (ServiceRecord r : mHistory) {
            // If service is not created, and bindService with the flag that is
            // not BIND_AUTO_CREATE, r.process is null
            if ((r.process == null || r.process.userId == userId)
                    && ComponentUtils.isSameComponent(serviceInfo, r.serviceInfo)) {
                return r;
            }
        }
        return null;
    }

    private ServiceRecord findRecordLocked(IServiceConnection connection) {
        for (ServiceRecord r : mHistory) {
            if (r.containConnection(connection)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public ComponentName startService(Intent service, String resolvedType, int userId) {
        synchronized (this) {
            return startServiceCommonLocked(service, true, userId);
        }
    }

    private ComponentName startServiceCommonLocked(Intent service,
                                                   boolean scheduleServiceArgs, int userId) {
        ServiceInfo serviceInfo = VCore.get().resolveServiceInfo(service, userId);
        if (serviceInfo == null) {
            return null;
        }
        ProcessRecord targetApp = startProcessIfNeedLocked(ComponentUtils.getProcessName(serviceInfo),
                userId,
                serviceInfo.packageName, -1, VBinder.getCallingUid());

        if (targetApp == null) {
            VLog.e(TAG, "Unable to start new process (" + ComponentUtils.toComponentName(serviceInfo) + ").");
            return null;
        }
        ServiceRecord r;
        synchronized (mHistory) {
            r = findRecordLocked(userId, serviceInfo);
        }
        if (r == null) {
            r = new ServiceRecord();
            r.startId = 0;
            r.activeSince = SystemClock.elapsedRealtime();
            r.process = targetApp;
            r.serviceInfo = serviceInfo;
            try {
                targetApp.client.scheduleCreateService(r, r.serviceInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            startShadowService(targetApp);
            addRecord(r);
        }
        r.lastActivityTime = SystemClock.uptimeMillis();
        if (scheduleServiceArgs) {
            r.startId++;
            try {
                targetApp.client.scheduleServiceArgs(r, r.startId, service);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return ComponentUtils.toComponentName(serviceInfo);
    }

    private void startShadowService(ProcessRecord app) {
        String serviceName = StubManifest.getStubServiceName(app.vpid);
        Intent intent = new Intent();
        intent.setClassName(StubManifest.getStubPackageName(app.is64bit), serviceName);
        try {
            VCore.get().getContext().bindService(intent, app.conn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int stopService(IBinder caller, Intent service, String resolvedType, int userId) {
        synchronized (this) {
            ServiceInfo serviceInfo = VCore.get().resolveServiceInfo(service, userId);
            if (serviceInfo == null) {
                return 0;
            }
            ServiceRecord r;
            synchronized (mHistory) {
                r = findRecordLocked(userId, serviceInfo);
            }
            if (r == null) {
                return 0;
            }
            stopServiceCommon(r, ComponentUtils.toComponentName(serviceInfo));
            return 1;
        }
    }

    @Override
    public boolean stopServiceToken(ComponentName className, IBinder token, int startId, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r != null && (r.startId == startId || startId == -1)) {
                stopServiceCommon(r, className);
                return true;
            }

            return false;
        }
    }

    private void stopServiceCommon(ServiceRecord r, ComponentName className) {
        synchronized (r.bindings) {
            for (ServiceRecord.IntentBindRecord bindRecord : r.bindings) {
                synchronized (bindRecord.connections) {
                    for (IServiceConnection connection : bindRecord.connections) {
                        // Report to all of the connections that the service is no longer
                        // available.
                        try {
                            if (BuildCompat.isOreo()) {
                                IServiceConnectionO.connected.call(connection, className, null, true);
                            } else {
                                connection.connected(className, null);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        r.process.client.scheduleUnbindService(r, bindRecord.intent);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            r.process.client.scheduleStopService(r);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        synchronized (mHistory) {
            mHistory.remove(r);
        }
    }

    @Override
    public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType,
                           IServiceConnection connection, int flags, int userId) {
        synchronized (this) {
            ServiceInfo serviceInfo = VCore.get().resolveServiceInfo(service, userId);
            if (serviceInfo == null) {
                return 0;
            }
            ServiceRecord r;
            synchronized (mHistory) {
                r = findRecordLocked(userId, serviceInfo);
            }
            boolean firstLaunch = r == null;
            if (firstLaunch) {
                if ((flags & Context.BIND_AUTO_CREATE) != 0) {
                    startServiceCommonLocked(service, false, userId);
                    synchronized (mHistory) {
                        r = findRecordLocked(userId, serviceInfo);
                    }
                }
            }
            if (r == null) {
                return 0;
            }
            synchronized (r.bindings) {
                ServiceRecord.IntentBindRecord boundRecord = r.peekBindingLocked(service);
                if (boundRecord != null && boundRecord.binder != null && boundRecord.binder.isBinderAlive()) {
                    if (boundRecord.doRebind) {
                        try {
                            r.process.client.scheduleBindService(r, service, true);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    ComponentName componentName = new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name);
                    connectServiceLocked(connection, componentName, boundRecord, false);
                } else {
                    try {
                        r.process.client.scheduleBindService(r, service, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                r.lastActivityTime = SystemClock.uptimeMillis();
                r.addToBoundIntentLocked(service, connection);
            }

            return 1;
        }
    }


    @Override
    public boolean unbindService(IServiceConnection connection, int userId) {
        synchronized (this) {
            ServiceRecord r;
            synchronized (mHistory) {
                r = findRecordLocked(connection);
            }
            if (r == null) {
                return false;
            }

            synchronized (r.bindings) {
                for (ServiceRecord.IntentBindRecord bindRecord : r.bindings) {
                    if (!bindRecord.containConnectionLocked(connection)) {
                        continue;
                    }
                    bindRecord.removeConnectionLocked(connection);
                    try {
                        r.process.client.scheduleUnbindService(r, bindRecord.intent);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (r.startId <= 0 && r.getConnectionCountLocked() <= 0) {
                try {
                    r.process.client.scheduleStopService(r);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    synchronized (mHistory) {
                        mHistory.remove(r);
                    }
                }
            }
            return true;
        }
    }

    @Override
    public void unbindFinished(IBinder token, Intent service, boolean doRebind, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r != null) {
                synchronized (r.bindings) {
                    ServiceRecord.IntentBindRecord boundRecord = r.peekBindingLocked(service);
                    if (boundRecord != null) {
                        boundRecord.doRebind = doRebind;
                    }
                }
            }
        }
    }


    @Override
    public boolean isVAServiceToken(IBinder token) {
        return token instanceof ServiceRecord;
    }


    @Override
    public void serviceDoneExecuting(IBinder token, int type, int startId, int res, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r == null) {
                return;
            }
            if (ActivityManagerCompat.SERVICE_DONE_EXECUTING_STOP == type) {
                synchronized (mHistory) {
                    mHistory.remove(r);
                }
            }
        }
    }

    @Override
    public IBinder peekService(Intent service, String resolvedType, int userId) {
        synchronized (this) {
            ServiceInfo serviceInfo = VCore.get().resolveServiceInfo(service, userId);
            if (serviceInfo == null) {
                return null;
            }
            ServiceRecord r;
            synchronized (mHistory) {
                r = findRecordLocked(userId, serviceInfo);
            }
            if (r != null) {
                synchronized (r.bindings) {
                    ServiceRecord.IntentBindRecord boundRecord = r.peekBindingLocked(service);
                    if (boundRecord != null) {
                        return boundRecord.binder;
                    }
                }
            }

            return null;
        }
    }

    @Override
    public void publishService(IBinder token, Intent intent, IBinder service, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r != null) {
                synchronized (r.bindings) {
                    ServiceRecord.IntentBindRecord boundRecord = r.peekBindingLocked(intent);
                    if (boundRecord != null) {
                        boundRecord.binder = service;
                        synchronized (boundRecord.connections) {
                            for (IServiceConnection conn : boundRecord.connections) {
                                ComponentName component = ComponentUtils.toComponentName(r.serviceInfo);
                                connectServiceLocked(conn, component, boundRecord, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void connectServiceLocked(IServiceConnection conn, ComponentName component, ServiceRecord.IntentBindRecord r, boolean dead) {
        try {
            boolean nativeTraceProcess = NativeEngine.nativeTraceProcessEx(Build.VERSION.SDK_INT);
            if (nativeTraceProcess) {
                BinderDelegateService delegateService =
                        new BinderDelegateService(component, r.binder);
                if (BuildCompat.isOreo()) {
                    IServiceConnectionO.connected.call(conn, component, delegateService, dead);
                } else {
                    conn.connected(component, delegateService);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VParceledListSlice<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags, int userId) {
        synchronized (mHistory) {
            List<ActivityManager.RunningServiceInfo> services = new ArrayList<>(mHistory.size());
            for (ServiceRecord r : mHistory) {
                if (r.process.userId != userId) {
                    continue;
                }
                ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
                info.uid = r.process.vuid;
                info.pid = r.process.pid;
                ProcessRecord processRecord;
                synchronized (mPidsSelfLocked) {
                    processRecord = findProcessLocked(r.process.pid);
                }
                if (processRecord != null) {
                    info.process = processRecord.processName;
                    info.clientPackage = processRecord.info.packageName;
                }
                info.activeSince = r.activeSince;
                info.lastActivityTime = r.lastActivityTime;
                info.clientCount = r.getClientCount();
                info.service = ComponentUtils.toComponentName(r.serviceInfo);
                info.started = r.startId > 0;
                services.add(info);
            }
            return new VParceledListSlice<>(services);
        }
    }

    @Override
    public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification,
                                     boolean removeNotification, int userId) {
        ServiceRecord r = (ServiceRecord) token;
        if (r != null) {
            if (id != 0) {
                if (notification == null) {
                    throw new IllegalArgumentException("null notification");
                }
                if (r.foregroundId != id) {
                    if (r.foregroundId != 0) {
                        cancelNotification(userId, r.foregroundId, r.serviceInfo.packageName);
                    }
                    r.foregroundId = id;
                }
                r.foregroundNoti = notification;
                postNotification(userId, id, r.serviceInfo.packageName, notification);
            } else {
                if (removeNotification) {
                    cancelNotification(userId, r.foregroundId, r.serviceInfo.packageName);
                    r.foregroundId = 0;
                    r.foregroundNoti = null;
                }
            }
        }
    }

    private void cancelNotification(int userId, int id, String pkg) {
        id = VNotificationManager.get().dealNotificationId(id, pkg, null, userId);
        String tag = VNotificationManager.get().dealNotificationTag(id, pkg, null, userId);
        nm.cancel(tag, id);
    }

    private void postNotification(int userId, int id, String pkg, Notification notification) {
        id = VNotificationManager.get().dealNotificationId(id, pkg, null, userId);
        String tag = VNotificationManager.get().dealNotificationTag(id, pkg, null, userId);
        VNotificationManager.get().addNotification(id, tag, pkg, userId);
        try {
            nm.notify(tag, id, notification);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean processRestarted2(String packageName, String processName, int userId) {
        int retry = 0;
        if (!mRestartProcessCount.containsKey(processName)) {
            mRestartProcessCount.put(processName, 0);
        } else {
            retry = mRestartProcessCount.get(processName);
        }
        if (retry > 10) {
            mRestartProcessCount.put(processName, ++retry);
            return false;
        }
        mRestartProcessCount.put(processName, ++retry);
        processRestarted(packageName, processName, userId);
        return true;
    }

    @Override
    public void processRestarted(String packageName, String processName, int userId) {
        int callingVUid = VBinder.getCallingUid();
        int callingPid = VBinder.getCallingPid();
        synchronized (this) {
            ProcessRecord app;
            synchronized (mProcessLock) {
                app = findProcessLocked(callingPid);
            }
            if (app == null) {
                String stubProcessName = getProcessName(callingPid);
                if (stubProcessName == null) {
                    return;
                }
                int vpid = parseVPid(stubProcessName);
                if (vpid != -1) {
                    startProcessIfNeedLocked(processName, userId, packageName, vpid, callingVUid);
                }
            }
        }
    }

    private int parseVPid(String stubProcessName) {
        String prefix;
        if (stubProcessName == null) {
            return -1;
        } else if (stubProcessName.startsWith(StubManifest.PACKAGE_NAME_64BIT)) {
            prefix = StubManifest.PACKAGE_NAME_64BIT + ":p";
        } else if (stubProcessName.startsWith(StubManifest.PACKAGE_NAME)) {
            prefix = VCore.get().getHostPkg() + ":p";
        } else {
            return -1;
        }
        if (stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return -1;
    }


    private String getProcessName(int pid) {
        for (ActivityManager.RunningAppProcessInfo info : VCore.get().getRunningAppProcessesEx()) {
            if (info.pid == pid) {
                return info.processName;
            }
        }
        return null;
    }


    private boolean attachClient(final ProcessRecord app, final IBinder clientBinder) {
        IVClient client = IVClient.Stub.asInterface(clientBinder);
        if (client == null) {
            app.kill();
            return false;
        }
        try {
            clientBinder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    clientBinder.unlinkToDeath(this, 0);
                    onProcessDied(app);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.client = client;
        try {
            app.appThread = ApplicationThreadCompat.asInterface(client.getAppThread());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void onProcessDied(ProcessRecord record) {
        synchronized (mProcessLock) {
            mProcessNames.remove(record.processName, record.vuid);
            mPidsSelfLocked.remove(record);
        }
        processDied(record);
    }

    @Override
    public int getFreeStubCount() {
        return StubManifest.STUB_COUNT - mPidsSelfLocked.size();
    }

    @Override
    public int checkPermission(boolean is64bit, String permission, int pid, int uid) {
        if (permission == null) {
            return PackageManager.PERMISSION_DENIED;
        }
        if (Manifest.permission.ACCOUNT_MANAGER.equals(permission)) {
            return PackageManager.PERMISSION_GRANTED;
        }
        if ("android.permission.INTERACT_ACROSS_USERS".equals(permission) || "android.permission.INTERACT_ACROSS_USERS_FULL".equals(permission)) {
            return PackageManager.PERMISSION_DENIED;
        }
        if (uid == 0) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return VPackageManagerService.get().checkUidPermission(is64bit, permission, uid);
    }

    @Override
    public ClientConfig initProcess(String packageName, String processName, int userId) {
        synchronized (this) {
            ProcessRecord r = startProcessIfNeedLocked(processName, userId, packageName, -1, VBinder.getCallingUid());
            if (r != null) {
                return r.getClientConfig();
            }
            return null;
        }
    }

    @Override
    public void appDoneExecuting(String packageName, int userId) {
        int pid = VBinder.getCallingPid();
        ProcessRecord r = findProcessLocked(pid);
        if (r != null) {
            r.pkgList.add(packageName);
        }
    }


    ProcessRecord startProcessIfNeedLocked(String processName, int userId, String packageName, int vpid, int callingUid) {
        runProcessGC();
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        ApplicationInfo info = VPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
        if (ps == null || info == null) {
            return null;
        }
        if (!ps.isLaunched(userId)) {
            sendFirstLaunchBroadcast(ps, userId);
            ps.setLaunched(userId, true);
            VAppManagerService.get().savePersistenceData();
        }
        int vuid = VUserHandle.getUid(userId, ps.appId);
        boolean is64bit = ps.isRunOn64BitProcess();
        synchronized (mProcessLock) {
            ProcessRecord app = null;
            if (vpid == -1) {
                app = mProcessNames.get(processName, vuid);
                if (app != null) {
                    if (app.initLock != null) {
                        app.initLock.block();
                    }
                    if (app.client != null) {
                        return app;
                    }
                }
                VLog.w(TAG, "start new process : " + processName);
                vpid = queryFreeStubProcess(is64bit);
            }
            if (vpid == -1) {
                VLog.e(TAG, "Unable to query free stub for : " + processName);
                return null;
            }
            if (app != null) {
                VLog.w(TAG, "remove invalid process record: " + app.processName);
                mProcessNames.remove(app.processName, app.vuid);
                mPidsSelfLocked.remove(app);
            }
            app = new ProcessRecord(info, processName, vuid, vpid, callingUid, is64bit);
            mProcessNames.put(app.processName, app.vuid, app);
            mPidsSelfLocked.add(app);
            if (initProcess(app)) {
                VCore.get().getVirtualEngineCallback().onProcessCreate(app.info.packageName,
                        app.processName);
                return app;
            } else {
                return null;
            }
        }
    }


    private void runProcessGC() {
        if (VActivityManagerService.get().getFreeStubCount() < 3) {
            // run GC
            killAllApps();
        }
    }

    private void sendFirstLaunchBroadcast(PackageSetting ps, int userId) {
        Intent intent = new Intent(Intent.ACTION_PACKAGE_FIRST_LAUNCH, Uri.fromParts("package", ps.packageName, null));
        intent.setPackage(ps.packageName);
        intent.putExtra(Intent.EXTRA_UID, VUserHandle.getUid(ps.appId, userId));
        intent.putExtra("android.intent.extra.user_handle", userId);
        sendBroadcastAsUser(intent, new VUserHandle(userId));
    }


    @Override
    public int getUidByPid(int pid) {
        if (pid == Process.myPid()) {
            return Constants.OUTSIDE_APP_UID;
        }
        boolean isClientPid = false;
        if (pid == 0) {
            pid = VBinder.getCallingPid();
            isClientPid = true;
        }
        synchronized (mProcessLock) {
            ProcessRecord r = findProcessLocked(pid);
            if (r != null) {
                if (isClientPid) {
                    return r.callingVUid;
                } else {
                    return r.vuid;
                }
            }
        }
        if (pid == Process.myPid()) {
            return Constants.OUTSIDE_APP_UID;
        }
        return Constants.OUTSIDE_APP_UID;
    }

    private void startRequestPermissions(boolean is64bit, String[] permissions,
                                         final ConditionVariable permissionLock) {

        PermissionCompat.startRequestPermissions(VCore.get().getContext(), is64bit, permissions, new PermissionCompat.CallBack() {
            @Override
            public boolean onResult(int requestCode, String[] permissions, int[] grantResults) {
                try {
                    mResult = PermissionCompat.isRequestGranted(grantResults);
                } finally {
                    permissionLock.open();
                }
                return mResult;
            }
        });
    }


    private boolean initProcess(ProcessRecord app) {
        try {
            requestPermissionIfNeed(app);
            Bundle extras = new Bundle();
            String value = NativeEngine.nativeGetValueEx(103);
            extras.putParcelable(value, app.getClientConfig());
            Bundle res = null;

            String value2 = NativeEngine.nativeGetValueEx(104);
            try {
                res = ProviderCall.callWithException(app.getProviderAuthority(), value2, null, extras);
            } catch (IllegalAccessException ex) {
                // 需要先启动进程对应的Activity，才能拉起进程
                if (app.is64bit) {
                    startStubActivityForProcessCreation(app.vpid, app.is64bit);
                    res = ProviderCall.callSafely(app.getProviderAuthority(), value2, null, extras);
                } else {
                    ex.printStackTrace();
                }
            }
            // Bundle res = ProviderCall.callSafely(app.getProviderAuthority(), "_VBOX_|_init_process_", null, extras);
            if (res == null) {
                return false;
            }
            app.pid = res.getInt("_VBOX_|_pid_");
            IBinder clientBinder = BundleCompat.getBinder(res, "_VBOX_|_client_");
            return attachClient(app, clientBinder);
        } finally {
            app.initLock.open();
            app.initLock = null;
        }
    }

    private void startStubActivityForProcessCreation(int vpid, boolean is64bit) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(
                StubManifest.getStubPackageName(is64bit),
                StubManifest.getStubActivityName(vpid)
        );
        intent.setFlags(268435456);
        intent.setComponent(componentName);
        VCore.get().getContext().startActivity(intent);
    }

    private void requestPermissionIfNeed(ProcessRecord app) {
        if (PermissionCompat.isCheckPermissionRequired(app.info.targetSdkVersion)) {
            String[] permissions = VPackageManagerService.get().getDangrousPermissions(app.info.packageName);
            if (!PermissionCompat.checkPermissions(permissions, app.is64bit)) {
                final ConditionVariable permissionLock = new ConditionVariable();
                startRequestPermissions(app.is64bit, permissions, permissionLock);
                permissionLock.block();
            }
        }
    }

    public int queryFreeStubProcess(boolean is64bit) {
        synchronized (mProcessLock) {
            for (int vpid = 0; vpid < StubManifest.STUB_COUNT; vpid++) {
                int N = mPidsSelfLocked.size();
                boolean using = false;
                while (N-- > 0) {
                    ProcessRecord r = mPidsSelfLocked.get(N);
                    if (r.vpid == vpid && r.is64bit == is64bit) {
                        using = true;
                        break;
                    }
                }
                if (using) {
                    continue;
                }
                return vpid;
            }
        }
        return -1;
    }

    @Override
    public boolean isAppProcess(String processName) {
        return parseVPid(processName) != -1;
    }

    @Override
    public boolean isAppPid(int pid) {
        synchronized (mProcessLock) {
            return findProcessLocked(pid) != null;
        }
    }

    @Override
    public String getAppProcessName(int pid) {
        synchronized (mProcessLock) {
            ProcessRecord r = findProcessLocked(pid);
            if (r != null) {
                return r.processName;
            }
        }
        return null;
    }

    @Override
    public List<String> getProcessPkgList(int pid) {
        synchronized (mProcessLock) {
            ProcessRecord r = findProcessLocked(pid);
            if (r != null) {
                return new ArrayList<>(r.pkgList);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void killAllApps() {
        synchronized (mProcessLock) {
            for (int i = 0; i < mPidsSelfLocked.size(); i++) {
                ProcessRecord r = mPidsSelfLocked.get(i);
                r.kill();
            }
        }
    }

    private void killAppByPkgExcludeUserId(final String pkg, int userId) {
        synchronized (mProcessLock) {
            ArrayMap<String, SparseArray<ProcessRecord>> map = mProcessNames.getMap();
            int N = map.size();
            while (N-- > 0) {
                SparseArray<ProcessRecord> uids = map.valueAt(N);
                if (uids != null) {
                    for (int i = 0; i < uids.size(); i++) {
                        ProcessRecord r = uids.valueAt(i);
                        if (userId != VUserHandle.USER_ALL) {
                            if (r.userId == userId) {
                                continue;
                            }
                        }
                        if (r.pkgList.contains(pkg)) {
                            r.kill();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void killAppByPkg(final String pkg, int userId) {
        synchronized (mProcessLock) {
            ArrayMap<String, SparseArray<ProcessRecord>> map = mProcessNames.getMap();
            int N = map.size();
            while (N-- > 0) {
                SparseArray<ProcessRecord> uids = map.valueAt(N);
                if (uids != null) {
                    for (int i = 0; i < uids.size(); i++) {
                        ProcessRecord r = uids.valueAt(i);
                        if (userId != VUserHandle.USER_ALL) {
                            if (r.userId != userId) {
                                continue;
                            }
                        }
                        if (r.pkgList.contains(pkg)) {
                            r.kill();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isAppLaunchRunning(String packageName, int userId) {
        TaskRecord taskRecord = mActivityStack.findTaskByPackageLocked(userId, packageName);
        return taskRecord != null;
    }

    @Override
    public boolean isAppRunning(String packageName, int userId, boolean foreground) {
        boolean running = false;
        synchronized (mProcessLock) {
            int N = mPidsSelfLocked.size();
            while (N-- > 0) {
                ProcessRecord r = mPidsSelfLocked.get(N);
                if (r.userId != userId) {
                    continue;
                }
                if (!r.info.packageName.equals(packageName)) {
                    continue;
                }
                if (foreground) {
                    if (!r.info.processName.equals(packageName)) {
                        continue;
                    }
                }
                try {
                    running = r.client.isAppRunning();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            return running;
        }
    }

    @Override
    public void killApplicationProcess(final String processName, int uid) {
        synchronized (mProcessLock) {
            ProcessRecord r = mProcessNames.get(processName, uid);
            if (r != null) {
                if (r.is64bit) {
                    V64BitHelper.forceStop64(r.pid);
                } else {
                    r.kill();
                }
            }
        }
    }

    @Override
    public void dump() {

    }

    @Override
    public String getInitialPackage(int pid) {
        synchronized (mProcessLock) {
            ProcessRecord r = findProcessLocked(pid);
            if (r != null) {
                return r.info.packageName;
            }
            return null;
        }
    }

    @Override
    public Bundle invokeVApp(String packageName, int userId, Bundle param) {
        String authority = "";
        synchronized (mProcessLock) {
            PackageSetting ps = PackageCacheManager.getSetting(packageName);
            int vuid = VUserHandle.getUid(userId, ps.appId);
            ProcessRecord record = findProcessLocked(packageName, vuid);
            if (record != null) {
                authority = record.getProviderAuthority();
            }
        }
        if (!TextUtils.isEmpty(authority)) {
            Bundle res = ProviderCall.callSafely(
                    authority,
                    "_VBOX_|_invoke_",
                    null,
                    param
            );
            return res;
        }
        return null;
    }

    /**
     * Should guard by {@link VActivityManagerService#mPidsSelfLocked}
     *
     * @param pid pid
     */
    public ProcessRecord findProcessLocked(int pid) {
        for (ProcessRecord r : mPidsSelfLocked) {
            if (r.pid == pid) {
                return r;
            }
        }
        return null;
    }

    /**
     * Should guard by {@link VActivityManagerService#mProcessNames}
     *
     * @param uid vuid
     */
    public ProcessRecord findProcessLocked(String processName, int uid) {
        return mProcessNames.get(processName, uid);
    }

    public int stopUser(int userHandle, IStopUserCallback.Stub stub) {
        synchronized (mProcessLock) {
            int N = mPidsSelfLocked.size();
            while (N-- > 0) {
                ProcessRecord r = mPidsSelfLocked.get(N);
                if (r.userId == userHandle) {
                    r.kill();
                }
            }
        }
        try {
            stub.userStopped(userHandle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void sendOrderedBroadcastAsUser(Intent intent, VUserHandle user, String receiverPermission,
                                           BroadcastReceiver resultReceiver, Handler scheduler, int initialCode,
                                           String initialData, Bundle initialExtras) {
        Context context = VCore.get().getContext();
        if (user != null) {
            intent.putExtra("_VBOX_|_user_id_", user.getIdentifier());
        }
        context.sendOrderedBroadcast(intent, null/* permission */, resultReceiver, scheduler, initialCode, initialData,
                initialExtras);
    }

    public void sendBroadcastAsUser(Intent intent, VUserHandle user) {
        SpecialComponentList.protectIntent(intent);
        Context context = VCore.get().getContext();
        if (user != null) {
            intent.putExtra("_VBOX_|_user_id_", user.getIdentifier());
        }
        context.sendBroadcast(intent);
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection connection, int flags, VUserHandle user) {
        service = new Intent(service);
        if (user != null) {
            service.putExtra("_VBOX_|_user_id_", user.getIdentifier());
        }
        return VCore.get().getContext().bindService(service, connection, flags);
    }

    public void sendBroadcastAsUser(Intent intent, VUserHandle user, String permission) {
        SpecialComponentList.protectIntent(intent);
        Context context = VCore.get().getContext();
        if (user != null) {
            intent.putExtra("_VBOX_|_user_id_", user.getIdentifier());
        }
        context.sendBroadcast(intent);
    }


    @Override
    public void notifyBadgerChange(BadgerInfo info) {
        Intent intent = new Intent(Constants.ACTION_BADGER_CHANGE);
        intent.putExtra("userId", info.userId);
        intent.putExtra("packageName", info.packageName);
        intent.putExtra("badgerCount", info.badgerCount);
        VCore.get().getContext().sendBroadcast(intent);
    }

    @Override
    public int getCallingUidByPid(int pid) {
        synchronized (mProcessLock) {
            ProcessRecord r = findProcessLocked(pid);
            if (r != null) {
                return r.getCallingVUid();
            }
        }
        return -1;
    }

    @Override
    public void setAppInactive(String packageName, boolean idle, int userId) {
        synchronized (sIdeMap) {
            sIdeMap.put(packageName + "@" + userId, idle);
        }
    }

    @Override
    public boolean isAppInactive(String packageName, int userId) {
        synchronized (sIdeMap) {
            Boolean idle = sIdeMap.get(packageName + "@" + userId);
            return idle != null && !idle;
        }
    }


    @Override
    public void handleDownloadCompleteIntent(Intent intent) {
        intent.setPackage(null);
        intent.setComponent(null);
        Intent send = ComponentUtils.redirectBroadcastIntent(intent, VUserHandle.USER_ALL);
        VCore.get().getContext().sendBroadcast(send);
    }


    public void beforeProcessKilled(ProcessRecord processRecord) {
        // EMPTY
    }
}