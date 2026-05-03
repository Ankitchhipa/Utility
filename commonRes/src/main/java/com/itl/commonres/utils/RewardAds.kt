package com.itl.commonres.utils

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.itl.commonres.BuildConfig
import com.itl.commonres.R
import com.itl.commonres.utils.CommonMethods.isConnectingToInternet


object RewardAds {
    const val TAG = "REWARD_ADS"
    fun loadAdShow(activity: Activity, intent: Intent, finish: Boolean) {
        if (isConnectingToInternet(activity)) {
            var goAhead = false
            val dialog = Dialog(activity)
            dialog.setContentView(R.layout.loading_ads)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(activity, BuildConfig.AD_UNIT_ID_REWARDED_AD, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    if(dialog.isShowing)
                        dismissWithTryCatch(dialog)
                    goToNext(activity, intent, finish)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            if(dialog.isShowing)
                                dismissWithTryCatch(dialog)
                            Log.d(TAG, "Ad was shown.")
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            Log.d(TAG, "Ad failed to show.")
                            if(dialog.isShowing)
                                dismissWithTryCatch(dialog)
                            goToNext(activity, intent, finish)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad was dismissed.")
                            if (goAhead)
                                goToNext(activity, intent, finish)
                        }
                    }
                    rewardedAd.show(activity) {
                        Log.e(TAG, "REWARD_CALLED")
                        goAhead = true
                    }
                }
            })

        } else {
            goToNext(activity, intent, finish)
        }
    }

    private fun dismissWithTryCatch(dialog: Dialog) {
        try {
            dialog.dismiss()
        } catch (e: IllegalArgumentException) {
            // Do nothing.
        } catch (e: Exception) {
            // Do nothing.
        } /*finally {
            dialog = null
        }*/
    }

    private fun goToNext(activity: Activity, intent: Intent, finish: Boolean) {
        activity.startActivity(intent)
        if (finish)
            activity.finish()
    }
}