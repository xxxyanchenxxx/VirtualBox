package com.fun.vbox.helper.compat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.env.Constants;

public class NotificationChannelCompat {
    public static final String DAEMON_ID = Constants.NOTIFICATION_DAEMON_CHANNEL;
    public static final String DEFAULT_ID = Constants.NOTIFICATION_CHANNEL;

    public static boolean enable(){
        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            return VCore.get().getTargetSdkVersion() >= android.os.Build.VERSION_CODES.O;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void checkOrCreateChannel(Context context, String channelId, String name) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Compatibility of old versions");
                channel.setSound(null, null);
                channel.setShowBadge(false);
                try {
                    manager.createNotificationChannel(channel);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static Notification.Builder createBuilder(Context context, String channelId){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                 && VCore.get().getTargetSdkVersion() >= android.os.Build.VERSION_CODES.O) {
            return new Notification.Builder(context, channelId);
        }else{
            return new Notification.Builder(context);
        }
    }
}
