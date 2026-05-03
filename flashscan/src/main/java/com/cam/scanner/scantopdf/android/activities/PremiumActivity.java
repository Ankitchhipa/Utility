package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.DescriptionAdapter;
import com.cam.scanner.scantopdf.android.adapters.FeatureAdapter;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.models.DescriptionModel;
import com.cam.scanner.scantopdf.android.models.FeatureModel;
import com.cam.scanner.scantopdf.android.rest.OrderToApi;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.rest.model.DummyPurchaseModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.DummyPurchase;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.SubscribeToTopic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PremiumActivity extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private static final String TAG = PremiumActivity.class.getSimpleName();
    private BillingClient billingClient;
    //    private TextView tvPrice;
    private View incPb;
    private SkuDetails finalSkuDetails, finalSkuDetailsQuarterly;
    private SkuDetails finalSkuDetailsInApp;
    private PrefManager prefManager;
    private boolean restoreFound, restoreFoundWatermark;
    private boolean restoreFoundQuertely;
    private FlashScanUtil util;
    private Context context;
    private Button btBuyNow;
    private Button btBuyNowQuartely;
    private FrameLayout flNoInternet;
    private RelativeLayout flActivityPremium;
    private ProgressBar pbPice;
    private ProgressBar pbPiceQuartely;

    private List<FeatureModel> featureModelList;
    private FeatureAdapter featureAdapter;
    private List<DescriptionModel> descriptionModelList;
    private DescriptionAdapter descriptionAdapter;

    private RecyclerView featureRecycler;
    private RecyclerView descriptionRecycler;

    private ImageView ivClose;
    private ImageView ivCloseNoInternet;

    private DBHandler dbHandler;

    private Purchase finalPurchase;
    private DummyPurchaseModel finalDummyPurchaseModel;
    private long lastClickedTime = 0;
    private TextView tv_getAccess;
   /* private RadioButton rb_quarterly_plan, rb_yearly_plan;
    private FrameLayout fl_yearly_plan;
    private LinearLayout ll_quarterly_plan;
    private TextView tv_saving_percent;
    private int quarterly_plan_price = 299;
    private int yearly_plan_price = 599;*/

    private CardView cardQuarterly;
    private int whichRestore;
    private int whichBuyNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_netcheck);

        findIds();
        initObjects();

        if (util.isConnectingToInternet()) {
            flActivityPremium.setVisibility(View.VISIBLE);
            flNoInternet.setVisibility(View.GONE);
            if (Constants.IS_ORDER_REAL) {
                connectBillingService();
            } else {
                productOnUI();
                // productOnUIQuarterly();
            }
        } else {
            flActivityPremium.setVisibility(View.GONE);
            flNoInternet.setVisibility(View.VISIBLE);
        }
        // caluclateSavePercent();
        setFeatureList();
        setDescriptionList();

        clickListeners();
    }

  /*  private void caluclateSavePercent() {
        int annualPriceAccToQuarter = quarterly_plan_price * 4;
        int priceDifference = annualPriceAccToQuarter - yearly_plan_price;
        if(priceDifference>0){
           double savePercent = FlashScanUtil.roundUpto2Decimal((priceDifference* 100)/annualPriceAccToQuarter);
            tv_saving_percent.setVisibility(View.VISIBLE);
            tv_saving_percent.setText("Save "+ savePercent + "%");
        }
        else
            tv_saving_percent.setVisibility(View.GONE);

    }*/

    private void loadPriceProgress() {
        pbPice.setVisibility(View.VISIBLE);
        pbPiceQuartely.setVisibility(View.VISIBLE);
//        tvPrice.setVisibility(View.GONE);
//        btBuyNow.setVisibility(View.VISIBLE);
        btBuyNow.setEnabled(false);
        // btBuyNowQuartely.setEnabled(false);
    }

    private void hidePriceProgress() {
        pbPice.setVisibility(View.GONE);
        pbPiceQuartely.setVisibility(View.GONE);
//        tvPrice.setVisibility(View.VISIBLE);
//        btBuyNow.setVisibility(View.VISIBLE);
        btBuyNow.setEnabled(true);
        //   btBuyNowQuartely.setEnabled(true);
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

                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                    if (purchasesResult.getPurchasesList() != null) {
                        List<Purchase> purchases = purchasesResult.getPurchasesList();
                        if (purchases.size() > 0) {
                            Log.i(TAG, "subs purchase found in restore");
                            for (Purchase purchase : purchases) {
                                if (Constants.PRODUCT_ID_PREMIUM.equals(purchase.getSku())) {
                                    Log.i(TAG, "premium in restore");
                                    restoreFound = true;
                                    finalPurchase = purchase;
                                }

                               *//**//* if (Constants.PRODUCT_ID_PREMIUM_QUARTELY.equals(purchase.getSku())) {
                                    Log.i(TAG, "premium quarterly in restore");
                                    restoreFoundQuertely = true;
                                }*//**//*
                            }

                            *//**//* if (restoreFound *//**//**//**//*&& restoreFoundQuertely*//**//**//**//*) {
                                whichRestore = Constants.RESTORE_PREMIUM_BOTH;
                            } else*//**//*
                            if (restoreFound) {
                                whichRestore = Constants.RESTORE_PREMIUM_YEARLY;
                            } *//**//*else if (restoreFoundQuertely) {
                                whichRestore = Constants.RESTORE_PREMIUM_QUARTERLY;
                            } *//**//* else {
                                whichRestore = Constants.RESTORE_PREMIUM_NONE;
                            }
                        } else {
                            whichRestore = Constants.RESTORE_PREMIUM_NONE;
                        }

                    } else {
                        whichRestore = Constants.RESTORE_PREMIUM_NONE;
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

       /* billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
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
        // btBuyNowQuartely.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        ivCloseNoInternet.setOnClickListener(this);
//        ll_quarterly_plan.setOnClickListener(this);
//        fl_yearly_plan.setOnClickListener(this);
    }

    private void findIds() {
        btBuyNow = findViewById(R.id.btn_buy_now);
        btBuyNowQuartely = findViewById(R.id.btn_buy_now_quartely);
//        tvPrice = findViewById(R.id.tv_price);
        incPb = findViewById(R.id.progress_lay);
        flNoInternet = findViewById(R.id.fl_no_internet_lay);
        flActivityPremium = findViewById(R.id.fl_activity_premium);
        pbPice = findViewById(R.id.progress_bar_price);
        pbPiceQuartely = findViewById(R.id.progress_bar_price_quartely);

        featureRecycler = findViewById(R.id.featureRecycler);
        descriptionRecycler = findViewById(R.id.descriptionRecycler);
        tv_getAccess = findViewById(R.id.tv_getAccess);
        tv_getAccess.requestFocus();
        ivClose = findViewById(R.id.closeIcon);
        ivCloseNoInternet = findViewById(R.id.iv_close_no_internet);

        cardQuarterly = findViewById(R.id.card_quartely);

//        cardQuarterly.setVisibility(View.GONE);

//        ll_quarterly_plan = findViewById(R.id.ll_quarterly_plan);
//        fl_yearly_plan = findViewById(R.id.fl_yearly_plan);
//        rb_quarterly_plan = findViewById(R.id.rb_quarterly_plan);
//        rb_yearly_plan = findViewById(R.id.rb_yearly_plan);
//        tv_saving_percent = findViewById(R.id.tv_saving_percent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_buy_now) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            whichBuyNow = Constants.BUY_NOW_YEARLY;
            buyNowClicked();
                /*case R.id.btn_buy_now_quartely:
                if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                    return;
                }
                lastClickedTime = SystemClock.elapsedRealtime();
                whichBuyNow = Constants.BUY_NOW_QUARTERLY;
                buyNowClicked();
                break;*/
        } else if (id == R.id.closeIcon) {
            finish();
        } else if (id == R.id.iv_close_no_internet) {
            finish();
                /* case R.id.ll_quarterly_plan:
                rb_quarterly_plan.setChecked(true);
                rb_yearly_plan.setChecked(false);
                btBuyNow.setText(String.format(getString(R.string.buy_now_quarterly), "Rs.299"));
                break;

            case R.id.fl_yearly_plan:
                rb_quarterly_plan.setChecked(false);
                rb_yearly_plan.setChecked(true);
                btBuyNow.setText(String.format(getString(R.string.buy_now_yearly), "Rs.599"));
                break;*/
        }
    }

    private void buyNowClicked() {
        switch (whichRestore) {
            /*case Constants.RESTORE_PREMIUM_QUARTERLY:
                restorePaymentQuarterly();
                break;*/
            case Constants.RESTORE_PREMIUM_YEARLY:
                restorePayment();
                break;
           /* case Constants.RESTORE_PREMIUM_BOTH:
                restorePaymentBoth();
                break;*/
            default:
                makePayment(whichBuyNow);
                break;
        }

        /*if (restoreFound) {
            restorePayment();
        } else {
            makePayment();
        }*/
    }

    private void restorePayment() {
        Log.i(TAG, "all done from restore payment");
        Constants.YEARLY_PLAN_RESTORED = true;
        allDone();
    }

    private void restorePaymentQuarterly() {
        Log.i(TAG, "all done from restore payment quarterly");
        Constants.QUARTERLY_PLAN_RESTORED = true;
        //allDoneQuartely();
    }

    private void restorePaymentBoth() {
        Log.i(TAG, "all done from restore payment both - premium quarterly and yearly");
        allDoneBoth();
    }

    private void makePayment(int whichBuyNow) {
        if (Constants.IS_ORDER_REAL) {
            // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
            SkuDetails skuDetails = null;
           /* if(whichBuyNow == Constants.BUY_NOW_QUARTERLY){
                skuDetails = finalSkuDetailsQuarterly;

            }*/

            if (whichBuyNow == Constants.BUY_NOW_YEARLY) {
                skuDetails = finalSkuDetails;

            }
            if (skuDetails != null) {
                // prefManager.setQuarterlyPlanPrice(finalSkuDetailsQuarterly.getPrice());
                prefManager.setYearlyPlanPrice(finalSkuDetails.getPrice());

                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build();
                BillingResult responseCode = billingClient.launchBillingFlow(PremiumActivity.this, flowParams);
                Log.e(TAG, "responseCode: " + responseCode);

            } else {
                Toast.makeText(context, context.getResources().getString(R.string.google_play_billing), Toast.LENGTH_SHORT).show();
            }

        } else {
            DummyPurchaseModel dummyPurchaseModel = new DummyPurchase().getDummyPurchasePremiumYearly();
            handleDummyPurchase(dummyPurchaseModel);
        }
    }

    private void getInAppProduct() {
        loadPriceProgress();
        List<String> skuList = new ArrayList<>();
        skuList.add(Constants.PRODUCT_ID_PREMIUM);
        //skuList.add(Constants.PRODUCT_ID_PREMIUM_QUARTELY);
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
                                if (Constants.PRODUCT_ID_PREMIUM.equals(sku)) {
//                                    String premiumUpgradePrice = price;
                                    finalSkuDetails = skuDetails;
                                    productOnUI();
                                }

                               *//* if (Constants.PRODUCT_ID_PREMIUM_QUARTELY.equals(sku)) {
//                                    String premiumUpgradePrice = price;
                                    finalSkuDetailsQuarterly = skuDetails;
                                    productOnUIQuarterly();
                                }*//*
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

//            if(rb_yearly_plan.isChecked()) {
//            btBuyNow.setText(getString(R.string.pay_now) + " " + price);
            btBuyNow.setText(String.format(getString(R.string.buy_now_yearly), price));
            //   btBuyNowQuartely.setText(String.format(getString(R.string.buy_now_yearly), price));
           /* }
            else{
                btBuyNow.setText(String.format(getString(R.string.buy_now_quarterly), "299"));
            }*/
        }
    }

    private void productOnUIQuarterly() {

        String price;

        if (Constants.IS_ORDER_REAL) {
            price = finalSkuDetailsQuarterly.getPrice();
        } else {
            price = "999";
        }

        if (restoreFoundQuertely) {
            btBuyNowQuartely.setText(getString(R.string.restore_payment));
            btBuyNow.setVisibility(View.GONE);
            btBuyNowQuartely.setVisibility(View.VISIBLE);
        } else {
            btBuyNowQuartely.setText(String.format(getString(R.string.buy_now_quarterly), price));
            btBuyNow.setVisibility(View.VISIBLE);
            btBuyNowQuartely.setVisibility(View.VISIBLE);
        }
    }

    private void productOnUI() {

        String price;

        if (Constants.IS_ORDER_REAL && finalSkuDetails != null) {
            price = finalSkuDetails.getPrice();
        } else {
            price = "999";
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvPrice.setText(Html.fromHtml(String.format(getString(R.string.remove_banner_ad_txt), price), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvPrice.setText(Html.fromHtml(String.format(getString(R.string.remove_banner_ad_txt), price)));
        }*/
        if (restoreFound) {
            btBuyNow.setText(getString(R.string.restore_payment));
            btBuyNow.setVisibility(View.VISIBLE);
            // btBuyNowQuartely.setVisibility(View.GONE);
        } else {

//            if(rb_yearly_plan.isChecked()) {
//            btBuyNow.setText(getString(R.string.pay_now) + " " + price);
            btBuyNow.setText(String.format(getString(R.string.buy_now_yearly), price));
            btBuyNow.setVisibility(View.VISIBLE);
            //   btBuyNowQuartely.setVisibility(View.VISIBLE);
            /*}
            else{
                btBuyNow.setText(String.format(getString(R.string.buy_now_quarterly), "299"));
            }*/
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

        if (Constants.IS_ORDER_REAL) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                // Grant entitlement to the user.

                prefManager.setOrderIdPremiumYearly(purchase.getOrderId());

                // Acknowledge the purchase if it hasn't already been acknowledged.
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    // .setDeveloperPayload(purchase.getDeveloperPayload())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
                } else {
                    if (whichBuyNow == Constants.BUY_NOW_YEARLY) {
                        allDone();
                    }/* else if(whichBuyNow == Constants.BUY_NOW_QUARTERLY){
                        allDoneQuartely();
                    }*/
                }
                finalPurchase = purchase;
            }
        }
    }

    private void handleDummyPurchase(DummyPurchaseModel dummyPurchaseModel) {
        //Dummy purchase case
        finalDummyPurchaseModel = dummyPurchaseModel;
        allDone();
        prefManager.setOrderIdPremiumYearly(dummyPurchaseModel.getOrderId());
    }

    private void allDoneBoth() {
        Log.i(TAG, "allDone Quarterly");
        // prefManager.setPremiumQuarterly(true);
        prefManager.setPremiumYearly(true);
        prefManager.setPurchasedPlanName(whichBuyNow);

        topicSubscription();

        if (Constants.IS_OWN_API_IMPLEMENT) {
            incPb.setVisibility(View.VISIBLE);
            orderToApi();
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    private void allDoneQuartely() {
       /* Log.i(TAG, "allDone Quarterly");
        prefManager.setPremiumQuarterly(true);
        prefManager.setPurchasedPlanName(whichBuyNow);

        topicSubscription();

        if (Constants.IS_OWN_API_IMPLEMENT) {
            incPb.setVisibility(View.VISIBLE);
            orderToApi();
        } else {
            setResult(RESULT_OK);
        }
        finish();*/
    }

    private void allDone() {
        Log.i(TAG, "allDone");
        prefManager.setPremiumYearly(true);
        prefManager.setPurchasedPlanName(whichBuyNow);

        topicSubscription();

        if (Constants.IS_OWN_API_IMPLEMENT) {
            incPb.setVisibility(View.VISIBLE);
            orderToApi();
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    private void topicSubscription() {
        SubscribeToTopic subscribeToTopic = new SubscribeToTopic(context);

        subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE);
        subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.FREE_TEST);

        Log.i(TAG, "Unsubscribed from free  and free_test");

        if (prefManager.isOcrMonthly()) {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PLANS);
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.BOTH_PLANS_TEST);
            Log.i(TAG, "Subscribed to Both_plans and both_plans_test");

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.OCR_MONTHLY);
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.OCR_MONTHLY_TEST);
            Log.i(TAG, "Unsubscribed to ocr_monthly and ocr_monthly_test");
        } else {
            if (whichRestore == Constants.RESTORE_PREMIUM_YEARLY) {
                subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY);
                subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY_TEST);
                Log.i(TAG, "Subscribed to Premium_yearly and premium_yearly_test");
            }

            /*if(whichRestore == Constants.RESTORE_PREMIUM_QUARTERLY){
                subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.PREMIUM_QUARTERLY);
                subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.PREMIUM_QUARTERLY_TEST);
                Log.i(TAG, "Subscribed to Premium_quarterly and premium_quarterly_test");
            }

            if(whichRestore == Constants.RESTORE_PREMIUM_BOTH){
                subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY);
                subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY_TEST);
                subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.PREMIUM_QUARTERLY);
                subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.PREMIUM_QUARTERLY_TEST);
                Log.i(TAG, "Subscribed to both_premium and both_premium_test");
            }*/
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
            orderToApi.doOrder(finalPurchase, Constants.PLAN_PEMIUM_YEARLY);
        } else {
            orderToApi.doOrder(finalDummyPurchaseModel, Constants.PLAN_PEMIUM_YEARLY);
        }
    }

    private void openHomeActivity() {
        Intent intent = new Intent(PremiumActivity.this, HomeActivity.class);
        intent.putExtra(Constants.EXTRA_PLAN_PAID_SUCCESS, Constants.PLAN_PEMIUM_YEARLY);
        // set the new task and clear flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        Log.i(TAG, "onAcknowledgePurchaseResponse: " + billingResult);
        if (whichBuyNow == Constants.BUY_NOW_YEARLY) {
            allDone();
        } /*else if(whichBuyNow == Constants.BUY_NOW_QUARTERLY){
            allDoneQuartely();
        }*/
    }

    private void setFeatureList() {

        int freeOcrCount = dbHandler.getAllowedFreeOcr();
        int premiumYearlyOcrCount = dbHandler.getAllowedPremiumYearlyOcr();

        String freeOcrCountStr = String.valueOf(freeOcrCount);
        String premiumYearlyOcrCountStr = String.valueOf(premiumYearlyOcrCount);

        featureModelList = new ArrayList<>();
        String[] basicVal = getResources().getStringArray(R.array.basic_val);
        String[] featureVal = getResources().getStringArray(R.array.feature_val);
        String[] premiumVal = getResources().getStringArray(R.array.premium_val);
        TypedArray basicIcon = getResources().obtainTypedArray(R.array.basic_icon);
        TypedArray premiumIcon = getResources().obtainTypedArray(R.array.premium_icon);
        for (int i = 0; i < featureVal.length; i++) {
            FeatureModel featureModel = new FeatureModel();
            featureModel.setFeature(featureVal[i]);
            featureModel.setBasic_val(i == 0 ? freeOcrCountStr : basicVal[i]);
            featureModel.setBasic_icon(basicIcon.getResourceId(i, 0));
            featureModel.setPremium_val(i == 0 ? premiumYearlyOcrCountStr : "");
            featureModel.setPremium_icon(i == 0 ? 0 : premiumIcon.getResourceId(0, 0));
            featureModelList.add(featureModel);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        featureRecycler.setLayoutManager(layoutManager);
        featureAdapter = new FeatureAdapter(context, featureModelList);
        featureRecycler.setAdapter(featureAdapter);

    }

    private void setDescriptionList() {
        String[] title = getResources().getStringArray(R.array.description_title);
        String[] description = getResources().getStringArray(R.array.description_detail);
        TypedArray descIcon = getResources().obtainTypedArray(R.array.desc_icon);

        descriptionModelList = new ArrayList<>();

        for (int i = 0; i < title.length; i++) {
            DescriptionModel descriptionModel = new DescriptionModel();
            descriptionModel.setTitle(title[i]);
            descriptionModel.setDescription(description[i]);
            descriptionModel.setImage(descIcon.getResourceId(i, 0));
            descriptionModelList.add(descriptionModel);
        }
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        descriptionRecycler.setLayoutManager(layoutManager);
        descriptionAdapter = new DescriptionAdapter(context, descriptionModelList);
        descriptionRecycler.setAdapter(descriptionAdapter);

    }

}