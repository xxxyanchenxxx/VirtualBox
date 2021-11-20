package com.zb.vv.delegate;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.os.Build;

import com.fun.vbox.client.env.VirtualRuntime;
import com.fun.vbox.client.hook.delegate.TaskDescriptionDelegate;
import com.fun.vbox.os.VUserManager;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MyTaskDescriptionDelegate implements TaskDescriptionDelegate {
    @Override
    public ActivityManager.TaskDescription getTaskDescription(ActivityManager.TaskDescription oldTaskDescription) {
        if (oldTaskDescription == null) {
            return null;
        }
        int userId = VUserManager.get().getUserHandle();
        int index = userId + 1;
        String labelPrefix = "[F-" + index + "] ";
        if (VirtualRuntime.is64bit()) {
            labelPrefix = "[F64-" + index + "] ";
        }
        String oldLabel = oldTaskDescription.getLabel() != null ? oldTaskDescription.getLabel() : "";

        if (!oldLabel.startsWith(labelPrefix)) {
            // Is it really necessary?
            return new ActivityManager.TaskDescription(labelPrefix + oldTaskDescription.getLabel(), oldTaskDescription.getIcon(), oldTaskDescription.getPrimaryColor());
        } else {
            return oldTaskDescription;
        }
    }
}
