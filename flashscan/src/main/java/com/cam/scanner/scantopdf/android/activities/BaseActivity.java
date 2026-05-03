package com.cam.scanner.scantopdf.android.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.ads.AdClosed;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.itl.commonres.appinterface.OnAdDismissInterface;
import com.itl.commonres.utils.CommonMethods;

public class BaseActivity extends AppCompatActivity implements OnAdDismissInterface {
    public static final int REQUEST_CODE_DRIVE_SIGN_IN = 1;
    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.isAppInBackground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Constants.isAppInBackground = true;
    }

    public void largeNativeAdSet(NativeAd nativeAd, FrameLayout customNativeViewSet) {
        if (nativeAd != null) {
            NativeAdView adView = (NativeAdView) this.getLayoutInflater().inflate(R.layout.ad_unified_large, null);
            populateLargeUnifiedNativeAdView(nativeAd, adView);
            customNativeViewSet.removeAllViews();
            customNativeViewSet.addView(adView);
            int padding = (int) this.getResources().getDimension(com.intuit.sdp.R.dimen._1sdp);
            customNativeViewSet.setPadding(padding, padding, padding, padding);
        }
    }

    public void smallNativeAdSet(NativeAd nativeAd, FrameLayout customNativeViewSet, boolean showArrow) {
        if (nativeAd != null) {
            NativeAdView adView = (NativeAdView) this.getLayoutInflater().inflate(R.layout.ad_unified_small, null);
            populateSmallUnifiedNativeAdView(nativeAd, adView, showArrow);
            customNativeViewSet.removeAllViews();
            customNativeViewSet.addView(adView);
            int padding = (int) this.getResources().getDimension(com.intuit.sdp.R.dimen._1sdp);
            customNativeViewSet.setPadding(padding, padding, padding, padding);
        }
    }

    public void smallDocNativeAdSet(NativeAd nativeAd, FrameLayout customNativeViewSet, boolean showArrow) {
        if (nativeAd != null) {
            NativeAdView adView = (NativeAdView) this.getLayoutInflater().inflate(R.layout.ad_unified_doc_item, null);
            populateSmallUnifiedNativeAdView(nativeAd, adView, showArrow);
            customNativeViewSet.removeAllViews();
            customNativeViewSet.addView(adView);
            int padding = (int) this.getResources().getDimension(com.intuit.sdp.R.dimen._1sdp);
            customNativeViewSet.setPadding(padding, padding, padding, padding);
        }
    }

    public void nativeAdSetInList(NativeAd nativeAd, FrameLayout customNativeViewSet) {
        if (nativeAd != null && this != null) {
            NativeAdView adView = (NativeAdView) ((Activity) this).getLayoutInflater().inflate(R.layout.ad_unified_listview, null);
            populateUnifiedNativeAdViewInList(nativeAd, adView);
            customNativeViewSet.removeAllViews();
            customNativeViewSet.addView(adView);
            int padding = (int) this.getResources().getDimension(com.intuit.sdp.R.dimen._1sdp);
            customNativeViewSet.setPadding(padding, padding, padding, padding);
        }
    }

    public void populateUnifiedNativeAdViewInList(NativeAd nativeAd, NativeAdView adView) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);
        } else {
            adView.getBodyView().setVisibility(View.GONE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.GONE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.GONE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.GONE);
        } else {
            adView.getStoreView().setVisibility(View.GONE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null || nativeAd.getStarRating() < 3) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.GONE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.GONE);
        }
        adView.setNativeAd(nativeAd);
    }

    public void populateSmallUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView, boolean showArrow) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

       /* if(nativeAd.getHeadline().toString().length()>25){
            String subStr = nativeAd.getHeadline().substring(0,25);
            ((TextView) adView.getHeadlineView()).setText( subStr+"...");
        }
        else {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        }*/

        if (showArrow) {
            ((TextView) adView.getHeadlineView()).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_small, 0);
        } else {
            ((TextView) adView.getHeadlineView()).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);
        } else {
            adView.getBodyView().setVisibility(View.GONE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.GONE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.GONE);
        } else {
            adView.getStoreView().setVisibility(View.GONE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null || nativeAd.getStarRating() < 3) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.GONE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    public void populateLargeUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        //adView.setMediaView(adView.findViewById(R.id.ad_media));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);
        } else {
            adView.getBodyView().setVisibility(View.GONE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.GONE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.GONE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.GONE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null || nativeAd.getStarRating() < 3) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getStarRating() == null || nativeAd.getStarRating() < 3) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText("" + nativeAd.getStarRating().floatValue());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

       /* if (nativeAd.getMediaContent() == null) {
            adView.getMediaView().setVisibility(View.GONE);
        }else {
            adView.getMediaView().setVisibility(View.GONE);
        }*/
        adView.setNativeAd(nativeAd);
    }

    AdClosed adClosed;

    public void loadInterstitialAd(Context context, String adUnitId, AdClosed adClosed) {
        /*if (AppController.interstitialAd == null) {
            FlashScanUtil.newShowLoading(context, "");
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(context, adUnitId, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    Log.i(TAG, "onAdLoaded");
                    AppController.interstitialAd = interstitialAd;
                    FlashScanUtil.newHideLoading();
                    show(adClosed);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.i(TAG, "onAdFailedToLoad  " + loadAdError.getMessage() + " errorcode " + loadAdError.getCode());
                    adClosed.onAdLoadedOrFailed(false);
                    FlashScanUtil.newHideLoading();
                }
            });
        }
        else{
            FlashScanUtil.newHideLoading();
            show(adClosed);
        }*/
        showInterstitialAd(adUnitId,adClosed);

    }

    private void showInterstitialAd(String adUnitId, AdClosed adClosed) {
        CommonMethods.onAdDismissInterface = this;
        this.adClosed = adClosed;
        if (com.itl.commonres.utils.Constants.interstitialAd != null) {
            com.itl.commonres.utils.Constants.interstitialAd.show(this);
        } else {
            CommonMethods.loadInterstitialAd(this, true, adUnitId);
        }
    }

    public void show(AdClosed adClosed) {
        try {
            if (AppController.interstitialAd != null && !Constants.isAppInBackground) {
                Log.d(TAG, "show_called");
                AppController.interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d(TAG, "The ad was dismissed.");
                        adClosed.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        Log.d(TAG, "The ad failed to show.");
                        adClosed.onAdLoadedOrFailed(false);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        AppController.interstitialAd = null;
                        Log.d(TAG, "The ad was shown.");
                    }
                });

                AppController.interstitialAd.show(this);
            } else {
                Log.d(TAG, "else show.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLoaded() {
        if (AppController.interstitialAd != null)
            return true;
        else
            return false;
    }

    public void loadRewardedAd(Context context, String adUnitId, AdClosed adClosed) {
        if (AppController.rewardedAd == null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(context, adUnitId, adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                    Log.i(TAG, "RewardedonAdLoaded");
                    AppController.rewardedAd = rewardedAd;
                    adClosed.onAdLoadedOrFailed(true);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    AppController.rewardedAd = null;
                    Log.i(TAG, "RewrdedonAdFailedToLoad  " + loadAdError.getMessage() + " errorcode " + loadAdError.getCode());

                }
            });
        }
    }

    public void rewardedAdShow(AdClosed adClosed) {
        if (AppController.rewardedAd != null) {
            Log.d(TAG, "show_called");
            AppController.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when fullscreen content is dismissed.
                    Log.d(TAG, "The rewarded ad was dismissed.");
                    AppController.rewardedAd = null;
                    adClosed.onAdClosed();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when fullscreen content failed to show.
                    Log.d(TAG, "The rewarded ad failed to show.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    // Make sure to set your reference to null so you don't
                    // show it a second time.
                    Log.d(TAG, "The rewarded ad was shown.");
                }
            });

            //OcrActivity.goAhead = false;
            Activity activityContext = this;
            AppController.rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    //OcrActivity.goAhead = true;
                }
            });
        } else {
            Log.d(TAG, "rewarded else show.");
        }
    }

    @Override
    public void onAdDismiss() {
        adClosed.onAdClosed();
    }
}
