package com.cam.scanner.scantopdf.android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.cam.scanner.scantopdf.android.activities.HomeActivity;
import com.cam.scanner.scantopdf.android.activities.ProductTourActivity;
import com.cam.scanner.scantopdf.android.activities.SplashActivity;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;
import static androidx.lifecycle.Lifecycle.Event.ON_STOP;

/**
 * Prefetches App Open Ads.
 */
public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String TAG = "AppOpenManager";
    public static boolean isShowingAd;
//    private final AppController appController;
    private AppOpenAd appOpenAd = null;
    private Activity currentActivity;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private long loadTime = 0;
    private PrefManager prefManager;

    /**
     * Constructor
     */
    public AppOpenManager() {
        //AppController.getINSTANCE().registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if (currentActivity != null) {
            Constants.isAppInBackground = false;
            prefManager = new PrefManager(currentActivity);
            if (currentActivity instanceof SplashActivity) {
                if (prefManager.showAppOpenAd() && !prefManager.isAppAdFree()) {  // check app open ad status from remote config
                    Log.d(TAG, "onStart");
                    showAdIfAvailable();
                }
            } else {
                Log.d(TAG, "onStart Not Splash");
            }
        }
    }


    @OnLifecycleEvent(ON_STOP)
    public void appInBackground() {
        Log.d(TAG, "ON_STOP");
        Constants.isAppInBackground = true;
        Log.d(TAG, "appInBackground"+Constants.isAppInBackground);
    }

    /**
     * Request an ad
     */
    public void fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        Log.d(TAG, "onAppOpenAd LOADED . > " + System.currentTimeMillis());
                        Constants.isSplashAdLoad = true;
                        AppOpenManager.this.appOpenAd = appOpenAd;
                        AppOpenManager.this.loadTime = (new Date()).getTime();
                        showAdIfAvailable();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        // Handle the error.
                        Log.d(TAG, "onAppOpenAd ID . > " + BuildConfig.APP_OPEN_AD_ID);
                        Log.d(TAG, "onAppOpenAd FAILED . > " + System.currentTimeMillis());
                        Log.d(TAG, "onAppOpenAdFailedToLoad called." + loadAdError.getMessage());
                        Constants.isSplashAdLoad = false;
                        isShowingAd = false;
                        startHome();
                    }

                };
        AdRequest request = getAdRequest();
        Log.d(TAG, "onAppOpenAd REQUESTED . > " + System.currentTimeMillis());
        AppOpenAd.load(
                AppController.getINSTANCE().context, BuildConfig.APP_OPEN_AD_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);

    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            Log.d(TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            // isShowingAd = false;
                            startHome();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.i(TAG, "Full screen ad failed");
                            isShowingAd = false;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                            Log.i(TAG, "Full screen ad showed");
                        }
                    };

            if (currentActivity != null && currentActivity instanceof SplashActivity  && !Constants.isAppInBackground) {
                Log.d(TAG, "show ad ");
                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAd.show(currentActivity);
            }

        } else {
            Log.d(TAG, "Can not show ad.");
            /*if (currentActivity != null && currentActivity instanceof SplashActivity)
                fetchAd();*/
        }
    }

    private void startHome() {

        if (prefManager == null) {
            prefManager = new PrefManager(currentActivity);
        }

        if (currentActivity != null && currentActivity instanceof SplashActivity && !Constants.isAppInBackground) {
            if (!prefManager.isFirstTimeLaunched()) {
                Intent intent = new Intent(currentActivity, ProductTourActivity.class);
                intent.putExtra(Constants.FROM_NAV, Constants.FROM_SPLASH);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent. FLAG_ACTIVITY_NEW_TASK);
                currentActivity.startActivity(intent);
                currentActivity.overridePendingTransition(0, 0);
                currentActivity.finish();
            } else {
                Intent intent = new Intent(currentActivity, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent. FLAG_ACTIVITY_NEW_TASK);
                currentActivity.startActivity(intent);
                currentActivity.overridePendingTransition(0, 0);
                currentActivity.finish();
            }
        }
    }


    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    /**
     * ActivityLifecycleCallback methods
     */
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        /*if (activity instanceof AdActivity) {
            return;
        }
        this.currentActivity = activity;*/
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // To not add adActivity to current activity
        if (activity instanceof AdActivity) {
            return;
        }
        this.currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // To not add adActivity to current activity
        if (activity instanceof AdActivity) {
            return;
        }
        this.currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        /*if (activity instanceof AdActivity) {
            return;
        }
        this.currentActivity = activity;*/

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        /*if (activity instanceof AdActivity) {
            return;
        }
        this.currentActivity = activity;*/
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
//        this.currentActivity = activity;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        this.currentActivity = null;
    }
}
