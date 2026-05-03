package com.cam.scanner.scantopdf.android.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

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
import com.cam.scanner.scantopdf.android.CirclePagerIndicatorDecoration;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.UpgradePlanAdapter;
import com.cam.scanner.scantopdf.android.models.DescriptionModel;
import com.cam.scanner.scantopdf.android.rest.model.DummyPurchaseModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.DummyPurchase;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.hhl.gridpagersnaphelper.GridPagerSnapHelper;

import java.util.ArrayList;
import java.util.List;


public class CurrentPlanActivity extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private static final String TAG = CurrentPlanActivity.class.getSimpleName();
    private TextView tv_current_plan_name, tv_current_plan_price, tv_upgrade_plan_name, tv_upgrade_plan_price;
    //private ImageView img_start_arrow,img_end_arrow;
    private ImageView img_indicator1,img_indicator2;
    private PrefManager prefManager;
    private Context context;
    private ImageView closeIcon;
    private List<DescriptionModel> descriptionModelList;
    private UpgradePlanAdapter upgradePlanAdapter;
    private FlashScanUtil util;
    private LinearLayout dotsLinearLayout;
    private RecyclerView upgradePlanFeatureRecycler;
    private SkuDetails finalSkuDetails, finalSkuDetailsQuarterly;
    private BillingClient billingClient;
    private ProgressBar pbUpgrade, pbActive;
    private Purchase finalPurchase;
    private boolean restoreFound;
    private boolean restoreFoundQuertely;
    private int whichRestore;
    private String premiumUpgradePrice = "";
    String quarterlyPlanPrice, yearlyPlanPrice;
    int purchasedPlanName;
    private CardView card_upgradePlan;
    private SkuDetails oldSku = null;
    private int upgradePlanName;
    private String puchasedPlanToken = "";
    private long lastClickedTime = 0;
    private LinearLayout tv_manage_subscription;
    private ImageView tv_manage_info;
    String skuName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_plan);

        findIds();
        initObjects();
        setClickListeners();

        purchasedPlanName = prefManager.getPurchasedPlanName();
        //quarterlyPlanPrice = prefManager.getQuarterlyPlanPrice();
        yearlyPlanPrice = prefManager.getYearlyPlanPrice();
        //Log.e("TAG ", " purchasedPlanName " + purchasedPlanName + " quarterlyPlanPrice " + quarterlyPlanPrice + " yearlyPlanPrice " + yearlyPlanPrice);

        if (util.isConnectingToInternet()) {
            connectBillingService();
        } else {

          /*  if (purchasedPlanName == Constants.BUY_NOW_QUARTERLY) {
                productOnUIQuarterly();
            } else */if (purchasedPlanName == Constants.BUY_NOW_YEARLY) {
                productOnUI();
            }

        }
        setDescriptionList();



        upgradePlanFeatureRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = ((GridLayoutManager)recyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                Log.e(TAG, "firstVisiblePosition: "+firstVisiblePosition+"lastVisiblePosition: "+lastVisiblePosition);


                if(firstVisiblePosition==0){
                    img_indicator1.setBackground(context.getResources().getDrawable(R.drawable.circle_filled_white));
                    img_indicator2.setBackground(context.getResources().getDrawable(R.drawable.circle_empty_white));
                }
                else if(lastVisiblePosition==7){
                    img_indicator1.setBackground(context.getResources().getDrawable(R.drawable.circle_empty_white));
                    img_indicator2.setBackground(context.getResources().getDrawable(R.drawable.circle_filled_white));
                }

            }
        });
    }

    private void connectBillingService() {
        loadPriceProgress();
        /*billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(CurrentPlanActivity.this).build();*/

       // clearGooglePlayStoreBillingCacheIfPossible();

        /*billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                hidePriceProgress();
                *//*if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                    if (purchasesResult.getPurchasesList() != null) {
                        List<Purchase> purchases = purchasesResult.getPurchasesList();
                        if (purchases.size() > 0) {
                            Log.i(TAG, "subs purchase found in restore"+"size "+purchases.size());
                            for (Purchase purchase : purchases) {
                                if (Constants.PRODUCT_ID_PREMIUM.equals(purchase.getSku())) {
                                    Log.i(TAG, "premium in restore");
                                    restoreFound = true;
                                    finalPurchase = purchase;
                                    puchasedPlanToken = purchase.getPurchaseToken();
                                    skuName = purchase.getSku();
                                }

                               *//**//* if (Constants.PRODUCT_ID_PREMIUM_QUARTELY.equals(purchase.getSku())) {
                                    Log.i(TAG, "premium quarterly in restore");
                                    restoreFoundQuertely = true;
                                    puchasedPlanToken = purchase.getPurchaseToken();
                                    skuName = purchase.getSku();
                                }*//**//*
                            }
                        } else {
                            // plan subscription expired or cancelled but app not killed
                            Intent intent = new Intent(CurrentPlanActivity.this, PremiumActivity.class);
                            startActivity(intent);
                        }

                    } else {
                        //whichRestore = Constants.RESTORE_PREMIUM_NONE;
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

    private void clearGooglePlayStoreBillingCacheIfPossible() {

        /*billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
                Log.i(TAG, "onPurchaseHistoryResponse");

            }
        });*/
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

                                if (Constants.PRODUCT_ID_PREMIUM.equals(sku)) {
                                    finalSkuDetails = skuDetails;
                                }
                                *//*if (Constants.PRODUCT_ID_PREMIUM_QUARTELY.equals(sku)) {
                                    finalSkuDetailsQuarterly = skuDetails;
                                }*//*
                            }

                           *//* if (restoreFound && restoreFoundQuertely) {
                                //Rare case - Handle scenario if both plans purchase (can occur if used 2 different devices with same gmail id)
                                bothProductOnUI();
                            } else *//*if (restoreFound) {
                                //premiumUpgradePrice = finalSkuDetailsQuarterly.getPrice();
                                productOnUI();
                            } *//*else if (restoreFoundQuertely) {
                                premiumUpgradePrice = finalSkuDetails.getPrice();
                                productOnUIQuarterly();
                            }*//*
                        }
                    }
                });*/
    }



    private void loadPriceProgress() {
        pbActive.setVisibility(View.VISIBLE);
        pbUpgrade.setVisibility(View.VISIBLE);
        tv_current_plan_name.setVisibility(View.GONE);
        tv_current_plan_price.setVisibility(View.GONE);
        tv_upgrade_plan_name.setVisibility(View.GONE);
        tv_upgrade_plan_price.setVisibility(View.GONE);
        card_upgradePlan.setEnabled(false);
    }

    private void hidePriceProgress() {
        pbActive.setVisibility(View.GONE);
        pbUpgrade.setVisibility(View.GONE);
        tv_current_plan_name.setVisibility(View.VISIBLE);
        tv_current_plan_price.setVisibility(View.VISIBLE);
        tv_upgrade_plan_name.setVisibility(View.VISIBLE);
        tv_upgrade_plan_price.setVisibility(View.VISIBLE);
        card_upgradePlan.setEnabled(true);
    }

    private void productOnUI() {
        card_upgradePlan.setVisibility(View.VISIBLE);
        String price;

        if (Constants.IS_ORDER_REAL) {
            if (util.isConnectingToInternet()) {
                price = finalSkuDetails.getPrice();
            } else {
                price = yearlyPlanPrice;
                //premiumUpgradePrice = quarterlyPlanPrice;
            }
        } else {
            price = "2400";
        }
        tv_current_plan_name.setText(R.string.annual_subscription);
        tv_current_plan_price.setText(String.format(getString(R.string.plan_price_yearly), price));
        //tv_upgrade_plan_name.setText(R.string.switch_to_quarterly_plan);
       // tv_upgrade_plan_price.setText(String.format(getString(R.string.upgrade_plan_price_quarterly), premiumUpgradePrice));
    }

    private void productOnUIQuarterly() {
        /*card_upgradePlan.setVisibility(View.VISIBLE);
        String price;

        if (Constants.IS_ORDER_REAL) {
            if (util.isConnectingToInternet()) {
                price = finalSkuDetailsQuarterly.getPrice();
            } else {
                price = quarterlyPlanPrice;
                premiumUpgradePrice = yearlyPlanPrice;
            }
        } else {
            price = "419";
        }
        tv_current_plan_name.setText(R.string.quarterly_subscription);
        tv_current_plan_price.setText(String.format(getString(R.string.plan_price_quarterly), price));
        tv_upgrade_plan_name.setText(R.string.switch_to_annual_plan);
        tv_upgrade_plan_price.setText(String.format(getString(R.string.upgrade_plan_price_yearly), premiumUpgradePrice));
   */ }

    private void bothProductOnUI() {
       /* card_upgradePlan.setVisibility(View.GONE);

        if (util.isConnectingToInternet()) {
            tv_current_plan_name.setText(R.string.quarterly_subscription + "\n "+ String.format(getString(R.string.plan_price_quarterly), finalSkuDetailsQuarterly.getPrice()));
            tv_current_plan_price.setText(R.string.annual_subscription + " \n " + String.format(getString(R.string.plan_price_yearly), finalSkuDetails.getPrice()));
        } else {
            tv_current_plan_name.setText(R.string.quarterly_subscription + " \n "+ String.format(getString(R.string.plan_price_quarterly), quarterlyPlanPrice));
            tv_current_plan_price.setText(R.string.annual_subscription + " \n " + String.format(getString(R.string.plan_price_yearly), yearlyPlanPrice));
        }*/

    }

    private void setClickListeners() {

        closeIcon.setOnClickListener(this);
        card_upgradePlan.setOnClickListener(this);
        tv_manage_subscription.setOnClickListener(this);
        tv_manage_info.setOnClickListener(this);
    }

    private void initObjects() {
        context = this;
        prefManager = new PrefManager(context);
        util = new FlashScanUtil(context);
    }

    private void findIds() {
        tv_current_plan_name = findViewById(R.id.tv_current_plan_name);
        tv_current_plan_price = findViewById(R.id.tv_current_plan_price);
        tv_upgrade_plan_name = findViewById(R.id.tv_upgrade_plan_name);
        tv_upgrade_plan_price = findViewById(R.id.tv_upgrade_plan_price);
        closeIcon = findViewById(R.id.closeIcon);
        upgradePlanFeatureRecycler = findViewById(R.id.upgradePlanFeatureRecycler);
        dotsLinearLayout = findViewById(R.id.dotsLinearLayout);
        pbUpgrade = findViewById(R.id.progress_bar_upgrade_plan);
        pbActive = findViewById(R.id.progress_bar_active_plan);
        card_upgradePlan = findViewById(R.id.card_upgradePlan);
        //img_start_arrow = findViewById(R.id.img_start_arrow);
        //img_end_arrow = findViewById(R.id.img_end_arrow);
        img_indicator1 = findViewById(R.id.img_indicator1);
        img_indicator2 = findViewById(R.id.img_indicator2);
        tv_manage_subscription = findViewById(R.id.tv_manage_subscription);
        tv_manage_info = findViewById(R.id.tv_manage_info);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.closeIcon) {
            finish();
        } else if (id == R.id.card_upgradePlan) {/* if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                    return;
                }
                lastClickedTime = SystemClock.elapsedRealtime();
                upgradePlan();*/
        } else if (id == R.id.tv_manage_subscription) {
            openPlaystoreAccount();
        } else if (id == R.id.tv_manage_info) {
            manageSubscriptionInfoDialog();
        }
    }

    private void manageSubscriptionInfoDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_manage_sunscription_info);


        TextView tv_manage_info = dialog.findViewById(R.id.tv_manage_info);
        tv_manage_info.setText(getString(R.string.manage_subs_info, getString(R.string.app_name)));
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private void openPlaystoreAccount() {
        if (util.isConnectingToInternet()) {

            if (skuName != null) {
                try {

                    if (skuName.isEmpty()) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions")));
                    } else {
                        String openGPlay = "https://play.google.com/store/account/subscriptions?sku=" + skuName + "&package=" + getPackageName();
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(openGPlay)));
                    }

                 /*   Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?sku=skuName&package=packageName"));
                    startActivity(browserIntent);*/

                } catch (ActivityNotFoundException e) {
                    util.showSnackBar(findViewById(android.R.id.content), "Please check your Internet connection!");
                    e.printStackTrace();
                }
            }
        } else {
            util.showSnackBar(findViewById(android.R.id.content), "Please check your Internet connection!");
        }
    }

    private void upgradePlan() {
        /*if (util.isConnectingToInternet()) {
            if (Constants.IS_ORDER_REAL) {
                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                SkuDetails skuDetails = null;
                String oldSku = null;
                upgradePlanName = 0;
                if (purchasedPlanName == Constants.BUY_NOW_YEARLY) { //yearly
                    skuDetails = finalSkuDetailsQuarterly;
                    oldSku = Constants.PRODUCT_ID_PREMIUM;
                    upgradePlanName = Constants.BUY_NOW_QUARTERLY;
                }

                if (purchasedPlanName == Constants.BUY_NOW_QUARTERLY) {  // quarterly
                    skuDetails = finalSkuDetails;
                    oldSku = Constants.PRODUCT_ID_PREMIUM_QUARTELY;
                    upgradePlanName = Constants.BUY_NOW_YEARLY;
                }
                if (skuDetails != null) {
                   // prefManager.setQuarterlyPlanPrice(finalSkuDetailsQuarterly.getPrice());
                    prefManager.setYearlyPlanPrice(finalSkuDetails.getPrice());
                    skuName = oldSku;
                }
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .setOldSku(oldSku,puchasedPlanToken) //give purchase token as second argument
                        .build();
                BillingResult responseCode = billingClient.launchBillingFlow(CurrentPlanActivity.this, flowParams);

            } else {
                DummyPurchaseModel dummyPurchaseModel = new DummyPurchase().getDummyPurchasePremiumYearly();
                //handleDummyPurchase(dummyPurchaseModel);
            }
        } else {
            util.showSnackBar(findViewById(android.R.id.content), "Please check your Internet connection!");
        }*/
    }

    private void setDescriptionList() {
        String[] title = getResources().getStringArray(R.array.premium_features_title);
        String[] description = getResources().getStringArray(R.array.description_detail);
        TypedArray descIcon = getResources().obtainTypedArray(R.array.premium_features_icon);

        descriptionModelList = new ArrayList<>();

        for (int i = 0; i < title.length; i++) {
            DescriptionModel descriptionModel = new DescriptionModel();
            descriptionModel.setTitle(title[i]);
            descriptionModel.setImage(descIcon.getResourceId(i, 0));
            descriptionModelList.add(descriptionModel);
        }
        GridLayoutManager layoutManager =
                new GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false);


        GridPagerSnapHelper gridPagerSnapHelper = new GridPagerSnapHelper();
        gridPagerSnapHelper.setRow(2).setColumn(2);
        gridPagerSnapHelper.attachToRecyclerView(upgradePlanFeatureRecycler);
        upgradePlanFeatureRecycler.setLayoutManager(layoutManager);
        upgradePlanAdapter = new UpgradePlanAdapter(context, descriptionModelList);
        upgradePlanFeatureRecycler.setAdapter(upgradePlanAdapter);
        // upgradePlanFeatureRecycler.addItemDecoration(new CirclePagerIndicatorDecoration());
        // upgradePlanFeatureRecycler.addItemDecoration(new LinePagerIndicatorDecoration());
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

                //Put appropriate check and setOrderIdPremiumYearly or setOrderIdPremiumQuarterly
                if(purchasedPlanName == Constants.BUY_NOW_YEARLY) {
                    prefManager.setOrderIdPremiumYearly(purchase.getOrderId());
                } /*else if(purchasedPlanName == Constants.BUY_NOW_QUARTERLY) {
                    prefManager.setOrderIdPremiumQuarterly(purchase.getOrderId());
                }*/

                // Acknowledge the purchase if it hasn't already been acknowledged.
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    //.setDeveloperPayload(purchase.getDeveloperPayload())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
                } else {
                    if (purchasedPlanName == Constants.BUY_NOW_YEARLY) {
                        allDone();
                    } /*else if (purchasedPlanName == Constants.BUY_NOW_QUARTERLY) {
                        allDoneQuartely();
                    }*/
                }
                finalPurchase = purchase;
            }
        }
    }

    @Override
    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        Log.i(TAG, "onAcknowledgePurchaseResponse: " + billingResult);
        if (purchasedPlanName == Constants.BUY_NOW_YEARLY) {
            allDone();
        }/* else if (purchasedPlanName == Constants.BUY_NOW_QUARTERLY) {
            allDoneQuartely();
        }*/
    }

    private void allDoneQuartely() {
/*        Log.i(TAG, "allDone Quarterly");
        prefManager.setPremiumQuarterly(true);
        prefManager.setPurchasedPlanName(upgradePlanName);
        setResult(RESULT_OK);
        //topicSubscription();
*//*
        if (Constants.IS_OWN_API_IMPLEMENT) {
            incPb.setVisibility(View.VISIBLE);
            orderToApi();
        } else {
            setResult(RESULT_OK);
        }*//*
        finish();*/
    }

    private void allDone() {
        Log.i(TAG, "allDone");
        prefManager.setPremiumYearly(true);
        prefManager.setPurchasedPlanName(upgradePlanName);
        setResult(RESULT_OK);
        //topicSubscription();

       /* if (Constants.IS_OWN_API_IMPLEMENT) {
            incPb.setVisibility(View.VISIBLE);
            orderToApi();
        } else {
            setResult(RESULT_OK);
        }*/
        finish();
    }
}
