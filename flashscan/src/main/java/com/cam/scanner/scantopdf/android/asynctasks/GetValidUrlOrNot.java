package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cam.scanner.scantopdf.android.interfaces.OnOfferUrlChecked;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.net.HttpURLConnection;
import java.net.URL;

public class GetValidUrlOrNot extends AsyncTask<String, Void, Boolean> {
    private static final String TAG = GetValidUrlOrNot.class.getSimpleName();
    private String url = "";
    private Context context;
    private FlashScanUtil util;
    private OnOfferUrlChecked onOfferUrlChecked;

    public GetValidUrlOrNot(Context _ctx, OnOfferUrlChecked _onOfferUrlChecked) {
        this.context = _ctx;
        this.onOfferUrlChecked = _onOfferUrlChecked;
        util = new FlashScanUtil(context);
    }

    //testUrl = "https://cdn.horoscopelogy.com/hor/appads/andr/TarotLife_Offer_Ask_Tarot_5.html";

    @Override
    protected Boolean doInBackground(String... strings) {
//            Log.e(TAG, "=======     doInBackground");

//        url = prefManager.getOfferUrlServer();
        String url = strings[0];
        return urlValidOrNot(url);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        try {
            if (result) {
//                    Log.e(TAG, "=======     onPostExecute Result "+result);
                if (util.isConnectingToInternet()) {
                    onOfferUrlChecked.onSuccess();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "RESULT 4 exception " + e.getMessage());
        }
    }

    private boolean urlValidOrNot(String urls) {
        boolean isOnline = false;
        try {
            URL url = new URL(urls);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout(2000);
            isOnline = conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            isOnline = false;
        }
        Log.i(TAG, "url valid: " + isOnline);
        return isOnline;
    }
}