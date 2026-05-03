package com.cam.scanner.scantopdf.android;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;

import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.analytics.FirebaseAnalytics;


public class AppController {

    private static AppController INSTANCE;
    private static final String TAG = AppController.class.getSimpleName();
    private static AppOpenManager appOpenManager;
    public DBHandler dbHandler;
    public static InterstitialAd interstitialAd;
    public static RewardedAd rewardedAd;
    public ContentResolver contentResolver;
    public static NativeAd nativeAdInSettings;
    public static NativeAd nativeAdOcr;
    public static NativeAd nativeAdQRCodeResult;
    public static NativeAd nativeAdExitAppDialog;
    public static NativeAd nativeAdThumbnail;
    public static NativeAd nativeAdDoc;
    static {
        System.loadLibrary("opencv_java4");
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(getINSTANCE().context);
        }
        return firebaseAnalytics;
    }

    private static FirebaseAnalytics firebaseAnalytics;

    public static AppController getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new AppController();
        }
        return INSTANCE;
    }

    public Context context;

    public void onCreate(Context context) {
        INSTANCE = this;

        this.context = context;
        dbHandler = new DBHandler(context);
        dbHandler.open();
        
        //FirebaseApp.initializeApp(this);
        //initMobileAds();
        //initFirebaseAnalytics();
        contentResolver = context.getContentResolver();
        appOpenManager = new AppOpenManager();
    }

    private void initFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    private void initMobileAds() {
        // MobileAds.initialize(INSTANCE);
        MobileAds.initialize(context, initializationStatus -> {
        });
    }
}
