package com.cam.scanner.scantopdf.android.barcodereader;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.barcodereader.camera.CameraSource;
import com.cam.scanner.scantopdf.android.barcodereader.camera.CameraSourcePreview;
import com.cam.scanner.scantopdf.android.barcodereader.camera.GraphicOverlay;
import com.cam.scanner.scantopdf.android.barcodereader.model.CalendarEvent;
import com.cam.scanner.scantopdf.android.barcodereader.model.ContactInfo;
import com.cam.scanner.scantopdf.android.barcodereader.model.Default;
import com.cam.scanner.scantopdf.android.barcodereader.model.Email;
import com.cam.scanner.scantopdf.android.barcodereader.model.Geo;
import com.cam.scanner.scantopdf.android.barcodereader.model.MultiplePhones;
import com.cam.scanner.scantopdf.android.barcodereader.model.Phone;
import com.cam.scanner.scantopdf.android.barcodereader.model.Product;
import com.cam.scanner.scantopdf.android.barcodereader.model.ResultBarCode;
import com.cam.scanner.scantopdf.android.barcodereader.model.SMS;
import com.cam.scanner.scantopdf.android.barcodereader.model.Text;
import com.cam.scanner.scantopdf.android.barcodereader.model.URL;
import com.cam.scanner.scantopdf.android.barcodereader.model.Wifi;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BarcodeReaderFragment extends Fragment implements View.OnTouchListener, BarcodeGraphicTracker.BarcodeGraphicTrackerListener {
    protected static final String TAG = BarcodeReaderFragment.class.getSimpleName();
    private static final String KEY_AUTO_FOCUS = "key_auto_focus";
    private static final String KEY_USE_FLASH = "key_use_flash";
    private static final String KEY_SCAN_OVERLAY_VISIBILITY = "key_scan_overlay_visibility";
    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    private static final int REQUEST_IMAGE_GET = 11;

    // constants used to pass extra data in the intent
    private boolean autoFocus = false;
    private boolean useFlash = false;
    private String beepSoundFile;
    private static final String BarcodeObject = "Barcode";
    private boolean isPaused = false;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private BarcodeReaderListener mListener;
    private SharedPreferences permissionStatus;
    private static final int PERMISSION_CALLBACK_CONSTANT = 101;
    private static final int REQUEST_PERMISSION_SETTING = 102;
    private boolean sentToSettings = false;
    private int scanOverlayVisibility;
    private RelativeLayout progress_lay;

    private Context context;

    public BarcodeReaderFragment() {
        // Required empty public constructor
    }

    static BarcodeReaderFragment newInstance(boolean autoFocus, boolean useFlash) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_AUTO_FOCUS, autoFocus);
        args.putBoolean(KEY_USE_FLASH, useFlash);
        BarcodeReaderFragment fragment = new BarcodeReaderFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public static BarcodeReaderFragment newInstance(boolean autoFocus, boolean useFlash, int scanOverlayVisibleStatus) {

        Bundle args = new Bundle();
        args.putBoolean(KEY_AUTO_FOCUS, autoFocus);
        args.putBoolean(KEY_USE_FLASH, useFlash);
        args.putInt(KEY_SCAN_OVERLAY_VISIBILITY, scanOverlayVisibleStatus);
        BarcodeReaderFragment fragment = new BarcodeReaderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this listener if you want more callbacks
     *
     * @param barcodeReaderListener
     */
    void setListener(BarcodeReaderListener barcodeReaderListener) {
        mListener = barcodeReaderListener;
    }

    public void setBeepSoundFile(String fileName) {
        beepSoundFile = fileName;
    }

    void pauseScanning() {
        isPaused = true;
    }

    public void resumeScanning() {
        isPaused = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = this.getArguments();
        if (arguments != null) {
            this.autoFocus = arguments.getBoolean(KEY_AUTO_FOCUS, false);
            this.useFlash = arguments.getBoolean(KEY_USE_FLASH, false);
            this.scanOverlayVisibility = arguments.getInt(KEY_SCAN_OVERLAY_VISIBILITY, View.VISIBLE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_barcode_reader, container, false);
        if (getActivity() != null) {
            permissionStatus = getActivity().getSharedPreferences("permissionStatus", getActivity().MODE_PRIVATE);
        }

        progress_lay = view.findViewById(R.id.progress_lay);
        mPreview = view.findViewById(R.id.preview);
        mGraphicOverlay = view.findViewById(R.id.graphicOverlay);
        ImageView iv_media = view.findViewById(R.id.iv_media);
        iv_media.setOnClickListener(new SelectImageListener());

        ScannerOverlay mScanOverlay = view.findViewById(R.id.scan_overlay);
        mScanOverlay.setVisibility(scanOverlayVisibility);
        gestureDetector = new GestureDetector(getActivity(), new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleListener());
        view.setOnTouchListener(this);
        return view;
    }


    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BarcodeReaderFragment);
        autoFocus = a.getBoolean(R.styleable.BarcodeReaderFragment_auto_focus, true);
        useFlash = a.getBoolean(R.styleable.BarcodeReaderFragment_use_flash, false);
        try {
            a.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof BarcodeReaderListener) {
            mListener = (BarcodeReaderListener) context;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null)
            return;
        permissionStatus = getActivity().getSharedPreferences("permissionStatus", getActivity().MODE_PRIVATE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.grant_permission));
                builder.setMessage(getString(R.string.permission_camera));
                builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mListener.onCameraPermissionDenied();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.CAMERA, false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.grant_permission));
                builder.setMessage(getString(R.string.permission_camera));
                builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mListener.onCameraPermissionDenied();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CALLBACK_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.CAMERA, true);
            editor.apply();
        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }

    private void proceedAfterPermission() {
        createCameraSource(autoFocus, useFlash);
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(final boolean autoFocus, final boolean useFlash) {
        Log.e(TAG, "createCameraSource:");
        Context context = getActivity();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, this);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = getActivity().registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(getActivity(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.

        CameraSource.Builder builder = new CameraSource.Builder(getActivity(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(1.0f);

        // make sure that auto focus is an available option
        builder = builder.setFocusMode(
                autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    /**
     * Trigger flash torch on and off, perhaps using a compound button.
     */
    public void setUseFlash(boolean use) {
        useFlash = use;
        mCameraSource.setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
    }

    /**
     * Trigger auto focus mode, perhaps using a compound button.
     */
    public void setAutoFocus(boolean continuous) {
        autoFocus = continuous;
        mCameraSource.setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : Camera.Parameters.FOCUS_MODE_AUTO);
    }

    /**
     * Returns true if device supports flash.
     */
    public boolean deviceSupportsFlash() {
        if (getActivity().getPackageManager() == null)
            return false;
        return getActivity().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * Restarts the camera.
     */
    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
        if (sentToSettings) {
            if (getActivity() != null)
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    proceedAfterPermission();
                } else {
                    mListener.onCameraPermissionDenied();
                }
        }
    }

    /**
     * Stops the camera.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.grant_permission));
                builder.setMessage(getString(R.string.permission_camera));
                builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mListener.onCameraPermissionDenied();
                    }
                });
                builder.show();
            } else {
                mListener.onCameraPermissionDenied();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (getActivity() != null)
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    proceedAfterPermission();
                }
        }
        switch (requestCode) {
            case REQUEST_IMAGE_GET:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        ArrayList<Uri> uriList;
                        if (FlashScanUtil.isOsLessThanR()) {
                            uriList = new ArrayList<>(Matisse.obtainResult(data));
                        } else {
                            uriList = new ArrayList<>(FlashScanUtil.obtainResult(data));
                        }
                        if (!uriList.isEmpty()) {
                            detectUri(uriList.get(0));
                        }
                    }
                }
                break;
        }
    }

    private void showProgressBar() {
        if (progress_lay != null) {
            progress_lay.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (progress_lay != null) {
            progress_lay.setVisibility(View.GONE);
        }
    }

    private void detectUri(Uri imageUri) {
        showProgressBar();
        FirebaseVisionImage firebaseVisionImage;
        try {
            firebaseVisionImage = FirebaseVisionImage.fromFilePath(context, imageUri);
            FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                    .setBarcodeFormats(
                            FirebaseVisionBarcode.FORMAT_QR_CODE,
                            FirebaseVisionBarcode.FORMAT_AZTEC)
                    .build();
            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
            detector.detectInImage(firebaseVisionImage).addOnSuccessListener(firebaseVisionBarcodes -> {
                // task completed successfully
                hideProgressBar();
                if (firebaseVisionBarcodes != null && !firebaseVisionBarcodes.isEmpty()) {
                    FirebaseVisionBarcode firebaseVisionBarcode = firebaseVisionBarcodes.get(0);
                    if (firebaseVisionBarcode != null) {
                        handleImageBarcodeResult(firebaseVisionBarcode);
                    }
                } else {
                    Toast.makeText(context, getString(R.string.error_detecting_qr_bar_code), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                hideProgressBar();
                // task failed with exception
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getActivity());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                best = barcode;
                bestDistance = distance;
            }
        }

        if (best != null) {
            Intent data = new Intent();
            data.putExtra(BarcodeObject, best);

            // TODO - pass the scanned value
            if (getActivity() != null)
                getActivity().setResult(CommonStatusCodes.SUCCESS, data);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        boolean b = scaleGestureDetector.onTouchEvent(motionEvent);

        boolean c = gestureDetector.onTouchEvent(motionEvent);

        return b || c || view.onTouchEvent(motionEvent);
    }

    @Override
    public void onScanned(final Barcode barcode) {
        if (mListener != null && !isPaused) {
            if (getActivity() == null) {
                return;
            }
            playBeep();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (barcode != null) {
                        handleCameraScannedBarcode(barcode);
                    }
                    /*mListener.onScanned(barcode);*/
                }
            });
        }
    }

    private void handleImageBarcodeResult(FirebaseVisionBarcode barcode) {
        ResultBarCode resultBarCode = new ResultBarCode();
        resultBarCode.setRawValue(barcode.getRawValue());
        switch (barcode.getValueType()) {
            case FirebaseVisionBarcode.TYPE_CALENDAR_EVENT:
                resultBarCode.setValueFormat(barcode.getValueType());
                CalendarEvent event = mapCalendarEventForImage(barcode.getCalendarEvent());
                resultBarCode.setCalendarEvent(event);
                break;
            case Barcode.CONTACT_INFO:
                resultBarCode.setValueFormat(barcode.getValueType());
                ContactInfo contactInfo = mapContactInfoForImage(barcode.getContactInfo());
                resultBarCode.setContactInfo(contactInfo);
                break;
            case Barcode.EMAIL:
                resultBarCode.setValueFormat(barcode.getValueType());
                Email email = mapEmailForImage(barcode.getEmail());
                resultBarCode.setEmail(email);
                break;
            case Barcode.GEO:
                resultBarCode.setValueFormat(barcode.getValueType());
                Geo geo = mapGeoForImage(barcode.getGeoPoint());
                resultBarCode.setGeo(geo);
                break;
            case Barcode.PHONE:
                resultBarCode.setValueFormat(barcode.getValueType());
                Phone phone = mapPhoneForImage(barcode.getPhone());
                resultBarCode.setPhone(phone);
                break;
            case Barcode.PRODUCT:
                resultBarCode.setValueFormat(barcode.getValueType());
                Product product = mapProduct(barcode.getDisplayValue());
                resultBarCode.setProduct(product);
                break;
            case Barcode.SMS:
                resultBarCode.setValueFormat(barcode.getValueType());
                SMS sms = mapSmsForImage(barcode.getSms());
                resultBarCode.setSms(sms);
                break;
            case Barcode.TEXT:
                resultBarCode.setValueFormat(barcode.getValueType());
                Text text = mapText(barcode.getDisplayValue());
                resultBarCode.setText(text);
                break;
            case Barcode.URL:
                resultBarCode.setValueFormat(barcode.getValueType());
                URL url = mapUrlForImage(barcode.getUrl());
                resultBarCode.setUrl(url);
                break;
            case Barcode.WIFI:
                resultBarCode.setValueFormat(barcode.getValueType());
                Wifi wifi = mapWifiForImage(barcode.getWifi());
                resultBarCode.setWifi(wifi);
                break;
            default:
                resultBarCode.setValueFormat(0);
                Default aDefault = mapDefault(barcode.getDisplayValue());
                resultBarCode.setaDefault(aDefault);
                break;
        }
        mListener.onScanned(resultBarCode);
    }


    private void handleCameraScannedBarcode(Barcode barcode) {
        ResultBarCode resultBarCode = new ResultBarCode();
        resultBarCode.setRawValue(barcode.rawValue);
        switch (barcode.valueFormat) {
            case Barcode.CALENDAR_EVENT:
                resultBarCode.setValueFormat(barcode.valueFormat);
                CalendarEvent event = mapCalendarEvent(barcode.calendarEvent);
                resultBarCode.setCalendarEvent(event);
                break;
            case Barcode.CONTACT_INFO:
                resultBarCode.setValueFormat(barcode.valueFormat);
                ContactInfo contactInfo = mapContactInfo(barcode.contactInfo);
                resultBarCode.setContactInfo(contactInfo);
                break;
            case Barcode.EMAIL:
                resultBarCode.setValueFormat(barcode.valueFormat);
                Email email = mapEmail(barcode.email);
                resultBarCode.setEmail(email);
                break;
            case Barcode.GEO:
                resultBarCode.setValueFormat(barcode.valueFormat);
                Geo geo = mapGeo(barcode.geoPoint);
                resultBarCode.setGeo(geo);
                break;
            case Barcode.PHONE:
                resultBarCode.setValueFormat(barcode.valueFormat);
                Phone phone = mapPhone(barcode.phone);
                resultBarCode.setPhone(phone);
                break;
            case Barcode.PRODUCT:
                resultBarCode.setValueFormat(barcode.valueFormat);
                Product product = mapProduct(barcode.displayValue);
                resultBarCode.setProduct(product);
                break;
            case Barcode.SMS:
                resultBarCode.setValueFormat(barcode.valueFormat);
                SMS sms = mapSms(barcode.sms);
                resultBarCode.setSms(sms);
                break;
            case Barcode.TEXT:
                resultBarCode.setValueFormat(barcode.valueFormat);
                Text text = mapText(barcode.displayValue);
                resultBarCode.setText(text);
                break;
            case Barcode.URL:
                resultBarCode.setValueFormat(barcode.valueFormat);
                URL url = mapUrl(barcode.url);
                resultBarCode.setUrl(url);
                break;
            case Barcode.WIFI:
                resultBarCode.setValueFormat(barcode.valueFormat);
                Wifi wifi = mapWifi(barcode.wifi);
                resultBarCode.setWifi(wifi);
                break;
            default:
                resultBarCode.setValueFormat(0);
                Default aDefault = mapDefault(barcode.displayValue);
                resultBarCode.setaDefault(aDefault);
                break;
        }
        mListener.onScanned(resultBarCode);
    }

    private Default mapDefault(String displayValue) {
        Default aDefault = new Default();
        aDefault.setDefaultText(displayValue);
        return aDefault;
    }

    private Wifi mapWifiForImage(FirebaseVisionBarcode.WiFi wifi) {
        Wifi wifi1 = new Wifi();
        wifi1.setEncryptionType(wifi.getEncryptionType());
        wifi1.setPassword(wifi.getPassword());
        wifi1.setSsid(wifi.getSsid());
        return wifi1;
    }

    private Wifi mapWifi(Barcode.WiFi wifi) {
        Wifi wifi1 = new Wifi();
        wifi1.setEncryptionType(wifi.encryptionType);
        wifi1.setPassword(wifi.password);
        wifi1.setSsid(wifi.ssid);
        return wifi1;
    }

    private URL mapUrlForImage(FirebaseVisionBarcode.UrlBookmark url) {
        URL url1 = new URL();
        url1.setUrl(url.getUrl());
        return url1;
    }

    private URL mapUrl(Barcode.UrlBookmark url) {
        URL url1 = new URL();
        url1.setUrl(url.url);
        return url1;
    }

    private Text mapText(String displayValue) {
        Text text = new Text();
        text.setText(displayValue);
        return text;
    }

    private SMS mapSmsForImage(FirebaseVisionBarcode.Sms sms) {
        SMS sms1 = new SMS();
        sms1.setMessage(sms.getMessage());
        sms1.setPhoneNumber(sms.getPhoneNumber());
        return sms1;
    }

    private SMS mapSms(Barcode.Sms sms) {
        SMS sms1 = new SMS();
        sms1.setMessage(sms.message);
        sms1.setPhoneNumber(sms.phoneNumber);
        return sms1;
    }


    private Product mapProduct(String displayValue) {
        Product product = new Product();
        product.setProductText(displayValue);
        return product;
    }

    private Phone mapPhoneForImage(FirebaseVisionBarcode.Phone phone) {
        Phone phone1 = new Phone();
        phone1.setNumber(phone.getNumber());
        return phone1;
    }

    private Phone mapPhone(Barcode.Phone phone) {
        Phone phone1 = new Phone();
        phone1.setNumber(phone.number);
        return phone1;
    }

    private Geo mapGeoForImage(FirebaseVisionBarcode.GeoPoint geoPoint) {
        Geo geo = new Geo();
        geo.setLat(geoPoint.getLat());
        geo.setLng(geoPoint.getLng());
        return geo;
    }

    private Geo mapGeo(Barcode.GeoPoint geoPoint) {
        Geo geo = new Geo();
        geo.setLat(geoPoint.lat);
        geo.setLng(geoPoint.lng);
        return geo;
    }

    private Email mapEmailForImage(FirebaseVisionBarcode.Email email) {
        Email mEmail = new Email();
        mEmail.setAddress(email.getAddress());
        mEmail.setBody(email.getBody());
        mEmail.setSubject(email.getSubject());
        return mEmail;
    }

    private Email mapEmail(Barcode.Email email) {
        Email mEmail = new Email();
        mEmail.setAddress(email.address);
        mEmail.setBody(email.body);
        mEmail.setSubject(email.subject);
        return mEmail;
    }

    private ContactInfo mapContactInfoForImage(FirebaseVisionBarcode.ContactInfo contactInfo) {
        ContactInfo info = new ContactInfo();
        List<FirebaseVisionBarcode.Email> emails = contactInfo.getEmails();
        if (emails.size() > 0) {
            ArrayList<String> emailAddresses = new ArrayList<>();
            for (FirebaseVisionBarcode.Email email : emails) {
                emailAddresses.add(email.getAddress());
            }
            if (!emailAddresses.isEmpty()) {
                info.setEmails(emailAddresses);
            }
        }
        info.setName(contactInfo.getName().getFormattedName());
        info.setOrganization(contactInfo.getOrganization());
        List<FirebaseVisionBarcode.Phone> phones = contactInfo.getPhones();
        if (phones.size() > 0) {
            ArrayList<MultiplePhones> phoneList = new ArrayList<>();
            for (FirebaseVisionBarcode.Phone phone : phones) {
                MultiplePhones multiplePhones = new MultiplePhones();
                multiplePhones.setNumber(phone.getNumber());
                multiplePhones.setType(phone.getType());
                phoneList.add(multiplePhones);
            }
            if (!phoneList.isEmpty()) {
                info.setMultiplePhones(phoneList);
            }
        }
        info.setTitle(contactInfo.getTitle());
        String address = null;
        List<FirebaseVisionBarcode.Address> addresses = contactInfo.getAddresses();
        if (!addresses.isEmpty()) {
            String[] addressLines = addresses.get(0).getAddressLines();
            if (addressLines.length > 0) {
                address = addressLines[0].trim();
            }
        }
        info.setAddress(address);
        String[] urls = contactInfo.getUrls();
        if (urls != null && urls.length > 0) {
            info.setUrls(urls);
        }
        return info;
    }

    private ContactInfo mapContactInfo(Barcode.ContactInfo contactInfo) {
        ContactInfo info = new ContactInfo();
        Barcode.Email[] emails = contactInfo.emails;
        if (emails != null && emails.length > 0) {
            ArrayList<String> emailAddresses = new ArrayList<>();
            for (Barcode.Email email : emails) {
                emailAddresses.add(email.address);
            }
            if (!emailAddresses.isEmpty()) {
                info.setEmails(emailAddresses);
            }

        }
        /*String email = null;
        if (emails != null && emails.length > 0) {
            email = emails[0].address;
        }
        info.setEmail(email);*/
        info.setName(contactInfo.name.formattedName);
        info.setOrganization(contactInfo.organization);
        Barcode.Phone[] phones = contactInfo.phones;

        if (phones != null && phones.length > 0) {
            ArrayList<MultiplePhones> phoneList = new ArrayList<>();
            for (Barcode.Phone phone : phones) {
                MultiplePhones multiplePhones = new MultiplePhones();
                multiplePhones.setNumber(phone.number);
                multiplePhones.setType(phone.type);
                phoneList.add(multiplePhones);
            }
            if (!phoneList.isEmpty()) {
                info.setMultiplePhones(phoneList);
            }
        }
        info.setTitle(contactInfo.title);
        String address = null;
        Barcode.Address[] addresses = contactInfo.addresses;
        if (addresses != null && addresses.length > 0) {
            String[] addressLines = addresses[0].addressLines;
            if (addressLines != null && addressLines.length > 0) {
                address = addressLines[0].trim();
            }
        }
        info.setAddress(address);

        String[] urls = contactInfo.urls;
        if (urls != null && urls.length > 0) {
            info.setUrls(urls);
        }
        return info;
    }

    private void insertPhones(Barcode.Phone[] phones) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        ArrayList<ContentValues> data = new ArrayList<>();
        for (Barcode.Phone phone : phones) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.number);
            contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, phone.type);
            data.add(contentValues);
        }
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
        startActivity(intent);
    }

    private CalendarEvent mapCalendarEventForImage(FirebaseVisionBarcode.CalendarEvent calendarEvent) {
        CalendarEvent event = new CalendarEvent();
        event.setDescription(calendarEvent.getDescription());
        event.setLocation(calendarEvent.getLocation());
        event.setOrganizer(calendarEvent.getOrganizer());
        event.setStatus(calendarEvent.getStatus());
        event.setSummary(calendarEvent.getSummary());
        event.setStartYear(calendarEvent.getStart().getYear());
        event.setStartMonth(calendarEvent.getStart().getMonth());
        event.setStartDay(calendarEvent.getStart().getDay());
        event.setEndYear(calendarEvent.getEnd().getYear());
        event.setEndMonth(calendarEvent.getEnd().getMonth());
        event.setEndDay(calendarEvent.getEnd().getDay());
        return event;
    }

    private CalendarEvent mapCalendarEvent(Barcode.CalendarEvent calendarEvent) {
        CalendarEvent event = new CalendarEvent();
        event.setDescription(calendarEvent.description);
        event.setLocation(calendarEvent.location);
        event.setOrganizer(calendarEvent.organizer);
        event.setStatus(calendarEvent.status);
        event.setSummary(calendarEvent.summary);
        event.setStartYear(calendarEvent.start.year);
        event.setStartMonth(calendarEvent.start.month);
        event.setStartDay(calendarEvent.start.day);
        event.setEndYear(calendarEvent.end.year);
        event.setEndMonth(calendarEvent.end.month);
        event.setEndDay(calendarEvent.end.day);
        return event;
    }

    @Override
    public void onScannedMultiple(final List<Barcode> barcodes) {
        if (mListener != null && !isPaused) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListener.onScannedMultiple(barcodes);
                }
            });

        }
    }

    @Override
    public void onBitmapScanned(final SparseArray<Barcode> sparseArray) {
        if (mListener != null) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListener.onBitmapScanned(sparseArray);
                }
            });

        }
    }

    @Override
    public void onScanError(final String errorMessage) {
        if (mListener != null) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListener.onScanError(errorMessage);
                }
            });

        }
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    private void playBeep() {
        MediaPlayer m = new MediaPlayer();
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }

            if (getActivity() == null)
                return;
            AssetFileDescriptor descriptor = getActivity().getAssets().openFd("beep.mp3");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface BarcodeReaderListener {
        void onScanned(ResultBarCode barcode);

        void onScannedMultiple(List<Barcode> barcodes);

        void onBitmapScanned(SparseArray<Barcode> sparseArray);

        void onScanError(String errorMessage);

        void onCameraPermissionDenied();

    }

    private class SelectImageListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            goForImageSelection();
        }
    }

    private void goForImageSelection() {
        if (FlashScanUtil.isOsLessThanR()) {
            try {
                Matisse.from(this).choose(MimeType.ofImage(), false).countable(true)
                        .showSingleMediaType(true)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.9f).maxSelectable(1).imageEngine(new GlideEngine())
                        .forResult(REQUEST_IMAGE_GET);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "" + getString(R.string.no_app_handle), Toast.LENGTH_SHORT).show();
            }

        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

}
