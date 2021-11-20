package com.ft.mapp;

import android.content.Context;

public class BasicConfig {

    private static final String TAG = "BasicConfig";

    private Context mContext;

    private long mActivityOnPauseTime = 0;

    private boolean goOutApp = false;

    private static BasicConfig mInstance = new BasicConfig();

    public static BasicConfig getInstance() {
        return mInstance;
    }

    public void setAppContext(Context context) {
        mContext = context;
    }

    public Context getAppContext() {
        return mContext;
    }

    public long getActivityOnPauseTime() {
        return mActivityOnPauseTime;
    }

    public void setActivityOnPauseTime(long activityOnPauseTime) {
        mActivityOnPauseTime = activityOnPauseTime;
    }

    public boolean isGoOutApp() {
        return goOutApp;
    }

    public void setGoOutApp(boolean goOutApp) {
        this.goOutApp = goOutApp;
    }
}
