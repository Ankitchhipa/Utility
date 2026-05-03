package com.cam.scanner.scantopdf.android.activities;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.asynctasks.ReadTextFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.WriteTextFileTask;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.dialogs.OcrChoosePlanDialog;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.interfaces.ReadFileListener;
import com.cam.scanner.scantopdf.android.interfaces.WriteFileTaskListener;
import com.cam.scanner.scantopdf.android.rest.UpdateCreditsToApi;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OcrResultActivity extends BaseActivity implements View.OnClickListener, WriteFileTaskListener, PurchasesUpdatedListener {

    private Context context;
    private TextView tv_toolbar, tv_edit, tv_copy, tv_translate, tv_rescan;
    private EditText tv_ocr_result;
    private String ocrResultText, folderPath;
    private Button btn_save_as_text, btn_progress_lay, btn_view_image;
    private ImageView iv_back_toolbar, iv_ocr, iv_share;
    private FlashScanUtil flashScanUtil;
    private View progress_lay;
    private long lastClickedTime = 0;
    private static final String TAG = OcrResultActivity.class.getSimpleName();
    private int ocrResultScreenFrom = 0;
    private PrefManager prefManager;

    private boolean isNetWorking = true;
    private int whichFab = Constants.FAB_DEFAULT;
    private int REQUEST_CODE_WIFI_SETTING = 1051;

    private boolean isTextChanged;
    private String resultTextTemp;
    private String fromSource = "";
    private DBHandler dbHandler;
    private BillingClient billingClient;
    private boolean isCreditsConsumed = false;
    private FrameLayout nativeLargeAdOcrResult;
    private CardView ad_view_banner_container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);
        initObjects();

        findViewIds();
        setClickListeners();
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.OCR_RESULT_FROM_SCREEN)) {
            ocrResultScreenFrom = getIntent().getIntExtra(Constants.PutExtraConstants.OCR_RESULT_FROM_SCREEN, 0);
            fromSource = getIntent().getStringExtra(Constants.PutExtraConstants.FROM_SOURCE);
            Log.e(TAG, "fromSource" + fromSource);
        }
        if (ocrResultScreenFrom != 0) {
            switch (ocrResultScreenFrom) {
                case Constants.OcrResultScreenFrom.FROM_DOCUMENT:
                    getDocumentDataAndSetData();
                    break;
                case Constants.OcrResultScreenFrom.FROM_PREVIEW:
                 /*   if (!prefManager.isAppAdFree() && Constants.SHOW_INTERSTITIAL_ADS.FOR_OCR_RESULT_ACTIVITY) {
                        loadAndShowInterstitialAds();
                    }*/
                    getPreviewIntentAndSetData();
                    break;
            }
        }

        tv_ocr_result.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i(TAG, "beforeTextChanged");
//                resultTextTemp = tv_ocr_result.getText().toString().trim();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "onTextChanged");
//                isTextChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged");
            }
        });

        resultTextTemp = tv_ocr_result.getText().toString().trim();
        Log.i(TAG, "resultTextTemp onCreate: " + resultTextTemp);

    }

    /*private void loadAndShowInterstitialAds() {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(BuildConfig.INTERSTITIAL_AD_ID_FOR_OCR_RESULT_ACTIVITY);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
            }
        });
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        checkAds();
    }

    private void checkAds() {
        if (flashScanUtil.isConnectingToInternet() && !prefManager.isAppAdFree() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_OCR_RESULT.getValue())) {
            ad_view_banner_container.setVisibility(View.VISIBLE);
            callNativeAd();
        } else {
            Log.e("Mobibuz : ", "Ad Not Showing");
            ad_view_banner_container.setVisibility(View.GONE);
        }
    }

    private void callNativeAd() {
        if (AppController.nativeAdOcr == null) {
            AdLoader customEventNativeLoader = new AdLoader.Builder(context,
                    BuildConfig.AD_UNIT_ID_OCR_RESULT_NATIVE_AD)
                    .forNativeAd(nativeAd -> {
                        Log.e("OCR_Result ", "onUnifiedNativeAdLoaded G `> " + "");
                        AppController.nativeAdOcr = nativeAd;
                        largeNativeAdSet(nativeAd, nativeLargeAdOcrResult);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.e("OCR_Result ", "onAdFailedToLoad G > " + loadAdError.getMessage());
                        }
                    }).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                            .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build())
                    .build();
            customEventNativeLoader.loadAd(new AdRequest.Builder().build());
        } else {
            largeNativeAdSet(AppController.nativeAdOcr, nativeLargeAdOcrResult);
        }
    }

    private void getDocumentDataAndSetData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FILE_PATH)) {
            folderPath = getIntent().getStringExtra(Constants.PutExtraConstants.FILE_PATH);
        }
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.OCR_SAVED_FILE_PATH)) {
            String ocrResultSavedFilePath = getIntent().getStringExtra(Constants.PutExtraConstants.OCR_SAVED_FILE_PATH);
            if (!TextUtils.isEmpty(ocrResultSavedFilePath)) {
                readFileTask(ocrResultSavedFilePath);
            }
            if (!TextUtils.isEmpty(folderPath)) {
                File fileFromFolder = getImageFileFromFolder(folderPath);
                Log.e(TAG, "fileFromFolder..   " + fileFromFolder);
                if (fileFromFolder != null) {
                    //loadImage(fileFromFolder);
                    setEnable(true);
                } else {
                    setEnable(false);
                }
            }
        } else {
            // no saved ocr result saved text file // so detect text from bitmap
            if (!TextUtils.isEmpty(folderPath)) {
                File fileFromFolder = getImageFileFromFolder(folderPath);
                Log.e(TAG, "fileFromFolder 2..   " + fileFromFolder);
                if (fileFromFolder != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(fileFromFolder.getPath());
                    if (bitmap != null) {
//                        detectTextFromBitmap(bitmap);
                        reScanBitmap(bitmap, false);
                        setEnable(true);
                    } else {
                        setEnable(false);
                    }
                    //loadImage(fileFromFolder);
                }
            }
        }
        /*if (!TextUtils.isEmpty(folderPath)) {
            File fileFromFolder = getFileFromFolder(folderPath);
            if (fileFromFolder != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(fileFromFolder.getPath());
                if (bitmap != null) {
                    detectTextFromBitmap(bitmap);
                }
                loadImage(fileFromFolder);
            }
        }*/
    }

    private void setEnable(boolean enable) {
        if (enable) {
            tv_rescan.setClickable(true);
            tv_rescan.setEnabled(true);
            tv_rescan.setCompoundDrawablesRelativeWithIntrinsicBounds(null, context.getResources().getDrawable(R.drawable.ic_rescan), null, null);
            btn_view_image.setTextColor(context.getResources().getColor(R.color.sky_blue));
            btn_view_image.setBackground(context.getResources().getDrawable(R.drawable.button_bg));
        } else {
            tv_rescan.setClickable(false);
            tv_rescan.setEnabled(false);
            tv_rescan.setCompoundDrawablesRelativeWithIntrinsicBounds(null, context.getResources().getDrawable(R.drawable.ic_rescan_disable), null, null);
            btn_view_image.setTextColor(context.getResources().getColor(R.color.transparent_color));
            btn_view_image.setBackground(context.getResources().getDrawable(R.drawable.pop_up_bg_disabled));
        }
    }

    private void readFileTask(String filePath) {
        new ReadTextFileTask(filePath, new ReadFileListener() {
            @Override
            public void onReadingStart() {
                progress_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReadingCompleted(String readedText) {
                progress_lay.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(readedText)) {
                    tv_ocr_result.setText(readedText);

                    resultTextTemp = tv_ocr_result.getText().toString().trim();
                    Log.i(TAG, "resultTextTemp on reading from file: " + resultTextTemp);
                } else {
                    tv_ocr_result.setText(getString(R.string.oops_no_txt_found));
                }
            }
        }).execute();
    }

    private void setClickListeners() {
        btn_save_as_text.setOnClickListener(this);
        iv_back_toolbar.setOnClickListener(this);
        btn_view_image.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        //tv_ocr_result.setOnClickListener(this);
        tv_copy.setOnClickListener(this);
        tv_translate.setOnClickListener(this);
        tv_rescan.setOnClickListener(this);
    }

    private void getPreviewIntentAndSetData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.OCR_RESULT_TEXT)) {
            ocrResultText = getIntent().getStringExtra(Constants.PutExtraConstants.OCR_RESULT_TEXT);
        }
        if (!TextUtils.isEmpty(ocrResultText)) {
            tv_ocr_result.setText(ocrResultText);
        } else {
            tv_ocr_result.setText(getString(R.string.oops_no_txt_found));
        }

        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FOLDER_PATH)) {
            folderPath = getIntent().getStringExtra(Constants.PutExtraConstants.FOLDER_PATH);
        }

        if (!TextUtils.isEmpty(folderPath)) {
            File fileFromFolder = getImageFileFromFolder(folderPath);
            Log.e(TAG, "fileFromFolder 3..   " + fileFromFolder);
            if (fileFromFolder != null) {
                //loadImage(fileFromFolder);
            }
        }
    }

    private File getImageFileFromFolder(String folderPath) {
        File requiredFile = null;
       /* String directoryPath;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            directoryPath = context.getExternalFilesDir(null)+ "/" + folderPath;
//            requiredFile = context.getExternalFilesDir(folderPath);
//            Log.e(TAG,"requiredFile"+requiredFile);
            //rootFile = getExternalFilesDir("VooHoo_Videos");
        } else {
            directoryPath = Environment.getExternalStorageDirectory() + "/" + folderPath;
        }*/

        Log.e(TAG, "directoryPath : " + folderPath);
        File dir = new File(folderPath);

        if (dir.isDirectory() && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file != null && file.isFile() && file.exists()) {
                        if (flashScanUtil.getExtensionFromFileName(file.getName()).equalsIgnoreCase(Constants.FileExtensions.JPG)) {
                            requiredFile = file;
                            break;
                        }
                    }
                }
                /*File file = files[0];*/

            }
        }
        //}
        Log.e(TAG, "requiredFile  " + requiredFile);
        return requiredFile;
    }

    /*private void loadImage(File file) {
        if (!isFinishing() || !isDestroyed()) {
            Glide.with(context).asBitmap().load(file.getPath()).centerCrop().apply(new RequestOptions()
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_notfound)))
                    .into(iv_ocr);
        }

    }*/

    private void findViewIds() {
        tv_ocr_result = findViewById(R.id.tv_ocr_result);
        btn_save_as_text = findViewById(R.id.btn_save_as_text);
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        progress_lay = findViewById(R.id.progress_lay);
        //iv_ocr = findViewById(R.id.iv_ocr);
        btn_view_image = findViewById(R.id.btn_view_image);
        iv_share = findViewById(R.id.iv_share);
        iv_share.setVisibility(View.VISIBLE);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getString(R.string.ocr_result));
        tv_edit = findViewById(R.id.tv_edit);
        tv_copy = findViewById(R.id.tv_copy);
        tv_translate = findViewById(R.id.tv_translate);
        tv_rescan = findViewById(R.id.tv_rescan);
        nativeLargeAdOcrResult = findViewById(R.id.nativeLargeAdOcrResult);
        ad_view_banner_container = findViewById(R.id.ad_view_banner_container);
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        prefManager = new PrefManager(context);

        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_save_as_text) {//                isTextChanged = false;

            resultTextTemp = tv_ocr_result.getText().toString().trim();
            Log.i(TAG, "resultTextTemp on save clicked: " + resultTextTemp);

            String resultText = tv_ocr_result.getText().toString().trim();
            Log.e(TAG, "onClick: folderPath = " + folderPath);
            if (!TextUtils.isEmpty(resultText) && !TextUtils.isEmpty(folderPath)) {
//                flashScanUtil.logOcrResultEvents(Constants.FirebaseClickEvents.OCR_RESULT_SAVE_AS_TEXT);
                saveTextInFileTask(resultText, folderPath, true);
            } else {
                flashScanUtil.showSnackOnTop(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found), Snackbar.LENGTH_LONG);
            }
        } else if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.btn_view_image) {
            if (!TextUtils.isEmpty(folderPath)) {
                File fileFromFolder = getImageFileFromFolder(folderPath);
                if (fileFromFolder != null) {
                    //openFile(fileFromFolder.getPath());
                    showImageDialog(fileFromFolder.getPath());
                }
            }
        } else if (id == R.id.iv_share) {
            String text = tv_ocr_result.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                openShareDialog(text);
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
            }
        } /*else if (id == R.id.tv_ocr_result) {
            tv_ocr_result.setCursorVisible(true);
            tv_ocr_result.requestFocus();
        }*/ else if (id == R.id.tv_copy) {
            String textToBeCopy = tv_ocr_result.getText().toString().trim();
            if (!TextUtils.isEmpty(textToBeCopy)) {
//                flashScanUtil.logOcrResultEvents(Constants.FirebaseClickEvents.OCR_RESULT_COPY);
                flashScanUtil.copyToClipboard(textToBeCopy);
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
            }
        } else if (id == R.id.tv_translate) {
            String textToBeTranslate = tv_ocr_result.getText().toString().trim();
            if (!TextUtils.isEmpty(textToBeTranslate)) {
                try {
//                    flashScanUtil.logOcrResultEvents(Constants.FirebaseClickEvents.OCR_RESULT_TRANSLATE);
//                    translateText(textToBeTranslate);
                    startActivity(new Intent(this, OcrTranslateActivity.class).putExtra(getString(R.string.transalate_text), textToBeTranslate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
            }
        } else if (id == R.id.tv_rescan) {
            /*if (flashScanUtil.isConnectingToInternet()) {
                connectBillingService();
            } else {
                if (checkOcrCount()) {
                    if (Constants.IS_CLOUD_VISION_ALLOW) {
                        reScanStart(true);
                    } else {*/
            reScanProcess();
                  /*  }
                }
            }*/
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

        int totalAllowed = allowedFreeOcr + allowedPremiumYearly + allowedOcrMonthly;

        if (flashScanUtil.isConnectingToInternet()) {
            dialogTitle.setText(getString(R.string.warning));
            if (!prefManager.isPremiumYearly() /*&& !prefManager.isPremiumQuarterly()*/ && !prefManager.isOcrMonthly()) {
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
        } else {
            dialogTitle.setText(getString(R.string.credits_expired));
            msgHeading.setText(getString(R.string.credits_consumed_internet_off));
        }

        btn_ok.setText(getString(R.string.ok));
        btn_cancel.setText(getString(R.string.cancel));

        btn_ok.setOnClickListener(v -> {

            if (flashScanUtil.isConnectingToInternet()) {
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
            } else {
                isCreditsConsumed = true;
                openWiFiSettings();

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

    private void openOcrMonthlyDialogActivity() {
        Intent intent = new Intent(OcrResultActivity.this, OcrPlanDialog.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_OCR_MONTHLY);
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
        Intent intent = new Intent(OcrResultActivity.this, OcrChoosePlanDialog.class);
        intent.putExtra(Constants.FROM_NAV_CHOOSE_PLAN, Constants.NAV_FROM_OCRACTIVITY);
        startActivity(intent);
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
        Intent intent = new Intent(OcrResultActivity.this, AskEmailActivity.class);
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity);
        startActivity(intent);
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(OcrResultActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    private void reScanStart(boolean isNetCheckRequired) {
        if (isNetCheckRequired) {
            checkInternet();
            if (isNetWorking) {
                reScanProcess();
                // connectBillingService();
            }
        } else {

            if (!checkOcrCount()) {
                askToChoosePlan();
            } else {
                reScanProcess();
            }
            //reScanProcess();
        }
    }

    private void reScanProcess() {


        progress_lay.setVisibility(View.VISIBLE);
        tv_ocr_result.setText("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(folderPath)) {
//                        flashScanUtil.logOcrResultEvents(Constants.FirebaseClickEvents.OCR_RESULT_RESCAN);
                    File fileFromFolder = getImageFileFromFolder(folderPath);
                    if (fileFromFolder != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(fileFromFolder.getPath());
                        if (bitmap != null) {
                            reScanBitmap(bitmap, true);
                        } else {
                            progress_lay.setVisibility(View.GONE);
                        }
                    } else {
                        progress_lay.setVisibility(View.GONE);
                    }
                } else {
                    progress_lay.setVisibility(View.GONE);
                }
            }
        }, Constants.OCR_RESCAN_DELAY);

    }

    private void checkInternet() {
        if (!flashScanUtil.isConnectingToInternet()) {
            isNetWorking = false;
            Log.i(TAG, "Net working: " + isNetWorking);
            showInternetDialog();
        }
    }

    private void showInternetDialog() {
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
        msgHeading.setText(getString(R.string.internet_for_ocr, getString(R.string.app_name)));

        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
            isCreditsConsumed = false;
            openWiFiSettings();

        });

        btn_cancel.setOnClickListener(v -> {
            isNetWorking = false;
            dialog.dismiss();
            reScanStart(false);
        });
        dialog.show();
    }

    private void openWiFiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_WIFI_SETTING);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode: " + requestCode);
        if (requestCode == REQUEST_CODE_WIFI_SETTING) {

            if (!flashScanUtil.isConnectingToInternet()) {
                progress_lay.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkInternetAfterDelay();
                    }
                }, Constants.OCR_RESCAN_DELAY);
            } else {
                checkInternetAfterDelay();
            }
        }
    }

    private void checkInternetAfterDelay() {
        Log.i(TAG, "wifi state callback");
        progress_lay.setVisibility(View.GONE);
        if (!flashScanUtil.isConnectingToInternet()) {
            isNetWorking = false;
        } else {
            isNetWorking = true;
        }
        Log.i(TAG, "in wifi state callback, net working: " + isNetWorking);
        if (!isCreditsConsumed)
            reScanStart(false);
    }

    private void reScanBitmap(Bitmap bitmap, boolean isRescan) {
        progress_lay.setVisibility(View.VISIBLE);
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer textRecognizer;

        if (/*!isNetWorking || !Constants.IS_CLOUD_VISION_ALLOW*/true) {
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
                        String text = firebaseVisionText.getText();
                        if (!TextUtils.isEmpty(text)) {
                            tv_ocr_result.setText(firebaseVisionText.getText());
                            resultTextTemp = tv_ocr_result.getText().toString().trim();
                            Log.i(TAG, "resultTextTemp just after scanning: " + resultTextTemp);
                            if (isRescan) {
                                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.re_scanned_successfully));
                            }
                        } else {
                            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_text_found));
                        }
                    }

//                    increaseOcrAttempted();
//                    updateCreditsToApi();
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

        /*int existingOcrAttempted = dbHandler.getOcrAttempted();

        Log.i(TAG, "existingOcrAttempted: " + existingOcrAttempted);

        int currentAttempt = existingOcrAttempted + 1;

        Log.i(TAG, "currentAttempt: " + currentAttempt);

        if (dbHandler.existOcrAttempted()) {
            dbHandler.updateOcrAttempt(currentAttempt);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertOcrAttempt(currentAttempt);
            Log.i(TAG, "insert");
        }*/
    }

    private void openShareDialog(String text) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.share_ocr_text_dialog);
        dialog.setCancelable(true);

        LinearLayout ll_share_as_plain_text = dialog.findViewById(R.id.ll_share_as_plain_text);
        LinearLayout ll_share_as_text_file = dialog.findViewById(R.id.ll_share_as_text_file);

        ll_share_as_plain_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                flashScanUtil.logOcrResultEvents(Constants.FirebaseClickEvents.OCR_RESULT_SHARE_AS_PLAIN_TEXT);
                sharePlainText(text);
            }
        });

        ll_share_as_text_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (!TextUtils.isEmpty(folderPath)) {
                    flashScanUtil.logOcrResultEvents(Constants.FirebaseClickEvents.OCR_RESULT_SHARE_AS_TEXT_FILE);
                    saveTextFileAndShare(text, folderPath);
                } else {
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.something_went_wrong));
                }
            }
        });

        dialog.show();
    }

    private void sharePlainText(String text) {
        flashScanUtil.shareTextContent(text);
    }

    /*
    private void detectTextFromBitmap(Bitmap bitmap) {
        progress_lay.setVisibility(View.VISIBLE);
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer onDeviceTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
       *//* FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("en", "hi"))
                .build();
        FirebaseVisionTextRecognizer cloudTextRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer(options);*//*
        onDeviceTextRecognizer.processImage(firebaseVisionImage)
                .addOnSuccessListener(firebaseVisionText -> {
                    progress_lay.setVisibility(View.GONE);
                    if (firebaseVisionText != null) {
                        String text = firebaseVisionText.getText();
                        if (!TextUtils.isEmpty(text)) {
                            tv_ocr_result.setText(text);
                        } else {
                            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_text_found));
                        }
                    }

                }).addOnFailureListener(e -> {
            progress_lay.setVisibility(View.GONE);
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    */

    private void translateText(String textToBeTranslate) {
        Intent intent = new Intent();
        intent.setType("text/plain");
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, textToBeTranslate);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setAction(Intent.ACTION_PROCESS_TEXT);
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, textToBeTranslate);
        } else {*/

//        }
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);
        if (!resolveInfos.isEmpty()) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                if (resolveInfo.activityInfo.packageName.contains("com.google.android.apps.translate")) {
                    intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                    startActivity(intent);
                } else {
                    Log.i(TAG, "No app found");
                    intentToBrowser(textToBeTranslate);
                }
                break;
            }
        } else {
            // intent to browser
            intentToBrowser(textToBeTranslate);
        }
    }

    private void intentToBrowser(String textToBeTranslate) {
        String url = "https://translate.google.com/#auto/en/" + textToBeTranslate;
        flashScanUtil.intentToBrowser(url);
    }

    private void copyToClipboard(String textToBeCopy) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("OCR text", textToBeCopy);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            Toast.makeText(context, getString(R.string.text_copied_clipboard), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTextInFileTask(String resultText, String folderPath, boolean shouldDlgShow) {
        deleteAllTextFilesExceptJpgFile(folderPath);
        new WriteTextFileTask(context, resultText, folderPath, this, shouldDlgShow).execute();
    }

    private void deleteAllTextFilesExceptJpgFile(String folderPath) {
        if (!TextUtils.isEmpty(folderPath)) {
            File dir = new File(folderPath);
            if (dir.isDirectory() && dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file != null && file.isFile() && file.exists()) {
                            if (flashScanUtil.getExtensionFromFileName(file.getName()).equalsIgnoreCase(Constants.FileExtensions.JPG)) {
                                continue;
                            }
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    private void saveTextFileAndShare(String resultText, String folderPath) {
        deleteAllTextFilesExceptJpgFile(folderPath);
        new WriteTextFileTask(context, resultText, folderPath, new WriteFileTaskListener() {
            @Override
            public void onWriteStart() {
                progress_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onWriteCompleted(String savedFilePath, boolean shouldDlgShow) {
                progress_lay.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(savedFilePath)) {
                    File file = new File(savedFilePath);
                    ArrayList<Uri> uriList = new ArrayList<>();
                    if (file.isFile()) {
                        Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                        if (uriForFile != null) /*uriList.add(uriForFile);*/
                            flashScanUtil.shareMultiple(uriForFile, OcrResultActivity.this);
                        /*if (!uriList.isEmpty()) {
                            flashScanUtil.shareMultiple(uriList);
                        }*/
                    }
                }
            }
        }, true).execute();
    }


    @Override
    public void onWriteStart() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onWriteCompleted(String savedFilePath, boolean shouldDlgShow) {
        progress_lay.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(savedFilePath)) {
            if (shouldDlgShow) {
                showSavedTextFilePathDialog(savedFilePath);
            }
        }
    }

    private void showSavedTextFilePathDialog(String savedFilePath) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.saved_pdf_dialog);
        TextView tvPath = dialog.findViewById(R.id.tv_pdf_path);
        TextView tvTitle = dialog.findViewById(R.id.tv_saved_file_title);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_open = dialog.findViewById(R.id.btn_open);
        tvTitle.setText(getString(R.string.file_saved_successfully));
        tvPath.setText(savedFilePath);

        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_open.setOnClickListener(v -> {
            openFile(savedFilePath);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void openFile(String savedPdfPath) {
        File file = new File(savedPdfPath);
        if (file.isFile() && file.exists()) {
            flashScanUtil.openFile(context, file);
        }
    }

    private void showImageDialog(String savedPdfPath) {
        Dialog dialog = new Dialog(context, com.itl.commonres.R.style.AlertDialogTheme);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_show_image);
        ImageView img_show = dialog.findViewById(R.id.img_show);
        ImageView img_close = dialog.findViewById(R.id.img_close);

        if (savedPdfPath != null) {
            Uri imageUri = Uri.parse(savedPdfPath);
            img_show.setImageURI(imageUri);
        }
        img_close.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onBackPressed() {

        String resultText = tv_ocr_result.getText().toString().trim();
        Log.i(TAG, "resultText onBackPressed: " + resultText);

        if (resultText.equalsIgnoreCase(resultTextTemp)) {
            Log.i(TAG, "no change in text");
            isTextChanged = false;
        } else {
            Log.i(TAG, "change in text");
            isTextChanged = true;
        }

        Log.i(TAG, "onBackPressed, text changed: " + isTextChanged);
        if (isTextChanged) {
            dlgOnBackPressed();
        } else {
            whatContentShouldSave(false);
        }
    }

    private void whatContentShouldSave(boolean isTempContent) {
        if (isTempContent) {
            if (!TextUtils.isEmpty(resultTextTemp) && !TextUtils.isEmpty(folderPath)) {
                saveTextInFileTask(resultTextTemp, folderPath, false);
            }
        } else {
            String resultText = tv_ocr_result.getText().toString().trim();
            if (!TextUtils.isEmpty(resultText) && !TextUtils.isEmpty(folderPath)) {
                saveTextInFileTask(resultText, folderPath, false);
            }
        }
        if (fromSource != null && fromSource.equalsIgnoreCase("ScanResultActivity")) {
            //navigate To ScanResultActivity
            //super.onBackPressed();
            finish();
        } else {
            navigateToOcrActivity();
        }

    }

    private void dlgOnBackPressed() {
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
        msgHeading.setText(getString(R.string.ocr_on_backpressed));

        btn_ok.setOnClickListener(v -> {
            whatContentShouldSave(true);
            dialog.dismiss();
        });

        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void navigateToOcrActivity() {
        if (Constants.ALWAYS_RELOAD_AD_ON_OCR_SCREEN) {
            intentToOcrScreen();
        } else {
            if (ocrResultScreenFrom != Constants.OcrResultScreenFrom.FROM_DOCUMENT) {
                intentToOcrScreen();
            }
        }
        finish();

    }

    private void intentToOcrScreen() {
        Intent intent = new Intent(context, OcrActivity.class);
        startActivity(intent);
    }

    private void connectBillingService() {
        if (flashScanUtil.isConnectingToInternet()) {
            /*billingClient = BillingClient.newBuilder(this)
                    .enablePendingPurchases()
                    .setListener(this).build();*/

            /*clearGooglePlayStoreBillingCacheIfPossible();

            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    *//*if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                        if (purchasesResult.getPurchasesList() != null) {
                            List<Purchase> purchases = purchasesResult.getPurchasesList();
                            if (purchases.size() > 0) {
                                Log.i(TAG, "purchase found in restore");

                                //reScanProcess();
                                if (checkOcrCount()) {
                                    if (Constants.IS_CLOUD_VISION_ALLOW) {
                                        reScanStart(true);
                                    } else {
                                        reScanProcess();
                                    }
                                }


                            } else {
                                int allowedFreeOcr = dbHandler.getAllowedFreeOcr();
                                Log.i(TAG, "allowedFreeOcr: " + allowedFreeOcr);

                                int existingOcrFreeAttempted = dbHandler.getOcrFreeAttempted();
                                Log.i(TAG, "existingOcrFreeAttempted: " + existingOcrFreeAttempted);

                                if(allowedFreeOcr == existingOcrFreeAttempted){
                                    askToChoosePlan();
                                }
                                else{
                                    reScanProcess();
                                }

                               *//**//* if(!checkOcrCount()) {
                                    askToChoosePlan();
                                }
                                else{
                                    reScanProcess();
                                }*//**//*
                                Log.i(TAG, "plans not found");
                            }
                        }
                    }*//*
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                        Log.i(TAG, "item already owned");
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            });
        } else
            return;*/
        }
    }

    private void clearGooglePlayStoreBillingCacheIfPossible() {

        /*billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
                Log.i(TAG, "onPurchaseHistoryResponse");

            }
        });*/

       /* billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, (responseCode, purchasesList) -> {
        });*/

        /*billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, (responseCode, purchasesList) -> {
        });*/
    }


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

    }

}
