package com.cam.scanner.scantopdf.android.rest;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.rest.request.RequestRegister;
import com.cam.scanner.scantopdf.android.rest.response.Order;
import com.cam.scanner.scantopdf.android.rest.response.Register;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterToApi {

    private String TAG = RegisterToApi.class.getSimpleName();
    private Context mContext;
    private FlashScanUtil util;
    private PrefManager prefManager;
    private ApiInterface apiInterface;
    private DBHandler dbHandler;

    private OnApiResult onApiResult;

    private String selfAndroidId;

    public RegisterToApi(Context _ctx, OnApiResult _onApiResult) {
        this.mContext = _ctx;
        util = new FlashScanUtil(mContext);
        prefManager = new PrefManager(mContext);
        dbHandler = AppController.getINSTANCE().dbHandler;
        this.onApiResult = _onApiResult;
    }

    public void doRegister() {
        if (util.isConnectingToInternet()) {

            RequestRegister requestRegister = requestParams();
            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<Register> call = apiInterface.register(requestRegister);

            call.enqueue(new Callback<Register>() {
                @Override
                public void onResponse(Call<Register> call, Response<Register> response) {
                    Log.i(TAG, "Register response: " + response);

                    Register register = response.body();
                    prefManager.setDeviceIdOfInstallTime(selfAndroidId);
                    putInDb(register);
                }

                @Override
                public void onFailure(Call<Register> call, Throwable t) {
                    Log.i(TAG, "Register call onFailure");
                    call.cancel();
                }
            });
        }
    }

    private RequestRegister requestParams() {

        selfAndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        //For testing dummy device id
//        selfAndroidId = "test_device_id_prakash19";
        ////

        Log.i(TAG, "self android_id: " + selfAndroidId);

        RequestRegister requestRegister = new RequestRegister();
        requestRegister.setDeviceId(selfAndroidId);
        requestRegister.setPlanId(prefManager.getPlanIdForApi());

        String orderIdPremium = prefManager.getOrderIdPremiumYearly();
        String orderIdOcr = prefManager.getOrderIdOcrMonthly();

        if (orderIdPremium != null) {
            requestRegister.setOrderId(orderIdPremium);
        } else if (orderIdOcr != null) {
            requestRegister.setOrderId(orderIdOcr);
        } else {
            requestRegister.setOrderId(null);
        }

        return requestRegister;
    }

    /*private void putInDb(Register register) {
        int credits = register.getCredits();
        String emailFromApi = register.getEmail();
        String subscriptionId = register.getSubscriptionId();
        Log.i(TAG, "Credits: " + credits);

        if (dbHandler.existCreditsFromApi(selfAndroidId)) {
            dbHandler.updateCreditsFromApi(credits, selfAndroidId, emailFromApi);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertCreditsFromApi(credits, selfAndroidId, emailFromApi);
            Log.i(TAG, "insert");
        }
    }*/

    private void putInDb(Register register) {
        int credits = 0;
        if (register.getCredits() != null) {
            credits = register.getCredits();
        }
        Log.i(TAG, "Credits: " + credits);

        String emailFromApi = register.getEmail();
        Log.i(TAG, "emailFromApi: " + emailFromApi);

        String subscriptionId = null;
        if (register.getSubscriptionId() != null) {
            subscriptionId = register.getSubscriptionId();
        }
        Log.i(TAG, "subscriptionId: " + subscriptionId);

        boolean isOcr = register.getIsOCR();
        Log.i(TAG, "isOcr: " + isOcr);

        int isOcrInt = isOcr ? 1 : 0;

        int planId = register.getPlanId();
        int isPremiumInt = 0;
        if (planId == Constants.PLAN_PEMIUM_YEARLY) {
            isPremiumInt = 1;
        }

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();

        if (dbHandler.existCreditsFromApi(deviceIdOfInstallTime)) {
            dbHandler.updateCreditsFromApi(credits, deviceIdOfInstallTime, subscriptionId,
                    isOcrInt, planId, isPremiumInt, emailFromApi);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertCreditsFromApi(credits, deviceIdOfInstallTime, subscriptionId,
                    isOcrInt, planId, isPremiumInt, emailFromApi);
            Log.i(TAG, "insert");
        }
    }
}
