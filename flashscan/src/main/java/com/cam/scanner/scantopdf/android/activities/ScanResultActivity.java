package com.cam.scanner.scantopdf.android.activities;

import static com.cam.scanner.scantopdf.android.activities.HomeActivity.REQUEST_GET_IMAGES_USING_LIBRARY;
import static com.cam.scanner.scantopdf.android.activities.HomeActivity.TAKE_PHOTO;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
import com.cam.scanner.scantopdf.android.SingleTon.PdfSettings;
import com.cam.scanner.scantopdf.android.adapters.PageSizesAdapter;
import com.cam.scanner.scantopdf.android.adapters.ScanResultAdapter;
import com.cam.scanner.scantopdf.android.ads.AdClosed;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.CreateOcrDocument;
import com.cam.scanner.scantopdf.android.asynctasks.CreatePdfTask;
import com.cam.scanner.scantopdf.android.asynctasks.DeleteFolderOrFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetCompressedBitmapFilePath;
import com.cam.scanner.scantopdf.android.asynctasks.GetCompressedBitmapFilePathList;
import com.cam.scanner.scantopdf.android.asynctasks.GetFilesTask;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.dialogs.OcrChoosePlanDialog;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.CreateMultipleTempBitmapListener;
import com.cam.scanner.scantopdf.android.interfaces.CreateTempBitmapListener;
import com.cam.scanner.scantopdf.android.interfaces.DocumentCreationListener;
import com.cam.scanner.scantopdf.android.interfaces.FileOrFolderDeleteListener;
import com.cam.scanner.scantopdf.android.interfaces.GoogleDriveDataUploadListener;
import com.cam.scanner.scantopdf.android.interfaces.OnDeselectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.OnFetchingCompleted;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.interfaces.OnSelectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.PDFCreationCallback;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.models.GoogleDriveFolderModel;
import com.cam.scanner.scantopdf.android.models.ImageToPdfOptions;
import com.cam.scanner.scantopdf.android.models.PageSize;
import com.cam.scanner.scantopdf.android.pdf.PdfEditorActivity;
import com.cam.scanner.scantopdf.android.rest.UpdateCreditsToApi;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.ScanConstants;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;
import com.itl.commonres.permissions.PermissionPreference;
import com.itl.commonres.permissions.PermissionUtils;
import com.itl.commonres.permissions.PermissionsListSealedClass;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;
import com.itl.commonres.utils.InterstitialAdCappingEnum;
import com.itl.commonres.utils.PermissionInterface;
import com.theartofdev.edmodo.cropper.CropImage;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// called from result fragment with its package as prefix
public class ScanResultActivity extends BaseActivity implements View.OnClickListener, OnFetchingCompleted, OnItemSelectListener,
        PDFCreationCallback, FileOrFolderDeleteListener, DocumentCreationListener, PurchasesUpdatedListener, AdClosed, PermissionUtils.RequestPermissionsInterface, PermissionInterface {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int PERMISSIONS_SETTING_REQUEST_CODE = 101;
    private static final int REQUEST_CODE_FOR_SINGLE_FILE = 201;
    private static final int REQUEST_CODE_FOR_MULTIPLE_FILES = 202;
    private static final int REQUEST_CODE_FOR_SHARE_SINGLE_FILE = 206;
    private static final int REQUEST_CODE_FOR_SHARE_MULTIPLE_FILES = 204;
    private static final int REQUEST_CODE_FOR_COMPLETE_DOC = 205;
    private static final int PDF_BY_DIRECT = 1;
    private static final int PDF_VIA_SHARE = 2;
    private static final int PDF_WHOLE_DOCUMENT = 3;
    private static final int REQUEST_PERM_DELETE = 44;
    private static final int REQUEST_PERM_WRITE = 55;
    private static final int STORAGE_PERMISSION_REQ_CODE = 66;
    private static final String TAG = ScanResultActivity.class.getSimpleName();
    private static final int SINGLE_TEXT = 1;
    private static final int WHOLE_TEXT = 2;
    final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
    private boolean isNetWorking = true;
    private boolean isWritePermissionGranted = false;
    private boolean isRenamed;
    private String fileName;
    private FileModel fileModel;
    private ImageView iv_back_toolbar, iv_share, iv_pdf, iv_menu, iv_camera, iv_media;
    private FlashScanUtil flashScanUtil;
    private Context context;
    private RecyclerView rv_scan_result;
    private TextView tv_no_scan_document, tv_toolbar, tv_file_count, tv_date, tv_save_as_pdf, tv_share, tv_delete, tv_copy, tv_move, tv_total_file_count, tv_select_all_files;
    private FloatingActionButton fab_camera, fab_media;
    private String folderName;
    private ScanResultAdapter scanResultAdapter;
    private LinearLayout ll_file_count, ll_no_document, ll_bottom_bar, ll_floating, ll_select_all_files;
    private List<String> selectedImagesList = new ArrayList<>();
    private boolean isPdfCreatedForSharing;
    private int screenFrom = 0;
    private View progress_lay;
    private int totalFilesSize;
    private Button btn_progress_lay;
    private PrefManager prefManager;
    private int sortingOrder;
    private RewardedAd rewardedAd;
    private int selectionAction = -1;
    private boolean isMultiplePdfCreationWithCompression = false;
    //    private MainIdentity mIdentity;
    private String selectedPageSize;
    private Uri fileUri, oldFileUri;
    private FrameLayout flMain;
    private String createdFolderPath = "";
    private DBHandler dbHandler;
    private BillingClient billingClient;
    private LinearLayout nativeAdScanResult;
    private FrameLayout customNativeAdScanResult;
    private String isPDForOCR = "";
    private boolean watermarkToBeShownOrNot;
    //This variable used for uploading newly added files to drive
    private boolean isSaveOnGoogleDrive = false;
    private String googleDriveFolderId;
    private FileModel fileModelForWaterMark;
    private List<FileModel> fileModelListForWaterMark = new ArrayList<>();
    private List<FileModel> totalFetchedFiles = new ArrayList<>();
    private String pdfFileNameForMultipleDocs;
    private boolean sharePdfDirectWithoutOpen = false;
    private View tut_scan_result_lay;
    private Bitmap bitmapFromUri;
    private String recognizedText;
    private boolean shouldIntersCreateShow;
    private boolean isMovedToResultScreen = false;
    // private boolean filesAlreadyFetched = false;
    private InterstitialAd ocrInterstitialAd;
    private ArrayList<String> permissionList = new ArrayList<>();
    private PermissionUtils permissionUtils;
    private boolean isCheckPermissionResultOnly = false;
    ActivityResultLauncher<String[]> requestMultiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        permissionUtils.onRequestPermissionResult(isCheckPermissionResultOnly);
    });
    private Boolean isPermissionGranted = false;
    private boolean isAdLoaded = false;

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean deleteFileUsingDisplayName(Context context, String displayName) {

        Uri uri = getUriFromDisplayName(context, displayName);
        if (uri != null) {
            final ContentResolver resolver = context.getContentResolver();
            String[] selectionArgsPdf = new String[]{displayName};

            try {
                resolver.delete(uri, MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", selectionArgsPdf);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                // show some alert message
            }
        }
        return false;

    }

    public static Uri getUriFromDisplayName(Context context, String displayName) {

        String[] projection;
        projection = new String[]{MediaStore.Files.FileColumns._ID};

        // TODO This will break if we have no matching item in the MediaStore.
        Uri extUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        Log.e(TAG, "extUri " + extUri);
        Cursor cursor = context.getContentResolver().query(extUri, projection,
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?", new String[]{displayName}, null);
        assert cursor != null;
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            int columnIndex = cursor.getColumnIndex(projection[0]);
            long fileId = cursor.getLong(columnIndex);

            cursor.close();
            return Uri.parse(extUri.toString() + "/" + fileId);
        } else {
            return null;
        }

    }

    public FileModel getFileModelForWaterMark() {
        return fileModelForWaterMark;
    }

    public void setFileModelForWaterMark(FileModel fileModelForWaterMark) {
        this.fileModelForWaterMark = fileModelForWaterMark;
    }

    public List<FileModel> getFileModelListForWaterMark() {
        if (fileModelListForWaterMark == null) {
            fileModelListForWaterMark = new ArrayList<>();
        }
        return fileModelListForWaterMark;
    }



    /*public void callNativeAd(Context context) {
        if (AppController.nativeAdScanResult == null) {
            AdLoader customEventNativeLoader = new AdLoader.Builder(context,
                    BuildConfig.NATIVE_AD_ID)
                    .forNativeAd(nativeAd -> {
                        Log.e("SCAN_RESULT", "onUnifiedNativeAdLoaded G `> " + "");
                        AppController.nativeAdScanResult = nativeAd;
                        nativeAdSquare(nativeAd, customNativeAdScanResult);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.e("SCAN_RESULT ", "onAdFailedToLoad G > " + loadAdError.getMessage());
                        }
                    }).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                            .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build())
                    .build();
            customEventNativeLoader.loadAd(new AdRequest.Builder().build());
        } else {
            nativeAdSquare(AppController.nativeAdScanResult, customNativeAdScanResult);
        }
    }*/

    /*private void loadAndShowInterstialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(BuildConfig.INTERSTITIAL_AD_ID_FOR_SCAN_RESULT_ACTIVITY);
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

    public void setFileModelListForWaterMark(List<FileModel> fileModelListForWaterMark) {
        if (!getFileModelListForWaterMark().isEmpty()) {
            getFileModelListForWaterMark().clear();
        }
        getFileModelListForWaterMark().addAll(fileModelListForWaterMark);
        /*this.fileModelListForWaterMark = fileModelListForWaterMark;*/
    }

    public List<FileModel> getTotalFetchedFiles() {
        if (totalFetchedFiles == null) {
            totalFetchedFiles = new ArrayList<>();
        }
        return totalFetchedFiles;
    }

    private String getPdfFileNameForMultipleDocs() {
        if (TextUtils.isEmpty(pdfFileNameForMultipleDocs)) {
            pdfFileNameForMultipleDocs = flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name);
        }
        return pdfFileNameForMultipleDocs;
    }

    /*private void loadRewardedAd() {
        rewardedAd = new RewardedAd(context, BuildConfig.REWARD_AD_ID);
        RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                Log.i(TAG, "onRewardedAdLoaded called");
            }

            @Override
            public void onRewardedAdFailedToLoad(int i) {
                super.onRewardedAdFailedToLoad(i);
                Log.i(TAG, "onRewardedAdFailedToLoad called");
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), rewardedAdLoadCallback);
    }*/

    /*private void getUriAndSetBitmap() {
        if (getIntent() != null && getIntent().hasExtra(ScanConstants.SCANNED_RESULT)) {
            *//*uri = getIntent().getParcelableExtra(ScanConstants.SCANNED_RESULT);*//*
     *//* byte[] byteArrayExtra = getIntent().getByteArrayExtra(ScanConstants.SCANNED_RESULT);
            if (byteArrayExtra != null && byteArrayExtra.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArrayExtra, 0, byteArrayExtra.length);
                if (bitmap != null)
                    iv_result.setImageBitmap(bitmap);
            }*//*
            String encodedBitmap = getIntent().getStringExtra(ScanConstants.SCANNED_RESULT);
            if (!TextUtils.isEmpty(encodedBitmap)) {
               *//* byte[] decode = Base64.decode(encodedBitmap, Base64.DEFAULT);
                if (decode != null && decode.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
                    if (bitmap != null)
                        iv_result.setImageBitmap(bitmap);
                }*//*
                try {
                    FileInputStream fileInputStream = context.openFileInput(encodedBitmap);
                    Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                    if (fileInputStream != null) fileInputStream.close();
                    if (bitmap != null) iv_result.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    private void setPdfFileNameForMultipleDocs(String pdfFileNameForMultipleDocs) {
        this.pdfFileNameForMultipleDocs = pdfFileNameForMultipleDocs;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        if (getIntent() != null && getIntent().hasExtra(ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE)) {
            isSaveOnGoogleDrive = getIntent().getBooleanExtra(ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE, false);
            if (isSaveOnGoogleDrive) {
                googleDriveFolderId = getIntent().getStringExtra(ScanConstants.PutExtraConstants.GOOGLE_DRIVE_FOLDER_ID);
            }
        }

        Log.e(TAG, "onCreate: isSaveOnGoogleDrive = " + isSaveOnGoogleDrive);
        Log.e(TAG, "onCreate: googleDriveFolderId = " + googleDriveFolderId);

        findIds();
        initObjects();

        setClickListeners();
        /*getUriAndSetBitmap();*/
        getIntentData();
        // loadInterstitialAd(context,BuildConfig.INTERSTITIAL_AD_ID_FOR_OCR_RESULT_ACTIVITY , this);

        /*mIdentity = new ViewModelProvider(this).get(MainIdentity.class);
        mIdentity.loadSettings();*/

        // for total file counts only
       /* new GetFilesTask(context, "", new OnFetchingCompleted() {
            @Override
            public void onFetchingComplete(List<FileModel> fileModelList) {

                if (fileModelList != null && !fileModelList.isEmpty()) {
                    totalFilesSize = fileModelList.size();
                }
            }
            @Override
            public void onFetchingStart() {
                progress_lay.setVisibility(View.VISIBLE);
            }
        }, Constants.RECENT_DOCS_COUNT_LIMITLESS, prefManager.getAppSortingOrder()).execute();*/

        manageTutorialView();
    }

    private void manageTutorialView() {
        if (prefManager.isScanResultTutWatched()) {
            tut_scan_result_lay.setVisibility(View.GONE);
        } else {
            tut_scan_result_lay.setVisibility(View.VISIBLE);
            prefManager.setScanResultTutWatched(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefManager.isPremium()) {
            if (nativeAdScanResult != null) {
                nativeAdScanResult.setVisibility(View.GONE);
            }
        }
        FlashScanUtil.newHideLoading();
        if (!flashScanUtil.isConnectingToInternet()) {
            isNetWorking = false;
        } else {
            isNetWorking = true;
        }
        Log.e(TAG, "onResume called");
        prefManager.saveFileSortingOrder(prefManager.getAppSortingOrder());
        getFolderNameAndFetchFiles();
    }

    private void getIntentData() {
        if (getIntent() != null && getIntent().hasExtra(ScanConstants.PutExtraConstants.FROM_SCREEN)) {
            screenFrom = getIntent().getIntExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, 0);
        }
/*
        if (!prefManager.isAppAdFree() && screenFrom == ScanConstants.ScreenConstants.FROM_SCAN_RESULT && Constants.SHOW_INTERSTITIAL_ADS.FOR_SCAN_RESULT_ACTIVITY) {
            loadAndShowInterstialAd();
        }*/

    }

    private void getFolderNameAndFetchFiles() {
        if (getIntent() != null && getIntent().hasExtra(ScanConstants.PutExtraConstants.FOLDER_NAME)) {
            folderName = getIntent().getStringExtra(ScanConstants.PutExtraConstants.FOLDER_NAME);
            if (!TextUtils.isEmpty(folderName)) {
                tv_toolbar.setText(folderName);
            }
            fetchFiles();
        }
        if (getIntent() != null && getIntent().hasExtra(ScanConstants.PutExtraConstants.DATE_TAKEN)) {
            long folderDateTaken = getIntent().getLongExtra(ScanConstants.PutExtraConstants.DATE_TAKEN, 0);
            if (folderDateTaken != 0) {
                tv_date.setVisibility(View.VISIBLE);
                tv_date.setText(Constants.getDateFromTimeStamp(folderDateTaken));
            } else {
                tv_date.setVisibility(View.GONE);
            }
        }
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        prefManager = new PrefManager(context);
        dbHandler = AppController.getINSTANCE().dbHandler;
        /*if (!prefManager.isAppRewardAdFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
            loadRewardedAd();
        }*/
        permissionUtils = new PermissionUtils(this, this, this);
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        fab_camera.setOnClickListener(this);
        fab_media.setOnClickListener(this);
        iv_pdf.setOnClickListener(this);
        iv_menu.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        tv_save_as_pdf.setOnClickListener(this);
        tv_copy.setOnClickListener(this);
        tv_move.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
        tv_select_all_files.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_media.setOnClickListener(this);
        tut_scan_result_lay.setOnClickListener(this);
    }

    private void findIds() {
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        rv_scan_result = findViewById(R.id.rv_scan_result);
        setRecyclerView();
        iv_share = findViewById(R.id.iv_share);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        fab_camera = findViewById(R.id.fab_camera);
        fab_media = findViewById(R.id.fab_media);
        iv_pdf = findViewById(R.id.iv_pdf);
        iv_pdf.setVisibility(View.VISIBLE);
        tv_file_count = findViewById(R.id.tv_file_count);
        ll_file_count = findViewById(R.id.ll_file_count);
        tv_date = findViewById(R.id.tv_date);
        iv_menu = findViewById(R.id.iv_menu);
        iv_menu.setVisibility(View.VISIBLE);
        progress_lay = findViewById(R.id.progress_lay);
        ll_no_document = findViewById(R.id.ll_no_document);
        tv_no_scan_document = findViewById(R.id.tv_no_scan_document);
        ll_bottom_bar = findViewById(R.id.ll_bottom_bar);
        tv_delete = findViewById(R.id.tv_delete);
        tv_save_as_pdf = findViewById(R.id.tv_save_as_pdf);
        tv_share = findViewById(R.id.tv_share);
        tv_copy = findViewById(R.id.tv_copy);
        tv_move = findViewById(R.id.tv_move);
        tv_copy.setVisibility(View.VISIBLE);
        tv_move.setVisibility(View.VISIBLE);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        ll_floating = findViewById(R.id.ll_floating);
        ll_select_all_files = findViewById(R.id.ll_select_all_files);
        tv_select_all_files = findViewById(R.id.tv_select_all_files);
        tv_total_file_count = findViewById(R.id.tv_total_file_count);
        iv_media = findViewById(R.id.iv_media);
        iv_camera = findViewById(R.id.iv_camera);
        tut_scan_result_lay = findViewById(R.id.tut_scan_result_lay);

        flMain = findViewById(R.id.fl_main);
        nativeAdScanResult = findViewById(R.id.nativeAdScanResult);
        customNativeAdScanResult = findViewById(R.id.customNativeAdScanResult);
    }

    private void setRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        rv_scan_result.setLayoutManager(layoutManager);

    }

    private void cameraIntent() {
        final File fileSink = getExternalCacheDir();

        if (fileSink.exists() || fileSink.mkdirs()) {
            Intent captureIntent = new Intent(this, CaptureImagesActivity.class);
            startActivityForResult(captureIntent, TAKE_PHOTO);
        }
    }

    /*private void showRewardAdDialogForSelectedFiles(List<FileModel> selectedFileModelList) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        *//*createPdfForAllFiles(selectedFileModelList, false);*//*
                        saveAsPdfSelectedDocuments(selectedFileModelList, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        saveAsPdfSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        saveAsPdfSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(ScanResultActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    /*private void showRewardAdDialogForAllFiles(List<FileModel> selectedFileModelList) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        createPdfForAllFiles(selectedFileModelList, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForAllFiles(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForAllFiles(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(ScanResultActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    @Override
    public void onClick(View v) {
//        ExtentionFunctionKt.setSafeClickListener(v, view -> {
        if (CommonMethods.multipleClicked()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.iv_share) {
        } else if (id == R.id.fab_camera || id == R.id.iv_camera) {
            cameraIntent();
        } else if (id == R.id.iv_pdf) {
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_PDF_ICON_CREATE_PDF
            );
            isPDForOCR = "PDF_MULTI";
            if (flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_SCANNED_PDF_ICON_CLICK.getValue()) && CommonMethods.isInterstitialCappingValid(InterstitialAdCappingEnum.SH_SCANNED_PDF_ICON_CLICK.getValue())) {
                int value = com.itl.commonres.utils.Constants.AdInterstitialCappingArrayList.get(InterstitialAdCappingEnum.SH_SCANNED_PDF_ICON_CLICK.getValue()) + 1;
                com.itl.commonres.utils.Constants.AdInterstitialCappingArrayList.set(InterstitialAdCappingEnum.SH_SCANNED_PDF_ICON_CLICK.getValue(), value);
                //show(this);
                // load interstitial Ad for PDF creation
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.AD_UNIT_ID_PDF_ICON_INTERSTITIAL_AD, this);

            } else {
                Log.e(TAG, "Mobibuz : Ad Not Showing");
                createPDF();
            }
            /*hideCheckBoxAndRemoveBottomBar();*/
        } else if (id == R.id.iv_menu) {
            showPopUpMoreMenu(v);
        } else if (id == R.id.fab_media || id == R.id.iv_media) {
            if (FlashScanUtil.isOsLessThanR()) {
                try {
                    Matisse.from(this)
                            .choose(MimeType.ofImage(), false)
                            .countable(true)
                            .showSingleMediaType(true)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            .thumbnailScale(0.9f)
                            .maxSelectable(1000)
                            .imageEngine(new GlideEngine())
                            .forResult(REQUEST_GET_IMAGES_USING_LIBRARY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, REQUEST_GET_IMAGES_USING_LIBRARY);
            }
        } else if (id == R.id.tv_save_as_pdf) {
            List<FileModel> selectedFileModelList = null;
            if (scanResultAdapter != null) {
                selectedFileModelList = scanResultAdapter.getSelectedFileModelList();
            }
            if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                if (Constants.IS_CREATE_PDF_DIRECT) {
                    if (!TextUtils.isEmpty(folderName)) {
                        setPdfFileNameForMultipleDocs(folderName);
                    } else {
                        setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));
                    }
                    //setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + folderName + "_" + getString(R.string.suffix_app_name));
                    handleMultipleDocPdfCreation(selectedFileModelList, PDF_BY_DIRECT);
                } else {
                    showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_BY_DIRECT);
                }
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
            }
        } else if (id == R.id.tv_share) {
            List<FileModel> fileModelList = null;
            if (scanResultAdapter != null) {
                fileModelList = scanResultAdapter.getSelectedFileModelList();
            }
            if (fileModelList != null && !fileModelList.isEmpty()) {
                showShareDialog();
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
            }
        } else if (id == R.id.tv_delete) {
            List<FileModel> fileModels = null;
            if (scanResultAdapter != null) {
                fileModels = scanResultAdapter.getSelectedFileModelList();
            }
            if (fileModels != null && !fileModels.isEmpty()) {
                showDeleteDialog(fileModels);
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
            }
        } else if (id == R.id.tv_copy) {
            if (totalFilesSize != 0 && totalFilesSize > 1) {
                if (scanResultAdapter != null && !scanResultAdapter.getSelectedFileModelList().isEmpty()) {
                    List<FileModel> fileModelList1 = scanResultAdapter.getSelectedFileModelList();
                    ArrayList<String> filePathList = new ArrayList<>();
                    String folderPath = null;
                    for (FileModel fileModel : fileModelList1) {
                        filePathList.add(fileModel.getPath());
                        if (TextUtils.isEmpty(folderPath)) {
                            folderPath = fileModel.getFolder();
                        }
                    }
                    if (!filePathList.isEmpty()) {
                        navigate(Constants.FileOperations.ACTION_COPY, filePathList, folderPath);
                    }
                    hideCheckBoxAndRemoveBottomBar();
                } else {
                    /*Toast.makeText(context, "" + getString(R.string.please_select_files), Toast.LENGTH_SHORT).show();*/
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
                }
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_add_more_documents));
            }
        } else if (id == R.id.tv_move) {
            if (totalFilesSize != 0 && totalFilesSize > 1) {
                if (scanResultAdapter != null && !scanResultAdapter.getSelectedFileModelList().isEmpty()) {
                    List<FileModel> modelList = scanResultAdapter.getSelectedFileModelList();
                    ArrayList<String> filePathList = new ArrayList<>();
                    String folderPath = null;
                    for (FileModel fileModel : modelList) {
                        filePathList.add(fileModel.getPath());
                        if (TextUtils.isEmpty(folderPath)) {
                            folderPath = fileModel.getFolder();
                        }
                    }
                    if (!filePathList.isEmpty()) {
                        navigate(Constants.FileOperations.ACTION_MOVE, filePathList, folderPath);
                    }
                    hideCheckBoxAndRemoveBottomBar();
                } else {
                    /*Toast.makeText(context, "" + getString(R.string.please_select_files), Toast.LENGTH_SHORT).show();*/
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
                }
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_add_more_documents));
            }
        } else if (id == R.id.btn_progress_lay) {
        } else if (id == R.id.tv_select_all_files) {
            switch (selectionAction) {
                case Constants.SELECT_ALL:
                    selectAllFiles();
                    break;
                case Constants.DESELECT_ALL:
                    deSelectAllFiles();
                    break;
            }
        } else if (id == R.id.tut_scan_result_lay) {
            tut_scan_result_lay.setVisibility(View.GONE);
        }
//            return null;
//        });
    }

    private void createPDF() {
        if (scanResultAdapter != null) {
            List<FileModel> selectedFileModelList = getTotalFetchedFiles();
            if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                if (Constants.IS_CREATE_PDF_DIRECT) {
                    if (!TextUtils.isEmpty(folderName)) {
                        setPdfFileNameForMultipleDocs(folderName);
                    } else {
                        setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));
                    }
                    handleMultipleDocPdfCreation(selectedFileModelList, PDF_WHOLE_DOCUMENT);
                } else {
                    showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_WHOLE_DOCUMENT);
                }
            } else {
                /*flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_file));*/
                showNoFilesInDocumentDialog();
            }
        }
    }

    private void showAskPdfNameDialogForMultiDoc(List<FileModel> selectedFileModelList, int pdfVia) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_ask_pdf_name);

        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);

        RadioButton rbOriginal = dialog.findViewById(R.id.rb_original);
        RadioButton rbCompressed = dialog.findViewById(R.id.rb_compressed);

        Spinner spinner = dialog.findViewById(R.id.spinner);
        List<PageSize> pageSizeList = flashScanUtil.getPageSizeList();

        if (pageSizeList != null && !pageSizeList.isEmpty()) {
            PageSizesAdapter pageSizesAdapter = new PageSizesAdapter(context, pageSizeList);
            spinner.setAdapter(pageSizesAdapter);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PageSize pageSize = (PageSize) parent.getItemAtPosition(position);
                if (pageSize != null) {
                    selectedPageSize = pageSize.getSizeValue();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button btn_done = dialog.findViewById(R.id.btn_done);
        switch (pdfVia) {
            case PDF_BY_DIRECT:
            case PDF_VIA_SHARE:
                et_pdf_name.setText(flashScanUtil.getFileDateFormatName() + "_" + folderName + "_" + getString(R.string.suffix_app_name));
                break;
            case PDF_WHOLE_DOCUMENT:
                if (!TextUtils.isEmpty(folderName)) {
                    et_pdf_name.setText(folderName + "_" + getString(R.string.suffix_app_name));
                } else {
                    et_pdf_name.setText(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));
                }
                break;
        }


        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_pdf_name.getText().toString().trim())) {
                    Toast.makeText(context, "" + getString(R.string.please_name_the_pdf), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(selectedPageSize)) {
                    Toast.makeText(context, "" + getString(R.string.please_select_page_size), Toast.LENGTH_SHORT).show();
                    return;

                }
                PdfSettings.getInstance().setSelectedPdfPageSize(selectedPageSize);
                setPdfFileNameForMultipleDocs(et_pdf_name.getText().toString().trim());
                if (rbOriginal.isChecked()) {
                    isMultiplePdfCreationWithCompression = false;
                } else if (rbCompressed.isChecked()) {
                    isMultiplePdfCreationWithCompression = true;
                }
                handleMultipleDocPdfCreation(selectedFileModelList, pdfVia);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void handleMultipleDocPdfCreation(List<FileModel> selectedFileModelList, int pdfVia) {
        switch (pdfVia) {
            case PDF_BY_DIRECT:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelListForWaterMark(selectedFileModelList);
                    goToWaterMarkActivityForMultipleFiles();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        saveAsPdfSelectedDocuments(selectedFileModelList, false);
                    } else {
                        saveAsPdfSelectedDocuments(selectedFileModelList, true);
                    }

                }
                hideCheckBoxAndRemoveBottomBar();
                break;
            case PDF_VIA_SHARE:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelListForWaterMark(selectedFileModelList);
                    goToWaterMarkActivityForShareMultipleFiles();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForSharingSelectedFiles(selectedFileModelList, false);
                    } else {
                        createPdfForSharingSelectedFiles(selectedFileModelList, true);
                    }
                }
                hideCheckBoxAndRemoveBottomBar();
                break;
            case PDF_WHOLE_DOCUMENT:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelListForWaterMark(selectedFileModelList);
                    goToWaterMarkActivityForCompleteDoc();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForAllFiles(selectedFileModelList, false);
                    } else {
                        createPdfForAllFiles(selectedFileModelList, true);
                    }
                }
                break;
        }

    }

    private void deSelectAllFiles() {
        if (scanResultAdapter != null) {
            scanResultAdapter.deSelectAllFiles(new OnDeselectAllFiles() {
                @Override
                public void onDeselect() {
                    tv_total_file_count.setText(scanResultAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
                }
            });
            manageSelectAllText();
        }
    }

    private void goToWaterMarkActivityForCompleteDoc() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_COMPLETE_DOC);
    }

    private void goToWaterMarkActivityForMultipleFiles() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_MULTIPLE_FILES);
    }

    private void createPdfForAllFiles(List<FileModel> selectedFileModelList, boolean isWaterMarkToBeShown) {
        /*Collections.reverse(selectedFileModelList);*/
        if (isMultiplePdfCreationWithCompression) {
            ArrayList<String> filesPathList = new ArrayList<>();
            for (FileModel fileModel : selectedFileModelList) {
                filesPathList.add(fileModel.getPath());
            }
            if (!filesPathList.isEmpty()) {
                new GetCompressedBitmapFilePathList(context, filesPathList, new CreateMultipleTempBitmapListener() {
                    @Override
                    public void onCompressBitmapStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCompressBitmapComplete(ArrayList<String> foldersList) {
                        progress_lay.setVisibility(View.GONE);
                        if (foldersList != null && !foldersList.isEmpty()) {
                            List<String> imagesUriList = new ArrayList<>();
                            for (String filePath : foldersList) {
                                imagesUriList.add(filePath);
                            }
                            if (!imagesUriList.isEmpty()) {
                                try {
                                    /*showRenameDialog(imagesUriList);*/
                                    // no need
                                    createPdf(imagesUriList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).execute();
            }
        } else {
            List<String> imagesUriList = new ArrayList<>();
            for (FileModel fileModel : selectedFileModelList) {
                if (fileModel.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                    continue;
                }
                imagesUriList.add(fileModel.getPath());
            }
            if (!imagesUriList.isEmpty()) {
                try {
                    /*showRenameDialog(imagesUriList);*/
                    createPdf(imagesUriList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // show warning dialog
                showNoFilesInDocumentDialog();
            }
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

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_sort_by) {
                showSortingDialog();
            } else if (itemId == R.id.menu_select_all) {/*selectAllFiles();*/
            }
            return true;
        });

        popupMenu.show();
    }

   /* private void fetchFilesByDescendingCreationTime() {
        if (!TextUtils.isEmpty(folderName))
            new GetFilesTask(context, folderName, this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                    Constants.SORT_BY.creationTimeDescending).execute();
    }*/

    private void selectAllFiles() {
        if (scanResultAdapter != null) {
            scanResultAdapter.selectAllFiles(new OnSelectAllFiles() {
                @Override
                public void onSelectedAllFiles() {
                    tv_total_file_count.setText(scanResultAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
                }
            });
            manageSelectAllText();
        }
    }

    /*private void fetchFilesByAscendingCreationTime() {
        if (!TextUtils.isEmpty(folderName))
            new GetFilesTask(context, folderName, this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                    Constants.SORT_BY.creationTimeAscending).execute();
    }*/

    private void manageSelectAllText() {
        if (scanResultAdapter != null && scanResultAdapter.getSelectedFileModelList().size() == getTotalFetchedFiles().size()) {
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

        int fileSortingOrder = prefManager.getFileSortingOrder();
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
                    fetchFilesByModificationTimeAscending();
                    prefManager.saveFileSortingOrder(Constants.SORT_BY.modificationTimeAscending);
                } else if (rbModificationTimeDescending.isChecked()) {
                    fetchFilesByModificationTimeDescending();
                    prefManager.saveFileSortingOrder(Constants.SORT_BY.modificationTimeDescending);
                } else if (rbNameAtoZ.isChecked()) {
                    fetchFilesBySortingAtoZ();
                    prefManager.saveFileSortingOrder(Constants.SORT_BY.nameAtoZ);
                } else if (rbNameZtoA.isChecked()) {
                    fetchFilesBySortingZtoA();
                    prefManager.saveFileSortingOrder(Constants.SORT_BY.nameZtoA);
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

    private void fetchFilesBySortingZtoA() {
        if (!TextUtils.isEmpty(folderName))
            new GetFilesTask(context, folderName, this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                    Constants.SORT_BY.nameZtoA).execute();
    }

    /*private void showRewardAdDialogForShareSelectedFiles(List<FileModel> finalSelectedFileModelList) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        createPdfForSharingSelectedFiles(finalSelectedFileModelList, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForSharingSelectedFiles(finalSelectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForSharingSelectedFiles(finalSelectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(ScanResultActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    private void fetchFilesBySortingAtoZ() {
        if (!TextUtils.isEmpty(folderName))
            new GetFilesTask(context, folderName, this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                    Constants.SORT_BY.nameAtoZ).execute();
    }

    private void fetchFilesByModificationTimeDescending() {
        if (!TextUtils.isEmpty(folderName))
            new GetFilesTask(context, folderName, this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                    Constants.SORT_BY.modificationTimeDescending).execute();
    }

    /*private void showRenameDialog(List<String> imagesUriList) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog);

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        et_pdf_name.setText(getString(R.string.prefix_document) + System.currentTimeMillis());

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                clearSelectedFiles();
            }
        });


        btn_ok.setOnClickListener(v -> {
            String pdfFileName = et_pdf_name.getText().toString().trim();
            if (TextUtils.isEmpty(pdfFileName)) {
                Toast.makeText(context, getString(R.string.please_name_file), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            createPdf(imagesUriList, pdfFileName);
            clearSelectedFiles();
        });

        dialog.show();
    }*/

    private void fetchFilesByModificationTimeAscending() {
        if (!TextUtils.isEmpty(folderName))
            new GetFilesTask(context, folderName, this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                    Constants.SORT_BY.modificationTimeAscending).execute();
    }

    private void navigate(int fileAction, ArrayList<String> filePathList, String folderPath) {
        Intent intent = new Intent(context, DocumentsListActivity.class);
        intent.putExtra(Constants.PutExtraConstants.FILE_OPERATION_ACTION, fileAction);
        intent.putStringArrayListExtra(Constants.PutExtraConstants.FILE_PATH_LIST, filePathList);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_PATH, folderPath);
        startActivity(intent);
    }

    private void showDeleteDialog(List<FileModel> finalSelectedFileModelList) {
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

        dialogTitle.setText(getString(R.string.delete));
        msgHeading.setText(getString(R.string.delete_msg));
        btn_cancel.setText(R.string.keep_it);
        btn_ok.setText(R.string.yes_btn_dialog);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                hideCheckBoxAndRemoveBottomBar();
            }
        });

        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
            List<String> filePathList = new ArrayList<>();
            for (FileModel fileModel : finalSelectedFileModelList) {
                if (fileModel != null) {
                    filePathList.add(fileModel.getPath());
                }
            }
            if (!filePathList.isEmpty()) {
                new DeleteFolderOrFileTask(context, filePathList, () -> {
                    fetchFiles();
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.delete_success_msg));
                }).execute();
            }
            hideCheckBoxAndRemoveBottomBar();
        });
        dialog.show();
    }

    private void showShareDialog() {

        List<FileModel> selectedFileModelList = null;
        if (scanResultAdapter != null) {
            selectedFileModelList = scanResultAdapter.getSelectedFileModelList();
        }
        if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
            Dialog dialog = new Dialog(context);
            if (dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.save_as_dailog);
            dialog.setCancelable(true);

            LinearLayout ll_preview_pdf = dialog.findViewById(R.id.ll_preview_pdf);
            if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {  // means user puchase product
                ll_preview_pdf.setVisibility(View.GONE);
            } else {
                ll_preview_pdf.setVisibility(View.VISIBLE);
            }

            LinearLayout ll_share_as_pdf = dialog.findViewById(R.id.ll_share_as_pdf);
            LinearLayout ll_share_as_image = dialog.findViewById(R.id.ll_share_as_image);

            TextView tv_preview = dialog.findViewById(R.id.tv_preview);
            TextView tv_pdf_watermark = dialog.findViewById(R.id.tv_pdf_watermark);
            tv_pdf_watermark.setText(getString(R.string.pdf_preview_txt, getString(R.string.app_name)));

            List<FileModel> finalSelectedFileModelList = selectedFileModelList;
            tv_preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (Constants.IS_CREATE_PDF_DIRECT) {
                        sharePdfDirectWithoutOpen = false;
                        setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + folderName + "_" + getString(R.string.suffix_app_name));
                        handleMultipleDocPdfCreation(finalSelectedFileModelList, PDF_VIA_SHARE);
                    } else {
                        showAskPdfNameDialogForMultiDoc(finalSelectedFileModelList, PDF_VIA_SHARE);
                    }
                }
            });


            ll_share_as_pdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (Constants.IS_CREATE_PDF_DIRECT) {
                        sharePdfDirectWithoutOpen = true;
                        setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + folderName + "_" + getString(R.string.suffix_app_name));
                        handleMultipleDocPdfCreation(finalSelectedFileModelList, PDF_VIA_SHARE);
                    } else {
                        showAskPdfNameDialogForMultiDoc(finalSelectedFileModelList, PDF_VIA_SHARE);
                    }
                    /*hideCheckBoxAndRemoveBottomBar();*/
                }
            });

            List<FileModel> finalSelectedFileModelList1 = selectedFileModelList;
            ll_share_as_image.setOnClickListener(v -> {
                dialog.dismiss();
                ArrayList<Uri> uriList = new ArrayList<>();
                for (FileModel fileModel : finalSelectedFileModelList1) {
                    if (fileModel != null) {
                        File fileOrDirectory = new File(fileModel.getPath());

                        if (fileOrDirectory.isDirectory()) {
                            File[] files = fileOrDirectory.listFiles();
                            if (files != null && files.length > 0) {
                                for (File file : files) {
                                    Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                                    if (uriForFile != null) uriList.add(uriForFile);
                                }
                            }
                        } else {
                            Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", fileOrDirectory);
                            if (uriForFile != null) uriList.add(uriForFile);
                        }
                    }
                }
                if (!uriList.isEmpty()) {
                    shareMultiple(uriList);
                } else {
                    showNoFileToShareDialog();
                }
                hideCheckBoxAndRemoveBottomBar();
            });
            dialog.show();
        } else {
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
        }

    }

    private void goToWaterMarkActivityForShareMultipleFiles() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_MULTIPLE_FILES);
    }

    private void createPdfForSharingSelectedFiles(List<FileModel> finalSelectedFileModelList, boolean isWaterMarkToBeShown) {
        if (isMultiplePdfCreationWithCompression) {
            ArrayList<String> filesPathList = new ArrayList<>();
            for (FileModel fileModel : finalSelectedFileModelList) {
                filesPathList.add(fileModel.getPath());
            }
            if (!filesPathList.isEmpty()) {
                new GetCompressedBitmapFilePathList(context, filesPathList, new CreateMultipleTempBitmapListener() {
                    @Override
                    public void onCompressBitmapStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCompressBitmapComplete(ArrayList<String> foldersList) {
                        progress_lay.setVisibility(View.GONE);
                        if (foldersList != null && !foldersList.isEmpty()) {
                            List<String> filePathList = new ArrayList<>();
                            for (String filePath : foldersList) {
                                File fileOrDirectory = new File(filePath);
                                if (fileOrDirectory.isDirectory()) {
                                    File[] files = fileOrDirectory.listFiles();
                                    if (files != null && files.length > 0) {
                                        for (File file : files) {
                                            filePathList.add(file.getPath());
                                        }
                                    }
                                } else {
                                    filePathList.add(fileOrDirectory.getPath());
                                }
                            }
                            if (!filePathList.isEmpty()) {
                                isPdfCreatedForSharing = true;
                                // no need
                                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
                            }
                        }
                    }
                }).execute();
            }
        } else {
            List<String> filePathList = new ArrayList<>();
            for (FileModel fileModel : finalSelectedFileModelList) {
                if (fileModel != null) {
                    File fileOrDirectory = new File(fileModel.getPath());
                    if (fileOrDirectory.isDirectory()) {
                        File[] files = fileOrDirectory.listFiles();
                        if (files != null && files.length > 0) {
                            for (File file : files) {
                                if (file.isFile() && file.exists()) {
                                    if (file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                        continue;
                                    }
                                    filePathList.add(file.getPath());
                                }

                            }
                        }
                    } else {
                        if (fileOrDirectory.isFile() && fileOrDirectory.exists()) {
                            if (!fileOrDirectory.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                filePathList.add(fileOrDirectory.getPath());
                            }

                        }

                    }
                }
            }
            if (!filePathList.isEmpty()) {
                isPdfCreatedForSharing = true;
                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
            } else {
                // show warning dialog
                showNoFilesInDocumentDialog();
            }
        }

    }

    private void saveAsPdfSelectedDocuments(List<FileModel> selectedFileModelList, boolean isWaterMarkToBeShown) {
        if (isMultiplePdfCreationWithCompression) {
            ArrayList<String> filesPathList = new ArrayList<>();
            for (FileModel fileModel : selectedFileModelList) {
                filesPathList.add(fileModel.getPath());
            }
            if (!filesPathList.isEmpty()) {
                new GetCompressedBitmapFilePathList(context, filesPathList, new CreateMultipleTempBitmapListener() {
                    @Override
                    public void onCompressBitmapStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCompressBitmapComplete(ArrayList<String> foldersList) {
                        progress_lay.setVisibility(View.GONE);
                        if (foldersList != null && !foldersList.isEmpty()) {
                            List<String> filePathList = new ArrayList<>();
                            for (String filePath : foldersList) {
                                File fileOrDirectory = new File(filePath);
                                if (fileOrDirectory.isDirectory()) {
                                    File[] files = fileOrDirectory.listFiles();
                                    if (files != null && files.length > 0) {
                                        for (File file : files) {
                                            filePathList.add(file.getPath());
                                        }
                                    }
                                } else {
                                    filePathList.add(fileOrDirectory.getPath());
                                }
                            }
                            if (!filePathList.isEmpty()) {
                                isPdfCreatedForSharing = false;
                                // no need
                                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
                            }
                        }
                    }
                }).execute();
            }
        } else {
            List<String> filePathList = new ArrayList<>();
            for (FileModel fileModel : selectedFileModelList) {
                if (fileModel != null) {
                    File fileOrDirectory = new File(fileModel.getPath());
                    if (fileOrDirectory.isDirectory()) {
                        File[] files = fileOrDirectory.listFiles();
                        if (files != null && files.length > 0) {
                            for (File file : files) {
                                if (file.isFile() && file.exists()) {
                                    if (file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                        continue;
                                    }
                                    filePathList.add(file.getPath());
                                }

                            }
                        }
                    } else {
                        if (fileOrDirectory.isFile() && fileOrDirectory.exists()) {
                            if (!fileOrDirectory.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                filePathList.add(fileOrDirectory.getPath());
                            }
                        }

                    }
                }
            }
            if (!filePathList.isEmpty()) {
                isPdfCreatedForSharing = false;
                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
            } else {
                showNoFilesInDocumentDialog();
            }
        }

    }

    private void clearSelectedFiles() {
        if (scanResultAdapter != null && scanResultAdapter.isVisibleAllCheckBox()) {
            scanResultAdapter.hideAllCheckBoxes();
        }
    }

    private void createPdf(List<String> imagesUriList, String pdfFileName, boolean isWaterMarkToBeShown) {
        ImageToPdfOptions imageToPdfOptions = new ImageToPdfOptions();
        imageToPdfOptions.setPageSize(Constants.PdfConstants.DEFAULT_PDF_PAGE_SIZE);
        imageToPdfOptions.setPageColor(Constants.PdfConstants.DEFAULT_PDF_PAGE_COLOR);
        imageToPdfOptions.setMargins(0, 0, 0, 0);
        imageToPdfOptions.setPdfQuality(Constants.PdfConstants.DEFAULT_PDF_QUALITY);
        imageToPdfOptions.setBorderWidth(Constants.PdfConstants.DEFAULT_BORDER_WIDTH);
        imageToPdfOptions.setWaterMarkAdded(isWaterMarkToBeShown);
        imageToPdfOptions.setWaterMark(flashScanUtil.getWaterMark());
        new CreatePdfTask(context, pdfFileName, imageToPdfOptions, imagesUriList, this, true).execute();
    }

    private void showNoFilesInDocumentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.no_files_in_document_warning_txt)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (scanResultAdapter != null && scanResultAdapter.isVisibleAllCheckBox()) {
            hideCheckBoxAndRemoveBottomBar();
        } else {
            goToHome();
        }

    }

    private void hideCheckBoxAndRemoveBottomBar() {
        scanResultAdapter.hideAllCheckBoxes();
        ll_bottom_bar.setVisibility(View.GONE);
        iv_menu.setVisibility(View.VISIBLE);
        ll_select_all_files.setVisibility(View.GONE);
        ll_floating.setVisibility(View.VISIBLE);
        iv_pdf.setVisibility(View.VISIBLE);
    }

    private void goToHome() {
        Intent intent;
        switch (screenFrom) {
            case ScanConstants.ScreenConstants.FROM_HOME_SCREEN:
                if (Constants.ALWAYS_RELOAD_AD_ON_HOME_SCREEN) {
                    intent = new Intent(context, HomeActivity.class);
                    startActivity(intent);
                }
                finish();
                break;
            case ScanConstants.ScreenConstants.FROM_MAIN_SCREEN:
                if (Constants.ALWAYS_RELOAD_AD_ON_MAIN_SCREEN) {
                    intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }
                finish();
                break;
            case ScanConstants.ScreenConstants.FROM_SCAN_RESULT:
            case ScanConstants.ScreenConstants.FROM_SELECTED_IMAGES_LIST_SCREEN:
                intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case ScanConstants.ScreenConstants.FROM_FAVORITES_SCREEN:
                finish();
                break;
            case ScanConstants.ScreenConstants.FROM_PDF_TO_IMAGES_IMPORT:
                intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                finish();
                break;
        }
    }

    @Override
    public void onFetchingComplete(List<FileModel> fileModelList) {
        progress_lay.setVisibility(View.GONE);
        if (fileModelList != null) {
            if (!getTotalFetchedFiles().isEmpty()) {
                getTotalFetchedFiles().clear();
            }

          /*  for(int i =0; i<fileModelList.size();i++){
                long fileSize = fileModelList.get(i).getSize();
                if(fileSize>0){
                    getTotalFetchedFiles().add(fileModelList.get(i));
                }
                else{
                    fileModelList.remove(fileModelList.get(i));
                }
            }*/
            getTotalFetchedFiles().addAll(fileModelList);
        }

        if (fileModelList != null && !fileModelList.isEmpty()) {
            totalFilesSize = fileModelList.size();
            final ArrayList<FileModel> tempArr = new ArrayList<>(fileModelList);

            AsyncTask.execute(() -> {
                File dstOriginalFolderName = new File(flashScanUtil.getDocOriginalPath(context), folderName);

                if (!dstOriginalFolderName.exists()) {
                    dstOriginalFolderName.mkdirs();

                    for (int i = 0; i < tempArr.size(); i++) {

                        File path = new File(tempArr.get(i).getPath());
                        File dstPath = new File(dstOriginalFolderName, tempArr.get(i).getName());
                        try {
                            CopyFileTask.copy(ScanResultActivity.this, false, path, dstPath);
                        } finally {
                            path = null;
                            dstPath = null;
                        }
                    }
                }
                runOnUiThread(() -> progress_lay.setVisibility(View.GONE));
            });

            ll_file_count.setVisibility(View.VISIBLE);
            if (fileModelList.size() > 1) {
                tv_file_count.setText(getString(R.string.total) + " " + fileModelList.size() + " " + getString(R.string.files));
            } else {
                tv_file_count.setText(getString(R.string.total) + " " + fileModelList.size() + " " + getString(R.string.file));
            }
            // add AD view here
            if (flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_SCANNED_IMAGE_LIST.getValue()) && fileModelList.size() > 0) {
                fileModelList.add(1, new FileModel("AD", "", ""));
            }
            // add last empty view here
            fileModelList.add(new FileModel(getString(R.string.tap_camera_icon), "", ""));
            showRecyclerView(fileModelList);
        } else {
            ll_file_count.setVisibility(View.GONE);
            if (fileModelList == null) {
                fileModelList = new ArrayList<>();
            }
            // add AD view here
            if (flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_SCANNED_IMAGE_LIST.getValue()) && fileModelList.size() > 0) {
                fileModelList.add(1, new FileModel("AD", "", ""));
            }
            // add last empty view here
            fileModelList.add(new FileModel(getString(R.string.tap_camera_icon), "", ""));
            showRecyclerView(fileModelList);
        }
    }

    @Override
    public void onFetchingStart() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    private void hideRecyclerView() {
        rv_scan_result.setVisibility(View.GONE);
        ll_no_document.setVisibility(View.VISIBLE);
    }

    private void showRecyclerView(List<FileModel> fileModelList) {
        ll_no_document.setVisibility(View.GONE);
        rv_scan_result.setVisibility(View.VISIBLE);
        Log.e(TAG, "fileModelList 1" + new Gson().toJson(fileModelList));
        scanResultAdapter = new ScanResultAdapter(context, prefManager, fileModelList, this, this, folderName, isSaveOnGoogleDrive, googleDriveFolderId);
        rv_scan_result.setAdapter(scanResultAdapter);
        ((SimpleItemAnimator) rv_scan_result.getItemAnimator()).setSupportsChangeAnimations(false);

    }

    @Override
    public void onItemSelect(Object o) {
        if (scanResultAdapter != null && scanResultAdapter.isVisibleAllCheckBox()) {
            tv_total_file_count.setText(scanResultAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
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
            if (!TextUtils.isEmpty(fileModel.getName()) &&
                    fileModel.getName().equalsIgnoreCase(context.getString(R.string.tap_camera_icon))
                    && TextUtils.isEmpty(fileModel.getPath()) && TextUtils.isEmpty(fileModel.getFileExtension())) {
                cameraIntent();
            }
        }

    }

    @Override
    public void onItemLongPress(Object o) {
        if (scanResultAdapter != null && scanResultAdapter.isVisibleAllCheckBox()) {
            ll_bottom_bar.setVisibility(View.VISIBLE);
            iv_menu.setVisibility(View.GONE);
            ll_floating.setVisibility(View.GONE);
            iv_pdf.setVisibility(View.GONE);
            ll_select_all_files.setVisibility(View.VISIBLE);
            tv_total_file_count.setText(scanResultAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
            manageSelectAllText();
        }
    }

    @Override
    public void onItemAction(Object o, View view) {
        fileModel = null;
        if (o == null && view == null)
            return;
        if (o instanceof FileModel) {
            fileModel = (FileModel) o;
        }
        if (fileModel != null) {
            showPopUpMenu(fileModel, view);
        }
    }

    private void showPopUpMenu(FileModel fileModel, View v) {
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
        popupMenu.getMenuInflater().inflate(R.menu.more_popup_menu, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.menu_copy).setVisible(true);
        //TODO ENABLE SHARE MENU
        popupMenu.getMenu().findItem(R.id.menu_share).setVisible(false);
        popupMenu.getMenu().findItem(R.id.menu_move).setVisible(true);
        popupMenu.getMenu().findItem(R.id.menu_ocr).setVisible(false);
        popupMenu.getMenu().findItem(R.id.menu_modify_scan).setVisible(false);
        if (Constants.PremiumFeatures.IS_ADD_SIGNATURE_ENABLED) {
            popupMenu.getMenu().findItem(R.id.menu_add_signature).setVisible(true);
        } else {
            popupMenu.getMenu().findItem(R.id.menu_add_signature).setVisible(false);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_save_as_pdf) {
                isPDForOCR = "PDF";
                if (scanResultAdapter.getFileModelListSize() > 0) {
                    if (scanResultAdapter.getFileModelListSize() == 2) {
                        createPDF();
                    } else {
                        if (fileModel != null) {

                            if (Constants.IS_CREATE_PDF_DIRECT) {
                                fileModel.setPdfFileName(flashScanUtil.removeExtensionFromFileName(fileModel.getName()));
                                handlePdfCreation(PDF_BY_DIRECT, fileModel);
                            } else {
                                showAskPdfNameDialog(fileModel, PDF_BY_DIRECT);
                            }

                        }
                    }
                }

            } else if (itemId == R.id.menu_save_to_gallery) {
            } else if (itemId == R.id.menu_share) {/*Toast.makeText(context, "share called", Toast.LENGTH_SHORT).show();*/
                showShareDialog(fileModel);
            } else if (itemId == R.id.menu_rename) {/*Toast.makeText(context, "rename called", Toast.LENGTH_SHORT).show();*/
                showCommonDialog(fileModel, Constants.FileOperations.ACTION_RENAME);
            } else if (itemId == R.id.menu_delete) {/*Toast.makeText(context, "delete called", Toast.LENGTH_SHORT).show();*/
                showCommonDialog(fileModel, Constants.FileOperations.ACTION_DELETE);
            } else if (itemId == R.id.menu_copy) {/*Toast.makeText(context, "copy called", Toast.LENGTH_SHORT).show();*/
                if (totalFilesSize != 0 && totalFilesSize > 1) {
                    navigateToDocumentListActivity(Constants.FileOperations.ACTION_COPY, fileModel.getPath(), fileModel.getFolder());
                } else {
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_add_more_documents));
                }
            } else if (itemId == R.id.menu_move) {/*Toast.makeText(context, "move called", Toast.LENGTH_SHORT).show();*/
                if (totalFilesSize != 0 && totalFilesSize > 1) {
                    navigateToDocumentListActivity(Constants.FileOperations.ACTION_MOVE, fileModel.getPath(), fileModel.getFolder());
                } else {
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_add_more_documents));
                }
            } else if (itemId == R.id.menu_modify_scan) {/*Toast.makeText(context, "Modify Scan", Toast.LENGTH_SHORT).show();*/
                modifyScan(fileModel);
            } else if (itemId == R.id.menu_add_signature) {
                if (prefManager.isPremium()) {
                    goToSignatureActivity(fileModel.getPath(), fileModel.getName());
                } else {
                    if (flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow) {
                        Constants.isAppInBackground = false;
                        isPDForOCR = getString(R.string.add_signature);
                        loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
                    } else {
                        Log.e(TAG, "Mobibuz : Ad Not Showing");
                        goToSignatureActivity(fileModel.getPath(), fileModel.getName());
                    }
                }

            } else if (itemId == R.id.menu_ocr) {
                isPDForOCR = "OCR";
                // progress_lay.setVisibility(View.VISIBLE);
                FlashScanUtil.newShowLoading(context, "");
                /*if (flashScanUtil.isConnectingToInternet()) {
                    connectBillingService(fileModel);
                } else {
                    if (checkOcrCount())*/
                ocrScan(fileModel);
//                }

                    /*int allowedFreeOcr = dbHandler.getAllowedFreeOcr();
                    Log.i(TAG, "allowedFreeOcr: " + allowedFreeOcr);

                    int existingOcrFreeAttempted = dbHandler.getOcrFreeAttempted();
                    Log.i(TAG, "existingOcrFreeAttempted: " + existingOcrFreeAttempted);

                    if(allowedFreeOcr == existingOcrFreeAttempted){
                        askToChoosePlan();
                    }
                    else{
                        ocrScan(fileModel);
                    }*/
            }
            return true;
        });
        popupMenu.show();
    }

    private void connectBillingService(FileModel fileModel) {
        if (flashScanUtil.isConnectingToInternet()) {
           /* billingClient = BillingClient.newBuilder(this)
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
                                ocrScan(fileModel);
                            } else {
                                int allowedFreeOcr = dbHandler.getAllowedFreeOcr();
                                Log.i(TAG, "allowedFreeOcr: " + allowedFreeOcr);

                                int existingOcrFreeAttempted = dbHandler.getOcrFreeAttempted();
                                Log.i(TAG, "existingOcrFreeAttempted: " + existingOcrFreeAttempted);

                                if (allowedFreeOcr == existingOcrFreeAttempted) {
                                    askToChoosePlan();
                                } else {
                                    if (checkOcrCount())
                                        ocrScan(fileModel);
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

    private void ocrScan(FileModel fileModel) {

        if (fileModel != null) {
            // get Image Uri from document file and make a new file in OCR
            Uri imageUri = Uri.fromFile(new File(fileModel.getPath()));
            if (imageUri != null) {
                //progress_lay.setVisibility(View.VISIBLE);
                // FlashScanUtil.newShowLoading(context,"");
                CropImage.activity(imageUri).start(this);
            }
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

    /*private void showRewardAdDialogForSingleFile(FileModel fileModel) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        createPdfForSingleFile(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForSingleFile(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForSingleFile(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(ScanResultActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    private void openAskEmailActivity(int whichPlanActivity) {
        Intent intent = new Intent(ScanResultActivity.this, AskEmailActivity.class);
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity);
        startActivity(intent);
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(ScanResultActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    private void showAskPdfNameDialog(FileModel fileModel, int pdfVia) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_ask_pdf_name);

        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        et_pdf_name.setText(flashScanUtil.removeExtensionFromFileName(fileModel.getName()));

        RadioButton rbOriginal = dialog.findViewById(R.id.rb_original);
        RadioButton rbCompressed = dialog.findViewById(R.id.rb_compressed);

        Spinner spinner = dialog.findViewById(R.id.spinner);
        List<PageSize> pageSizeList = flashScanUtil.getPageSizeList();

        if (pageSizeList != null && !pageSizeList.isEmpty()) {
            PageSizesAdapter pageSizesAdapter = new PageSizesAdapter(context, pageSizeList);
            spinner.setAdapter(pageSizesAdapter);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PageSize pageSize = (PageSize) parent.getItemAtPosition(position);
                if (pageSize != null) {
                    selectedPageSize = pageSize.getSizeValue();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_pdf_name.getText().toString().trim())) {
                    Toast.makeText(context, "" + getString(R.string.please_name_the_pdf), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(selectedPageSize)) {
                    Toast.makeText(context, "" + getString(R.string.please_select_page_size), Toast.LENGTH_SHORT).show();
                    return;

                }
                PdfSettings.getInstance().setSelectedPdfPageSize(selectedPageSize);
                fileModel.setPdfFileName(et_pdf_name.getText().toString().trim());
                if (rbOriginal.isChecked()) {
                    fileModel.setCompressedPdf(false);
                } else if (rbCompressed.isChecked()) {
                    fileModel.setCompressedPdf(true);
                }
                handlePdfCreation(pdfVia, fileModel);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void handlePdfCreation(int pdfVia, FileModel fileModel) {
        switch (pdfVia) {
            case PDF_BY_DIRECT:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelForWaterMark(fileModel);
                    goToWaterMarkActivityForSingleFile();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForSingleFile(fileModel, false);
                    } else {
                        createPdfForSingleFile(fileModel, true);
                    }

                }
                break;
            case PDF_VIA_SHARE:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelForWaterMark(fileModel);
                    goToWaterMarkActivityForShareSingleFile();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForShareSingleFile(fileModel, false);
                    } else {
                        createPdfForShareSingleFile(fileModel, true);
                    }

                }
                break;
        }
    }

    private void goToSignatureActivity(String imagePath, String fileName) {
        Intent intent = new Intent(context, SignatureActivity.class);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_NAME, folderName);
        intent.putExtra(Constants.PutExtraConstants.FILE_PATH, imagePath);
        intent.putExtra(Constants.PutExtraConstants.FILE_NAME, fileName);
        intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, screenFrom);
        startActivity(intent);
    }

    /*private void showRewardAdDialogForShareFile(FileModel fileModel) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        createPdfForShareSingleFile(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForShareSingleFile(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForShareSingleFile(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(ScanResultActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    private void goToWaterMarkActivityForSingleFile() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SINGLE_FILE);
    }

    private void createPdfForSingleFile(FileModel fileModel, boolean isWaterMarkToBeShown) {
        isPdfCreatedForSharing = false;

        if (!prefManager.isAppAdFree() && flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow) {
            //show(this);
            // load interstitial Ad for PDF creation
            watermarkToBeShownOrNot = isWaterMarkToBeShown;
            Constants.isAppInBackground = false;
            loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);

        } else {
            Log.e(TAG, "Mobibuz : Ad Not Showing");
            createPdf(fileModel, isWaterMarkToBeShown);
        }

    }

    private void modifyScan(FileModel fileModel) {

    }

    private void navigateToDocumentListActivity(int fileAction, String filePath, String folderPath) {
        Intent intent = new Intent(context, DocumentsListActivity.class);
        intent.putExtra(Constants.PutExtraConstants.FILE_OPERATION_ACTION, fileAction);
        intent.putExtra(Constants.PutExtraConstants.FILE_PATH, filePath);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_PATH, folderPath);
        startActivity(intent);
    }

    private void showShareDialog(FileModel fileModel) {

        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.save_as_dailog);
        dialog.setCancelable(true);


        LinearLayout ll_preview_pdf = dialog.findViewById(R.id.ll_preview_pdf);
        if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {  // means user puchase product
            ll_preview_pdf.setVisibility(View.GONE);
        } else {
            ll_preview_pdf.setVisibility(View.VISIBLE);
        }

        LinearLayout ll_share_as_pdf = dialog.findViewById(R.id.ll_share_as_pdf);
        LinearLayout ll_share_as_image = dialog.findViewById(R.id.ll_share_as_image);

        TextView tv_preview = dialog.findViewById(R.id.tv_preview);
        TextView tv_pdf_watermark = dialog.findViewById(R.id.tv_pdf_watermark);
        tv_pdf_watermark.setText(getString(R.string.pdf_preview_txt, getString(R.string.app_name)));

        tv_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Constants.IS_CREATE_PDF_DIRECT) {
                    sharePdfDirectWithoutOpen = false;
                    fileModel.setPdfFileName(flashScanUtil.removeExtensionFromFileName(fileModel.getName()));
                    handlePdfCreation(PDF_VIA_SHARE, fileModel);
                } else {
                    showAskPdfNameDialog(fileModel, PDF_VIA_SHARE);
                }
            }
        });
        ll_share_as_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Constants.IS_CREATE_PDF_DIRECT) {
                    sharePdfDirectWithoutOpen = true;
                    fileModel.setPdfFileName(flashScanUtil.removeExtensionFromFileName(fileModel.getName()));
                    handlePdfCreation(PDF_VIA_SHARE, fileModel);
                } else {
                    showAskPdfNameDialog(fileModel, PDF_VIA_SHARE);
                }


            }
        });

        ll_share_as_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                File file = new File(fileModel.getPath());
                ArrayList<Uri> uriList = new ArrayList<>();
                if (file.isFile()) {
                    Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                    if (uriForFile != null) uriList.add(uriForFile);
                    if (!uriList.isEmpty()) {
                        shareMultiple(uriList);
                    } else {
                        showNoFileToShareDialog();
                    }
                }
            }
        });
        dialog.show();
    }

    private void showNoFileToShareDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.no_files_in_document_to_share_warning)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void goToWaterMarkActivityForShareSingleFile() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_SINGLE_FILE);
    }

    private void createPdfForShareSingleFile(FileModel fileModel, boolean isWaterMarkToBeShown) {
        isPdfCreatedForSharing = true;
        createPdf(fileModel, isWaterMarkToBeShown);
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
                et_pdf_name.setText(flashScanUtil.removeExtensionFromFileName(fileModel.getName()));
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
                dialog.dismiss();
                /*clearSelectedFiles();*/
            }
        });


        btn_ok.setOnClickListener(v -> {
            switch (action) {
                case Constants.FileOperations.ACTION_RENAME:
                    fileName = et_pdf_name.getText().toString().trim();
                    if (TextUtils.isEmpty(fileName)) {
                        Toast.makeText(context, getString(R.string.please_name_file), Toast.LENGTH_SHORT).show();
                        return;
                    } else if (fileName.equalsIgnoreCase(flashScanUtil.removeExtensionFromFileName(fileModel.getName()))) {
                        Toast.makeText(context, getString(R.string.file_name_same_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }

                   /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                        List<Uri> uris = new ArrayList<>();
                        File tempFile = new File(fileModel.getPath());

                        ///////////////////////////////////////////////////////////////////
                        Uri  Uri_one = getImageContentUri(context, tempFile);
                        Log.e(TAG, "Uri_one"+Uri_one);
                        uris.add(Uri_one);
                        //rename(Uri_one, fileName);
                        requestWritePermission(uris);
                       *//* Log.e(TAG, "tempFile" + tempFile);
                        long mediaID = getFilePathToMediaID(tempFile.getAbsolutePath(), context);
                        Log.e(TAG, "Write mediaID.." + mediaID);
                        Uri  Uri_one = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), mediaID);
                        //uris.clear();
                        uris.add(Uri_one);



                        requestWritePermission(uris);*//*
                    }
                    else {
                        renameFile(fileName, fileModel);
                    }*/
                    renameFile(fileName, fileModel);
                    dialog.dismiss();
                    break;
                case Constants.FileOperations.ACTION_DELETE:
                    /*     *//*File dir = new File(fileModel.getPath());*//*
                 *//*deleteRecursive(dir);*//*
                    Log.e(TAG, "fileModel.getPath.." + fileModel.getPath());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        List<Uri> uriPathList = new ArrayList<>();
                        if (fileModel != null) {
                            //deleteFileUsingDisplayName(context, fileModel.getName());
                            File tempFile = new File(fileModel.getPath());

                            Uri  Uri_one = getImageContentUri(context, tempFile);
                            Log.e(TAG, "Uri_one"+Uri_one);
                            uriPathList.add(Uri_one);

                            *//*long mediaID = getFilePathToMediaID(tempFile.getAbsolutePath(), context);
                            //long mediaID=getFilePathToMediaID(Uri.parse(fileModel.getPath()));
                            Log.e(TAG, "mediaID.." + mediaID);
                            fileUri = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), mediaID);
                            Log.e(TAG, "Uri.." + fileUri);
                            uriPathList.clear();
                            uriPathList.add(fileUri);*//*
                 *//* try {
                                DocumentsContract.deleteDocument(getApplicationContext().getContentResolver(),fileUri);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }*//*
                            requestDeletePermission(uriPathList);
                        }
                    } else {
                        new DeleteFolderOrFileTask(context, fileModel.getPath(), this).execute();
                    }*/
                    new DeleteFolderOrFileTask(context, fileModel.getPath(), this).execute();
                    dialog.dismiss();
                    scanResultAdapter.notifyDataSetChanged();
                    if (((RadioButton) dialog.findViewById(R.id.rd_delete_from_both)).isChecked()) {
                        //deleteFromGoogleDrive(fileModel.getName());
                        deleteFromGoogleDriveById(fileModel);
                    }
                    break;
            }

        });

        dialog.show();
    }

    public long getFilePathToMediaID(String songPath, Context context) {
        long id = 0;
        // ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Images.Media.getContentUri("external");
        Log.e(TAG, "uri" + uri);
       /* String selection = MediaStore.Images.Media.DATA;
        Log.e(TAG, "selection" + selection);
        String[] selectionArgs = {songPath};
        Log.e(TAG, "selectionArgs" + selectionArgs);
        String[] projection = {MediaStore.Images.Media._ID};
        Log.e(TAG, "projection" + projection);
        String sortOrder = MediaStore.Images.Media.TITLE + " ASC";

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                id = Long.parseLong(cursor.getString(idIndex));
            }
            cursor.close();
        }

        return id;*/


       /* String selection = null;
        String[] selectionArgs = null;
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Long.parseLong(Environment.getExternalStorageDirectory() + "/" + split[1]);
            } else if (isDownloadsDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return Long.parseLong(cursor.getString(column_index));
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return Long.parseLong(uri.getPath());
        }
        return id;*/

        //String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String docId = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{docId}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            id = Long.parseLong(cursor.getString(columnIndex));
        }
        cursor.close();
        return id;

    }

    private void renameFile(String newFileName, FileModel fileModel) {

        File oldFile = new File(fileModel.getFolder(), fileModel.getName());
        File newFile = new File(fileModel.getFolder(), newFileName + "." + flashScanUtil.getExtensionFromFileName(fileModel.getName()));
        if (newFile.exists()) {
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.same_file_already_exist));
            return;
        }

       /* if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            isRenamed = renameFileName(newFileName,oldFile.getName());
        }
        else {*/
        isRenamed = oldFile.renameTo(newFile);
        // }

        if (isRenamed) {

            AppController.getINSTANCE().dbHandler.updateFileName(fileModel.getName(), newFile.getName());

            File dstOriginalFolderName = new File(flashScanUtil.getDocOriginalPath(context), folderName);
            dstOriginalFolderName = new File(dstOriginalFolderName, fileModel.getName());

            File tempOriginal = new File(flashScanUtil.getDocOriginalPath(context), folderName);
            tempOriginal = new File(tempOriginal, newFile.getName());
            dstOriginalFolderName.renameTo(tempOriginal);

            fetchFiles();
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.rename_success_msg));
            Log.e(TAG, "renameFile: " + fileModel.isSavedOnGoogleDrive());
            if (fileModel.isSavedOnGoogleDrive()) {
                ArrayList<String> pathList = new ArrayList<>();
                pathList.add(tempOriginal.getAbsolutePath());
                uploadFilesToDrive(pathList, context.getResources().getString(R.string.updating_files_to_google_drive), true, fileModel.getGoogleDriveFolderId());
                Log.e(TAG, "here");
            }
        } else {
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.same_file_already_exist));
        }
        fetchFiles();
        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.rename_success_msg));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    boolean renameFileName(String newName, String displayName) {

        try {
            Long id = getIdFromDisplayName(displayName);
            Log.e(TAG, "My_id" + id);
            ContentResolver contentResolver = context.getContentResolver();
            Uri mUri = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), id);
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
            contentResolver.update(mUri, contentValues, null, null);

            contentValues.clear();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, newName);
            // contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "files/pdf");
            // contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativeLocation);
            // contentValues.put(MediaStore.Files.FileColumns.TITLE, "SomeName");
            // contentValues.put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
            // contentValues.put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
            contentResolver.update(mUri, contentValues, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    Long getIdFromDisplayName(String displayName) {
        String[] projection;
        projection = new String[]{MediaStore.Images.Media._ID};

        // TODO This will break if we have no matching item in the MediaStore.
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), projection,
                MediaStore.Images.Media.DISPLAY_NAME + " LIKE ?", new String[]{displayName}, null);
        assert cursor != null;
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            int columnIndex = cursor.getColumnIndex(projection[0]);
            long fileId = cursor.getLong(columnIndex);

            cursor.close();
            return fileId;
        }
        return null;
    }

    private void fetchFiles() {
        if (!TextUtils.isEmpty(folderName))
            new GetFilesTask(context, folderName, this, Constants.RECENT_DOCS_COUNT_LIMITLESS, Constants.SORT_BY.nameAtoZ).execute();
    }

    private List<String> getSelectedImagesList() {
        if (selectedImagesList == null) {
            selectedImagesList = new ArrayList<>();
        }
        return selectedImagesList;
    }

    private void createPdf(FileModel fileModel, boolean isWaterMarkToBeShown) {
        if (fileModel.isCompressedPdf()) {
            new GetCompressedBitmapFilePath(context, fileModel.getPath(), new CreateTempBitmapListener() {
                @Override
                public void onCompressingStart() {
                    progress_lay.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCompressingComplete(File compressedFile) {
                    progress_lay.setVisibility(View.GONE);
                    if (compressedFile != null) {
                        createPdfFromFilePath(fileModel, isWaterMarkToBeShown, compressedFile.getPath());


                    }
                }
            }).execute();
        } else {
            createPdfFromFilePath(fileModel, isWaterMarkToBeShown, fileModel.getPath());

        }

    }

    private void createPdfFromFilePath(FileModel fileModel, boolean isWaterMarkToBeShown, String path) {
        String fileName = fileModel.getPdfFileName();
        ImageToPdfOptions imageToPdfOptions = new ImageToPdfOptions();
        imageToPdfOptions.setPageSize(Constants.PdfConstants.DEFAULT_PDF_PAGE_SIZE);
        imageToPdfOptions.setPageColor(Constants.PdfConstants.DEFAULT_PDF_PAGE_COLOR);
        imageToPdfOptions.setMargins(0, 0, 0, 0);
        imageToPdfOptions.setPdfQuality(Constants.PdfConstants.DEFAULT_PDF_QUALITY);
        imageToPdfOptions.setBorderWidth(Constants.PdfConstants.DEFAULT_BORDER_WIDTH);
        imageToPdfOptions.setWaterMarkAdded(isWaterMarkToBeShown);
        imageToPdfOptions.setWaterMark(flashScanUtil.getWaterMark());
        if (!getSelectedImagesList().isEmpty()) {
            getSelectedImagesList().clear();
        }
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            if (!file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                getSelectedImagesList().add(file.getPath());
            }
        }
        if (!getSelectedImagesList().isEmpty()) {
            new CreatePdfTask(context, fileName, imageToPdfOptions, getSelectedImagesList(), this, true).execute();
        } else {
            // showWarning Dialog
            showNoFilesInDocumentDialog();
        }

    }

    @Override
    public void onPdfCreationStarted() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPdfCreated(String savedPdfPath) {
        progress_lay.setVisibility(View.GONE);
        Log.i(TAG, "PDF created successfully.");
        if (Constants.IS_SHOWING_CREATED_PDF_IN_OWN_APP) {
            if (!sharePdfDirectWithoutOpen) {
                Intent intent = new Intent(context, PdfEditorActivity.class);
                intent.putExtra(Constants.PutExtraConstants.SAVED_PDF_PATH, savedPdfPath);
                startActivity(intent);
                finish();
            } else {
                File file = new File(savedPdfPath);
                ArrayList<Uri> uris = new ArrayList<>();
                if (file.isFile()) {
                    Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                    if (uriForFile != null) uris.add(uriForFile);
                }
                if (!uris.isEmpty()) {
                    shareMultiple(uris);
                }
            }
            sharePdfDirectWithoutOpen = false;
        } else {
            if (!isPdfCreatedForSharing) {
                if (!isFinishing() || !isDestroyed()) {
                    showPdfPathDialog(savedPdfPath);
                }
            } else {
                File file = new File(savedPdfPath);
                ArrayList<Uri> uris = new ArrayList<>();
                if (file.isFile()) {
                    Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                    if (uriForFile != null) uris.add(uriForFile);
                }
                if (!uris.isEmpty()) {
                    shareMultiple(uris);
                }
            }
        }


//        Toast.makeText(this, getString(R.string.pdf_created_successfully), Toast.LENGTH_LONG).show();

        /*Toast toast= Toast.makeText(getApplicationContext(),
                getString(R.string.pdf_created_successfully), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();*/

        /*if (flashScanUtil != null)
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_created_successfully));*/
    }

    private void shareMultiple(ArrayList<Uri> uris) {
        if (uris == null || uris.isEmpty()) return;
        flashScanUtil.shareMultiple(uris, this);
    }

    private void showPdfPathDialog(String savedPdfPath) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.saved_pdf_dialog);
        TextView tv_pdf_path = dialog.findViewById(R.id.tv_pdf_path);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_open = dialog.findViewById(R.id.btn_open);

        tv_pdf_path.setText(savedPdfPath);
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile(savedPdfPath);
                dialog.dismiss();
            }
        });
        if (!isFinishing()) {
            try {
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void openFile(String savedPdfPath) {
        File file = new File(savedPdfPath);
        if (file.isFile()) {
            flashScanUtil.openFile(context, file);
        }
    }

    @Override
    public void onFileOrFolderDeleted() {
        fetchFiles();
        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.delete_success_msg));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_DRIVE_SIGN_IN:
                //progress_lay.setVisibility(View.GONE);
                FlashScanUtil.newHideLoading();
                if (resultCode == Activity.RESULT_OK && data != null) {
                    flashScanUtil.handleSignInResult(context, data);
                    Log.d(TAG, "onActivityResult: isGetAllDataFromDrive");
                }
                break;
            case PERMISSIONS_SETTING_REQUEST_CODE: {
                finish();
                break;
            }
            case REQUEST_CODE_FOR_SINGLE_FILE:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForSingleFile(getFileModelForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForSingleFile(getFileModelForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelForWaterMark() != null)
                            createPdfForSingleFile(getFileModelForWaterMark(), true);
                        break;
                }
                break;
            case REQUEST_CODE_FOR_MULTIPLE_FILES:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelListForWaterMark() != null) {
                            saveAsPdfSelectedDocuments(getFileModelListForWaterMark(), false);
                        }
                        hideCheckBoxAndRemoveBottomBar();
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelListForWaterMark() != null) {
                            saveAsPdfSelectedDocuments(getFileModelListForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        hideCheckBoxAndRemoveBottomBar();
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelListForWaterMark() != null) {
                            saveAsPdfSelectedDocuments(getFileModelListForWaterMark(), true);
                        }
                        hideCheckBoxAndRemoveBottomBar();
                        break;
                }
                break;
            case REQUEST_CODE_FOR_SHARE_SINGLE_FILE:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForShareSingleFile(getFileModelForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForShareSingleFile(getFileModelForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelForWaterMark() != null)
                            createPdfForShareSingleFile(getFileModelForWaterMark(), true);
                        break;
                }
                break;
            case REQUEST_CODE_FOR_SHARE_MULTIPLE_FILES:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForSharingSelectedFiles(getFileModelListForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForSharingSelectedFiles(getFileModelListForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelListForWaterMark() != null)
                            createPdfForSharingSelectedFiles(getFileModelListForWaterMark(), true);
                        break;
                }
                break;
            case REQUEST_CODE_FOR_COMPLETE_DOC:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForAllFiles(getFileModelListForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForAllFiles(getFileModelListForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelListForWaterMark() != null)
                            createPdfForAllFiles(getFileModelListForWaterMark(), true);
                        break;
                }
                break;
            case TAKE_PHOTO:
                if (data == null) return;
                ArrayList<String> capturedPaths = (ArrayList<String>) data.getSerializableExtra("cam_paths");
                copyFiles(capturedPaths);
                break;
            case REQUEST_GET_IMAGES_USING_LIBRARY:
                if (resultCode == RESULT_OK) {
                    if (data != null) {

                        int corruptFileCount = 0;

                        ArrayList<String> selectedImagesPathList;
                        if (FlashScanUtil.isOsLessThanR()) {
                            selectedImagesPathList = new ArrayList<>(Matisse.obtainPathResult(data));
                        } else {
                            selectedImagesPathList = new ArrayList(FlashScanUtil.getClipData(data, this));
                        }
                        ArrayList<String> tempList = new ArrayList<>();
                        try {
                            Log.e(TAG, "aa " + selectedImagesPathList.size());

                            for (int i = 0; i < selectedImagesPathList.size(); i++) {
                                File file = new File(selectedImagesPathList.get(i));

                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                                int imageHeight = options.outHeight;
                                int imageWidth = options.outWidth;


                                if ((imageHeight == -1 && imageWidth == -1) || file.length() <= 0) {
                                    corruptFileCount++;
                                } else {
                                    tempList.add(selectedImagesPathList.get(i));

                                }

                            }
                            selectedImagesPathList = new ArrayList<>(tempList);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (corruptFileCount > 0) {
                            flashScanUtil.showSnackBar(flMain, String.format(getString(R.string.corrupted_file_error) + "", corruptFileCount));
                        }
                        if (selectedImagesPathList.size() > 0) {
                            copyFiles(selectedImagesPathList);
                        }

                    }
                }
                break;
            case Constants.REQUEST_CODE_PREMIUM_YEALY:
                Log.i(TAG, "onActivityResult REQUEST_CODE_PREMIUM_YEALY");
                if (resultCode == RESULT_OK) {
                    //PREMIUM taken
                    handlePremium();
                    if (prefManager.getPurchasedPlanName() == Constants.BUY_NOW_YEARLY) {
                        flashScanUtil.showSnackBar(flMain, getString(R.string.premium_yearly_success_msg, getString(R.string.app_name)));
                    } else {
                        flashScanUtil.showSnackBar(flMain, getString(R.string.premium_quarterly_success_msg, getString(R.string.app_name)));
                    }
                    // flashScanUtil.showSnackBar(flMain, getString(R.string.premium_yearly_success_msg));
                    try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    assert result != null;
                    Uri uri = result.getUri();
                    if (uri != null) {
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(uri);
                        } catch (FileNotFoundException e) {
                            FlashScanUtil.newHideLoading();
                            //progress_lay.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                       /* Intent intent = new Intent(context, OcrPreviewActivity.class);
                        intent.putExtra(Constants.PutExtraConstants.URI, uri);
                        intent.putExtra(Constants.PutExtraConstants.FROM_SOURCE, TAG);
                        intent.putExtra(Constants.PutExtraConstants.OCR_IS_NET_WORKING, isNetWorking);
                        Log.i(TAG, "At the time of navigating to OcrPreviewActivity, net working: " + isNetWorking);
                        startActivity(intent);*/
                        getImageUriAndSetBitmap(uri);
                    } else {
                        FlashScanUtil.newHideLoading();
                        //progress_lay.setVisibility(View.GONE);
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    FlashScanUtil.newHideLoading();
                    // progress_lay.setVisibility(View.GONE);
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PERM_DELETE:
                Log.e(TAG, "RESULT_OK" + RESULT_OK);
                if (resultCode == RESULT_OK) {
                    Log.e(TAG, "File deleted succesfully!");
                } else {
                    Log.e(TAG, "File not deleted");
                }
                break;
            case REQUEST_PERM_WRITE:
                if (resultCode == Activity.RESULT_OK) {
                    renameFile(fileName, fileModel);

                }
                break;

            /*case STORAGE_PERMISSION_REQ_CODE:

                if (resultCode == RESULT_OK) {

                    getContentResolver().takePersistableUriPermission(oldFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    DocumentsContract.renameDocument(getContentResolver(), uri, "newFileName.ext");
                }
                break;*/
        }
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
                        // progress_lay.setVisibility(View.VISIBLE);
                        FlashScanUtil.newShowLoading(context, "");
                        bitmapFromUri = resource;
                        scanOCR(bitmapFromUri);
                        //imageView.setImageBitmap(bitmapFromUri);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // progress_lay.setVisibility(View.GONE);
                    }
                });
            } else {
                //progress_lay.setVisibility(View.GONE);
                FlashScanUtil.newHideLoading();
            }

        } else {
            // progress_lay.setVisibility(View.GONE);
            FlashScanUtil.newHideLoading();
        }
    }

    private void scanOCR(Bitmap bitmapFromUri) {
        //progress_lay.setVisibility(View.GONE);
        FlashScanUtil.newHideLoading();
        if (/*checkOcrCount() && */bitmapFromUri != null) {
            /*showAreaSelectDialog(bitmapFromUri);*/
            detectTextFromBitmap(bitmapFromUri, WHOLE_TEXT);
        }
    }

    private void detectTextFromBitmap(Bitmap bitmapFromUri, int textMode) {
        // progress_lay.setVisibility(View.VISIBLE);
        FlashScanUtil.newShowLoading(context, "");
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmapFromUri);

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
                    //progress_lay.setVisibility(View.GONE);
                    FlashScanUtil.newHideLoading();
                    if (firebaseVisionText != null) {
                        processResult(firebaseVisionText, textMode, bitmapFromUri);
                    }

                    increaseOcrAttempted();
                    updateCreditsToApi();
                }).addOnFailureListener(e -> {
                    //progress_lay.setVisibility(View.GONE);
                    FlashScanUtil.newHideLoading();
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        /*InputImage image = InputImage.fromBitmap(bitmapFromUri,180);
        TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    FlashScanUtil.newHideLoading();
                    if (visionText != null) {
                        if (!TextUtils.isEmpty(visionText.getText())) {
                            recognizedText = visionText.getText();
                            *//*Log.d(TAG, "detectWholeText : " + text);*//*
                            createOcrDocument(bitmapFromUri);
                        } else {
                            Log.d(TAG, "No detect whole text found");
                            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_text_found));
                        }
                    }

                    increaseOcrAttempted();
                    updateCreditsToApi();
                })
                .addOnFailureListener(
                        e -> {
                            FlashScanUtil.newHideLoading();
                            Toast.makeText(context, "text_recog" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });*/

    }

    private void updateCreditsToApi() {
        if (Constants.IS_OWN_API_IMPLEMENT) {

            // progress_lay.setVisibility(View.VISIBLE);
            FlashScanUtil.newShowLoading(context, "");

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
        Intent intent = new Intent(ScanResultActivity.this, OcrChoosePlanDialog.class);
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

    private void openOcrMonthlyDialogActivity() {
        Intent intent = new Intent(ScanResultActivity.this, OcrPlanDialog.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_OCR_MONTHLY);
    }

    private void handlePremium() {
        //Do required UI or any changes
    }

    private void reCreate() {
        //No need to reCreate here
        /*finish();
        startActivity(getIntent());*/
    }

    private void copyFiles(ArrayList<String> selectedImagesPathList) {

        File dstFolderName = new File(flashScanUtil.getDocProcessingPath(context), folderName);
        File dstOriginalFolderName = new File(flashScanUtil.getDocOriginalPath(context), folderName);

        if (!dstFolderName.exists())
            dstFolderName.mkdirs();

        if (!dstOriginalFolderName.exists())
            dstOriginalFolderName.mkdirs();

        new CopyFileTask(this, selectedImagesPathList, dstFolderName.getAbsolutePath(), dstOriginalFolderName.getAbsolutePath(),
                new CopyOperationListener() {
                    @Override
                    public void onCopyStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCopyComplete(int fileOperation) {
                        progress_lay.setVisibility(View.GONE);
                        if (isSaveOnGoogleDrive && googleDriveFolderId != null) {
                            uploadFilesToDrive(selectedImagesPathList, context.getResources().getString(R.string.uploading_files_to_drive), false, "");
                        } else {
                            getFolderNameAndFetchFiles();
                        }
                    }
                }, true).execute();
    }

    @Override
    public void onDocumentCreationStart() {
       /* if (progress_lay != null) {
            progress_lay.setVisibility(View.VISIBLE);
        }*/
        FlashScanUtil.newShowLoading(context, "");
    }

    @Override
    public void onDocumentCreated(String folderPath) {
       /* if (progress_lay != null) {
            progress_lay.setVisibility(View.GONE);
        }*/
        FlashScanUtil.newHideLoading();
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
        if (/*!prefManager.isAppAdFree() && showIntersCreation && shouldIntersCreateShow*/true) {
            if (flashScanUtil.isConnectingToInternet() /*&& ocrInterstitialAd == null*/ && com.itl.commonres.utils.Constants.isAdShow) {
                //progress_lay.setVisibility(View.GONE);
                // load interstitial Ad for PDF creation
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_OCR, this);

            /*    progress_lay.setVisibility(View.VISIBLE);
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
            } else {
                Log.e(TAG, "Mobibuz : Ad Not Showing");
                if (!isMovedToResultScreen) {
                    moveToOcrResult(folderPath);
                }
            }
        } else {
            if (!isMovedToResultScreen) {
                moveToOcrResult(folderPath);
            }

        }

/*
//        if (!prefManager.isAppAdFree() && Constants.SHOW_INTERSTITIAL_ADS.FOR_OCR_RESULT_ACTIVITY) {
        if (!prefManager.isAppAdFree() && showIntersCreation && shouldIntersCreateShow) {
            if (flashScanUtil.isConnectingToInternet()) {
                show(this);
             *//*   if (!OcrAdManager.getInstance().isAdLoaded()) {
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
                }*//*
            } else {
                if (!isMovedToResultScreen) {
                    moveToOcrResult(folderPath);
                }

            }
        } else {*/
          /*  if (!isMovedToResultScreen) {
                moveToOcrResult(folderPath);
            }*/

        //}
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
        FlashScanUtil.newHideLoading();
        Intent intent = new Intent(context, OcrResultActivity.class);
        intent.putExtra(Constants.PutExtraConstants.OCR_RESULT_TEXT, text);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_PATH, folderPath);
        intent.putExtra(Constants.PutExtraConstants.FROM_SOURCE, TAG);
        intent.putExtra(Constants.PutExtraConstants.OCR_RESULT_FROM_SCREEN, Constants.OcrResultScreenFrom.FROM_PREVIEW);
        startActivity(intent);
        //finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void requestDeletePermission(List<Uri> uriList) {
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            PendingIntent pi = MediaStore.createDeleteRequest(context.getContentResolver(), uriList);

            try {
                startIntentSenderForResult(pi.getIntentSender(), REQUEST_PERM_DELETE, null, 0, 0,
                        0);
            } catch (IntentSender.SendIntentException e) {
            }
        }*/
    }

    private void requestWritePermission(List<Uri> uriList) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PendingIntent pi = MediaStore.createWriteRequest(context.getContentResolver(), uriList);

            try {
                startIntentSenderForResult(pi.getIntentSender(), REQUEST_PERM_WRITE, null, 0, 0,
                        0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }*/
    }

    private Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ContentResolver resolver = context.getContentResolver();
                    Uri picCollection = MediaStore.Images.Media
                            .getContentUri(MediaStore.VOLUME_EXTERNAL);
                    ContentValues picDetail = new ContentValues();
                    picDetail.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
                    picDetail.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                    picDetail.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + UUID.randomUUID().toString());
                    picDetail.put(MediaStore.Images.Media.IS_PENDING, 1);
                    Uri finaluri = resolver.insert(picCollection, picDetail);
                    picDetail.clear();
                    picDetail.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(picCollection, picDetail, null, null);
                    Log.e(TAG, "finaluri..." + finaluri);
                    return finaluri;
                } else {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return context.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }

            } else {
                return null;
            }
        }
    }

    public void rename(Uri uri, String rename) {

        //create content values with new name and update
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, rename);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.getContentResolver().update(uri, contentValues, null);
            }
        } catch (RecoverableSecurityException e) {
            Log.e(TAG, "exception" + e.getMessage());
        }

    }

    @Override
    public void onAdClosed() {
        if (isPDForOCR.equals("PDF")) {
            createPdf(fileModel, watermarkToBeShownOrNot);
        } else if (isPDForOCR.equals("OCR")) {
            if (!isMovedToResultScreen) {
                moveToOcrResult(createdFolderPath);
            }
        } else if (isPDForOCR.equals(getString(R.string.add_signature))) {
            askToBePremium();
        } else {
            createPDF();
        }
    }

    @Override
    public void onAdLoadedOrFailed(boolean isLoaded) {
        if (isPDForOCR.equals("PDF")) {
            createPdf(fileModel, watermarkToBeShownOrNot);
        } else if (isPDForOCR.equals("OCR")) {
            if (!isMovedToResultScreen) {
                moveToOcrResult(createdFolderPath);
            }
        } else if (isPDForOCR.equals(getString(R.string.add_signature))) {
            goToSignatureActivity(fileModel.getPath(), fileModel.getName());
        } else {
            createPDF();
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

    private void deleteFromGoogleDriveById(FileModel fileModel) {
        if (flashScanUtil.isDriveSignedIn()) {
            Log.e(TAG, "Drive signed in " + flashScanUtil.isDriveSignedIn());
            flashScanUtil.deleteFolderByIdFromGoogleDrive(context, fileModel.getGoogleDriveFolderId(), context.getResources().getString(R.string.delete_files_from_drive), () -> {
                prefManager.deleteFolderFromGoogleDriveDataList(fileModel.getGoogleDriveFolderId());
                fileModel.setSavedOnGoogleDrive(false);
                fileModel.setGoogleDriveFolderId("");
            });
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil.isDriveSignedIn());
            // progress_lay.setVisibility(View.VISIBLE);
            FlashScanUtil.newShowLoading(context, "");
            startActivityForResult((flashScanUtil.requestSignIn(context)).getSignInIntent(), REQUEST_CODE_DRIVE_SIGN_IN);
        }
    }

    private void uploadFilesToDrive(ArrayList<String> selectedImagesPathList, String dialogMsg, boolean isRename, String driveFileId) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(dialogMsg);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Log.d(TAG, "saveFileInDrive: " + new Gson().toJson(fileModel));

        List<GoogleDriveFolderModel> googleDriveFolderModelList = prefManager.getGoogleDriveDataList();
        for (GoogleDriveFolderModel googleDriveFolderModel : googleDriveFolderModelList) {
            if (googleDriveFolderModel.getId().equalsIgnoreCase(googleDriveFolderId)) {
                int fileCount = 0;
                boolean isDissmissDialog = false;
                for (String filePath : selectedImagesPathList) {
                    File file = new File(filePath);
                    fileCount++;
                    if (fileCount == selectedImagesPathList.size()) {
                        isDissmissDialog = true;
                    }
                    Log.e(TAG, "uploadFilesToDrive: googleDriveFolderId = " + googleDriveFolderId);
                    Log.e(TAG, "uploadFilesToDrive: folderName = " + folderName);
                    Log.e(TAG, "uploadFilesToDrive: file.getName() = " + file.getName());
                    Log.e(TAG, "uploadFilesToDrive: file.getGoogleDriveChildFileModelList() = " + new Gson().toJson(googleDriveFolderModel.getGoogleDriveChildFileModelList()));
                    Log.e(TAG, "uploadFilesToDrive: file.getAbsolutePath() = " + file.getAbsolutePath());
                    flashScanUtil.uploadFile(googleDriveFolderId, folderName, file.getName(), googleDriveFolderModel.getGoogleDriveChildFileModelList(), file, flashScanUtil.getMimeType(file.getAbsolutePath()), progressDialog, isDissmissDialog, true, new GoogleDriveDataUploadListener() {
                        @Override
                        public void onUploadFinish(String folderId) {
                            // Toast.makeText(context, context.getResources().getString(R.string.file_uploded),Toast.LENGTH_SHORT).show();
                            getFolderNameAndFetchFiles();
                            if (isRename) {
                                flashScanUtil.deleteFolderByIdFromGoogleDrive(context, driveFileId, context.getResources().getString(R.string.updating_file_metadata), () -> {
                                    prefManager.deleteFolderFromGoogleDriveDataList(driveFileId);
                                    Log.e(TAG, "onUploadFinish: file deleted");
                                    Toast.makeText(context, context.getResources().getString(R.string.file_updated), Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                getFolderNameAndFetchFiles();
                                Toast.makeText(context, context.getResources().getString(R.string.file_uploded), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
            }
        }
    }

    private void processForPermissions() {
        String permissionName = Constants.Storage_and_Camera;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            permissionName = Constants.Camera;
        }

        List<String> list = PermissionsListSealedClass.Companion.from(
                permissionName
        ).getPermissionsList();

        permissionList.addAll(list);
        permissionUtils.addPermissionsToList(list);

        permissionUtils.setPermissionName(
                PermissionsListSealedClass.Companion.from(
                        permissionName
                ).getPermissionName()
        );
        permissionUtils.checkAndRequestMultiplePermissions();
    }

    @Override
    public void requestPermissions(@NonNull List<String> permissionList, int requestCode) {
        try {
            requestMultiplePermissions.launch((String[]) permissionList.toArray());
            PermissionPreference permissionPreference = new PermissionPreference(this);

            for (String permission : permissionList) {
                permissionPreference.setPermissionRequested(permission);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getPermissionResult(boolean isPermissionGiven) {
        isPermissionGranted = isPermissionGiven;
        if (isPermissionGiven) {
            getFolderNameAndFetchFiles();
        }
    }

    @Override
    public void onPermissionClickOkay(boolean isAllFilesAccess, Context context) {
        new CommonMethods(this).processPermission(isAllFilesAccess, context);
    }

    @Override
    public void onPermissionClickNotNow(@NonNull Context context) {

    }
}
