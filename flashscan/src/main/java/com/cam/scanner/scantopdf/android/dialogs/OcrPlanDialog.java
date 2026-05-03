package com.cam.scanner.scantopdf.android.dialogs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.cam.scanner.scantopdf.android.activities.HomeActivity;
import com.cam.scanner.scantopdf.android.activities.PremiumActivity;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.rest.OrderToApi;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.rest.model.DummyPurchaseModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.DummyPurchase;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.SubscribeToTopic;

import java.util.ArrayList;
import java.util.List;


public class OcrPlanDialog extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {
//    OcrPlanDialogBinding binding;

    private static final String TAG = OcrPlanDialog.class.getSimpleName();
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
    private FrameLayout flNoInternet;
    private RelativeLayout flActivityOcrMonthly;
    private ProgressBar pbPice, pbTvPrice;

    private ImageView closeIcon;
    private ImageView ivCloseNoInternet;

    private TextView tvOcrCount;
    private DBHandler dbHandler;

    private Purchase finalPurchase;
    private DummyPurchaseModel finalDummyPurchaseModel;
    private  String oldPurchaseId="";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ocr_monthly_netcheck);
//        binding = DataBindingUtil.setContentView(this, R.layout.ocr_plan_dialog);


        findIds();
        initObjects();

        displayText();

        oldPurchaseId = prefManager.getOrderIdOcrMonthly();

        if (util.isConnectingToInternet()) {
            flActivityOcrMonthly.setVisibility(View.VISIBLE);
            flNoInternet.setVisibility(View.GONE);
            if (Constants.IS_ORDER_REAL) {
                connectBillingService();
            } else {
                productOnUI();
            }
        } else {
            flActivityOcrMonthly.setVisibility(View.GONE);
            flNoInternet.setVisibility(View.VISIBLE);
        }

        clickListeners();

        /*closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/
    }

    private void displayText() {
        int ocrCount = dbHandler.getAllowedOcrMonthly();

        tvOcrCount.setText(String.valueOf(ocrCount));
    }

    private void loadPriceProgress() {
        pbPice.setVisibility(View.VISIBLE);
        pbTvPrice.setVisibility(View.VISIBLE);
        tvPrice.setVisibility(View.INVISIBLE);
        btBuyNow.setEnabled(false);
    }

    private void hidePriceProgress() {
        pbPice.setVisibility(View.GONE);
        pbTvPrice.setVisibility(View.GONE);
        tvPrice.setVisibility(View.VISIBLE);
        btBuyNow.setEnabled(true);
    }

    private void connectBillingService() {
        loadPriceProgress();
       /* billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this).build();

        clearGooglePlayStoreBillingCacheIfPossible();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                hidePriceProgress();
                *//*if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
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
                                    finalPurchase = purchase;
                                }
                            }
                        }
                    }

                    //For Watermark free check (commented bcoz instead of here, marking in prefManager on Splash
                    *//**//*Purchase.PurchasesResult purchasesResultInApp = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
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
                    }*//**//*
                    ////

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
        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    private void clickListeners() {
        btBuyNow.setOnClickListener(this);
        closeIcon.setOnClickListener(this);
        ivCloseNoInternet.setOnClickListener(this);
    }

    private void findIds() {
        btBuyNow = findViewById(R.id.btn_buy_now);
        tvPrice = findViewById(R.id.tv_price);
        incPb = findViewById(R.id.progress_lay);
        flNoInternet = findViewById(R.id.fl_no_internet_lay);
        flActivityOcrMonthly = findViewById(R.id.fl_activity_ocr_monthly);
        pbPice = findViewById(R.id.progress_bar_price);
        pbTvPrice = findViewById(R.id.progress_bar_tv_price);

        closeIcon = findViewById(R.id.closeIcon);
        ivCloseNoInternet = findViewById(R.id.iv_close_no_internet);

        tvOcrCount = findViewById(R.id.tv_ocr_count);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_buy_now) {
            buyNowClicked();
        } else if (id == R.id.closeIcon) {
            finish();
        } else if (id == R.id.iv_close_no_internet) {
            finish();
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
        if (Constants.IS_ORDER_REAL && finalSkuDetails!=null) {
            // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(finalSkuDetails)
                    .build();
            BillingResult responseCode = billingClient.launchBillingFlow(OcrPlanDialog.this, flowParams);
            Log.e(TAG, "responseCode: "+responseCode);

        } else {
            DummyPurchaseModel dummyPurchaseModel = new DummyPurchase().getDummyPurchaseOcrMonthly();
            handleDummyPurchase(dummyPurchaseModel);
        }
    }

    private void getInAppProduct() {
        loadPriceProgress();
        List<String> skuList = new ArrayList<>();
        skuList.add(Constants.PRODUCT_ID_OCR_MONTH);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
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
            btBuyNow.setText(getString(R.string.restore_payment));
        } else {
            btBuyNow.setText(String.format(getString(R.string.buy_now_for_499_per_month), price));
        }

        tvPrice.setText(price + " " + getString(R.string.per_month));
    }

    private void productOnUI() {

        String price;
        if (Constants.IS_ORDER_REAL && finalSkuDetails!=null) {
            price = finalSkuDetails.getPrice();
        } else {
            price = "499";
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvPrice.setText(Html.fromHtml(String.format(getString(R.string.remove_banner_ad_txt), price), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvPrice.setText(Html.fromHtml(String.format(getString(R.string.remove_banner_ad_txt), price)));
        }*/
        if (restoreFound) {
            btBuyNow.setText(getString(R.string.restore_payment));
        } else {
            btBuyNow.setText(String.format(getString(R.string.buy_now_for_499_per_month), price));
        }

        tvPrice.setText(price + " " + getString(R.string.per_month));
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
        if (Constants.IS_ORDER_REAL) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                // Grant entitlement to the user.

                prefManager.setOrderIdOcrMonthly(purchase.getOrderId());

                // Acknowledge the purchase if it hasn't already been acknowledged.
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    //.setDeveloperPayload(purchase.getDeveloperPayload())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
                } else {
                    allDone();
                }
                finalPurchase = purchase;
            }
        }
    }

    private void handleDummyPurchase(DummyPurchaseModel dummyPurchaseModel) {
        //Dummy purchase case
        finalDummyPurchaseModel = dummyPurchaseModel;
        allDone();
        prefManager.setOrderIdOcrMonthly(dummyPurchaseModel.getOrderId());
    }

    private void allDone() {
        Log.i(TAG, "allDone");
        prefManager.setOcrMonthly(true);
        ocrCountResetIfOrderIdDifferForOcrMonthly(oldPurchaseId);
        topicSubscription();

        if (Constants.IS_OWN_API_IMPLEMENT) {
            incPb.setVisibility(View.VISIBLE);
            orderToApi();
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    private void ocrCountResetIfOrderIdDifferForOcrMonthly(String orderIdOcrMonthly) {
        String existingOrderOcrMonthly = prefManager.getOrderIdOcrMonthly();

        if(orderIdOcrMonthly!=null && existingOrderOcrMonthly!=null) {
            if (!orderIdOcrMonthly.equalsIgnoreCase(existingOrderOcrMonthly)) {
                resetOcrAttempted();
            }
        }
    }
    private void resetOcrAttempted() {
        if (dbHandler.existOcrAttempted()) {
            dbHandler.updateOcrAttempt(0, Constants.PLAN_OCR_MONTHLY);
            Log.i(TAG, "reset ocr attempted for ocr monthly.");
        }
    }

    private void topicSubscription() {
        SubscribeToTopic subscribeToTopic = new SubscribeToTopic(context);

        subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE);
        subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.FREE_TEST);

        Log.i(TAG, "Unsubscribed from free  and free_test");

        if(prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/){
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PLANS);
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.BOTH_PLANS_TEST);
            Log.i(TAG, "Subscribed to Both_plans and both_plans_test");

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY);
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY_TEST);
            Log.i(TAG, "Unsubscribed to premium_yearly and premium_yearly_test");
        } else {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.OCR_MONTHLY);
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.OCR_MONTHLY_TEST);
            Log.i(TAG, "Subscribed to ocr_monthly and ocr_monthly_test");
        }

        prefManager.setUnsubscribeFromFree(true);
    }

    private void orderToApi() {
        OrderToApi orderToApi = new OrderToApi(context, new OnApiResult() {
            @Override
            public void onApiResponse() {
                Log.i(TAG, "onApiResponse");
                incPb.setVisibility(View.GONE);
                openHomeActivity();
            }

            @Override
            public void onApiFailure() {
                incPb.setVisibility(View.GONE);
                Log.i(TAG, "onApiFailure");
            }
        });
        if (Constants.IS_ORDER_REAL) {
            orderToApi.doOrder(finalPurchase, Constants.PLAN_OCR_MONTHLY);
        } else {
            orderToApi.doOrder(finalDummyPurchaseModel, Constants.PLAN_OCR_MONTHLY);
        }
    }

    private void openHomeActivity() {
        Intent intent = new Intent(OcrPlanDialog.this, HomeActivity.class);
        intent.putExtra(Constants.EXTRA_PLAN_PAID_SUCCESS, Constants.PLAN_OCR_MONTHLY);
        // set the new task and clear flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        Log.i(TAG, "onAcknowledgePurchaseResponse: " + billingResult);
        allDone();
    }
}


