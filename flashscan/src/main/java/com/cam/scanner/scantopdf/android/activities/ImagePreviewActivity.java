package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.Constants;

public class ImagePreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView, iv_back_toolbar;
    private String imagePath;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        init();
        findIds();
        setClickListeners();
        getIntentData();
        if (!TextUtils.isEmpty(imagePath)) {
            if (!isFinishing() || !isDestroyed()) {
                Glide.with(context).load(imagePath).into(imageView);
            }

        }
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
    }

    private void init() {
        context = this;
    }

    private void getIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FILE_PATH)) {
            imagePath = getIntent().getStringExtra(Constants.PutExtraConstants.FILE_PATH);
        }
    }

    private void findIds() {
        imageView = findViewById(R.id.imageView);
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_toolbar) {
            onBackPressed();
        }
    }
}
