package com.cam.scanner.scantopdf.android.util;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.google.firebase.messaging.FirebaseMessaging;

public class SubscribeToTopic {
    private String TAG = SubscribeToTopic.class.getSimpleName();
    private Context mContext;
    private DBHandler dbHandler;

    public SubscribeToTopic(Context _ctx) {
        this.mContext = _ctx;
        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    public void doSubscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    public void doUnsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }

    public void doSubscribeToTestTopic(String topic) {
        String selfAndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.i(TAG, "self android_id: " + selfAndroidId);

        if (dbHandler.existDevicesAllowed(selfAndroidId)) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
    }

    public void doUnsubscribeFromTestTopic(String topic) {
        String selfAndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.i(TAG, "self android_id: " + selfAndroidId);

        if (dbHandler.existDevicesAllowed(selfAndroidId)) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        }
    }
}
