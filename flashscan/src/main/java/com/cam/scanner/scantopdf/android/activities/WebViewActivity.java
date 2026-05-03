package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.Constants;

public class WebViewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOCAL_PRIVACY_POLICY_ASSET = "file:///android_asset/privacy_policy.html";
    private static final String EXTRA_TITLE_FALLBACK = "title";
    private static final String EXTRA_URL_FALLBACK = "url";
    private WebView webView;
    private Context context;
    private String url;
    private String title;
    private ImageView iv_back_toolbar;
    private TextView tv_toolbar;
    private View progress_lay;
    private static final String TAG = WebViewActivity.class.getSimpleName();
    private Button btn_progress_lay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        findIds();
        setClickListeners();
        initObjects();
        getIntentUrl();
        if (!TextUtils.isEmpty(url)) {
            String resolvedUrl = resolveDisplayUrl(url);
            loadWebView(resolvedUrl);
            setToolbarText(resolvedUrl);
        }

    }

    private void setToolbarText(String url) {
        if (!TextUtils.isEmpty(title)) {
            tv_toolbar.setText(title);
            return;
        }

        if (shouldLoadLocalPrivacyPolicy(url)) {
            tv_toolbar.setText(getString(R.string.privacy_policy));
        }
    }

    private String resolveDisplayUrl(String sourceUrl) {
        if (shouldLoadLocalPrivacyPolicy(sourceUrl)) {
            return LOCAL_PRIVACY_POLICY_ASSET;
        }
        return sourceUrl;
    }

    private boolean shouldLoadLocalPrivacyPolicy(String sourceUrl) {
        if (LOCAL_PRIVACY_POLICY_ASSET.equals(sourceUrl)) {
            return true;
        }

        if (!TextUtils.isEmpty(title) && getString(R.string.privacy_policy).contentEquals(title)) {
            return true;
        }

        return !TextUtils.isEmpty(sourceUrl) && sourceUrl.toLowerCase().contains("privacy");
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
    }

    private void getIntentUrl() {
        if (getIntent() == null) {
            return;
        }

        title = getIntent().getStringExtra(EXTRA_TITLE_FALLBACK);

        if (getIntent().hasExtra(Constants.PutExtraConstants.URL)) {
            url = getIntent().getStringExtra(Constants.PutExtraConstants.URL);
        } else if (getIntent().hasExtra(EXTRA_URL_FALLBACK)) {
            url = getIntent().getStringExtra(EXTRA_URL_FALLBACK);
        }
    }

    private void loadWebView(String url) {
        progress_lay.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        webView.setWebViewClient(new MyWebViewClient(progress_lay));
        webView.loadUrl(url);
    }

    private static class MyWebViewClient extends WebViewClient {
        private View progress_lay;

        MyWebViewClient(View progress_lay) {
            this.progress_lay = progress_lay;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("mailto:")) {
                MailTo mailTo = MailTo.parse(url);
                if (mailTo != null) {
                    try {
                        Intent intent = newEmailIntent(mailTo);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (intent.resolveActivity(AppController.getINSTANCE().context.getPackageManager()) != null) {
                            AppController.getINSTANCE().context.startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    view.reload();
                    return true;
                }
            } else if (url.startsWith("tel:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(AppController.getINSTANCE().context.getPackageManager()) != null) {
                        AppController.getINSTANCE().context.startActivity(intent);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            view.loadUrl(url);
            return true;
        }

        private Intent newEmailIntent(MailTo mailTo) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo.getTo()});
            intent.putExtra(Intent.EXTRA_TEXT, mailTo.getBody());
            intent.putExtra(Intent.EXTRA_SUBJECT, mailTo.getSubject());
            intent.putExtra(Intent.EXTRA_CC, mailTo.getCc());
            intent.setType("message/rfc822");
            return intent;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progress_lay.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress_lay.setVisibility(View.GONE);
        }
    }

    private void initObjects() {
        context = this;
    }

    private void findIds() {
        webView = findViewById(R.id.webView);
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        progress_lay = findViewById(R.id.progress_lay);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.btn_progress_lay) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress_lay.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progress_lay.setVisibility(View.GONE);
    }
}
