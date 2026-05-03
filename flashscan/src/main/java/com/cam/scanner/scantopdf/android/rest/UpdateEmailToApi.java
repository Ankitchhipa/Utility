package com.cam.scanner.scantopdf.android.rest;

import android.content.Context;
import android.util.Log;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.rest.request.RequestUpdateCredits;
import com.cam.scanner.scantopdf.android.rest.request.RequestUpdateEmail;
import com.cam.scanner.scantopdf.android.rest.response.UpdateCredits;
import com.cam.scanner.scantopdf.android.rest.response.UpdateEmail;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateEmailToApi {

    private String TAG = UpdateEmailToApi.class.getSimpleName();
    private Context mContext;
    private FlashScanUtil util;
    private PrefManager prefManager;
    private ApiInterface apiInterface;
    private DBHandler dbHandler;

    private OnApiResult onApiResult;

    public UpdateEmailToApi(Context _ctx, OnApiResult _onApiResult) {
        this.mContext = _ctx;
        util = new FlashScanUtil(mContext);
        prefManager = new PrefManager(mContext);
        dbHandler = AppController.getINSTANCE().dbHandler;
        this.onApiResult = _onApiResult;
    }

    public void doUpdateEmail(String emailAddress) {
        if (util.isConnectingToInternet()) {
            RequestUpdateEmail requestUpdateEmail = requestParams(emailAddress);

            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<UpdateEmail> call = apiInterface.updateEmail(requestUpdateEmail);

            call.enqueue(new Callback<UpdateEmail>() {
                @Override
                public void onResponse(Call<UpdateEmail> call, Response<UpdateEmail> response) {
                    Log.i(TAG, "UpdateEmail response: " + response);

                    UpdateEmail updateEmail = response.body();

                    putInDb(updateEmail, emailAddress);

                    onApiResult.onApiResponse();
                }

                @Override
                public void onFailure(Call<UpdateEmail> call, Throwable t) {
                    Log.i(TAG, "UpdateCredits call onFailure");
                    call.cancel();

                    onApiResult.onApiFailure();
                }
            });
        }
    }

    private RequestUpdateEmail requestParams(String emailAddress) {
        RequestUpdateEmail requestUpdateEmail = new RequestUpdateEmail();

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();

        requestUpdateEmail.setDeviceId(deviceIdOfInstallTime);
        requestUpdateEmail.setEmail(emailAddress);

        return requestUpdateEmail;
    }

    private void putInDb(UpdateEmail updateEmail, String inputEmail) {
        String subscriptionId = updateEmail.getSubscriptionId();
        Log.i(TAG, "subscriptionId: " + subscriptionId);

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();

        if (dbHandler.existCreditsFromApi(deviceIdOfInstallTime)) {
            dbHandler.updateEmailSubscriptionId(subscriptionId, inputEmail, deviceIdOfInstallTime);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertEmailSubscriptionId(subscriptionId, inputEmail);
            Log.i(TAG, "insert");
        }
    }
}
