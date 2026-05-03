package com.utilify.boost.cleaner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.itl.commonres.BuildConfig;
import com.itl.commonres.utils.AppPref;
import com.utilify.boost.cleaner.activity.SplashActivity;


import java.util.Map;
import java.util.Objects;


import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = MyFirebaseMessagingService.class.getName();
    private Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public void onNewToken(@NonNull String fcmToken) {
        super.onNewToken(fcmToken);
        AppPref.setString(this, AppPref.FCM_TOKEN, fcmToken);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.e(TAG, "onMessageReceived " + "From: " + message.getFrom());

        try {
            if (!message.getData().isEmpty()) {
                handleNotificationData(message);
            }
        } catch (Throwable t) {
            Log.d("MYFCMLIST", "Error parsing FCM message", t);
        }
    }

    private void handleNotificationData(RemoteMessage remoteMessage) {
        try {
            if (!remoteMessage.getData().isEmpty()) {
                //AppController.getInstance().logger.logForBoth(TAG, "Data Payload: " + remoteMessage.getData());
                try {
                    Map<String, String> notificationData = remoteMessage.getData();

                    String sid = notificationData.get("sid");

                    Log.e(TAG, "Notification sid====: " + sid);

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, BuildConfig.APPLICATION_ID);

                    Intent actionIntent = new Intent(this, SplashActivity.class);
                    actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    int flag;
                    if (Build.VERSION.SDK_INT >= 31) {
                        flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
                    } else {
                        flag = PendingIntent.FLAG_CANCEL_CURRENT;
                    }

                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    mContext,
                                    0,
                                    actionIntent,
                                    flag
                            );

                    showNotification(mBuilder, com.itl.commonres.R.mipmap.ic_launcher, Objects.requireNonNull(remoteMessage.getNotification()).getTitle(), remoteMessage.getNotification().getBody(), resultPendingIntent);

                } catch (Throwable t) {
                    Log.d("MYFCMLIST", "Error parsing FCM message", t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNotification(NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(message);

        Notification notification;
        notification = mBuilder.setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setStyle(inboxStyle)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(BuildConfig.APPLICATION_ID, "NOTIFICATION_CHANNEL", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(123, notification);

    }
}