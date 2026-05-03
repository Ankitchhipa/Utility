package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.ads.RewardedAdManager;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.interfaces.AdManagerListener;
import com.cam.scanner.scantopdf.android.interfaces.RewardedAdShownListener;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.android.gms.ads.rewarded.RewardedAd;

import java.util.ArrayList;
import java.util.List;

public class WaterMarkRemoveActivity extends FragmentActivity implements View.OnClickListener, PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private Button btn_progress_lay;
    private LinearLayout btn_watch_video, btn_purchase;
    private RewardedAd rewardedAd;
    private Context context;
    private static final String TAG = WaterMarkRemoveActivity.class.getSimpleName();
    private ImageView iv_cancel, iv_cancel_no_internet;

    private BillingClient billingClient;
    private SkuDetails finalSkuDetails;
    private PrefManager prefManager;
    private boolean restoreFound;
    private View progress_lay;
    private TextView tv_watch_video;
//    private  TextView tv_price;
    private FrameLayout fl_remove_water_mark, fl_no_internet_lay;
    private FlashScanUtil util;
    private ProgressBar progress_bar_video;
//    private ProgressBar progress_bar_price;
    private boolean isAdLoadingComplete = true;
    private TextView lblTvPremium;
    private DBHandler dbHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_remove_water_mark_dialog);

        initObjects();
        findViewIds();
        displayText();

        /*progress_lay.setVisibility(View.VISIBLE);*/
        if (util.isConnectingToInternet()) {
            fl_no_internet_lay.setVisibility(View.GONE);
            fl_remove_water_mark.setVisibility(View.VISIBLE);
//            connectBillingService();
        } else {
            fl_no_internet_lay.setVisibility(View.VISIBLE);
            fl_remove_water_mark.setVisibility(View.GONE);
        }


        setClickListener();

        //Stop to load in advance
        /*if (util.isConnectingToInternet()) {
            loadRewardedAd();
        }*/

        hideVideoProgress();

    }

    private void displayText() {
        lblTvPremium.setText(getString(R.string.premium_plan));
    }

    /*private void loadRewardedAd() {
        if (!RewardedAdManager.getInstance().isAdLoaded()) {
            loadVideoProgress();
            RewardedAdManager.getInstance().loadAd(new AdManagerListener() {
                @Override
                public void onAdLoaded() {
                    Log.e(TAG, "onAdLoaded called");
                    hideVideoProgress();
                }

                @Override
                public void onAdFailedToLoad() {
                    Log.e(TAG, "onAdFailedToLoad called");
                    hideVideoProgress();
                }

                @Override
                public void onAdClosed() {
                    Log.e(TAG, "onAdClosed called");
                }
            });
        } else {
            Log.e(TAG, "Ad is already loaded");
            hideVideoProgress();
        }
    }*/

    private void initObjects() {
        context = this;
        prefManager = new PrefManager(context);
        util = new FlashScanUtil(context);
        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    private void setClickListener() {
        btn_watch_video.setOnClickListener(this);
        btn_purchase.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
        iv_cancel_no_internet.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
    }

    private void findViewIds() {
        btn_purchase = findViewById(R.id.btn_purchase);
        btn_watch_video = findViewById(R.id.btn_watch_video);
        iv_cancel = findViewById(R.id.iv_cancel);
        iv_cancel_no_internet = findViewById(R.id.iv_cancel_no_internet);
        progress_lay = findViewById(R.id.progress_lay);
        fl_no_internet_lay = findViewById(R.id.fl_no_internet_lay);
        fl_remove_water_mark = findViewById(R.id.fl_remove_water_mark);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        tv_watch_video = findViewById(R.id.tv_watch_video);
        progress_bar_video = findViewById(R.id.progress_bar_video);
        /*progress_bar_price = findViewById(R.id.progress_bar_price);
        tv_price = findViewById(R.id.tv_price);*/

        lblTvPremium = findViewById(R.id.lbl_tv_premium);
    }

    private void loadVideoProgress() {
        progress_bar_video.setVisibility(View.VISIBLE);
        tv_watch_video.setVisibility(View.GONE);
        btn_watch_video.setEnabled(false);
    }

    private void hideVideoProgress() {
        progress_bar_video.setVisibility(View.GONE);
        tv_watch_video.setVisibility(View.VISIBLE);
        btn_watch_video.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_watch_video) {/*if (RewardedAdManager.getInstance().isAdLoaded()) {
                    RewardedAdManager.getInstance().showAd(this, new RewardedAdShownListener() {
                        @Override
                        public void onUserEarnedReward() {
                            Log.e(TAG, "onUserEarnedReward called");
                            setResult(Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD);
                            finish();
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            Log.e(TAG, "onRewardedAdClosed called");
                            setResult(Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED);
                            finish();
                        }

                        @Override
                        public void onRewardedAdFailedToShow() {
                            Log.e(TAG, "onRewardedAdFailedToShow called");
                            setResult(Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED);
                            finish();
                        }

                        @Override
                        public void onRewardedAdOpened() {
                            Log.e(TAG, "onRewardedAdOpened called");
                        }
                    });
                } else {
                    Log.e(TAG, "Ad not loaded yet");
                    if (util.isConnectingToInternet()) {
                        //loadAndShowRewardedAd();
                    } else {
                        Toast.makeText(context, getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
                    }
                }*/
        } else if (id == R.id.btn_purchase) {/*if (restoreFound) {
                    restorePayment();
                } else {
                    makePayment();
                }*/
            askToBePremium();
            finish();
        } else if (id == R.id.iv_cancel) {
            setResult(Constants.WaterMarkActivityResultCodes.RESULT_IGNORE);
            finish();
        } else if (id == R.id.btn_progress_lay) {
        } else if (id == R.id.iv_cancel_no_internet) {
            onBackPressed();
        }
    }

    private void askToBePremium() {
        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();
        String existingEmailInDb = dbHandler.getEmail(deviceIdOfInstallTime);
        if (Constants.IS_OWN_API_IMPLEMENT && (existingEmailInDb == null || existingEmailInDb.isEmpty())) {
            openAskEmailActivity(Constants.PLAN_ACTIVITY_PREMIUM);
        } else {
            openPremiumActivity();
        }
    }

    private void openAskEmailActivity(int whichPlanActivity) {
        Intent intent = new Intent(WaterMarkRemoveActivity.this, AskEmailActivity.class);
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity);
        startActivity(intent);
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(WaterMarkRemoveActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    /*private void loadAndShowRewardedAd() {
        showProgressForAd();
        RewardedAdManager.getInstance().loadAd(new AdManagerListener() {
            @Override
            public void onAdLoaded() {
                Log.e(TAG, "onAdLoaded called");
                hideProgressForAd();
                RewardedAdManager.getInstance().showAd(WaterMarkRemoveActivity.this, new RewardedAdShownListener() {
                    @Override
                    public void onUserEarnedReward() {
                        Log.e(TAG, "onUserEarnedReward called");
                        setResult(Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD);
                        finish();
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        Log.e(TAG, "onRewardedAdClosed called");
                        setResult(Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED);
                        finish();
                    }

                    @Override
                    public void onRewardedAdFailedToShow() {
                        Log.e(TAG, "onRewardedAdFailedToShow called");
                        setResult(Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED);
                        finish();
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        Log.e(TAG, "onRewardedAdOpened called");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad() {
                Log.e(TAG, "onAdFailedToLoad called");
                hideProgressForAd();
            }

            @Override
            public void onAdClosed() {
                Log.e(TAG, "onAdClosed called");
            }
        });
    }*/

    private void hideProgressForAd() {
        progress_lay.setVisibility(View.GONE);
        isAdLoadingComplete = true;
    }

    private void showProgressForAd() {
        progress_lay.setVisibility(View.VISIBLE);
        isAdLoadingComplete = false;
    }


    private void connectBillingService() {
        loadPriceProgress();
        /*billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this).build();*/

        clearGooglePlayStoreBillingCacheIfPossible();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                hidePriceProgress();
                /*if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    if (purchasesResult.getPurchasesList() != null) {
                        List<Purchase> purchases = purchasesResult.getPurchasesList();
                        if (purchases.size() > 0) {
                            Log.i(TAG, "purchase found in restore");
                            for (Purchase purchase : purchases) {
                                if (Constants.PRODUCT_ID_WATERMARK_FREE.equals(purchase.getSku())) {
                                    Log.i(TAG, "ad free in restore");
                                    restoreFound = true;
                                }
                            }
                        }
                    }

                    getInAppProduct();
                }*/
            }

            @Override
            public void onBillingServiceDisconnected() {
                hidePriceProgress();
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void hidePriceProgress() {
        /*progress_bar_price.setVisibility(View.GONE);
        tv_price.setVisibility(View.VISIBLE);
        btn_purchase.setEnabled(true);*/
    }

    private void loadPriceProgress() {
        /*progress_bar_price.setVisibility(View.VISIBLE);
        tv_price.setVisibility(View.GONE);
        btn_purchase.setEnabled(false);*/
    }

    private void clearGooglePlayStoreBillingCacheIfPossible() {

        /*billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
                Log.i(TAG, "onPurchaseHistoryResponse");

            }
        });*/

       /* billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, (responseCode, purchasesList) -> {
        });*/

        /*billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, (responseCode, purchasesList) -> {
        });*/
    }

    private void getInAppProduct() {
        loadPriceProgress();
        List<String> skuList = new ArrayList<>();
        skuList.add(Constants.PRODUCT_ID_WATERMARK_FREE);
//        skuList.add("gas");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        /*billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        hidePriceProgress();
                        // Process the result.
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
//                                String price = skuDetails.getPrice();
                                if (Constants.PRODUCT_ID_WATERMARK_FREE.equals(sku)) {
//                                    String premiumUpgradePrice = price;
                                    finalSkuDetails = skuDetails;
                                    productOnUI();
                                }
                            }
                        }
                    }
                });*/
    }

    private void productOnUI() {
        /*progress_lay.setVisibility(View.GONE);*/
        String price = finalSkuDetails.getPrice();
        /*tv_remove_watermark_txt.setText(String.format(getString(R.string.remove_water_mark_permanent_txt), price));*/

        /*tv_price.setText(getString(R.string.pay_now) + " " + price);

        if (restoreFound) {
            tv_price.setText(getString(R.string.restore_payment));
        }*/
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && list != null) {
            Log.i(TAG, "BillingResponseCode.OK");
            for (Purchase purchase : list) {
                Log.i(TAG, "list has purchases");
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.i(TAG, "BillingResponseCode.USER_CANCELED");
        } else {
            // Handle any other error codes.
            Log.i(TAG, "on purchase updated else condition");
        }
    }

    private void handlePurchase(Purchase purchase) {
        Log.i(TAG, "Purchase handling: " + purchase);

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.

            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                               // .setDeveloperPayload(purchase.getDeveloperPayload())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
            } else {
                allDone();
            }
        }
    }

    private void allDone() {
        Log.i(TAG, "allDone");
        prefManager.setAppWatermarkFree(true);
        setResult(Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK);
        finish();
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        Log.i(TAG, "onAcknowledgePurchaseResponse: " + billingResult);
        allDone();
    }

    private void restorePayment() {
        Log.i(TAG, "all done from restore payment");
        allDone();
    }

    private void makePayment() {
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(finalSkuDetails)
                .build();
        BillingResult responseCode = billingClient.launchBillingFlow(WaterMarkRemoveActivity.this, flowParams);
    }

    @Override
    public void onBackPressed() {
        if (isAdLoadingComplete) {
            super.onBackPressed();
        }
    }
}
