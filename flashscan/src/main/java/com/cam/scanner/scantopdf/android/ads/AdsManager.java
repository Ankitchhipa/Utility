package com.cam.scanner.scantopdf.android.ads;

import android.util.Log;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.interfaces.AdManagerListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;

public class AdsManager {
/*
    private static volatile AdsManager INSTANCE = null;
    private InterstitialAd interstitialAdForExitApp;
    private AdManagerListener adManagerListener;
    private static final String TAG = AdsManager.class.getSimpleName();
    private boolean adLoadedForExitApp;

    public boolean isExitAdAlreadyLoaded() {
        return exitAdAlreadyLoaded;
    }

    private void setExitAdAlreadyLoaded(boolean exitAdAlreadyLoaded) {
        this.exitAdAlreadyLoaded = exitAdAlreadyLoaded;
    }

    private boolean exitAdAlreadyLoaded;

    public boolean isAdLoadedForExitApp() {
        return adLoadedForExitApp;
    }

    private void setAdLoadedForExitApp(boolean adLoadedForExitApp) {
        this.adLoadedForExitApp = adLoadedForExitApp;
    }


    private AdsManager() {

    }

    public static AdsManager getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (AdsManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AdsManager();
                }
            }
        }
        return INSTANCE;
    }

    public void loadInterstitialAdForExitApp(String adUnitIdForExitInterstitialAd) {
        interstitialAdForExitApp = new InterstitialAd(AppController.getINSTANCE());
        interstitialAdForExitApp.setAdUnitId(adUnitIdForExitInterstitialAd);
        interstitialAdForExitApp.loadAd(new AdRequest.Builder().build());
        interstitialAdForExitApp.setAdListener(adListener);
    }

    private AdListener adListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            Log.e(TAG, "onAdLoaded called");
            setAdLoadedForExitApp(true);
            setExitAdAlreadyLoaded(true);

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


    public void showInterstitialAdForExitApp(AdManagerListener adManagerListener) {
        this.adManagerListener = adManagerListener;
        interstitialAdForExitApp.setAdListener(adListener);
        if (interstitialAdForExitApp.isLoaded()) {
            interstitialAdForExitApp.show();
            setAdLoadedForExitApp(false);
        }

    }*/

}
