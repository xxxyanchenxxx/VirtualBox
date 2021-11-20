package com.fun.vbox.helper.compat;

import android.annotation.TargetApi;
import android.app.job.JobWorkItem;
import android.content.Intent;
import android.os.Build;

import com.fun.vbox.helper.utils.ComponentUtils;

@TargetApi(Build.VERSION_CODES.O)
public class JobWorkItemCompat {

    public static JobWorkItem redirect(JobWorkItem item, String pkg) {
        if (item != null) {
            Intent target = mirror.vbox.app.job.JobWorkItem.getIntent.call(item);
            if (target.hasExtra("_VBOX_|_intent_")) {
                return item;
            }
            // TODO: is it work?
            Intent intent = ComponentUtils.redirectIntentSender(
                    ActivityManagerCompat.INTENT_SENDER_SERVICE, pkg, target);

            JobWorkItem workItem = (JobWorkItem) mirror.vbox.app.job.JobWorkItem.ctor.newInstance(intent);
            int wordId = mirror.vbox.app.job.JobWorkItem.mWorkId.get(item);
            mirror.vbox.app.job.JobWorkItem.mWorkId.set(workItem, wordId);

            Object obj = mirror.vbox.app.job.JobWorkItem.mGrants.get(item);
            mirror.vbox.app.job.JobWorkItem.mGrants.set(workItem, obj);

            int deliveryCount = mirror.vbox.app.job.JobWorkItem.mDeliveryCount.get(item);
            mirror.vbox.app.job.JobWorkItem.mDeliveryCount.set(workItem, deliveryCount);
            return workItem;
        }
        return null;
    }
}
