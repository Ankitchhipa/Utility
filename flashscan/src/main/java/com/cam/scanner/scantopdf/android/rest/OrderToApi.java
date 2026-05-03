package com.cam.scanner.scantopdf.android.rest;

import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.Purchase;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.rest.model.DummyPurchaseModel;
import com.cam.scanner.scantopdf.android.rest.request.RequestOrder;
import com.cam.scanner.scantopdf.android.rest.request.RequestUpdateCredits;
import com.cam.scanner.scantopdf.android.rest.response.Order;
import com.cam.scanner.scantopdf.android.rest.response.UpdateCredits;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.DummyPurchase;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderToApi {

    private String TAG = OrderToApi.class.getSimpleName();
    private Context mContext;
    private FlashScanUtil util;
    private PrefManager prefManager;
    private ApiInterface apiInterface;
    private DBHandler dbHandler;

    private OnApiResult onApiResult;

    public OrderToApi(Context _ctx, OnApiResult _onApiResult) {
        this.mContext = _ctx;
        util = new FlashScanUtil(mContext);
        prefManager = new PrefManager(mContext);
        dbHandler = AppController.getINSTANCE().dbHandler;
        this.onApiResult = _onApiResult;
    }

    public void doOrder(Purchase purchase, int whichPlan) {
        if (util.isConnectingToInternet()) {
            RequestOrder requestOrder = requestParams(purchase, whichPlan);

            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<Order> call = apiInterface.order(requestOrder);

            call.enqueue(new Callback<Order>() {
                @Override
                public void onResponse(Call<Order> call, Response<Order> response) {
                    Log.i(TAG, "UpdateCredit response: " + response);

                    Order order = response.body();

                    putInDb(order);

                    onApiResult.onApiResponse();
                }

                @Override
                public void onFailure(Call<Order> call, Throwable t) {
                    Log.i(TAG, "UpdateCredits call onFailure");
                    call.cancel();

                    onApiResult.onApiFailure();
                }
            });
        }
    }

    public void doOrder(DummyPurchaseModel dummyPurchaseModel, int whichPlan) {
        if (util.isConnectingToInternet()) {
            RequestOrder requestOrder = requestParams(dummyPurchaseModel, whichPlan);

            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<Order> call = apiInterface.order(requestOrder);

            call.enqueue(new Callback<Order>() {
                @Override
                public void onResponse(Call<Order> call, Response<Order> response) {
                    Log.i(TAG, "UpdateCredit response: " + response);

                    Order order = response.body();

                    putInDb(order);

                    onApiResult.onApiResponse();
                }

                @Override
                public void onFailure(Call<Order> call, Throwable t) {
                    Log.i(TAG, "UpdateCredits call onFailure");
                    call.cancel();

                    onApiResult.onApiFailure();
                }
            });
        }
    }

    private RequestOrder requestParams(Purchase purchase, int whichPlan) {
        RequestOrder requestOrder = new RequestOrder();

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();
        String email = dbHandler.getEmail(deviceIdOfInstallTime);
        String orderId = purchase.getOrderId();
        String packageNameInPurchase = purchase.getPackageName();
        String productIdInPurchase = /*purchase.getSku()*/"";
        long purchaseTime = purchase.getPurchaseTime();
        int purchaseState = purchase.getPurchaseState();
        String purchaseToken = purchase.getPurchaseToken();
        boolean isAutoRenewing = purchase.isAutoRenewing();
        boolean isAcknowledged = purchase.isAcknowledged();

        requestOrder.setDeviceId(deviceIdOfInstallTime);
        requestOrder.setPlanId(whichPlan);
        requestOrder.setEmail(email);
        requestOrder.setOrderId(orderId);
        requestOrder.setPackageName(packageNameInPurchase);
        requestOrder.setProductId(productIdInPurchase);
        requestOrder.setPurchaseTime(purchaseTime);
        requestOrder.setPurchaseState(purchaseState);
        requestOrder.setPurchaseToken(purchaseToken);
        requestOrder.setAutoRenewing(isAutoRenewing);
        requestOrder.setAcknowledged(isAcknowledged);

        return requestOrder;
    }

    private RequestOrder requestParams(DummyPurchaseModel purchase, int whichPlan) {
        RequestOrder requestOrder = new RequestOrder();

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();
        String email = dbHandler.getEmail(deviceIdOfInstallTime);
        String orderId = purchase.getOrderId();
        String packageNameInPurchase = purchase.getPackageName();
        String productIdInPurchase = purchase.getProductId();
        long purchaseTime = purchase.getPurchaseTime();
        int purchaseState = purchase.getPurchaseState();
        String purchaseToken = purchase.getPurchaseToken();
        boolean isAutoRenewing = purchase.getAutoRenewing();
        boolean isAcknowledged = purchase.getAcknowledged();

        requestOrder.setDeviceId(deviceIdOfInstallTime);
        requestOrder.setPlanId(whichPlan);
        requestOrder.setEmail(email);
        requestOrder.setOrderId(orderId);
        requestOrder.setPackageName(packageNameInPurchase);
        requestOrder.setProductId(productIdInPurchase);
        requestOrder.setPurchaseTime(purchaseTime);
        requestOrder.setPurchaseState(purchaseState);
        requestOrder.setPurchaseToken(purchaseToken);
        requestOrder.setAutoRenewing(isAutoRenewing);
        requestOrder.setAcknowledged(isAcknowledged);

        return requestOrder;
    }

    private void putInDb(Order order) {
        int credits = 0;
        if (order.getCredits() != null) {
            credits = order.getCredits();
        }
        Log.i(TAG, "Credits: " + credits);

        String subscriptionId = null;
        if (order.getSubscriptionId() != null) {
            subscriptionId = order.getSubscriptionId();
        }
        Log.i(TAG, "subscriptionId: " + subscriptionId);

        boolean isOcr = order.getIsOCR();
        Log.i(TAG, "isOcr: " + isOcr);

        int isOcrInt = isOcr ? 1 : 0;

        int planId = order.getPlanId();
        int isPremiumInt = 0;
        if (planId == Constants.PLAN_PEMIUM_YEARLY) {
            isPremiumInt = 1;
        }

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();

        if (dbHandler.existCreditsFromApi(deviceIdOfInstallTime)) {
            dbHandler.updateCreditsFromApi(credits, deviceIdOfInstallTime, subscriptionId,
                    isOcrInt, planId, isPremiumInt);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertCreditsFromApi(credits, deviceIdOfInstallTime, subscriptionId,
                    isOcrInt, planId, isPremiumInt);
            Log.i(TAG, "insert");
        }
    }
}
