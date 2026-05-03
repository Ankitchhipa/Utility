package com.cam.scanner.scantopdf.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.HomeActivity;
import com.cam.scanner.scantopdf.android.activities.PremiumActivity;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.PushWrapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private String screenId = null;
    private String actionId = null;
    private String offer_url = null;
    private Context applicationContext;
    private String notificationClickedScreeName;
    private PushWrapper pushData;

    private Context context;
    private PrefManager prefManager;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(TAG, "onNewToken: " + s);

        if (context == null)
            context = this;

        if (prefManager == null)
            prefManager = new PrefManager(context);

        prefManager.setFirebaseDeviceToken(s);
    }

    private void loadFirebaseDataAndCheckRedirection(RemoteMessage remoteMessage) {
        Log.i(TAG, "inside loadFirebaseDataAndCheckRedirection()");
        Intent intent;
        if (remoteMessage.getData().containsKey("app_package_name")) {
            final String appPackageName = remoteMessage.getData().get("app_package_name"); // getPackageName()
            Log.i(TAG, "appPackageName: " + appPackageName);
            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            } catch (android.content.ActivityNotFoundException anfe) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            }
            int notificaionId = 1;
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, /*PendingIntent.FLAG_ONE_SHOT | */PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.BigTextStyle bigTextNotiStyle = null;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int color = ContextCompat.getColor(this, R.color.primary_grey_300);
            NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(com.itl.commonres.R.mipmap.ic_launcher)
                    .setContentTitle("" + remoteMessage.getData().get("title"))
                    .setContentText("" + remoteMessage.getData().get("desc"))
                    .setStyle(bigTextNotiStyle)
                    .setAutoCancel(true)
                    .setColor(color)
                    .setContentIntent(pIntent)
                    .setLights(Color.RED, 3000, 3000);
            notificationManager.notify(notificaionId, mBuilder.build());
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        loadFirebaseDataAndCheckRedirection(remoteMessage);

        context = getApplicationContext();

        if (prefManager == null)
            prefManager = new PrefManager(context);

        Log.i(TAG, "remoteMessage: " + remoteMessage);

        Log.i(TAG, "From: " + remoteMessage.getFrom());
        applicationContext = getBaseContext();
        // Check if message contains a notification payload.
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            Log.i(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            Log.i(TAG, "Message data payload: " + data);
            pushData = new PushWrapper(data);

            prefManager.setOfferUrlServer(pushData.offer_url);
            Log.i(TAG, "offer_url: " + pushData.offer_url);
            if (!TextUtils.isEmpty(pushData.offer_url) && pushData.offer_url.length() > 10) {
                Log.i(TAG, "Offer URL is available");
                sendNotificationWithOffer(pushData);
            } else {
                Log.i(TAG, "Offer URL is not available");
                // With out offer URL case perform other task with notification
                 sendNotification(pushData);
            }
        }
    }

    private void sendNotificationWithOffer(PushWrapper pushData) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), com.itl.commonres.R.mipmap.ic_launcher);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, goToScreensIntentWithOffer(pushData.plan_id), /*PendingIntent.FLAG_ONE_SHOT | */PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(pushData.title)
                .setContentText(pushData.body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(pushData.title)
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_stat_ic_notification);

        try {
            String picture_url = pushData.img_url;
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(pushData.body));
            }
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage());
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_id", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("channel description");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private Intent goToScreensIntentWithOffer(String planIdStr) {
        Log.e(TAG, "=======     goToScreensIntentWithOffer");
        Intent defaultIntent;

        defaultIntent = new Intent(applicationContext, HomeActivity.class);

        /*int planId = 0;
        if(planIdStr != null){
            try {
                planId = Integer.parseInt(planIdStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if(planId == Constants.PLAN_PEMIUM_YEARLY){
            defaultIntent = new Intent(applicationContext, PremiumActivity.class);
        } else if(planId == Constants.PLAN_OCR_MONTHLY){
            defaultIntent = new Intent(applicationContext, OcrPlanDialog.class);
        } else {
            defaultIntent = new Intent(applicationContext, HomeActivity.class);
        }*/

        defaultIntent.putExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF, planIdStr);
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACKOFFER, true);
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACK, true);
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return defaultIntent;
    }

    private void sendNotification(PushWrapper pushData) {
        Log.e(TAG + " SendTo ", "sendNotification");
        Bitmap icon = BitmapFactory.decodeResource(getResources(), com.itl.commonres.R.mipmap.ic_launcher);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, goToScreensIntent(pushData.plan_id), /*PendingIntent.FLAG_ONE_SHOT | */PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(pushData.title)
                .setContentText(pushData.body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(pushData.title)
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_stat_ic_notification);

        try {
            String picture_url = pushData.img_url;
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(pushData.body));
            }
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage());
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_id", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("channel description");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private Intent goToScreensIntent(String planIdStr) {
        Log.e(TAG, "=======     goToScreensIntentWithOffer");
        Intent defaultIntent;
        int planId = 0;
        if(planIdStr != null){
            try {
                planId = Integer.parseInt(planIdStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if(planId == Constants.PLAN_PEMIUM_YEARLY){
            defaultIntent = new Intent(applicationContext, PremiumActivity.class);
        } else if(planId == Constants.PLAN_OCR_MONTHLY){
            defaultIntent = new Intent(applicationContext, OcrPlanDialog.class);
        } else {
            defaultIntent = new Intent(applicationContext, HomeActivity.class);
        }
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACKOFFER, true);
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACK, true);
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return defaultIntent;
    }

}
