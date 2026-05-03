package com.cam.scanner.scantopdf.android.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

public class OcrMonthlyActivity extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private static final String TAG = OcrMonthlyActivity.class.getSimpleName();
    private BillingClient billingClient;
    private TextView tvPrice;
    private View incPb;
    private SkuDetails finalSkuDetails;
    private SkuDetails finalSkuDetailsInApp;
    private PrefManager prefManager;
    private boolean restoreFound, restoreFoundWatermark;
    private FlashScanUtil util;
    private Context context;
    private Button btBuyNow;
    private FrameLayout flActivityPremium, flNoInternet;
    private ProgressBar pbPice;
    private long lastClickedTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_monthly_netcheck);

        findIds();
        initObjects();

        if (util.isConnectingToInternet()) {
            flActivityPremium.setVisibility(View.VISIBLE);
            flNoInternet.setVisibility(View.GONE);

            connectBillingService();
        } else {
            flActivityPremium.setVisibility(View.GONE);
            flNoInternet.setVisibility(View.VISIBLE);
        }

        clickListeners();
    }

    private void loadPriceProgress() {
        pbPice.setVisibility(View.VISIBLE);
        tvPrice.setVisibility(View.GONE);
        btBuyNow.setEnabled(false);
    }

    private void hidePriceProgress() {
        pbPice.setVisibility(View.GONE);
        tvPrice.setVisibility(View.VISIBLE);
        btBuyNow.setEnabled(true);
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

                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                    if (purchasesResult.getPurchasesList() != null) {
                        List<Purchase> purchases = purchasesResult.getPurchasesList();
                        if (purchases.size() > 0) {
                            Log.i(TAG, "subs purchase found in restore");
                            for (Purchase purchase : purchases) {
                                if (Constants.PRODUCT_ID_OCR_MONTH.equals(purchase.getSku())) {
                                    Log.i(TAG, "premium in restore");
                                    restoreFound = true;
                                }
                            }
                        }
                    }

                    //For Watermark free check (commented bcoz instead of here, marking in prefManager on Splash
                    *//*Purchase.PurchasesResult purchasesResultInApp = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    if (purchasesResultInApp.getPurchasesList() != null) {
                        List<Purchase> purchasesInApp = purchasesResultInApp.getPurchasesList();
                        if (purchasesInApp.size() > 0) {
                            Log.i(TAG, "inapp purchase found in restore");
                            for (Purchase purchaseInApp : purchasesInApp) {
                                if (Constants.PRODUCT_ID_WATERMARK_FREE.equals(purchaseInApp.getSku())) {
                                    Log.i(TAG, "watermark free in restore");
                                    restoreFoundWatermark = true;
                                }
                            }
                        }
                    }*//*
                    ////

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

    private void initObjects() {
        context = this;
        prefManager = new PrefManager(context);
        util = new FlashScanUtil(context);
    }

    private void clickListeners() {
        btBuyNow.setOnClickListener(this);
    }

    private void findIds() {
        btBuyNow = findViewById(R.id.btn_buy_now);
        tvPrice = findViewById(R.id.tv_price);
        incPb = findViewById(R.id.progress_lay);
        flNoInternet = findViewById(R.id.fl_no_internet_lay);
        flActivityPremium = findViewById(R.id.fl_activity_premium);
        pbPice = findViewById(R.id.progress_bar_price);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_buy_now) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            buyNowClicked();
        }
    }

    private void buyNowClicked() {
        if (restoreFound) {
            restorePayment();
        } else {
            makePayment();
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
        BillingResult responseCode = billingClient.launchBillingFlow(OcrMonthlyActivity.this, flowParams);
    }

    private void getInAppProduct() {
        loadPriceProgress();
        List<String> skuList = new ArrayList<>();
        skuList.add(Constants.PRODUCT_ID_OCR_MONTH);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
       /* billingClient.querySkuDetailsAsync(params.build(),
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
                                if (Constants.PRODUCT_ID_OCR_MONTH.equals(sku)) {
//                                    String premiumUpgradePrice = price;
                                    finalSkuDetails = skuDetails;
                                    productOnUI();
                                }
                            }
                        }
                    }
                });*/
    }

    private void getInAppProductInApp() {
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
                                    finalSkuDetailsInApp = skuDetails;
                                    productOnUIForWatermark();
                                }
                            }
                        }
                    }
                });*/
    }

    private void productOnUIForWatermark() {
        String price = finalSkuDetailsInApp.getPrice();

        if (restoreFoundWatermark) {
            tvPrice.setText(getString(R.string.restore_payment));
        } else {
            tvPrice.setText(getString(R.string.pay_now) + " " + price);
        }
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

            prefManager.setOrderIdOcrMonthly(purchase.getOrderId());

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
        prefManager.setOcrMonthly(true);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        Log.i(TAG, "onAcknowledgePurchaseResponse: " + billingResult);
        allDone();
    }
}