package com.cam.scanner.scantopdf.android.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

public class OfferActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = OfferActivity.class.getSimpleName();
    private boolean isComingFromNotiOffer;
    private RelativeLayout offer_layout;
    private WebView webView;
    private PrefManager prefManager;
    private Context context;
    private FlashScanUtil util;
    private RelativeLayout rlLoading;
    private ImageView ivClose;
    private String planIdStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        getFromIntent();
        initObjs();
        findIds();

        showOffer();

        clickListeners();
//        new GetValidUrlOrNot().execute();
    }

    private void getFromIntent() {
        if (getIntent() != null && getIntent().hasExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF)) {
            planIdStr = getIntent().getStringExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF);

            Log.i(TAG, "planIdStr" + planIdStr);
        }
    }

    private void openPlanScreenAfterOffer() {
        Intent defaultIntent = null;
        int planId = 0;
        if (planIdStr != null) {
            try {
                planId = Integer.parseInt(planIdStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (planId == Constants.PLAN_PEMIUM_YEARLY) {
            defaultIntent = new Intent(context, PremiumActivity.class);
        } else if (planId == Constants.PLAN_OCR_MONTHLY) {
            defaultIntent = new Intent(context, OcrPlanDialog.class);
        }

        if (defaultIntent != null) {
            startActivity(defaultIntent);
        }
    }

    private void clickListeners() {
        ivClose.setOnClickListener(this);
    }

    private void progressVisibility(boolean b) {
        if (!b) {
            rlLoading.setVisibility(View.GONE);
        }
    }

    private void showOffer() {
        String url = prefManager.getOfferUrlServer();
        offer_layout.setVisibility(View.VISIBLE);
//        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new MyWebViewClient(context));
    }

    private void findIds() {
        offer_layout = findViewById(R.id.offer_layout);
        webView = findViewById(R.id.web_view_offer);
        rlLoading = findViewById(R.id.rl_loading);
        ivClose = findViewById(R.id.iv_close);
    }

    private void initObjs() {
        context = this;
        prefManager = new PrefManager(context);
        util = new FlashScanUtil(context);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_close) {
            openPlanScreenAfterOffer();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        openPlanScreenAfterOffer();
        finish();
    }

    private class GetValidUrlOrNot extends AsyncTask<Void, Void, Boolean> {
        String url = "";
        //testUrl = "https://cdn.horoscopelogy.com/hor/appads/andr/TarotLife_Offer_Ask_Tarot_5.html";

        @Override
        protected Boolean doInBackground(Void... voids) {
//            Log.e(TAG, "=======     doInBackground");

            url = prefManager.getOfferUrlServer();
            return util.urlValidOrNot(url);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                if (result) {
//                    Log.e(TAG, "=======     onPostExecute Result "+result);
                    if (util.isConnectingToInternet()) {


                        // getHtmlFromWeb(url);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "RESULT 4 exception " + e.getMessage());
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        Context contx;

        MyWebViewClient(Context ctx) {
            contx = ctx;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            Log.e(TAG, "=======     URL" + url);
            String TEL_PREFIX = "tel:";
            if (url.startsWith(TEL_PREFIX)) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            } else if (url.equalsIgnoreCase(Constants.WEBSITE_URL)) {
//                Log.e(TAG, "=======     main Activity  else if Offer Case");
                webView.setVisibility(View.GONE);
                offer_layout.setVisibility(View.GONE);
                return true;
            } else {
                webView.setVisibility(View.GONE);
                offer_layout.setVisibility(View.GONE);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressVisibility(false);
            //cancelProgressDialog();
        }
    }
}