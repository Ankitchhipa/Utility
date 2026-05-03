package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

public class AboutAppActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_back_toolbar;
    private Context context;
    private TextView tv_toolbar, tv_app_version_code, tv_app_website, tv_customer_support;
    private FlashScanUtil flashScanUtil;
    private LinearLayout ll_website, ll_contact_support;
    private long lastClickedTime = 0;

    private EditText etSecureAndroidId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        initObjects();
        findViewIds();
        setClickListeners();
        setData();

        showSecureAndroidId();
    }

    private void showSecureAndroidId() {
        String selfAndroidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        etSecureAndroidId.setText(selfAndroidId);
    }

    private void setData() {
        tv_toolbar.setText(getString(R.string.about_app));
        if (!TextUtils.isEmpty(flashScanUtil.getCurrentAppVersionName())) {
            tv_app_version_code.setText(flashScanUtil.getCurrentAppVersionName());
        }
        tv_app_website.setText(Constants.URLs.APP_WEBSITE_URL);
        tv_customer_support.setText(Constants.URLs.CUSTOMER_SUPPORT_URL);
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
        ll_website.setOnClickListener(this);
        ll_contact_support.setOnClickListener(this);
    }

    private void findViewIds() {
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_app_version_code = findViewById(R.id.tv_app_version_code);
        tv_app_website = findViewById(R.id.tv_app_website);
        tv_customer_support = findViewById(R.id.tv_customer_support);
        ll_website = findViewById(R.id.ll_website);
        ll_contact_support = findViewById(R.id.ll_contact_support);

        etSecureAndroidId = findViewById(R.id.et_secure_android_id);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.ll_website) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            flashScanUtil.intentToBrowser(Constants.URLs.APP_WEBSITE_URL);
        } else if (id == R.id.ll_contact_support) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            flashScanUtil.intentToEmail(Constants.URLs.CUSTOMER_SUPPORT_URL, "", "");
        }
    }
}
