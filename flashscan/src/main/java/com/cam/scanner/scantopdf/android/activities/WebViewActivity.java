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

    private WebView webView;
    private Context context;
    private String url;
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
            loadWebView(url);
            setToolbarText(url);
        }

    }

    private void setToolbarText(String url) {
        switch (url) {
            case Constants.URLs.PRIVACY_POLICY:
                tv_toolbar.setText(getString(R.string.privacy_policy));
                break;
        }
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
    }

    private void getIntentUrl() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.URL)) {
            url = getIntent().getStringExtra(Constants.PutExtraConstants.URL);
        }
    }

    private void loadWebView(String url) {
        progress_lay.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
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
