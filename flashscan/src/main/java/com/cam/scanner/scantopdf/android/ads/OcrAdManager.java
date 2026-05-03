package com.cam.scanner.scantopdf.android.ads;

import android.util.Log;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.interfaces.AdManagerListener;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;

public class OcrAdManager {/*

    private static volatile OcrAdManager instance = null;
    private InterstitialAd interstitialAd;
    private static final String TAG = OcrAdManager.class.getSimpleName();
    private boolean adLoaded;
    private AdManagerListener adManagerListener;

    public boolean isAdLoaded() {
        return adLoaded;
    }

    private void setAdLoaded(boolean adLoaded) {
        this.adLoaded = adLoaded;
    }


    private OcrAdManager() {

    }

    public static OcrAdManager getInstance() {
        if (instance == null) {
            synchronized (OcrAdManager.class) {
                if (instance == null) {
                    instance = new OcrAdManager();
                }
            }
        }
        return instance;
    }

    public void loadAd(String adUnitId, AdManagerListener adManagerListener) {
        this.adManagerListener = adManagerListener;
        interstitialAd = new InterstitialAd(AppController.getINSTANCE());
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
    }
*/
}
