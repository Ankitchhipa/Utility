package com.cam.scanner.scantopdf.android.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.interfaces.AdManagerListener;
import com.cam.scanner.scantopdf.android.interfaces.RewardedAdShownListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
//import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class RewardedAdManager {/*

    private static final String TAG = RewardedAdManager.class.getSimpleName();
    private static volatile RewardedAdManager instance = null;
    private RewardedAd rewardedAd = null;
    private AdManagerListener adManagerListener;
    private RewardedAdShownListener rewardedAdShownListener;

    public boolean isAdLoaded() {
        boolean adLoaded;
        if (rewardedAd != null) {
            adLoaded = rewardedAd.isLoaded();
        } else {
            adLoaded = false;
        }
        return adLoaded;
    }

    *//*private void setAdLoaded(boolean adLoaded) {
        this.adLoaded = adLoaded;
    }

    private boolean adLoaded;*//*

    private RewardedAdManager() {

    }

    public static RewardedAdManager getInstance() {
        if (instance == null) {
            synchronized (RewardedAdManager.class) {
                if (instance == null) {
                    instance = new RewardedAdManager();
                }
            }
        }
        return instance;
    }

    public void loadAd(AdManagerListener adManagerListener) {
        this.adManagerListener = adManagerListener;
        rewardedAd = new RewardedAd(AppController.getINSTANCE(), BuildConfig.REWARD_AD_ID);
        rewardedAd.loadAd(new AdRequest.Builder().build(), rewardedAdLoadCallback);
    }

    private RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onRewardedAdLoaded() {
            super.onRewardedAdLoaded();
            Log.e(TAG, "onAdLoaded called");
            *//*setAdLoaded(true);*//*
            if (adManagerListener != null) {
                adManagerListener.onAdLoaded();
            }
        }

        @Override
        public void onRewardedAdFailedToLoad(int i) {
            super.onRewardedAdFailedToLoad(i);
            Log.e(TAG, "onRewardedAdFailedToLoad called");
            *//*setAdLoaded(false);*//*
            if (adManagerListener != null) {
                adManagerListener.onAdFailedToLoad();
            }
        }
    };

    public void showAd(Activity activity, RewardedAdShownListener rewardedAdShownListener) {
        this.rewardedAdShownListener = rewardedAdShownListener;
        if (rewardedAd != null && rewardedAd.isLoaded()) {
            rewardedAd.show(activity, rewardedAdCallback);
            *//*setAdLoaded(false);*//*
        }
    }

    private RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
        @Override
        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
            Log.e(TAG, "onUserEarnedReward called");
            if (rewardedAdShownListener != null) {
                rewardedAdShownListener.onUserEarnedReward();
            }
        }

        @Override
        public void onRewardedAdOpened() {
            super.onRewardedAdOpened();
            Log.e(TAG, "onRewardedAdOpened called");
            if (rewardedAdShownListener != null) {
                rewardedAdShownListener.onRewardedAdOpened();
            }
        }

        @Override
        public void onRewardedAdClosed() {
            super.onRewardedAdClosed();
            Log.e(TAG, "onRewardedAdClosed called");
            if (rewardedAdShownListener != null) {
                rewardedAdShownListener.onRewardedAdClosed();
            }
        }

        @Override
        public void onRewardedAdFailedToShow(int i) {
            super.onRewardedAdFailedToShow(i);
            Log.e(TAG, "onRewardedAdFailedToShow called");
            if (rewardedAdShownListener != null) {
                rewardedAdShownListener.onRewardedAdFailedToShow();
            }
        }
    };*/
}
