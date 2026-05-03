package com.cam.scanner.scantopdf.android.ads;

import android.content.Context;
import android.util.Log;

import com.cam.scanner.scantopdf.android.interfaces.AdManagerListener;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;

public class AdManager {
    /*private AdManager() {
    }

    private static volatile AdManager instance = null;
    private InterstitialAd interstitialAd;
    private static final String TAG = AdManager.class.getSimpleName();
    private boolean adLoaded;
    private AdManagerListener adManagerListener;

    public boolean isAdLoaded() {
        return adLoaded;
    }

    private void setAdLoaded(boolean adLoaded) {
        this.adLoaded = adLoaded;
    }

    public static AdManager getInstance() {
        if (instance == null) {
            synchronized (AdManager.class) {
                if (instance == null) {
                    instance = new AdManager();
                }
            }
        }
        return instance;
    }

    public void loadInterstitialAd(Context context, String adUnitId, AdManagerListener adManagerListener) {
        this.adManagerListener = adManagerListener;
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(adUnitId);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(adListener);
    }

    private AdListener adListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            Log.e(TAG, "onAdLoaded called");
            setAdLoaded(true);
            if (adManagerListener != null) {
                adManagerListener.onAdLoaded();
            }

        }

        @Override
        public void onAdFailedToLoad(int i) {
            super.onAdFailedToLoad(i);
            Log.e(TAG, "onAdFailedToLoad called");
            if (adManagerListener != null) {
                adManagerListener.onAdFailedToLoad();
            }
        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            Log.e(TAG, "onAdClosed called");
            if (adManagerListener != null) {
                adManagerListener.onAdClosed();
            }
        }
    };

    public void showAd(AdManagerListener adManagerListener) {
        this.adManagerListener = adManagerListener;
        interstitialAd.setAdListener(adListener);
        if (interstitialAd != null && interstitialAd.isLoaded() && !Constants.isAppInBackground) {
            interstitialAd.show();
            setAdLoaded(false);
        }

    }*/
}
