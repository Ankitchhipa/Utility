package com.cam.scanner.scantopdf.android.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.ads.AdClosed;
import com.cam.scanner.scantopdf.android.asynctasks.CreateOcrDocument;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.dialogs.OcrChoosePlanDialog;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.interfaces.DocumentCreationListener;
import com.cam.scanner.scantopdf.android.rest.ApiInterface;
import com.cam.scanner.scantopdf.android.rest.UpdateCreditsToApi;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class OcrPreviewActivity extends BaseActivity implements View.OnClickListener, DocumentCreationListener, AdClosed {

    private static final int SINGLE_TEXT = 1;
    private static final int WHOLE_TEXT = 2;
    private static final String TAG = OcrPreviewActivity.class.getSimpleName();
    private ImageView imageView, iv_back_toolbar;
    private Uri imageUri;
    private FlashScanUtil flashScanUtil;
    private Context context;
    private TextView tv_done, tv_rotate_left, tv_rotate_right, tv_crop;
    private Bitmap bitmapFromUri;
    private View progress_lay;
    private Button btn_progress_lay;
    private String recognizedText;
    private long lastClickedTime = 0;
    private static final int REQUEST_TAKE_PHOTO = 99;
    private static final int PICK_IMAGE_REQ_CODE = 100;
    private static final int IMAGE_COMPRESSION = 80;
    private static final int ASPECT_RATIO_X = 1;
    private static final int ASPECT_RATIO_Y = 1;
    private PrefManager prefManager;
    private boolean isMovedToResultScreen = false;

    private DBHandler dbHandler;

    private boolean shouldIntersCreateShow;
    private boolean isNetWorking;

    private FlashScanUtil util;

    private ApiInterface apiInterface;
    private String fromSource="";
    private String createdFolderPath="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_ocr_preview);
        initObjects();
        findViewIds();
        setClickListeners();
        getImageUriAndSetBitmap();
        getFromIntent();

    }

    private void getFromIntent() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.OCR_IS_NET_WORKING)) {
            isNetWorking = getIntent().getBooleanExtra(Constants.PutExtraConstants.OCR_IS_NET_WORKING, false);
            fromSource = getIntent().getStringExtra(Constants.PutExtraConstants.FROM_SOURCE);
        }
    }


    private void setClickListeners() {
        tv_done.setOnClickListener(this);
        tv_rotate_left.setOnClickListener(this);
        tv_rotate_right.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
        iv_back_toolbar.setOnClickListener(this);
        tv_crop.setOnClickListener(this);
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        prefManager = new PrefManager(context);
        dbHandler = AppController.getINSTANCE().dbHandler;
        util = new FlashScanUtil(context);
    }

    private void getImageUriAndSetBitmap() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.URI)) {
            imageUri = getIntent().getParcelableExtra(Constants.PutExtraConstants.URI);
        }
        if (imageUri != null) {
            /*bitmapFromUri = flashScanUtil.getBitmapFromUri(imageUri);
            if (bitmapFromUri != null) {
                imageView.setImageBitmap(bitmapFromUri);
            }*/
            if (!isFinishing() || !isDestroyed()) {
                Glide.with(context).asBitmap().load(imageUri).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmapFromUri = resource;
                        imageView.setImageBitmap(bitmapFromUri);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            }

        }
    }

    private void findViewIds() {
        imageView = findViewById(R.id.imageView);
        tv_done = findViewById(R.id.tv_done);
        tv_rotate_left = findViewById(R.id.tv_rotate_left);
        tv_rotate_right = findViewById(R.id.tv_rotate_right);
        progress_lay = findViewById(R.id.progress_lay);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        tv_crop = findViewById(R.id.tv_crop);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_done) {
            if (checkOcrCount() && bitmapFromUri != null) {
                /*showAreaSelectDialog(bitmapFromUri);*/
                detectTextFromBitmap(bitmapFromUri, WHOLE_TEXT);
            }
        } else if (id == R.id.tv_rotate_left) {
            if (bitmapFromUri != null) {
                bitmapFromUri = flashScanUtil.rotateImage(bitmapFromUri, -90);
                imageView.setImageBitmap(bitmapFromUri);
            }
        } else if (id == R.id.tv_rotate_right) {
            if (bitmapFromUri != null) {
                bitmapFromUri = flashScanUtil.rotateImage(bitmapFromUri, 90);
                imageView.setImageBitmap(bitmapFromUri);
            }
        } else if (id == R.id.btn_progress_lay) {
        } else if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.tv_crop) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            if (imageUri != null) {
                cropImage(imageUri);
            }
        }
    }
    private boolean checkOcrCount() {

        boolean canDoOcr = true;

        int allowedFreeOcr = dbHandler.getAllowedFreeOcr();
        Log.i(TAG, "allowedFreeOcr: " + allowedFreeOcr);

        int existingOcrFreeAttempted = dbHandler.getOcrFreeAttempted();
        Log.i(TAG, "existingOcrFreeAttempted: " + existingOcrFreeAttempted);

        int allowedPremiumYearly = 0;
        int allowedOcrMonthly = 0;

        int existingOcrMonthlyAttempted = 0;
        int existingOcrPremiumYearlyAttempted = 0;

        if (prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
            allowedPremiumYearly = dbHandler.getAllowedPremiumYearlyOcr();
            existingOcrPremiumYearlyAttempted = dbHandler.getOcrPremiumYearlyAttempted();

            Log.i(TAG, "allowedPremiumYearly: " + allowedPremiumYearly);
            Log.i(TAG, "existingOcrPremiumYearlyAttempted: " + existingOcrPremiumYearlyAttempted);
        }

        if (prefManager.isOcrMonthly()) {
            allowedOcrMonthly = dbHandler.getAllowedOcrMonthly();
            existingOcrMonthlyAttempted = dbHandler.getOcrMonthlyAttempted();

            Log.i(TAG, "allowedOcrMonthly: " + allowedOcrMonthly);
            Log.i(TAG, "existingOcrMonthlyAttempted: " + existingOcrMonthlyAttempted);
        }

        int totalAllowedOcr = allowedFreeOcr + allowedPremiumYearly + allowedOcrMonthly;

        int totalAttemptedOcr = existingOcrFreeAttempted + existingOcrPremiumYearlyAttempted + existingOcrMonthlyAttempted;

        if (totalAttemptedOcr >= totalAllowedOcr) {
            if (!prefManager.isPremiumYearly() /*&& !prefManager.isPremiumQuarterly()*/ && !prefManager.isOcrMonthly()) {
                askToChoosePlan();
            } else {
                dlgOcrComplete(allowedFreeOcr, allowedPremiumYearly, allowedOcrMonthly);
            }
            canDoOcr = false;
        }
        return canDoOcr;
    }

    private void askToChoosePlan() {
        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();
        String existingEmailInDb = dbHandler.getEmail(deviceIdOfInstallTime);
        if (Constants.IS_OWN_API_IMPLEMENT && (existingEmailInDb == null || existingEmailInDb.isEmpty())) {
            openAskEmailActivity(Constants.PLAN_ACTIVITY_CHOOSE_PLAN);
        } else {
            openChoosePlanDialogActivity();
        }
    }
    private void openChoosePlanDialogActivity() {
        Intent intent = new Intent(OcrPreviewActivity.this, OcrChoosePlanDialog.class);
        intent.putExtra(Constants.FROM_NAV_CHOOSE_PLAN, Constants.NAV_FROM_OCRACTIVITY);
        startActivity(intent);
    }
    private void dlgOcrComplete(int allowedFreeOcr, int allowedPremiumYearly, int allowedOcrMonthly) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog_yes_no);

        TextView msgHeading = dialog.findViewById(R.id.msg_heading);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        TextView dialogTitle = dialog.findViewById(R.id.tv_dialog_title);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);

        dialogTitle.setText(getString(R.string.warning));

        int totalAllowed = allowedFreeOcr + allowedPremiumYearly + allowedOcrMonthly;

        if (!prefManager.isPremiumYearly()/* && !prefManager.isPremiumQuarterly()*/ && !prefManager.isOcrMonthly()) {
            //Will never come in this condition bcoz handled before this method call
            msgHeading.setText(String.format(getString(R.string.free_ocr_complete), String.valueOf(allowedFreeOcr)));
            //Show both plans
        } else if (!prefManager.isOcrMonthly()) {
//            msgHeading.setText(String.format(getString(R.string.free_and_premium_ocr_complete), String.valueOf(totalAllowed)));
            msgHeading.setText(String.format(getString(R.string.free_and_premium_ocr_complete), String.valueOf(allowedPremiumYearly)));
            //Show OCR monthly plan only
        } else if (!prefManager.isPremiumYearly() /*&& !prefManager.isPremiumQuarterly()*/) {
//            msgHeading.setText(String.format(getString(R.string.free_and_ocr_mohtly_complete), String.valueOf(totalAllowed)));
            msgHeading.setText(String.format(getString(R.string.free_and_ocr_mohtly_complete), String.valueOf(allowedOcrMonthly)));
            //Show premium plan only
        } else {
//            msgHeading.setText(String.format(getString(R.string.premium_ocr_monthly_complete), String.valueOf(totalAllowed)));
            int paidTotal = allowedPremiumYearly + allowedOcrMonthly;
            msgHeading.setText(String.format(getString(R.string.premium_ocr_monthly_complete), String.valueOf(paidTotal)));
        }

        btn_ok.setText(getString(R.string.ok));
        btn_cancel.setText(getString(R.string.cancel));

        btn_ok.setOnClickListener(v -> {

            if (!prefManager.isPremiumYearly() /*&& !prefManager.isPremiumQuarterly()*/ && !prefManager.isOcrMonthly()) {
                //Show both plans
                askToChoosePlan();
            } else if (!prefManager.isOcrMonthly()) {
                //Show OCR monthly plan only
                askToGetOcrMonthly();
            } else if (!prefManager.isPremiumYearly() /*&& !prefManager.isPremiumQuarterly()*/) {
                //Show premium plan only
                askToBePremium();
            } else {
                //just dismiss dialog
            }

            dialog.dismiss();
        });

        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void askToGetOcrMonthly() {
        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();
        String existingEmailInDb = dbHandler.getEmail(deviceIdOfInstallTime);
        if (Constants.IS_OWN_API_IMPLEMENT && (existingEmailInDb == null || existingEmailInDb.isEmpty())) {
            openAskEmailActivity(Constants.PLAN_ACTIVITY_OCR_MONTHLY);
        } else {
            openOcrMonthlyDialogActivity();
        }
    }

    private void askToBePremium() {
        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();
        String existingEmailInDb = dbHandler.getEmail(deviceIdOfInstallTime);
        if (Constants.IS_OWN_API_IMPLEMENT && (existingEmailInDb == null || existingEmailInDb.isEmpty())) {
            openAskEmailActivity(Constants.PLAN_ACTIVITY_PREMIUM);
        } else {
            openPremiumActivity();
        }
    }

    private void openAskEmailActivity(int whichPlanActivity) {
        Intent intent = new Intent(OcrPreviewActivity.this, AskEmailActivity.class);
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity);
        startActivity(intent);
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(OcrPreviewActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }
    private void openOcrMonthlyDialogActivity() {
        Intent intent = new Intent(OcrPreviewActivity.this, OcrPlanDialog.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_OCR_MONTHLY);
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
                            if (!isFinishing() || !isDestroyed()) {
                                Glide.with(context).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        bitmapFromUri = resource;
                                        imageView.setImageBitmap(resource);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                            }

                            /*bitmapFromUri = flashScanUtil.getBitmapFromUri(uri);
                            if (bitmapFromUri != null) {
                                imageView.setImageBitmap(bitmapFromUri);
                            }*/
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

    public void disableProgress() {

    }


    private void detectTextFromBitmap(Bitmap bitmapFromUri, int textMode) {
        progress_lay.setVisibility(View.VISIBLE);
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmapFromUri);

        FirebaseVisionTextRecognizer textRecognizer;

        if (!isNetWorking || !Constants.IS_CLOUD_VISION_ALLOW) {
            //On-device
            textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        } else {

            //Cloud
        /*FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudTextRecognizer();*/

            // Or, to provide language hints to assist with language detection:
            // See https://cloud.google.com/vision/docs/languages for supported languages

            FirebaseVisionCloudTextRecognizerOptions options;
            if (!BuildConfig.DEBUG) {
                options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("en", "hi"))
                        .enforceCertFingerprintMatch()
                        .build();

            } else {
                options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("en", "hi"))
                        .build();
            }

            /*FirebaseVisionCloudImageLabelerOptions.Builder optionsBuilder =
                    new FirebaseVisionCloudImageLabelerOptions.Builder();
            if (!BuildConfig.DEBUG) {
                // Requires physical, non-rooted device:
                optionsBuilder.enforceCertFingerprintMatch();
            }*/

            if (!BuildConfig.DEBUG) {
                // Requires physical, non-rooted device:
                boolean isEnforceCert = options.isEnforceCertFingerprintMatch();
                Log.i(TAG, "isEnforceCert: " + isEnforceCert);
            }

            // Or, to change the default settings:
            textRecognizer = FirebaseVision.getInstance()
                    .getCloudTextRecognizer(options);

        }


        textRecognizer.processImage(firebaseVisionImage)
                .addOnSuccessListener(firebaseVisionText -> {
                    progress_lay.setVisibility(View.GONE);
                    if (firebaseVisionText != null) {
                        processResult(firebaseVisionText, textMode, bitmapFromUri);
                    }

                    increaseOcrAttempted();
                    updateCreditsToApi();
                }).addOnFailureListener(e -> {
            progress_lay.setVisibility(View.GONE);
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCreditsToApi() {
        if (Constants.IS_OWN_API_IMPLEMENT) {

            progress_lay.setVisibility(View.VISIBLE);

            UpdateCreditsToApi updateCreditsToApi = new UpdateCreditsToApi(context, new OnApiResult() {
                @Override
                public void onApiResponse() {
                    Log.i(TAG, "onApiResponse");
                    progress_lay.setVisibility(View.GONE);
                }

                @Override
                public void onApiFailure() {
                    Log.i(TAG, "onApiFailure");
                    progress_lay.setVisibility(View.GONE);
                }
            });

            updateCreditsToApi.doUpdateCredits(1);
        }
    }

    /*private void putInDb(UpdateCredits updateCredits) {
        int credits = updateCredits.getCredits();
        Log.i(TAG, "Credits: " + credits);

        if (dbHandler.existCreditsFromApi()) {
            dbHandler.updateCreditsFromApi(credits);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertCreditsFromApi(credits);
            Log.i(TAG, "insert");
        }
    }

    private RequestUpdateCredits requestParams(int creditsToDeduct) {
        RequestUpdateCredits requestUpdateCredits = new RequestUpdateCredits();

        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();

        requestUpdateCredits.setDeviceId(deviceIdOfInstallTime);
        requestUpdateCredits.setCredits(creditsToDeduct);

        return requestUpdateCredits;
    }*/

    private void increaseOcrAttempted() {

        int allowedFreeOcr = dbHandler.getAllowedFreeOcr();
        Log.i(TAG, "allowedFreeOcr: " + allowedFreeOcr);

        int existingOcrFreeAttempted = dbHandler.getOcrFreeAttempted();
        Log.i(TAG, "existingOcrFreeAttempted: " + existingOcrFreeAttempted);

        int allowedPremiumYearly = 0;
        int allowedOcrMonthly = 0;

        int existingOcrMonthlyAttempted = 0;
        int existingOcrPremiumYearlyAttempted = 0;

        if (prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
            allowedPremiumYearly = dbHandler.getAllowedPremiumYearlyOcr();
            existingOcrPremiumYearlyAttempted = dbHandler.getOcrPremiumYearlyAttempted();

            Log.i(TAG, "allowedPremiumYearly: " + allowedPremiumYearly);
            Log.i(TAG, "existingOcrPremiumYearlyAttempted: " + existingOcrPremiumYearlyAttempted);
        }

        if (prefManager.isOcrMonthly()) {
            allowedOcrMonthly = dbHandler.getAllowedOcrMonthly();
            existingOcrMonthlyAttempted = dbHandler.getOcrMonthlyAttempted();

            Log.i(TAG, "allowedOcrMonthly: " + allowedOcrMonthly);
            Log.i(TAG, "existingOcrMonthlyAttempted: " + existingOcrMonthlyAttempted);
        }

        int whichPlan = 0;
        int currentAttempt = 0;
        if (existingOcrFreeAttempted < allowedFreeOcr) {
            whichPlan = Constants.PLAN_FREE;
            currentAttempt = existingOcrFreeAttempted + 1;
        } else if (prefManager.isOcrMonthly() && existingOcrMonthlyAttempted < allowedOcrMonthly) {
            whichPlan = Constants.PLAN_OCR_MONTHLY;
            currentAttempt = existingOcrMonthlyAttempted + 1;
        } else if ((prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) && existingOcrPremiumYearlyAttempted < allowedPremiumYearly) {
            whichPlan = Constants.PLAN_PEMIUM_YEARLY;
            currentAttempt = existingOcrPremiumYearlyAttempted + 1;
        }

        Log.i(TAG, "whichPlan: " + whichPlan + " ::: going to increase count in this plan.");
        Log.i(TAG, "currentAttempt: " + currentAttempt);

        if (dbHandler.existOcrAttempted()) {
            dbHandler.updateOcrAttempt(currentAttempt, whichPlan);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertOcrAttempt(currentAttempt, whichPlan);
            Log.i(TAG, "insert");
        }
    }


    private void processResult(FirebaseVisionText firebaseVisionText, int textMode, Bitmap
            bitmapFromUri) {
        switch (textMode) {
            case SINGLE_TEXT:
                detectSingleText(firebaseVisionText);
                break;
            case WHOLE_TEXT:
                detectWholeText(firebaseVisionText, bitmapFromUri);
                break;
        }
    }

    private void detectWholeText(FirebaseVisionText firebaseVisionText, Bitmap bitmapFromUri) {
        String text = firebaseVisionText.getText();
        if (!TextUtils.isEmpty(text)) {
            recognizedText = text;
            /*Log.d(TAG, "detectWholeText : " + text);*/
            createOcrDocument(bitmapFromUri);
        } else {
            Log.d(TAG, "No detect whole text found");
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_text_found));
        }

    }

    private void createOcrDocument(Bitmap bitmapFromUri) {
        String folderName = flashScanUtil.getFolderCurrentTime();
        new CreateOcrDocument(context, folderName, bitmapFromUri, this).execute();
    }

    private void navigateToOcrResultActivity(String text, String folderPath) {
        Intent intent = new Intent(context, OcrResultActivity.class);
        intent.putExtra(Constants.PutExtraConstants.OCR_RESULT_TEXT, text);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_PATH, folderPath);
        intent.putExtra(Constants.PutExtraConstants.FROM_SOURCE, fromSource);
        intent.putExtra(Constants.PutExtraConstants.OCR_RESULT_FROM_SCREEN, Constants.OcrResultScreenFrom.FROM_PREVIEW);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void detectSingleText(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();
        if (!textBlocks.isEmpty()) {
            for (FirebaseVisionText.TextBlock textBlock : textBlocks) {
                String text = textBlock.getText();
                if (!TextUtils.isEmpty(text)) {
                    Log.d(TAG, "Single Text : " + text);
                }
            }
        } else {
            Log.d(TAG, "No single text found");
        }
    }

    @Override
    public void onDocumentCreationStart() {
        if (progress_lay != null) {
            progress_lay.setVisibility(View.VISIBLE);
        }
    }

    private boolean isAdLoaded = false;

    @Override
    public void onDocumentCreated(String folderPath) {
        if (progress_lay != null) {
            progress_lay.setVisibility(View.GONE);
        }

        boolean showIntersCreation = dbHandler.showIntersCreation();

        int existingFreq = dbHandler.getIntersCreateFreq();
        Log.i(TAG, "at the time of load ::: existingFreq: " + existingFreq);

        int allowedFreq = dbHandler.intersCreateFreqInSession();
        Log.i(TAG, "allowedFreq: " + allowedFreq);

        if (existingFreq < allowedFreq) {
            shouldIntersCreateShow = true;
        } else {
            shouldIntersCreateShow = false;
        }

        Log.i(TAG, "showIntersCreation: " + showIntersCreation + ", shouldIntersCreateShow: " + shouldIntersCreateShow);

//        if (!prefManager.isAppAdFree() && Constants.SHOW_INTERSTITIAL_ADS.FOR_OCR_RESULT_ACTIVITY) {
        if (!prefManager.isAppAdFree() && showIntersCreation && shouldIntersCreateShow && com.itl.commonres.utils.Constants.isAdShow) {
            if (flashScanUtil.isConnectingToInternet()) {
                //show(this);
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_OCR, this);

               /* if (!OcrAdManager.getInstance().isAdLoaded()) {
                    loadAndShowInterstitialAd(folderPath);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isAdLoaded) {
                                if (!isMovedToResultScreen) {
                                    moveToOcrResult(folderPath);
                                }

                            }
                        }
                    }, Constants.AD_HOLDING_TIME);
                } else {

                    increaseIntersCount();

                    OcrAdManager.getInstance().showAd(new AdManagerListener() {
                        @Override
                        public void onAdLoaded() {

                        }

                        @Override
                        public void onAdFailedToLoad() {

                        }

                        @Override
                        public void onAdClosed() {
                            if (!isMovedToResultScreen) {
                                moveToOcrResult(folderPath);
                            }

                        }
                    });
                }*/
            } else {
                if (!isMovedToResultScreen) {
                    moveToOcrResult(folderPath);
                }

            }
        } else {
            Log.e(TAG, "Mobibuz : Ad Not Showing");
            if (!isMovedToResultScreen) {
                moveToOcrResult(folderPath);
            }

        }
    }

    private void increaseIntersCount() {
        int existingFreq = dbHandler.getIntersCreateFreq();
        Log.i(TAG, "at the time of show::: existingFreq: " + existingFreq);

        int currentFreq = existingFreq + 1;
        Log.i(TAG, "currentAttempt: " + currentFreq);

        if (dbHandler.existIntersCreateFreq()) {
            dbHandler.updateIntersCreateFreq(currentFreq);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertIntersCreateFreq(currentFreq);
            Log.i(TAG, "insert");
        }
    }

    /*private void loadAndShowInterstitialAd(String folderPath) {
        progress_lay.setVisibility(View.VISIBLE);
        OcrAdManager.getInstance().loadAd(BuildConfig.INTERSTITIAL_AD_ID_FOR_OCR_RESULT_ACTIVITY, new AdManagerListener() {
            @Override
            public void onAdLoaded() {
                progress_lay.setVisibility(View.GONE);
                isAdLoaded = true;
                OcrAdManager.getInstance().showAd(new AdManagerListener() {
                    @Override
                    public void onAdLoaded() {

                    }

                    @Override
                    public void onAdFailedToLoad() {

                    }

                    @Override
                    public void onAdClosed() {
                        if (!isMovedToResultScreen) {
                            moveToOcrResult(folderPath);
                        }

                    }
                });

                increaseIntersCount();
            }

            @Override
            public void onAdFailedToLoad() {
                progress_lay.setVisibility(View.GONE);
                if (!isMovedToResultScreen) {
                    moveToOcrResult(folderPath);
                }

            }

            @Override
            public void onAdClosed() {

            }
        });
    }*/

    private void moveToOcrResult(String folderPath) {
        isMovedToResultScreen = true;
        if (!TextUtils.isEmpty(recognizedText) && !TextUtils.isEmpty(folderPath)) {
            navigateToOcrResultActivity(recognizedText, folderPath);
        }
    }

    @Override
    public void onAdClosed() {
        if (!isMovedToResultScreen) {
            moveToOcrResult(createdFolderPath);
        }
    }

    @Override
    public void onAdLoadedOrFailed(boolean isLoaded) {
        if (!isMovedToResultScreen) {
            moveToOcrResult(createdFolderPath);
        }
    }
}
