package com.zb.vv.client.stub;

import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.IBinder;

import com.zb.vv.R;
import com.fun.vbox.helper.compat.NotificationChannelCompat;

public class HiddenForeNotification extends Service {
    private static final int ID = 2781;

    public static void bindForeground(Service service) {
        Builder builder = NotificationChannelCompat.createBuilder(service.getApplicationContext(),
                NotificationChannelCompat.DAEMON_ID);
        builder.setSmallIcon(android.R.drawable.ic_dialog_dialer);
        if (VERSION.SDK_INT > 24) {
            Intent nfIntent =
                    service.getPackageManager().getLaunchIntentForPackage(service.getPackageName());
            PendingIntent
                    pendingIntent = PendingIntent
                    .getActivity(service.getApplicationContext(), 0, nfIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentTitle(service.getString(R.string.keep_service_damon_noti_title_v24));
            builder.setContentText(service.getString(R.string.keep_service_damon_noti_text_v24));
            builder.setContentIntent(pendingIntent);
        } else {
            builder.setContentTitle(service.getString(R.string.keep_service_damon_noti_title));
            builder.setContentText(service.getString(R.string.keep_service_damon_noti_text));
            builder.setContentIntent(PendingIntent
                    .getService(service, 0, new Intent(service, HiddenForeNotification.class), 0));
        }
        builder.setSound(null);
        service.startForeground(ID, builder.getNotification());
        if (VERSION.SDK_INT <= 24) {
            service.startService(new Intent(service, HiddenForeNotification.class));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Builder builder = NotificationChannelCompat.createBuilder(getBaseContext(),
                    NotificationChannelCompat.DAEMON_ID);
            builder.setSmallIcon(android.R.drawable.ic_dialog_dialer);
            builder.setContentTitle(getString(R.string.keep_service_noti_title));
            builder.setContentText(getString(R.string.keep_service_noti_text));
            builder.setSound(null);
            startForeground(ID, builder.getNotification());
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}