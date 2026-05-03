package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class BarCodeScanActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, Detector.Processor<Barcode> {

    private static final String TAG = BarCodeScanActivity.class.getSimpleName();
    private Context context;
    private BarcodeDetector barcodeDetector;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private FlashScanUtil flashScanUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code_scanner);

        findViewIds();
        initObjects();
        setClickListeners();
        initializeDetectorsAndCameraSource();
    }

    private void findViewIds() {
        surfaceView = findViewById(R.id.surfaceView);
    }

    private void initializeDetectorsAndCameraSource() {
        barcodeDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        if (barcodeDetector != null) {
            cameraSource = new CameraSource.Builder(context, barcodeDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
        }
        surfaceView.getHolder().addCallback(this);


    }

    private void setClickListeners() {

    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated called");
        if (cameraSource != null) {
            try {
                cameraSource.start(holder);
                if (barcodeDetector != null) {
                    barcodeDetector.setProcessor(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    @Override
    public void release() {
        /*Toast.makeText(context, "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        if (detections != null) {
            SparseArray<Barcode> detectedItems = detections.getDetectedItems();
            if (detectedItems != null && detectedItems.size() > 0) {
                surfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (detectedItems.valueAt(0).displayValue != null) {
                            String displayValue = detectedItems.valueAt(0).displayValue;
                            if (!TextUtils.isEmpty(displayValue)) {
                                Log.i(TAG, "result display value : " + displayValue);
                                flashScanUtil.playBeepSound();
                                navigateToBarCodeResultActivity(displayValue);
                            }
                        }
                    }
                });
                if (barcodeDetector != null)
                    barcodeDetector.release();
            }
        }

    }

    private void navigateToBarCodeResultActivity(String resultText) {
        /*Intent intent = new Intent(context, BarCodeResultActivity.class);
        intent.putExtra(Constants.PutExtraConstants.BAR_QR_CODE_RESULT, resultText);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.stop();
        }
        if (barcodeDetector != null) {
            barcodeDetector.release();
        }
    }
}
