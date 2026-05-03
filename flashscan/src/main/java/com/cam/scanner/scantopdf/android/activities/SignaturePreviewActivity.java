package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class SignaturePreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private Context context;
    private TextView tv_crop, tv_done;
    private Uri imageUri;
    private long lastClickedTime = 0;
    private FlashScanUtil flashScanUtil;
    private static final int IMAGE_COMPRESSION = 80;
    private static final int ASPECT_RATIO_X = 1;
    private static final int ASPECT_RATIO_Y = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_preview);

        initObjects();
        findViewIds();
        setClickListeners();
        getImageUriAndSetBitmap();
    }

    private void getImageUriAndSetBitmap() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.URI)) {
            imageUri = getIntent().getParcelableExtra(Constants.PutExtraConstants.URI);
        }
        if (imageUri != null) {
            if (!isFinishing() || !isDestroyed()) {
                Glide.with(context).asBitmap().load(imageUri).into(imageView);
            }

        }
    }

    private void setClickListeners() {
        tv_crop.setOnClickListener(this);
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
    }

    private void findViewIds() {
        imageView = findViewById(R.id.imageView);
        tv_crop = findViewById(R.id.tv_crop);
        tv_done = findViewById(R.id.tv_done);
        tv_done.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_crop) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            if (imageUri != null) {
                cropImage(imageUri);
            }
        }
    }

    private void cropImage(Uri sourceUri) {
        String uriName = flashScanUtil.uriNameFromUri(getContentResolver(), sourceUri);
        Uri destinationUri = null;
        if (!TextUtils.isEmpty(uriName)) {
            destinationUri = Uri.fromFile(new File(getCacheDir(), uriName));
        }
        if (destinationUri != null) {
            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(IMAGE_COMPRESSION);
            options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            options.setToolbarWidgetColor(ContextCompat.getColor(context, android.R.color.white));
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            options.setActiveControlsWidgetColor(ContextCompat.getColor(context, R.color.colorPrimary));
            /*options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary));*/
            options.withAspectRatio(ASPECT_RATIO_X, ASPECT_RATIO_Y);
            options.withMaxResultSize(1024, 1024);
            options.setFreeStyleCropEnabled(true);
            UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
                    .start(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri uri = UCrop.getOutput(data);
                        if (uri != null) {
                           /* bitmapFromUri = flashScanUtil.getBitmapFromUri(uri);
                            if (bitmapFromUri != null) {
                                imageView.setImageBitmap(bitmapFromUri);
                            }*/
                            Intent intent = new Intent();
                            intent.putExtra(Constants.PutExtraConstants.URI, uri);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
                break;
            case UCrop.RESULT_ERROR:
                if (data != null) {
                    Throwable error = UCrop.getError(data);
                    if (error != null) {
                        flashScanUtil.showSnackBar(findViewById(android.R.id.content), "" + error.getMessage());
                    }
                }
                break;
        }
    }
}
