package com.cam.scanner.scantopdf.android.rest;

import android.content.Context;
import android.util.Log;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.rest.request.RequestUpdateCredits;
import com.cam.scanner.scantopdf.android.rest.response.UpdateCredits;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateCreditsToApi {

    private String TAG = UpdateCreditsToApi.class.getSimpleName();
    private Context mContext;
    private FlashScanUtil util;
    private PrefManager prefManager;
    private ApiInterface apiInterface;
    private DBHandler dbHandler;

    private OnApiResult onApiResult;

    public UpdateCreditsToApi(Context _ctx, OnApiResult _onApiResult) {
        this.mContext = _ctx;
        util = new FlashScanUtil(mContext);
        prefManager = new PrefManager(mContext);
        dbHandler = AppController.getINSTANCE().dbHandler;
        this.onApiResult = _onApiResult;
    }

    public void doUpdateCredits(int creditsToDeduct) {
        if (util.isConnectingToInternet()) {
            RequestUpdateCredits requestUpdateCredits = requestParams(creditsToDeduct);

            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<UpdateCredits> call = apiInterface.updateCredits(requestUpdateCredits);

            call.enqueue(new Callback<UpdateCredits>() {
                @Override
                public void onResponse(Call<UpdateCredits> call, Response<UpdateCredits> response) {
                    Log.i(TAG, "UpdateCredit response: " + response);

                    UpdateCredits updateCredits = response.body();

                    putInDb(updateCredits);

                    onApiResult.onApiResponse();
                }

                @Override
                public void onFailure(Call<UpdateCredits> call, Throwable t) {
                    Log.i(TAG, "UpdateCredits call onFailure");
                    call.cancel();

                    onApiResult.onApiFailure();
                }
            });
        }
    }

    private RequestUpdateCredits requestParams(int creditsToDeduct) {
        RequestUpdateCredits requestUpdateCredits = new RequestUpdateCredits();

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();

        requestUpdateCredits.setDeviceId(deviceIdOfInstallTime);
        requestUpdateCredits.setCredits(creditsToDeduct);

        return requestUpdateCredits;
    }

    private void putInDb(UpdateCredits updateCredits) {
        int credits = updateCredits.getCredits();
        Log.i(TAG, "Credits: " + credits);

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();

        if (dbHandler.existCreditsFromApi(deviceIdOfInstallTime)) {
            dbHandler.updateCreditsFromApi(credits, deviceIdOfInstallTime);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertCreditsFromApi(credits, deviceIdOfInstallTime);
            Log.i(TAG, "insert");
        }
    }
}
