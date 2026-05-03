package com.cam.scanner.scantopdf.android.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class AdFreeActivity extends AppCompatActivity implements PurchasesUpdatedListener, View.OnClickListener, AcknowledgePurchaseResponseListener {

    private static final String TAG = AdFreeActivity.class.getSimpleName();
    private BillingClient billingClient;
    private TextView tvPrice, tv_enjoy_ads_free, tv_ad_free_price;
    private Button btn_progress_lay;
    private View progress_lay;
    private SkuDetails finalSkuDetails;
    private PrefManager prefManager;
    private boolean restoreFound;
    private ImageView iv_close, iv_close_no_internet;
    private FrameLayout fl_activity_ad_free, fl_no_internet_lay;
    private FlashScanUtil util;
    private Context context;
    private LinearLayout btMakePayment;
    private ProgressBar progress_bar_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_ads);

        initObjects();

        findViewByIds();

        /*progress_lay.setVisibility(View.VISIBLE);*/
        if (util.isConnectingToInternet()) {
            fl_activity_ad_free.setVisibility(View.VISIBLE);
            fl_no_internet_lay.setVisibility(View.GONE);

            connectBillingService();
        } else {
            fl_activity_ad_free.setVisibility(View.GONE);
            fl_no_internet_lay.setVisibility(View.VISIBLE);
        }
        clickListeners();
    }

    private void initObjects() {
        context = this;
        prefManager = new PrefManager(context);
        util = new FlashScanUtil(context);
    }

    private void clickListeners() {
        btMakePayment.setOnClickListener(this);
        iv_close.setOnClickListener(this);
        iv_close_no_internet.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
    }

    private void findViewByIds() {
        tvPrice = findViewById(R.id.tv_price);
        btMakePayment = findViewById(R.id.bt_make_payment);
        progress_lay = findViewById(R.id.progress_lay);
        iv_close = findViewById(R.id.iv_close);
        iv_close_no_internet = findViewById(R.id.iv_close_no_internet);
        fl_no_internet_lay = findViewById(R.id.fl_no_internet_lay);
        fl_activity_ad_free = findViewById(R.id.fl_activity_ad_free);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        progress_bar_price = findViewById(R.id.progress_bar_price);
        tv_enjoy_ads_free = findViewById(R.id.tv_enjoy_ads_free);
        tv_enjoy_ads_free.setText(getString(R.string.enjoy_flash_scan_ads_free,getString(R.string.app_name)));
        tv_ad_free_price = findViewById(R.id.tv_ad_free_price);
        tv_ad_free_price.setText(getString(R.string.upgrade_ad_msg,getString(R.string.app_name)));
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

    private void loadPriceProgress() {
        progress_bar_price.setVisibility(View.VISIBLE);
        tvPrice.setVisibility(View.GONE);
        btMakePayment.setEnabled(false);
    }

    private void hidePriceProgress() {
        progress_bar_price.setVisibility(View.GONE);
        tvPrice.setVisibility(View.VISIBLE);
        btMakePayment.setEnabled(true);
    }

    private void connectBillingService() {
        /*loadPriceProgress();
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this).build();

        clearGooglePlayStoreBillingCacheIfPossible();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                hidePriceProgress();
               *//* if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    if (purchasesResult.getPurchasesList() != null) {
                        List<Purchase> purchases = purchasesResult.getPurchasesList();
                        if (purchases.size() > 0) {
                            Log.i(TAG, "purchase found in restore");
                            for (Purchase purchase : purchases) {
                                if (Constants.PRODUCT_ID_AD_FREE.equals(purchase.getSku())) {
                                    Log.i(TAG, "ad free in restore");
                                    restoreFound = true;
                                }
                            }
                        }
                    }

                    getInAppProduct();
                }*//*
            }

            @Override
            public void onBillingServiceDisconnected() {
                hidePriceProgress();
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });*/
    }


    private void getInAppProduct() {
        loadPriceProgress();
        List<String> skuList = new ArrayList<>();
        skuList.add(Constants.PRODUCT_ID_AD_FREE);
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
                                if (Constants.PRODUCT_ID_AD_FREE.equals(sku)) {
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

        String price = finalSkuDetails.getPrice();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvPrice.setText(Html.fromHtml(String.format(getString(R.string.remove_banner_ad_txt), price), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvPrice.setText(Html.fromHtml(String.format(getString(R.string.remove_banner_ad_txt), price)));
        }*/
        if (restoreFound) {
            tvPrice.setText(getString(R.string.restore_payment));
        } else {
            tvPrice.setText(getString(R.string.pay_now) + " " + price);
        }
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
        prefManager.setAppAdFree(true);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bt_make_payment) {
            if (restoreFound) {
                restorePayment();
            } else {
                makePayment();
            }
        } else if (id == R.id.iv_close || id == R.id.iv_close_no_internet) {
            finish();
        } else if (id == R.id.btn_progress_lay) {
        }
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
        BillingResult responseCode = billingClient.launchBillingFlow(AdFreeActivity.this, flowParams);
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        Log.i(TAG, "onAcknowledgePurchaseResponse: " + billingResult);
        allDone();
    }
}
