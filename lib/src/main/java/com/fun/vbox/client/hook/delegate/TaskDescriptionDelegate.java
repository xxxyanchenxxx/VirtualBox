package com.fun.vbox.client.hook.delegate;

import android.app.ActivityManager;

import com.fun.vbox.helper.Keep;

@Keep
public interface TaskDescriptionDelegate {
    public ActivityManager.TaskDescription getTaskDescription(ActivityManager.TaskDescription oldTaskDescription);
}
