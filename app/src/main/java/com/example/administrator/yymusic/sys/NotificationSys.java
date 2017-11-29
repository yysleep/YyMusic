package com.example.administrator.yymusic.sys;

import android.app.NotificationChannel;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.util.LogHelper;

/**
 * Created by archermind on 17-2-14.
 *
 * @author yysleep
 */
public class NotificationSys {

    private NotificationSys() {

    }

    private static final String LOG_TAG = "NotificationSys";
    private static final String mChannelId = "yymusic";
    private static final int NOTIFICATION_ID = 1011;

    private Notification.Builder mBuilder;
    private Notification mNotification;
    private NotificationManager mMgr;

    private static NotificationSys instance;

    public static NotificationSys getInstance() {
        if (instance == null) {
            synchronized (NotificationSys.class) {
                if (instance == null) {
                    instance = new NotificationSys();
                }
            }
        }
        return instance;
    }

    public void onCreate(Service context) {
        if (mBuilder == null) {
            mBuilder = createBuiler(context.getApplicationContext())
                    .setContentTitle("yymusic")
                    .setSmallIcon(R.mipmap.icon_yymusic_launcher);
            mNotification = mBuilder.build();
            mMgr = createNotificationManager(context.getApplicationContext());
            context.startForeground(NOTIFICATION_ID, mNotification);
        }

    }

    public void notify(String content) {
        if (mBuilder != null && content != null) {
            mBuilder.setContentText(content);
            mNotification = mBuilder.build();
            mMgr.notify(NOTIFICATION_ID, mNotification);
        }
    }

    public void clear(){

    }

    public void onDestroy(Service context) {
        if (mBuilder != null)
            context.stopForeground(true);

        mBuilder = null;
        mNotification = null;
        mMgr = null;
        instance = null;
    }

    public static NotificationManager createNotificationManager(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (largerThanO80()) {
            CharSequence name = "noticication_channel_name";
            String description = "notification_description";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(mChannelId, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
        return notificationManager;
    }

    public static Notification.Builder createBuiler(Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        if (largerThanO80())
            builder.setChannelId(mChannelId);
        return builder;
    }

    public static boolean largerThanO80() {
        boolean bIsLargerThanO80 = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            bIsLargerThanO80 = true;
        LogHelper.d(LOG_TAG, "[largerThanO80] = " + bIsLargerThanO80);
        return bIsLargerThanO80;
    }

}
