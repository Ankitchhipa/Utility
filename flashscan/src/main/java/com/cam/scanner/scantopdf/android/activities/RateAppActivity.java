package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

public class RateAppActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_back_toolbar;
    private TextView tv_toolbar, tv_skip, tv_heading;
    private Button btn_begin;
    private FlashScanUtil flashScanUtil;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_app);
        initObjects();
        findViewIds();
        setClickListeners();
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
        btn_begin.setOnClickListener(this);
        tv_skip.setOnClickListener(this);
    }

    private void findViewIds() {
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getString(R.string.rate_the_app));
        btn_begin = findViewById(R.id.btn_begin);
        tv_skip = findViewById(R.id.tv_skip);
        tv_heading = findViewById(R.id.tv_heading);
        tv_heading.setText(getString(R.string.rate_app_msg_txt,getString(R.string.app_name)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back_toolbar || id == R.id.tv_skip) {
            onBackPressed();
        } else if (id == R.id.btn_begin) {
            flashScanUtil.rateUs();
        }
    }
}
