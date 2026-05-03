package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import java.io.File;

// cuurently not in use
public class CropImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_FOR_EDIT_SELECTED_IMAGE = 101;
    /*private CropImageView cropImageView;*/
    private Context context;
    private Button btn_save, btn_rotate_left, btn_rotate_right;
    private String imagePath;
    private static final String TAG = CropImageActivity.class.getSimpleName();
    private Uri uriForFile;
    private String tempFolderName;
    private FlashScanUtil flashScanUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        init();
        findIds();
        setClickListeners();

        getImagePath();

    }

    private void getImagePath() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FOLDER_NAME)) {
            tempFolderName = getIntent().getStringExtra(Constants.PutExtraConstants.FOLDER_NAME);
        }
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FILE_PATH)) {
            imagePath = getIntent().getStringExtra(Constants.PutExtraConstants.FILE_PATH);
        }
        if (!TextUtils.isEmpty(imagePath)) {
            File file = new File(imagePath);
            if (file.isFile() && file.exists()) {

                uriForFile = FileProvider.getUriForFile(context, Constants.AUTHORITY_APP, file);
                if (uriForFile != null) {
                    setUpCropImageView(uriForFile);
                }
            }

        }
    }

    private void setUpCropImageView(Uri uri) {
        /*cropImageView.setImageUriAsync(uri);*/
    }

    private void setClickListeners() {
        btn_rotate_left.setOnClickListener(this);
        btn_rotate_right.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        /*cropImageView.setOnCropImageCompleteListener(this);*/
    }

    private void init() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
    }

    private void findIds() {
       /* cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setCropShape(CropImageView.CropShape.OVAL);*/
        btn_save = findViewById(R.id.btn_save);
        btn_rotate_left = findViewById(R.id.btn_rotate_left);
        btn_rotate_right = findViewById(R.id.btn_rotate_right);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_rotate_left) {/*cropImageView.rotateImage(-90);*/
        } else if (id == R.id.btn_rotate_right) {/*cropImageView.rotateImage(90);*/
        } else if (id == R.id.btn_save) {/*cropImageView.getCroppedImageAsync();*/
        }
    }
/*
    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        Log.e(TAG, "onCropImageComplete called");
        if (result != null) {
            if (result.isSuccessful()) {
                *//*Uri uri = result.getUri();
                if (uri != null && !TextUtils.isEmpty(imagePath) && !TextUtils.isEmpty(tempFolderName)) {
                    intentToResultFragment(uri, imagePath, tempFolderName);
                }*//*
                Bitmap scannedBitmap = result.getBitmap();
                if (scannedBitmap != null) {
                    String savedBitmapFileName = flashScanUtil.getSavedBitmapFileName(context, scannedBitmap);
                    scannedBitmap.recycle();
                    if (!TextUtils.isEmpty(savedBitmapFileName)) {
                        intentToResultFragment(savedBitmapFileName, imagePath, tempFolderName);
                    }
                }
            }
        }
    }*/

    /*private void intentToResultFragment(String scannedBitmapFileName, String imagePath, String tempFolderName) {
        Intent intent = new Intent(context, ScanActivity.class);
        intent.putExtra(Constants.PutExtraConstants.IS_COMING_FROM_CROPPED_IMAGE_ACTIVITY, true);
        intent.putExtra(Constants.PutExtraConstants.SCANNED_RESULT, scannedBitmapFileName);
        intent.putExtra(Constants.PutExtraConstants.FILE_PATH, imagePath);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_NAME, tempFolderName);
        startActivityForResult(intent, REQUEST_CODE_FOR_EDIT_SELECTED_IMAGE);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_FOR_EDIT_SELECTED_IMAGE:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e(TAG, "RESULT_OK");
                        String folderName = null;
                        if (data != null && data.hasExtra(Constants.PutExtraConstants.FOLDER_NAME)) {
                            folderName = data.getStringExtra(Constants.PutExtraConstants.FOLDER_NAME);
                        }
                        if (!TextUtils.isEmpty(folderName)) {
                            Intent intent = new Intent();
                            intent.putExtra(Constants.PutExtraConstants.FOLDER_NAME, folderName);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                        break;
                    case RESULT_CANCELED:
                        Log.e(TAG, "RESULT_CANCELED");
                        break;
                }
                break;
        }
    }
}
