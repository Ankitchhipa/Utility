package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.DescriptionAdapter;
import com.cam.scanner.scantopdf.android.adapters.FeatureAdapter;
import com.cam.scanner.scantopdf.android.db.DBHandler;
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

import java.util.ArrayList;
import java.util.List;

public class PremiumActivity extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private static final String TAG = PremiumActivity.class.getSimpleName();
    private BillingClient billingClient;
    private View incPb;
    private ProductDetails finalProductDetails, finalProductDetailsMonthly;
    private PrefManager prefManager;
    private boolean restoreFound, restoreFoundMonthly;
    private FlashScanUtil util;
    private Context context;
    private Button btBuyNow;
    private Button btBuyNowMonthly;
    private FrameLayout flNoInternet;
    private RelativeLayout flActivityPremium;
    private ProgressBar pbPice;
    private ProgressBar pbPiceMonthly;

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
    private long lastClickedTime = 0;
    private TextView tv_getAccess;

    private int whichRestore;
    private int whichBuyNow;

    private void runOnMainThread(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            runOnUiThread(action);
        }
    }

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
                productOnUIMonthly();
            }
        } else {
            flActivityPremium.setVisibility(View.GONE);
            flNoInternet.setVisibility(View.VISIBLE);
        }
        setFeatureList();
        setDescriptionList();

        clickListeners();
    }

    private void loadPriceProgress() {
        runOnMainThread(() -> {
            pbPice.setVisibility(View.VISIBLE);
            pbPiceMonthly.setVisibility(View.VISIBLE);
            btBuyNow.setEnabled(false);
            btBuyNowMonthly.setEnabled(false);
        });
    }

    private void hidePriceProgress() {
        runOnMainThread(() -> {
            pbPice.setVisibility(View.GONE);
            pbPiceMonthly.setVisibility(View.GONE);
            btBuyNow.setEnabled(true);
            btBuyNowMonthly.setEnabled(true);
        });
    }

    private void connectBillingService() {
        loadPriceProgress();
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .setListener(this).build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                hidePriceProgress();
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    QueryPurchasesParams queryPurchasesParams = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build();

                    billingClient.queryPurchasesAsync(queryPurchasesParams, (billingResult1, purchases) -> {
                        if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                            if (!purchases.isEmpty()) {
                                for (Purchase purchase : purchases) {
                                    List<String> products = purchase.getProducts();
                                    if (products.contains(Constants.PRODUCT_ID_PREMIUM)) {
                                        restoreFound = true;
                                        finalPurchase = purchase;
                                    }

                                    if (products.contains(Constants.PRODUCT_ID_MONTHLY)) {
                                        restoreFoundMonthly = true;
                                    }
                                }

                                if (restoreFound && restoreFoundMonthly) {
                                    whichRestore = Constants.RESTORE_PREMIUM_BOTH;
                                } else if (restoreFound) {
                                    whichRestore = Constants.RESTORE_PREMIUM_YEARLY;
                                } else if (restoreFoundMonthly) {
                                    whichRestore = Constants.RESTORE_PREMIUM_MONTHLY;
                                } else {
                                    whichRestore = Constants.RESTORE_PREMIUM_NONE;
                                }
                            } else {
                                whichRestore = Constants.RESTORE_PREMIUM_NONE;
                            }
                        } else {
                            whichRestore = Constants.RESTORE_PREMIUM_NONE;
                        }
                        runOnMainThread(() -> getInAppProduct());
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                hidePriceProgress();
            }
        });
    }

    private void initObjects() {
        context = this;
        prefManager = new PrefManager(context);
        util = new FlashScanUtil(context);
        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    private void clickListeners() {
        btBuyNow.setOnClickListener(this);
        btBuyNowMonthly.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        ivCloseNoInternet.setOnClickListener(this);
    }

    private void findIds() {
        btBuyNow = findViewById(R.id.btn_buy_now);
        btBuyNowMonthly = findViewById(R.id.btn_buy_now_quartely);
        incPb = findViewById(R.id.progress_lay);
        flNoInternet = findViewById(R.id.fl_no_internet_lay);
        flActivityPremium = findViewById(R.id.fl_activity_premium);
        pbPice = findViewById(R.id.progress_bar_price);
        pbPiceMonthly = findViewById(R.id.progress_bar_price_quartely);

        featureRecycler = findViewById(R.id.featureRecycler);
        descriptionRecycler = findViewById(R.id.descriptionRecycler);
        tv_getAccess = findViewById(R.id.tv_getAccess);
        tv_getAccess.requestFocus();
        ivClose = findViewById(R.id.closeIcon);
        ivCloseNoInternet = findViewById(R.id.iv_close_no_internet);
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
        } else if (id == R.id.btn_buy_now_quartely) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            whichBuyNow = Constants.BUY_NOW_MONTHLY;
            buyNowClicked();
        } else if (id == R.id.closeIcon) {
            finish();
        } else if (id == R.id.iv_close_no_internet) {
            finish();
        }
    }

    private void buyNowClicked() {
        switch (whichRestore) {
            case Constants.RESTORE_PREMIUM_MONTHLY:
                restorePaymentMonthly();
                break;
            case Constants.RESTORE_PREMIUM_YEARLY:
                restorePayment();
                break;
            case Constants.RESTORE_PREMIUM_BOTH:
                restorePaymentBoth();
                break;
            default:
                makePayment(whichBuyNow);
                break;
        }
    }

    private void restorePayment() {
        Constants.YEARLY_PLAN_RESTORED = true;
        allDone();
    }

    private void restorePaymentMonthly() {
        allDoneMonthly();
    }

    private void restorePaymentBoth() {
        allDoneBoth();
    }

    private void makePayment(int whichBuyNow) {
        if (Constants.IS_ORDER_REAL) {
            ProductDetails productDetails = null;
            if (whichBuyNow == Constants.BUY_NOW_MONTHLY) {
                productDetails = finalProductDetailsMonthly;
            } else if (whichBuyNow == Constants.BUY_NOW_YEARLY) {
                productDetails = finalProductDetails;
            }

            if (productDetails != null && productDetails.getSubscriptionOfferDetails() != null && !productDetails.getSubscriptionOfferDetails().isEmpty()) {
                String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();

                if (whichBuyNow == Constants.BUY_NOW_MONTHLY) {
                    prefManager.setMonthlyPlanPrice(price);
                } else {
                    prefManager.setYearlyPlanPrice(price);
                }

                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                productDetailsParamsList.add(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                );

                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

                billingClient.launchBillingFlow(PremiumActivity.this, flowParams);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.google_play_billing), Toast.LENGTH_SHORT).show();
            }
        } else {
            DummyPurchaseModel dummyPurchaseModel = new DummyPurchase().getDummyPurchasePremiumYearly();
            handleDummyPurchase(dummyPurchaseModel, whichBuyNow);
        }
    }

    private void getInAppProduct() {
        loadPriceProgress();
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(Constants.PRODUCT_ID_PREMIUM)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(Constants.PRODUCT_ID_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsResult) -> {
            runOnMainThread(() -> {
                hidePriceProgress();
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsResult.getProductDetailsList() != null) {
                    for (ProductDetails productDetails : productDetailsResult.getProductDetailsList()) {
                        if (Constants.PRODUCT_ID_PREMIUM.equals(productDetails.getProductId())) {
                            finalProductDetails = productDetails;
                        }
                        if (Constants.PRODUCT_ID_MONTHLY.equals(productDetails.getProductId())) {
                            finalProductDetailsMonthly = productDetails;
                        }
                    }
                    productOnUI();
                    productOnUIMonthly();
                }
            });
        });
    }

    private void productOnUIMonthly() {
        String price = "249";
        if (finalProductDetailsMonthly != null && finalProductDetailsMonthly.getSubscriptionOfferDetails() != null && !finalProductDetailsMonthly.getSubscriptionOfferDetails().isEmpty()) {
            price = finalProductDetailsMonthly.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
        }

        if (restoreFoundMonthly) {
            btBuyNowMonthly.setText(getString(R.string.restore_payment));
        } else {
            btBuyNowMonthly.setText(String.format(getString(R.string.buy_now_for_499_per_month), price));
        }
    }

    private void productOnUI() {
        String price = "799";
        if (finalProductDetails != null && finalProductDetails.getSubscriptionOfferDetails() != null && !finalProductDetails.getSubscriptionOfferDetails().isEmpty()) {
            price = finalProductDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
        }

        if (restoreFound) {
            btBuyNow.setText(getString(R.string.restore_payment));
        } else {
            btBuyNow.setText(String.format(getString(R.string.buy_now_yearly), price));
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (Constants.IS_ORDER_REAL) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                List<String> products = purchase.getProducts();
                if (products.contains(Constants.PRODUCT_ID_PREMIUM)) {
                    prefManager.setOrderIdPremiumYearly(purchase.getOrderId());
                    whichBuyNow = Constants.BUY_NOW_YEARLY;
                } else if (products.contains(Constants.PRODUCT_ID_MONTHLY)) {
                    prefManager.setOrderIdPremiumMonthly(purchase.getOrderId());
                    whichBuyNow = Constants.BUY_NOW_MONTHLY;
                }

                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
                } else {
                    if (whichBuyNow == Constants.BUY_NOW_YEARLY) {
                        allDone();
                    } else if (whichBuyNow == Constants.BUY_NOW_MONTHLY) {
                        allDoneMonthly();
                    }
                }
                finalPurchase = purchase;
            }
        }
    }

    private void handleDummyPurchase(DummyPurchaseModel dummyPurchaseModel, int whichBuyNow) {
        this.whichBuyNow = whichBuyNow;
        if (whichBuyNow == Constants.BUY_NOW_YEARLY) {
            allDone();
            prefManager.setOrderIdPremiumYearly(dummyPurchaseModel.getOrderId());
        } else {
            allDoneMonthly();
            prefManager.setOrderIdPremiumMonthly(dummyPurchaseModel.getOrderId());
        }
    }

    private void allDoneBoth() {
        prefManager.setPremiumMonthly(true);
        prefManager.setPremiumYearly(true);
        prefManager.setPurchasedPlanName(Constants.RESTORE_PREMIUM_BOTH);
        topicSubscription();
        finishWithResult();
    }

    private void allDoneMonthly() {
        prefManager.setPremiumMonthly(true);
        prefManager.setPurchasedPlanName(whichBuyNow);
        topicSubscription();
        finishWithResult();
    }

    private void allDone() {
        prefManager.setPremiumYearly(true);
        prefManager.setPurchasedPlanName(whichBuyNow);
        topicSubscription();
        finishWithResult();
    }

    private void finishWithResult() {
        runOnMainThread(() -> {
            if (Constants.IS_OWN_API_IMPLEMENT) {
                incPb.setVisibility(View.VISIBLE);
                orderToApi();
            } else {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void topicSubscription() {
        SubscribeToTopic subscribeToTopic = new SubscribeToTopic(context);
        subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE);
        if (prefManager.isPremium()) {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PREMIUM);
        }
        prefManager.setUnsubscribeFromFree(true);
    }

    private void orderToApi() {
        OrderToApi orderToApi = new OrderToApi(context, new OnApiResult() {
            @Override
            public void onApiResponse() {
                runOnMainThread(() -> {
                    incPb.setVisibility(View.GONE);
                    openHomeActivity();
                });
            }

            @Override
            public void onApiFailure() {
                runOnMainThread(() -> incPb.setVisibility(View.GONE));
            }
        });
        orderToApi.doOrder(finalPurchase, whichBuyNow == Constants.BUY_NOW_YEARLY ? Constants.PLAN_PEMIUM_YEARLY : Constants.PLAN_PEMIUM_MONTHLY);
    }

    private void openHomeActivity() {
        Intent intent = new Intent(PremiumActivity.this, HomeActivity.class);
        intent.putExtra(Constants.EXTRA_PLAN_PAID_SUCCESS, whichBuyNow == Constants.BUY_NOW_YEARLY ? Constants.PLAN_PEMIUM_YEARLY : Constants.PLAN_PEMIUM_MONTHLY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            runOnMainThread(() -> {
                if (whichBuyNow == Constants.BUY_NOW_YEARLY) {
                    allDone();
                } else if (whichBuyNow == Constants.BUY_NOW_MONTHLY) {
                    allDoneMonthly();
                }
            });
        }
    }

    private void setFeatureList() {
        featureModelList = new ArrayList<>();
        String[] featureVal = getResources().getStringArray(R.array.feature_val);
        String[] basicVal = getResources().getStringArray(R.array.basic_val);
        TypedArray basicIcon = getResources().obtainTypedArray(R.array.basic_icon);
        TypedArray premiumIcon = getResources().obtainTypedArray(R.array.premium_icon);

        for (int i = 0; i < featureVal.length; i++) {
            // Skip OCR related features for now as requested
            if (featureVal[i].toLowerCase().contains("ocr")) {
                continue;
            }
            FeatureModel featureModel = new FeatureModel();
            featureModel.setFeature(featureVal[i]);
            featureModel.setBasic_val(i < basicVal.length ? basicVal[i] : "");
            featureModel.setBasic_icon(i < basicIcon.length() ? basicIcon.getResourceId(i, 0) : 0);
            featureModel.setPremium_val("");
            featureModel.setPremium_icon(premiumIcon.getResourceId(0,0));
            featureModelList.add(featureModel);
        }

        basicIcon.recycle();
        premiumIcon.recycle();

        featureRecycler.setLayoutManager(new LinearLayoutManager(context));
        featureAdapter = new FeatureAdapter(context, featureModelList);
        featureRecycler.setAdapter(featureAdapter);
    }

    private void setDescriptionList() {
        String[] title = getResources().getStringArray(R.array.description_title);
        String[] description = getResources().getStringArray(R.array.description_detail);
        TypedArray descIcon = getResources().obtainTypedArray(R.array.desc_icon);

        descriptionModelList = new ArrayList<>();
        for (int i = 0; i < title.length; i++) {
            DescriptionModel model = new DescriptionModel();
            model.setTitle(title[i]);
            model.setDescription(description[i]);
            model.setImage(descIcon.getResourceId(i, 0));
            descriptionModelList.add(model);
        }
        descriptionRecycler.setLayoutManager(new LinearLayoutManager(context));
        descriptionAdapter = new DescriptionAdapter(context, descriptionModelList);
        descriptionRecycler.setAdapter(descriptionAdapter);
    }
}
