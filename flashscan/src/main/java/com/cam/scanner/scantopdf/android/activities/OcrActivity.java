package com.cam.scanner.scantopdf.android.activities;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.OcrDocumentsAdapter;
import com.cam.scanner.scantopdf.android.ads.AdClosed;
import com.cam.scanner.scantopdf.android.asynctasks.CreateOcrDocument;
import com.cam.scanner.scantopdf.android.asynctasks.DeleteFolderOrFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetOcrDocuments;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.dialogs.OcrChoosePlanDialog;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.interfaces.DocumentCreationListener;
import com.cam.scanner.scantopdf.android.interfaces.FetchOcrDocumentsListener;
import com.cam.scanner.scantopdf.android.interfaces.FileOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.FileOrFolderDeleteListener;
import com.cam.scanner.scantopdf.android.interfaces.OnDeselectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.interfaces.OnSelectAllFiles;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.rest.UpdateCreditsToApi;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OcrActivity extends BaseActivity implements View.OnClickListener,
        FetchOcrDocumentsListener, OnItemSelectListener, FileOperationListener, FileOrFolderDeleteListener, DocumentCreationListener, PurchasesUpdatedListener, AdClosed {

    private static final int REQUEST_TAKE_PHOTO = 99;
    private static final int PICK_IMAGE_REQ_CODE = 100;
    private static final int IMAGE_COMPRESSION = 80;
    private static final int ASPECT_RATIO_X = 1;
    private static final int ASPECT_RATIO_Y = 1;
    private static final int REQUEST_CODE_FETCH_OCR_DOCUMENTS = 301;
    private static final String TAG = OcrActivity.class.getSimpleName();
    private static final int SINGLE_TEXT = 1;
    private static final int WHOLE_TEXT = 2;
    private ImageView iv_home, iv_search, iv_more_menu, iv_camera, iv_media;
    private TextView tvToolbarTxt, tvNoFile, tv_no_doc_title, tv_no_doc_msg, tv_save_as_pdf, tv_share, tv_delete, tv_total_file_count, tv_select_all_files;
    private Context context;
    private FlashScanUtil flashScanUtil;
    private Uri imageUri;
    private FloatingActionButton fab_media, fab_camera;
    private RecyclerView recyclerView;
    private View progress_lay;
    private OcrDocumentsAdapter ocrDocumentsAdapter;
    private Button btn_progress_lay;
    private LinearLayout ll_bottom_bar, ll_no_document, ll_floating, ll_select_all_files;
    private EditText et_search;
    private FrameLayout fl_camera, fl_media, fl_native_ad_view;
    private Bitmap bitmapFromUri;
    private PrefManager prefManager;
    private String directoryPath;
    private AdView adView;
    private int selectionAction = -1;
    private NativeAdView nativeAd;
    private boolean isNativeAdAlreadyLoaded = false;
    private TextView tvScannedText;
    private BillingClient billingClient;
    private String createdFolderPath = "";
    private List<FileModel> totalDocListIncludingAds = new ArrayList<>();
    private boolean isNetWorking = true;
    private int whichFab = Constants.FAB_DEFAULT;
    private int REQUEST_CODE_WIFI_SETTING = 1051;
    private DBHandler dbHandler;
    private String recognizedText;
    private boolean shouldIntersCreateShow;
    private boolean isMovedToResultScreen = false;
    private CardView ad_view_banner_container;
    private FrameLayout nativeSmallAdNoOcr;
    private FileModel mFileModelForSaveToDrive = null;
    private int positionForSaveToDrive = -1;
    private InterstitialAd ocrInterstitialAd;
    private List<FileModel> fetchedFolderNamesList = new ArrayList<>();
    private boolean isAdLoaded = false;

    private List<FileModel> getDocumentsListIncludingAds() {
        if (totalDocListIncludingAds == null) {
            totalDocListIncludingAds = new ArrayList<>();
        }
        return totalDocListIncludingAds;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        initObjects();

        if (!flashScanUtil.isConnectingToInternet()) {
            Log.i(TAG, "Net not working");
        }
        if (getIntent() != null) {
            String display_msg = getIntent().getStringExtra("DISPLAY_MSG");
            if (display_msg != null && display_msg != "")
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), display_msg);
        }

        /*if (!prefManager.isOCRLangWatched()) {
            showLanguageDialog();
        }*/

        findViewIds();
        setClickListeners();
        fetchFiles();
        manageSearchedFolders();
        prefManager.saveFoldersSortingOrder(prefManager.getAppSortingOrder());
        //loadInterstitialAd(context,BuildConfig.INTERSTITIAL_AD_ID_FOR_OCR_RESULT_ACTIVITY, this);
        //loadRewardedAd(context, BuildConfig.REWARD_AD_ID, this);
    }

    private void showLanguageDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog_ok_only);

        TextView msgHeading = dialog.findViewById(R.id.msg_heading);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);

        msgHeading.setText(getString(R.string.ocr_lang_warning));


        btn_ok.setOnClickListener(v -> {
            prefManager.setOCRLangWatched(true);
            dialog.dismiss();
        });
        dialog.show();
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
            openWiFiSettings();
            dialog.dismiss();
        });

        btn_cancel.setOnClickListener(v -> {
            isNetWorking = false;
            dialog.dismiss();
            whichFabActionToBeTaken();
        });
        dialog.show();
    }

    private void whichFabActionToBeTaken() {
        if (whichFab == Constants.FAB_CAMERA) {
            takePicture();
        } else if (whichFab == Constants.FAB_MEDIA) {
            openMedia();
        }
    }

    private void openWiFiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_WIFI_SETTING);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        /*if (!flashScanUtil.isConnectingToInternet()) {
            isNetWorking = false;
        } else {
            isNetWorking = true;
        }*/

        Log.i(TAG, "onResume::: net working: " + isNetWorking);
    }

    private List<FileModel> getFetchedFileList() {
        if (fetchedFolderNamesList == null) {
            fetchedFolderNamesList = new ArrayList<>();
        }
        return fetchedFolderNamesList;
    }

    private void manageSearchedFolders() {
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
    }

    private void filter(String text) {
        List<FileModel> filterFileList = new ArrayList<>();
        if (getFetchedFileList() != null && !getFetchedFileList().isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            ll_floating.setVisibility(View.VISIBLE);
            ll_no_document.setVisibility(View.GONE);
            tvScannedText.setVisibility(View.VISIBLE);
            for (FileModel fileModel : getFetchedFileList()) {
                if (fileModel.getName().toLowerCase().contains(text.toLowerCase())) {
                    filterFileList.add(fileModel);
                }
            }
            if (ocrDocumentsAdapter != null && !filterFileList.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                tvNoFile.setVisibility(View.GONE);
                ocrDocumentsAdapter.filterList(filterFileList);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvNoFile.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            ll_no_document.setVisibility(View.VISIBLE);
            tvScannedText.setVisibility(View.GONE);
            ll_floating.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
        }
    }

    private void fetchOcrDocuments(String directoryPath) {
        Log.d(TAG, "fetchOcrDocuments: " + directoryPath);
        new GetOcrDocuments(context, directoryPath, this, prefManager.getAppSortingOrder()).execute();
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        prefManager = new PrefManager(context);

        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    private void setClickListeners() {
        iv_home.setOnClickListener(this);
        fab_camera.setOnClickListener(this);
        fab_media.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        fl_camera.setOnClickListener(this);
        fl_media.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        iv_more_menu.setOnClickListener(this);
        tv_select_all_files.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_media.setOnClickListener(this);
    }

    private void findViewIds() {
        iv_home = findViewById(R.id.iv_home);
        tvToolbarTxt = findViewById(R.id.toolbar_title);
        tvToolbarTxt.setText(getString(R.string.ocr));
        fab_camera = findViewById(R.id.fab_camera);
        fab_media = findViewById(R.id.fab_media);
        recyclerView = findViewById(R.id.recyclerView);
        setUpRecyclerView(recyclerView);
        progress_lay = findViewById(R.id.progress_lay);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        ll_bottom_bar = findViewById(R.id.ll_bottom_bar);
        et_search = findViewById(R.id.et_search);
        iv_search = findViewById(R.id.iv_search);
        ll_no_document = findViewById(R.id.ll_no_document);
        tvNoFile = findViewById(R.id.tv_no_file);
        ll_floating = findViewById(R.id.ll_floating);
        tv_no_doc_msg = findViewById(R.id.tv_no_doc_msg);
        tv_no_doc_msg.setText(getString(R.string.no_ocr_doc_msg));
        tv_no_doc_title = findViewById(R.id.tv_no_doc_title);
        tv_no_doc_title.setText(getString(R.string.no_ocr_doc_title));
        fl_media = findViewById(R.id.fl_media);
        fl_camera = findViewById(R.id.fl_camera);
        tv_save_as_pdf = findViewById(R.id.tv_save_as_pdf);
        tv_save_as_pdf.setVisibility(View.GONE);
        tv_share = findViewById(R.id.tv_share);
        tv_share.setVisibility(View.GONE);
        tv_delete = findViewById(R.id.tv_delete);
        iv_more_menu = findViewById(R.id.iv_more_menu);
        iv_more_menu.setVisibility(View.VISIBLE);
        //ll_adView = findViewById(R.id.ll_adView);
        adView = findViewById(R.id.adView);
        ll_select_all_files = findViewById(R.id.ll_select_all_files);
        tv_select_all_files = findViewById(R.id.tv_select_all_files);
        tv_total_file_count = findViewById(R.id.tv_total_file_count);
        //ll_native_ad_view = findViewById(R.id.ll_native_ad_view);
        fl_native_ad_view = findViewById(R.id.fl_native_ad);
        iv_media = findViewById(R.id.iv_media);
        iv_camera = findViewById(R.id.iv_camera);
        tvScannedText = findViewById(R.id.tvScannedText);
        ad_view_banner_container = findViewById(R.id.ad_view_banner_container);
        nativeSmallAdNoOcr = findViewById(R.id.nativeSmallAdNoOcr);
    }

    private void setUpRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        if (CommonMethods.multipleClicked()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.iv_home) {
            goToHome();
        } else if (id == R.id.fab_camera || id == R.id.fl_camera || id == R.id.iv_camera) {
            //TODO: komal - uncomment for premium
            takePicture();
            /*if (flashScanUtil.isConnectingToInternet()) {
                connectBillingService(1);
            } else {
                if (checkOcrCount()) {
                    if (Constants.IS_CLOUD_VISION_ALLOW) {
                        whichFab = Constants.FAB_CAMERA;
                        checkInternet();
                        if (isNetWorking) {
                            takePicture();
                            //connectBillingService(1);  // 1 to open camera
                        }
                    } else {
                        takePicture();
                    }
                }
            }*/
        } else if (id == R.id.fab_media || id == R.id.fl_media || id == R.id.iv_media) {
            //TODO: komal - uncomment for premium
            openMedia();
            /*if (flashScanUtil.isConnectingToInternet()) {
                connectBillingService(2);
            } else {
                if (checkOcrCount()) {
                    if (Constants.IS_CLOUD_VISION_ALLOW) {
                        whichFab = Constants.FAB_MEDIA;
                        checkInternet();
                        if (isNetWorking) {
                            openMedia();
                            //connectBillingService(2);   // 2 to open gallery
                        }
                    } else {
                        openMedia();
                    }
                }
            }*/
        } else if (id == R.id.btn_progress_lay) {// disabled progrss lay click
        } else if (id == R.id.iv_search) {
            handleSearchBarVisibility();
        } else if (id == R.id.tv_delete) {
            List<FileModel> selectedFiles = null;
            if (ocrDocumentsAdapter != null) {
                selectedFiles = ocrDocumentsAdapter.getSelectedFileModelList();
            }
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                showDeleteDialog();
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
            }
        } else if (id == R.id.iv_more_menu) {
            showPopUpMoreMenu(v);
        } else if (id == R.id.tv_select_all_files) {
            switch (selectionAction) {
                case Constants.SELECT_ALL:
                    selectAllDocuments();
                    break;
                case Constants.DESELECT_ALL:
                    deSelectAllDocuments();
                    break;
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
        Intent intent = new Intent(OcrActivity.this, OcrPlanDialog.class);
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
        Intent intent = new Intent(OcrActivity.this, OcrChoosePlanDialog.class);
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
        Intent intent = new Intent(OcrActivity.this, AskEmailActivity.class);
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity);
        startActivity(intent);
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(OcrActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    private void checkInternet() {
        if (!flashScanUtil.isConnectingToInternet()) {
            isNetWorking = false;
            Log.i(TAG, "Net working: " + isNetWorking);
            showInternetDialog();
        }
    }

    private void deSelectAllDocuments() {
        if (ocrDocumentsAdapter != null) {
            ocrDocumentsAdapter.deSelectAllFies(new OnDeselectAllFiles() {
                @Override
                public void onDeselect() {
                    tv_total_file_count.setText(ocrDocumentsAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
                }
            });
            manageSelectAllText();
        }
    }

    private void showPopUpMoreMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        Field[] fields = popupMenu.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("mPopup".equals(field.getName())) {
                field.setAccessible(true);
                try {
                    Object menuPopupHelper = field.get(popupMenu);
                    if (menuPopupHelper != null) {
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceShowIcon.invoke(menuPopupHelper, true);
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        popupMenu.getMenuInflater().inflate(R.menu.file_operation_pop_menu, popupMenu.getMenu());

        popupMenu.getMenu().removeItem(R.id.menu_import_pdf);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_sort_by) {
                    showSortingDialog();
                } else if (itemId == R.id.menu_select_all) {/*selectAllDocuments();*/
                }
                return true;
            }
        });

        popupMenu.show();
    }

    private void selectAllDocuments() {
        if (ocrDocumentsAdapter != null) {
            ocrDocumentsAdapter.selectAllDocuments(new OnSelectAllFiles() {
                @Override
                public void onSelectedAllFiles() {
                    tv_total_file_count.setText(ocrDocumentsAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
                }
            });
            manageSelectAllText();
        }
    }

    private void manageSelectAllText() {
        if (ocrDocumentsAdapter != null && ocrDocumentsAdapter.getSelectedFileModelList().size() == getFetchedFileList().size()) {
            tv_select_all_files.setText(getString(R.string.deselect_all));
            selectionAction = Constants.DESELECT_ALL;
        } else {
            tv_select_all_files.setText(getString(R.string.select_all));
            selectionAction = Constants.SELECT_ALL;
        }
    }

    private void showSortingDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.sorting_dialog);

        RadioButton rbCreationTimeAscending, rbCreationTimeDescending, rbModificationTimeAscending,
                rbModificationTimeDescending, rbNameAtoZ, rbNameZtoA;

        rbCreationTimeAscending = dialog.findViewById(R.id.rb_creation_time_ascending);
        rbCreationTimeDescending = dialog.findViewById(R.id.rb_creation_time_descending);
        rbModificationTimeAscending = dialog.findViewById(R.id.rb_modification_time_ascending);
        rbModificationTimeDescending = dialog.findViewById(R.id.rb_modification_time_descending);
        rbNameAtoZ = dialog.findViewById(R.id.rb_name_a_to_z);
        rbNameZtoA = dialog.findViewById(R.id.rb_name_z_to_a);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        TextView tv_note = dialog.findViewById(R.id.tv_note);
        TextView tv_settings = dialog.findViewById(R.id.tv_settings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_note.setText(Html.fromHtml(getString(R.string.sorting_note_txt), Html.FROM_HTML_MODE_LEGACY));
            tv_settings.setText(Html.fromHtml(getString(R.string.underlined_settings), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_note.setText(Html.fromHtml(getString(R.string.sorting_note_txt)));
            tv_settings.setText(Html.fromHtml(getString(R.string.underlined_settings)));
        }

        tv_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openSettingScreen();
            }
        });

        int fileSortingOrder = prefManager.getFoldersSortingOrder();
        switch (fileSortingOrder) {
            case Constants.SORT_BY.defaultOrder:
            case Constants.SORT_BY.modificationTimeDescending:
                rbModificationTimeDescending.setChecked(true);
                break;
            case Constants.SORT_BY.modificationTimeAscending:
                rbModificationTimeAscending.setChecked(true);
                break;
            case Constants.SORT_BY.nameAtoZ:
                rbNameAtoZ.setChecked(true);
                break;
            case Constants.SORT_BY.nameZtoA:
                rbNameZtoA.setChecked(true);
                break;
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbModificationTimeAscending.isChecked()) {
                    fetchDocumentsByModificationTimeAscending();
                    prefManager.saveFoldersSortingOrder(Constants.SORT_BY.modificationTimeAscending);
                } else if (rbModificationTimeDescending.isChecked()) {
                    fetchDocumentsByModificationTimeDescending();
                    prefManager.saveFoldersSortingOrder(Constants.SORT_BY.modificationTimeDescending);
                } else if (rbNameAtoZ.isChecked()) {
                    fetchDocumentsBySortingAtoZ();
                    prefManager.saveFoldersSortingOrder(Constants.SORT_BY.nameAtoZ);
                } else if (rbNameZtoA.isChecked()) {
                    fetchDocumentsBySortingZtoA();
                    prefManager.saveFoldersSortingOrder(Constants.SORT_BY.nameZtoA);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void openSettingScreen() {
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchDocumentsBySortingZtoA() {
        if (!TextUtils.isEmpty(directoryPath))
            new GetOcrDocuments(context, directoryPath, this, Constants.SORT_BY.nameZtoA).execute();
    }

    private void fetchDocumentsBySortingAtoZ() {
        if (!TextUtils.isEmpty(directoryPath))
            new GetOcrDocuments(context, directoryPath, this, Constants.SORT_BY.nameAtoZ).execute();
    }

    private void fetchDocumentsByModificationTimeDescending() {
        if (!TextUtils.isEmpty(directoryPath))
            new GetOcrDocuments(context, directoryPath, this, Constants.SORT_BY.modificationTimeDescending).execute();
    }

    private void fetchDocumentsByModificationTimeAscending() {
        if (!TextUtils.isEmpty(directoryPath))
            new GetOcrDocuments(context, directoryPath, this, Constants.SORT_BY.modificationTimeAscending).execute();
    }

    private void showDeleteDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextView msgHeading = dialog.findViewById(R.id.msg_heading);
        TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);
        TextView btn_ok = dialog.findViewById(R.id.btn_ok);
        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        et_pdf_name.setVisibility(View.GONE);
        dialog.findViewById(R.id.rdo_grp_delete_options).setVisibility(View.VISIBLE);

        dialogTitle.setText(getString(R.string.delete));
        msgHeading.setText(getString(R.string.delete_msg));
        btn_cancel.setText(R.string.keep_it);
        btn_ok.setText(R.string.yes_btn_dialog);
        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
            hideCheckBoxAndRemoveBottomBar();
        });

        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
            List<FileModel> finalSelectedFileModelList = null;
            if (ocrDocumentsAdapter != null) {
                finalSelectedFileModelList = ocrDocumentsAdapter.getSelectedFileModelList();
            }
            if (finalSelectedFileModelList != null && !finalSelectedFileModelList.isEmpty()) {
                List<String> filePathList = new ArrayList<>();
                for (FileModel fileModel : finalSelectedFileModelList) {
                    if (fileModel != null) {
                        filePathList.add(fileModel.getPath());
                        if (((RadioButton) dialog.findViewById(R.id.rd_delete_from_both)).isChecked() && fileModel.isSavedOnGoogleDrive()) {
                            deleteFromGoogleDrive(fileModel.getName());
                        }
                    }
                }
                if (!filePathList.isEmpty()) {
                    new DeleteFolderOrFileTask(context, filePathList, () -> {
                        fetchFiles();
                        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.delete_success_msg));
                    }).execute();
                }
            }
            hideCheckBoxAndRemoveBottomBar();
        });
        dialog.show();

    }

    private void handleSearchBarVisibility() {
        if (tvToolbarTxt.getVisibility() == View.VISIBLE) {
            tvScannedText.setText("");
            tvToolbarTxt.setVisibility(View.GONE);
            et_search.setVisibility(View.VISIBLE);
            et_search.requestFocus();
            iv_search.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_white));
            showKeyboard();
        } else {
            clearSearchView();
        }
    }

    private void clearSearchView() {
        tvScannedText.setText(getString(R.string.scanned_text));
        tvToolbarTxt.setVisibility(View.VISIBLE);
        et_search.setText("");
        et_search.setVisibility(View.GONE);
        iv_search.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_search));
        hideKeyboard();
        if (ocrDocumentsAdapter != null) {
            ocrDocumentsAdapter.clearFilterList(getDocumentsListIncludingAds());
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void openMedia() {
        try {
            /*Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE_REQ_CODE);
            } else {
                Toast.makeText(context, "" + getString(R.string.no_app_handle), Toast.LENGTH_SHORT).show();
            }*/
            if (FlashScanUtil.isOsLessThanR()) {
                Matisse.from(this).choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF), false).countable(true)
                        .showSingleMediaType(true)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.9f).maxSelectable(1).imageEngine(new GlideEngine())
                        .forResult(PICK_IMAGE_REQ_CODE);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQ_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "" + getString(R.string.no_app_handle), Toast.LENGTH_SHORT).show();
        }

    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = flashScanUtil.createTempImageFile(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider"
                            , photoFile);
                } else {
                    imageUri = Uri.fromFile(photoFile);
                }
                if (imageUri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_DRIVE_SIGN_IN:
                // progress_lay.setVisibility(View.GONE);
                FlashScanUtil.newHideLoading();
                if (resultCode == Activity.RESULT_OK && data != null) {
                    flashScanUtil.handleSignInResult(context, data);
                    Log.d(TAG, "onActivityResult: REQUEST_CODE_DRIVE_SIGN_IN");
                    if (mFileModelForSaveToDrive != null) {
                        checkDriveSignIn(mFileModelForSaveToDrive, positionForSaveToDrive);
                    }
                }
                break;
            case Constants.REQUEST_CODE_PREMIUM_YEALY:
                Log.i(TAG, "OcrActivity REQUEST_CODE_PREMIUM_YEALY");
                if (resultCode == RESULT_OK) {
                    //PREMIUM taken
                    handlePremium();
                    if (prefManager.getPurchasedPlanName() == Constants.BUY_NOW_YEARLY) {
                        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.premium_yearly_success_msg, getString(R.string.app_name)));
                    } else {
                        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.premium_quarterly_success_msg, getString(R.string.app_name)));
                    }
                }
                break;
            case Constants.REQUEST_CODE_OCR_MONTHLY:
                Log.i(TAG, "OcrActivity REQUEST_CODE_OCR_MONTHLY");
                if (resultCode == RESULT_OK) {
                    //OCR monthly taken
                    handleOcrMonthly();
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.ocr_monthly_success_msg, getString(R.string.app_name)));
                    /*try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                break;
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (imageUri != null) {
                /*util.getBitmapFromUri(imageUri);*/
                /*cropImage(imageUri, REQUEST_TAKE_PHOTO);*/
                CropImage.activity(imageUri).start(this);
            }
        } else if (requestCode == PICK_IMAGE_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<Uri> uriList;
                if (FlashScanUtil.isOsLessThanR()) {
                    uriList = new ArrayList<>(Matisse.obtainResult(data));
                } else {
                    uriList = new ArrayList<>(FlashScanUtil.obtainResult(data));
                }
                if (!uriList.isEmpty()) {
                    CropImage.activity(uriList.get(0)).start(this);
                }
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                imageUri = result.getUri();
                //navigateToOcrPreviewActivity(imageUri);
                if (imageUri != null) {
                    /*if (!prefManager.isAppAdFree() && flashScanUtil.isConnectingToInternet()) {
                        rewardedAdShow(this);
                    }
                    else{*/
                    getImageUriAndSetBitmap(imageUri);
                    // }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        switch (requestCode) {
            case REQUEST_CODE_FETCH_OCR_DOCUMENTS:
                isNativeAdAlreadyLoaded = true;
                fetchFiles();
                break;
        }
        Log.i(TAG, "requestCode: " + requestCode);
        if (requestCode == REQUEST_CODE_WIFI_SETTING) {
            Log.i(TAG, "wifi state callback");
            if (!flashScanUtil.isConnectingToInternet()) {
                isNetWorking = false;
            } else {
                isNetWorking = true;
            }
            Log.i(TAG, "in wifi state callback, net working: " + isNetWorking);
            whichFabActionToBeTaken();
        }

      /*  else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = UCrop.getOutput(data);
                if (uri != null) {
                    navigateToOcrPreviewActivity(uri);
                    *//*bitmapFromUri = util.getBitmapFromUri(uri);
                    if (bitmapFromUri != null) {

                    }*//*
                }
            }
        }
        else if (requestCode == UCrop.RESULT_ERROR) {
            if (data != null) {
                Throwable error = UCrop.getError(data);
                Log.i(TAG, "Crop error: " + error);
            }
        }*/
    }

    private void getImageUriAndSetBitmap(Uri uri) {

        if (uri != null) {
            bitmapFromUri = flashScanUtil.getBitmapFromUri(uri);
           /* if (bitmapFromUri != null) {
                imageView.setImageBitmap(bitmapFromUri);
            }*/
            if (!isFinishing() || !isDestroyed()) {
                Glide.with(context).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmapFromUri = resource;
                        scanOCR(bitmapFromUri);
                        //imageView.setImageBitmap(bitmapFromUri);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            }

        }
    }

    private void scanOCR(Bitmap bitmapFromUri) {
        if (/*checkOcrCount() && */bitmapFromUri != null) {
            /*showAreaSelectDialog(bitmapFromUri);*/
            detectTextFromBitmap(bitmapFromUri, WHOLE_TEXT);
        }
    }

    private void detectTextFromBitmap(Bitmap bitmapFromUri, int textMode) {
         progress_lay.setVisibility(View.VISIBLE);
        //FlashScanUtil.newShowLoading(context, "");
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmapFromUri);

        FirebaseVisionTextRecognizer textRecognizer;

        //TODO: komal - uncomment for firebase plan purchase
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
                    FlashScanUtil.newHideLoading();
                    // progress_lay.setVisibility(View.GONE);
                    if (firebaseVisionText != null) {
                        processResult(firebaseVisionText, textMode, bitmapFromUri);
                    }

                    increaseOcrAttempted();
                    updateCreditsToApi();
                }).addOnFailureListener(e -> {
                    FlashScanUtil.newHideLoading();
                    // progress_lay.setVisibility(View.GONE);
                    Log.e("textRecognizer", "" + e.getMessage().toString());
                    Toast.makeText(context, "" + getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCreditsToApi() {
        if (Constants.IS_OWN_API_IMPLEMENT) {
            //FlashScanUtil.newShowLoading(context, "");
            progress_lay.setVisibility(View.VISIBLE);

            UpdateCreditsToApi updateCreditsToApi = new UpdateCreditsToApi(context, new OnApiResult() {
                @Override
                public void onApiResponse() {
                    Log.i(TAG, "onApiResponse");
                    //progress_lay.setVisibility(View.GONE);
                    FlashScanUtil.newHideLoading();
                }

                @Override
                public void onApiFailure() {
                    Log.i(TAG, "onApiFailure");
                    //progress_lay.setVisibility(View.GONE);
                    FlashScanUtil.newHideLoading();
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

    private void handlePremium() {
        reCreate();
    }

    private void handleOcrMonthly() {

    }

    private void reCreate() {
        //No need to reCreate here
        finish();
        startActivity(getIntent());
    }

    private void cropImage(Uri sourceUri, int requestMode) {
        String queryName = queryName(getContentResolver(), sourceUri);
        Uri destinationUri = null;
        if (!TextUtils.isEmpty(queryName)) {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                destinationUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider"
                        , new File(getCacheDir(), queryName));
            } else {
                destinationUri = Uri.fromFile(new File(getCacheDir(), queryName));
            }
*/
            destinationUri = Uri.fromFile(new File(getCacheDir(), queryName));
        }
        if (destinationUri != null) {
            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(IMAGE_COMPRESSION);
            options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary));
            options.withAspectRatio(ASPECT_RATIO_X, ASPECT_RATIO_Y);
            if (requestMode == REQUEST_TAKE_PHOTO) {
                options.withMaxResultSize(1024, 1024);
            }
            UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
                    .start(this);
        }

    }

    private String queryName(ContentResolver resolver, Uri uri) {
        String name = null;
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        int nameIndex = 0;
        if (returnCursor != null) {
            nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();

            name = returnCursor.getString(nameIndex);
            returnCursor.close();
        }
        return name;
    }

    private void navigateToOcrPreviewActivity(Uri imageUri) {
        Intent intent = new Intent(context, OcrPreviewActivity.class);
        intent.putExtra(Constants.PutExtraConstants.URI, imageUri);
        intent.putExtra(Constants.PutExtraConstants.OCR_IS_NET_WORKING, isNetWorking);
        Log.i(TAG, "At the time of navigating to OcrPreviewActivity, net working: " + isNetWorking);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (ocrDocumentsAdapter != null && ocrDocumentsAdapter.isVisibleAllCheckbox()) {
            /*iv_more_menu.setVisibility(View.GONE);*/
            hideCheckBoxAndRemoveBottomBar();
        } else if (et_search.getVisibility() == View.VISIBLE) {
            clearSearchView();
        } else {
           /* super.onBackPressed();
            overridePendingTransition(R.anim.slide_from_leftt, R.anim.slide_to_rightt);*/
            goToHome();
        }

    }

    private void goToHome() {
        hideKeyboard();
        if (Constants.ALWAYS_RELOAD_AD_ON_HOME_SCREEN) {
            intentToHomeDashBoard();
        } else {
            if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.IS_COMING_FROM_HOME_DASHBOARD)) {
                boolean isComingFromHomeDashboard = getIntent().getBooleanExtra(Constants.PutExtraConstants.IS_COMING_FROM_HOME_DASHBOARD, false);
                if (isComingFromHomeDashboard) {
                    finish();
                } else {
                    intentToHomeDashBoard();
                }
            } else {
                intentToHomeDashBoard();
            }
        }
    }

    private void intentToHomeDashBoard() {
        Intent intent = new Intent(context, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void hideCheckBoxAndRemoveBottomBar() {
        ocrDocumentsAdapter.hideAllCheckBoxes();
        ll_bottom_bar.setVisibility(View.GONE);
        ll_select_all_files.setVisibility(View.GONE);
        iv_more_menu.setVisibility(View.VISIBLE);
        ll_floating.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFetchingStartOcr() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFetchingCompleted(List<FileModel> fileModelList) {
        progress_lay.setVisibility(View.GONE);
        if (fileModelList != null && !fileModelList.isEmpty()) {
            if (!getFetchedFileList().isEmpty()) {
                getFetchedFileList().clear();
            }
            getFetchedFileList().addAll(fileModelList);
            showRecyclerView(fileModelList);
        } else {
            hideRecyclerView();
        }
    }

    private void hideRecyclerView() {
        recyclerView.setVisibility(View.GONE);
        ll_no_document.setVisibility(View.VISIBLE);
        tvScannedText.setVisibility(View.GONE);
        ll_floating.setVisibility(View.GONE);

        if (!prefManager.isAppAdFree() && flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_OCR_EMPTY_LIST.getValue())) {
            ad_view_banner_container.setVisibility(View.VISIBLE);
            callNativeAd(nativeSmallAdNoOcr);
        } else {
            Log.e("Mobibuz : ", "Ad Not Showing");
            ad_view_banner_container.setVisibility(View.GONE);
        }
    }

    private void showRecyclerView(List<FileModel> fileModelList) {
        ll_no_document.setVisibility(View.GONE);
        tvScannedText.setVisibility(View.VISIBLE);
        ad_view_banner_container.setVisibility(View.GONE);
        //ll_adView.setVisibility(View.GONE);
        ll_floating.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        //ll_native_ad_view.setVisibility(View.GONE);

        List<FileModel> finalFileModelList = new ArrayList<>();
        //boolean showNative = AppController.getINSTANCE().dbHandler.showNative();
        /*if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
                && Constants.SHOW_NATIVE_ADS.FOR_OCR_ACTIVITY) {*/
        /*if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
                && showNative) {
            if (fileModelList.size() <= Constants.AD_PER_ITEM) {
                finalFileModelList.addAll(fileModelList);
                FileModel fileModel = new FileModel();
                fileModel.setAdView(true);
                finalFileModelList.add(fileModel);
            } else {
                for (int i = 0; i < fileModelList.size(); i++) {
                    if (Constants.AdAfterItems.FOR_MULTIPLE_ITEMS) {
                        if (i != 0 && i % Constants.AD_PER_ITEM == 0) {
                            FileModel fileModel = new FileModel();
                            fileModel.setAdView(true);
                            finalFileModelList.add(fileModel);
                        }
                        finalFileModelList.add(fileModelList.get(i));
                    } else if (Constants.AdAfterItems.FOR_SINGLE_ITEM) {
                        if (i == Constants.AD_PER_ITEM) {
                            FileModel fileModel = new FileModel();
                            fileModel.setAdView(true);
                            finalFileModelList.add(fileModel);
                        }
                        finalFileModelList.add(fileModelList.get(i));
                    }

                }
            }
        } else {
            finalFileModelList.addAll(fileModelList);
        }*/

        finalFileModelList.addAll(fileModelList);
        if (com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isConnectingToInternet(this)) {
            FileModel fileModel = new FileModel();
            fileModel.setAdView(true);
            finalFileModelList.add(1, fileModel);
        }


        if (!getDocumentsListIncludingAds().isEmpty()) {
            getDocumentsListIncludingAds().clear();
        }
        getDocumentsListIncludingAds().addAll(finalFileModelList);
        ocrDocumentsAdapter = new OcrDocumentsAdapter(context, finalFileModelList, this, this);
        recyclerView.setAdapter(ocrDocumentsAdapter);
    }

    @Override
    public void onItemSelect(Object o) {
        if (ocrDocumentsAdapter != null && ocrDocumentsAdapter.isVisibleAllCheckbox()) {
            tv_total_file_count.setText(ocrDocumentsAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
            manageSelectAllText();
            return;
        }
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel != null) {
            File file = new File(fileModel.getPath());
            boolean isOcrResultFileAlreadySaved = false;
            String ocrResultSavedFilePath = null;
            if (file.isDirectory() && file.exists()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File eachFile : files) {
                        if (eachFile != null && eachFile.isFile() && eachFile.exists()) {
                            if (eachFile.getName().contains("_" + getString(R.string.suffix_app_name)) &&
                                    flashScanUtil.getExtensionFromFileName(eachFile.getName()).equalsIgnoreCase(Constants.TXT_FILE_EXTENSION_WITHOUT_DOT)) {
                                isOcrResultFileAlreadySaved = true;
                                ocrResultSavedFilePath = eachFile.getPath();
                                break;
                            }
                        }
                    }
                }
            }

            Intent intent = new Intent(context, OcrResultActivity.class);
            intent.putExtra(Constants.PutExtraConstants.OCR_RESULT_FROM_SCREEN, Constants.OcrResultScreenFrom.FROM_DOCUMENT);
            intent.putExtra(Constants.PutExtraConstants.FILE_PATH, FlashScanUtil.getOcrProcessingPath(context).getAbsolutePath() + "/" + fileModel.getName());
            if (isOcrResultFileAlreadySaved && !TextUtils.isEmpty(ocrResultSavedFilePath)) {
                intent.putExtra(Constants.PutExtraConstants.OCR_SAVED_FILE_PATH, ocrResultSavedFilePath);
            }
            startActivityForResult(intent, REQUEST_CODE_FETCH_OCR_DOCUMENTS);
            // TODO: 29-06-2020 commented for avoiding reload ad
            /*finish();*/
            //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
    }

    @Override
    public void onItemLongPress(Object o) {
        if (ocrDocumentsAdapter != null && ocrDocumentsAdapter.isVisibleAllCheckbox()) {
            ll_bottom_bar.setVisibility(View.VISIBLE);
            ll_select_all_files.setVisibility(View.VISIBLE);
            iv_more_menu.setVisibility(View.GONE);
            ll_floating.setVisibility(View.GONE);
            tv_total_file_count.setText(ocrDocumentsAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
            manageSelectAllText();
        }
    }

    @Override
    public void onItemAction(Object o, View view) {

    }

    @Override
    public void actionAddToDrive(Object o, int position) {
        if (!flashScanUtil.isConnectingToInternet()) {
            Toast.makeText(context, "Connect to internet then try again!", Toast.LENGTH_LONG).show();
            return;
        }

        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel == null) return;
        CommonMethods.logCustomFireBaseEvents(
                "OCRActivity",
                com.itl.commonres.utils.Constants.CLICK_GOOGLE_DRIVE_ICON
        );
        checkDriveSignIn(fileModel, position);
    }

    @Override
    public void actionShare(Object o) {

    }

    @Override
    public void actionRename(Object o) {
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel != null)
            showCommonDialog(fileModel, Constants.FileOperations.ACTION_RENAME);
    }

    @Override
    public void actionDelete(Object o) {
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel != null) {
            CommonMethods.logCustomFireBaseEvents(
                    "OCRActivity",
                    com.itl.commonres.utils.Constants.CLICK_DELETE_ICON
            );
            showCommonDialog(fileModel, Constants.FileOperations.ACTION_DELETE);
        }
    }

    private void showCommonDialog(FileModel fileModel, int action) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextView msgHeading = dialog.findViewById(R.id.msg_heading);
        TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);
        TextView btn_ok = dialog.findViewById(R.id.btn_ok);
        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);

        switch (action) {
            case Constants.FileOperations.ACTION_RENAME:
                dialogTitle.setText(getString(R.string.rename_file));
                msgHeading.setText(getString(R.string.rename_msg));
                msgHeading.setText("");
                et_pdf_name.setText(fileModel.getName());
                et_pdf_name.setSelection(et_pdf_name.getText().length());
                /*et_pdf_name.setSelectAllOnFocus(true);*/
                break;
            case Constants.FileOperations.ACTION_DELETE:
                dialogTitle.setText(getString(R.string.delete));
                msgHeading.setText(getString(R.string.delete_msg));
                btn_cancel.setText(R.string.keep_it);
                btn_ok.setText(R.string.yes_btn_dialog);
                et_pdf_name.setVisibility(View.GONE);
                if (fileModel.isSavedOnGoogleDrive()) {
                    dialog.findViewById(R.id.rdo_grp_delete_options).setVisibility(View.VISIBLE);
                }
                break;
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                dialog.dismiss();
                /*clearSelectedFiles();*/
            }
        });


        btn_ok.setOnClickListener(v -> {
            hideKeyboard();
            switch (action) {
                case Constants.FileOperations.ACTION_RENAME:
                    String folderName = et_pdf_name.getText().toString().trim();
                    if (TextUtils.isEmpty(folderName)) {
                        Toast.makeText(context, getString(R.string.please_name_file), Toast.LENGTH_SHORT).show();
                        return;
                    } else if (folderName.equalsIgnoreCase(fileModel.getName())) {
                        Toast.makeText(context, getString(R.string.file_name_same_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    renameFolder(folderName, fileModel);
                    dialog.dismiss();
                    break;
                case Constants.FileOperations.ACTION_DELETE:

                    //new DeleteFolderOrFileTask(MainActivity.this,fileModel.getPath(), this).execute();
//                    AppController.getINSTANCE().dbHandler.deleteApplyFilterFolder(fileModel.getName());
//                    dialog.dismiss();

                    new DeleteFolderOrFileTask(OcrActivity.this, fileModel.getPath(), this).execute();
                    AppController.getINSTANCE().dbHandler.deleteApplyFilterFolder(fileModel.getName());
                    dialog.dismiss();
                    if (((RadioButton) dialog.findViewById(R.id.rd_delete_from_both)).isChecked()) {
                        deleteFromGoogleDrive(fileModel.getName());
                    }
                    break;
            }

        });

        dialog.show();
    }

    private void renameFolder(String newFolderName, FileModel fileModel) {
        File oldFolder = new File(fileModel.getFolder(), fileModel.getName());
        File newFolder = new File(fileModel.getFolder(), newFolderName);
        if (newFolder.exists()) {
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.same_folder_already_exist));
            return;
        }
        boolean isRenamed = oldFolder.renameTo(newFolder);
        if (isRenamed) {
            AppController.getINSTANCE().dbHandler.updateFolderName(fileModel.getName(), newFolderName);
            AppController.getINSTANCE().dbHandler.updateApplyFilterFolder(newFolderName, fileModel.getName());

            File dstOriginalFolderName = new File(flashScanUtil.getDocOriginalPath(context), fileModel.getName());
            File tempOriginal = new File(flashScanUtil.getDocOriginalPath(context), newFolderName);
            dstOriginalFolderName.renameTo(tempOriginal);

            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.rename_success_msg));

            if (fileModel.isSavedOnGoogleDrive()) {
                String strFileId = fileModel.getGoogleDriveFolderId();
                flashScanUtil.deleteFolderByIdFromGoogleDrive(context, strFileId, context.getResources().getString(R.string.updating_files_to_google_drive), () -> {
                    prefManager.deleteFolderFromGoogleDriveDataList(strFileId);
                    fileModel.setName(newFolderName);
                    fileModel.setPath(newFolder.getPath());
                    flashScanUtil.saveFileInGoogleDrive(context, Constants.FLASH_SCAN_OCR, fileModel, false, context.getResources().getString(R.string.updating_file_metadata), folderId -> {
                        fetchFiles();
                    });
                });
            } else {
                fetchFiles();
            }

        } else {
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.something_went_wrong));
        }

    }

    private void fetchFiles() {
        String defaultPathForOcr = FlashScanUtil.getOcrProcessingPath(context).getAbsolutePath();
        if (!TextUtils.isEmpty(defaultPathForOcr)) {
            directoryPath = FlashScanUtil.getOcrProcessingPath(context).getAbsolutePath();
            if (!TextUtils.isEmpty(directoryPath)) {
                fetchOcrDocuments(directoryPath);
            } else {
                Toast.makeText(context, "" + getString(R.string.unable_to_fetch_documents), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "" + getString(R.string.unable_to_fetch_documents), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void actionSaveAsPdf(Object o) {

    }

    @Override
    public void makeFavourite(Object o) {

    }

    @Override
    public void removeFavourite(Object o) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        hideKeyboard();
    }

    @Override
    public void onFileOrFolderDeleted() {
        fetchFiles();
        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.delete_success_msg));
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        FlashScanUtil.newHideLoading();

        if (ocrDocumentsAdapter != null) {
            ocrDocumentsAdapter.destroyAdapterNativeAd();
        }
        super.onDestroy();
    }

    @Override
    public void onDocumentCreationStart() {
        if (progress_lay != null) {
            progress_lay.setVisibility(View.VISIBLE);
        }
        //FlashScanUtil.newShowLoading(context, "");
    }

    //public static boolean goAhead = false;
    @Override
    public void onDocumentCreated(String folderPath) {
        if (progress_lay != null) {
            progress_lay.setVisibility(View.GONE);
        }
        //FlashScanUtil.newHideLoading();
        createdFolderPath = folderPath;
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
            if (flashScanUtil.isConnectingToInternet() /*&& ocrInterstitialAd == null*/) {
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_OCR, this);
                    /*progress_lay.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    InterstitialAd.load(context, BuildConfig.INTERSTITIAL_OCR, adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            Log.i(TAG, "onAdLoaded");
                            ocrInterstitialAd = interstitialAd;
                            progress_lay.setVisibility(View.GONE);
                            showOcrInterstitial(folderPath);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            progress_lay.setVisibility(View.GONE);
                            Log.i(TAG, "onAdFailedToLoad  " + loadAdError.getMessage() + " errorcode " + loadAdError.getCode());
                            if (!isMovedToResultScreen) {
                                moveToOcrResult(folderPath);
                            }
                        }
                    });*/


                //show(this);
                /*if (!isMovedToResultScreen) {
                    moveToOcrResult(folderPath);
                }*/
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

    private void navigateToOcrResultActivity(String text, String folderPath) {
        Intent intent = new Intent(context, OcrResultActivity.class);
        intent.putExtra(Constants.PutExtraConstants.OCR_RESULT_TEXT, text);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_PATH, folderPath);
        intent.putExtra(Constants.PutExtraConstants.FROM_SOURCE, TAG);
        intent.putExtra(Constants.PutExtraConstants.OCR_RESULT_FROM_SCREEN, Constants.OcrResultScreenFrom.FROM_PREVIEW);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void connectBillingService(int i) {
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
                            allowOCR(i);
                        } else {
                            int allowedFreeOcr = dbHandler.getAllowedFreeOcr();
                            Log.i(TAG, "allowedFreeOcr: " + allowedFreeOcr);

                            int existingOcrFreeAttempted = dbHandler.getOcrFreeAttempted();
                            Log.i(TAG, "existingOcrFreeAttempted: " + existingOcrFreeAttempted);

                            if(allowedFreeOcr == existingOcrFreeAttempted){
                                askToChoosePlan();
                            }
                            else{
                                allowOCR(i);
                            }
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
        });*/
    }

    private void allowOCR(int i) {

        if (checkOcrCount()) {
            if (Constants.IS_CLOUD_VISION_ALLOW) {
                whichFab = Constants.FAB_CAMERA;
                checkInternet();
                if (isNetWorking) {
                    if (i == 1) {
                        takePicture();
                    } else {
                        openMedia();
                    }
                }
            } else {
                if (i == 1) {
                    takePicture();
                } else {
                    openMedia();
                }
            }
        }
    }

    private void clearGooglePlayStoreBillingCacheIfPossible() {

       /* billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
                Log.i(TAG, "onPurchaseHistoryResponse");

            }
        });*/
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
    }

    private void callNativeAd(FrameLayout nativeSmallAdNoOcr) {
        if (AppController.nativeAdOcr == null) {
            AdLoader customEventNativeLoader = new AdLoader.Builder(context,
                    BuildConfig.AD_UNIT_ID_OCR_EMPTY_LIST_SCREEN_NATIVE_AD)
                    .forNativeAd(nativeAd -> {
                        Log.e("NO_OCR_NATIVE_AD ", "onUnifiedNativeAdLoaded G `> " + "");
                        AppController.nativeAdOcr = nativeAd;
                        smallDocNativeAdSet(nativeAd, nativeSmallAdNoOcr, false);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.e("NO_OCR_NATIVE_AD ", "onAdFailedToLoad G > " + loadAdError.getMessage());
                        }
                    }).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                            .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build())
                    .build();
            customEventNativeLoader.loadAd(new AdRequest.Builder().build());
        } else {
            smallDocNativeAdSet(AppController.nativeAdOcr, nativeSmallAdNoOcr, false);
        }
    }

    private void checkDriveSignIn(FileModel fileModel, int position) {
        if (flashScanUtil.isDriveSignedIn()) {
            Log.e(TAG, "Drive signed in " + flashScanUtil.isDriveSignedIn());
            if (fileModel.isSavedOnGoogleDrive()) {
                //showDeleteDialogGoogleDrive(fileModel);
                Toast.makeText(context, "Document already synced", Toast.LENGTH_LONG).show();
            } else {
                flashScanUtil.saveFileInGoogleDrive(context, Constants.FLASH_SCAN_OCR, fileModel, false, context.getResources().getString(R.string.uploading_files_to_drive), folderId -> {
                    fileModel.setGoogleDriveFolderId(folderId);
                    fileModel.setSavedOnGoogleDrive(true);
                    ocrDocumentsAdapter.notifyItemChanged(position);
                });
            }
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil.isDriveSignedIn());
            progress_lay.setVisibility(View.VISIBLE);
            //FlashScanUtil.newShowLoading(context, "");
            mFileModelForSaveToDrive = fileModel;
            positionForSaveToDrive = position;
            startActivityForResult((flashScanUtil.requestSignIn(context)).getSignInIntent(), REQUEST_CODE_DRIVE_SIGN_IN);
        }
    }

    private void deleteFromGoogleDrive(String folderName) {
        if (flashScanUtil.isDriveSignedIn()) {
            Log.e(TAG, "Drive signed in " + flashScanUtil.isDriveSignedIn());
            flashScanUtil.deleteFolderFromGoogleDrive(context, folderName);
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil.isDriveSignedIn());
            progress_lay.setVisibility(View.VISIBLE);
            //FlashScanUtil.newShowLoading(context, "");
            startActivityForResult((flashScanUtil.requestSignIn(context)).getSignInIntent(), REQUEST_CODE_DRIVE_SIGN_IN);
        }
    }

    public void showOcrInterstitial(String folderPath) {
        if (ocrInterstitialAd != null) {
            Log.d(TAG, "show_called");
            ocrInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when fullscreen content is dismissed.
                    Log.d(TAG, "The ad was dismissed.");
                    //adClosed.onAdClosed();
                    if (!isMovedToResultScreen) {
                        moveToOcrResult(folderPath);
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when fullscreen content failed to show.
                    Log.d(TAG, "The ad failed to show.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    // Make sure to set your reference to null so you don't
                    // show it a second time.
                    ocrInterstitialAd = null;
                    Log.d(TAG, "The ad was shown.");
                }
            });

            ocrInterstitialAd.show(this);
        } else {
            Log.d(TAG, "else show.");
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

