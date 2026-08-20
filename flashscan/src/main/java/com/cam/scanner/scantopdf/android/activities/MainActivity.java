package com.cam.scanner.scantopdf.android.activities;

import static com.cam.scanner.scantopdf.android.activities.HomeActivity.REQUEST_GET_IMAGES_USING_LIBRARY;
import static com.cam.scanner.scantopdf.android.activities.HomeActivity.TAKE_PHOTO;
import static com.cam.scanner.scantopdf.android.activities.ScanResultActivity.getUriFromDisplayName;
import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.SingleTon.PdfSettings;
import com.cam.scanner.scantopdf.android.adapters.FileModelAdapter;
import com.cam.scanner.scantopdf.android.adapters.PageSizesAdapter;
import com.cam.scanner.scantopdf.android.ads.AdClosed;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.CreatePdfTask;
import com.cam.scanner.scantopdf.android.asynctasks.DeleteFolderOrFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetFilesTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetTempCompressBitmapFolders;
import com.cam.scanner.scantopdf.android.asynctasks.GetTempCompressedBitmapPath;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.CreateMultipleTempBitmapListener;
import com.cam.scanner.scantopdf.android.interfaces.CreateTempBitmapListener;
import com.cam.scanner.scantopdf.android.interfaces.FileOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.FileOrFolderDeleteListener;
import com.cam.scanner.scantopdf.android.interfaces.OnDeselectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.OnFetchingCompleted;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.interfaces.OnSelectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.PDFCreationCallback;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.models.ImageToPdfOptions;
import com.cam.scanner.scantopdf.android.models.PageSize;
import com.cam.scanner.scantopdf.android.pdf.PdfEditorActivity;
import com.cam.scanner.scantopdf.android.pdf.PdfReaderActivity;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.ScanConstants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itl.commonres.permissions.PermissionPreference;
import com.itl.commonres.permissions.PermissionUtils;
import com.itl.commonres.permissions.PermissionsListSealedClass;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;
import com.itl.commonres.utils.PermissionInterface;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseActivity implements View.OnClickListener, OnFetchingCompleted, OnItemSelectListener,
        PDFCreationCallback, FileOrFolderDeleteListener, FileOperationListener, AdClosed, PermissionUtils.RequestPermissionsInterface, PermissionInterface {
    private static final int REQUEST_PERM_DELETE = 44;

    private static final int REQUEST_CODE_FOR_SINGLE_DOCUMENT = 201;
    private static final int REQUEST_CODE_FOR_MULTIPLE_DOCUMENT = 202;
    private static final int REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT = 203;
    private static final int REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS = 204;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int PERMISSIONS_SETTING_REQUEST_CODE = 101;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_FETCH_ALL_DOCUMENTS = 301;
    private static final int PDF_BY_DIRECT = 1;
    private static final int PDF_VIA_SHARE = 2;
    private static final int REQUEST_CODE_GET_PDF_FILES = 444;
    private static final int REQUEST_CODE_FOR_IMPORTED_PDF = 666;
    private static final int LOCATION_REQUEST = 123;
    String fileName;
    //    private MainIdentity mIdentity;
    Uri extUri;
    private long lastClickedTime = 0;
    private Context context;
    private FlashScanUtil flashScanUtil;
    private RecyclerView recyclerView;
    private TextView tvNoFile, tv_save_as_pdf, tv_share, tv_delete, tv_total_file_count, tv_select_all_files, tv_scanned_docs;
    private boolean isFetchingFilesForPdfConvert = false;
    private String pdfFileName;
    private ImageView iv_menu, iv_search, iv_more_menu, iv_home, iv_camera, iv_media;
    private DrawerLayout drawerLayout;
    private TextView toolbarTitle;
    private EditText etSearch;
    private FrameLayout fl_camera, fl_media, fl_native_ad_view;
    private FileModelAdapter fileModelAdapter;
    private boolean isPdfCreationForSharing;
    private FloatingActionButton fab_camera, fab_media;
    private RelativeLayout progress_lay;
    private LinearLayout ll_bottom_bar, ll_no_document, ll_floating, ll_select_all_files;
    private Button btn_progress_lay;
    private PrefManager prefManager;
    private RewardedAd rewardedAd;
    private AdView adView;
    private int selectionAction = -1;
    private NativeAdView nativeAd;
    private boolean isNativeAdAlreadyLoaded = false;
    private boolean isMultiplePdfCreationWithCompression = false;
    private String selectedPageSize;
    private CardView ad_view_banner_container;
    private FrameLayout nativeSmallAdNoDoc;
    private FileModel selectedFileModel = null;
    private FileModel mFileModelForSaveToDrive = null;
    private int positionForSaveToDrive = -1;

//    public SdkFactory.Routine routine;

    private ArrayList<String> permissionList = new ArrayList<>();
    private PermissionUtils permissionUtils;
    private boolean isCheckPermissionResultOnly = false;
    ActivityResultLauncher<String[]> requestMultiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        permissionUtils.onRequestPermissionResult(isCheckPermissionResultOnly);
    });
    private Boolean isPermissionGranted = false;
    private List<Object> recyclerViewItems = new ArrayList<>();
    private FileModel fileModelForWaterMark;
    private List<FileModel> fileModelListForWaterMark = new ArrayList<>();
    private List<FileModel> fetchedFolderNamesList = new ArrayList<>();
    private String pdfFileNameForMultipleDocs;
    private boolean sharePdfDirectWithoutOpen = false;
    private List<FileModel> totalDocListIncludingAds = new ArrayList<>();

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static boolean delete(final Context context, final File file) {
        final String where = MediaStore.MediaColumns.DATA + "=?";
        final String[] selectionArgs = new String[]{
                file.getAbsolutePath()
        };
        final ContentResolver contentResolver = context.getContentResolver();
        //contentResolver.delete(Uri.parse(file.getPath()), null, null);
        final Uri filesUri = MediaStore.Files.getContentUri(file.getAbsolutePath());
        //Uri filesUri = Uri.fromFile(file);
        Log.e(TAG, "filesUri " + filesUri);
        try {
            contentResolver.delete(filesUri, where, selectionArgs);

            if (file.exists()) {
                //contentResolver.delete(Uri.parse(file.getPath()), null, null);
                contentResolver.delete(filesUri, where, selectionArgs);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception " + e.getMessage());
        }

        return !file.exists();
    }

    public static boolean deleteFileUsingDisplayName(Context context, String displayName) {

        Uri uri = getUriFromDisplayName(context, displayName);
        Log.e(TAG, "uri " + uri);
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

    public List<Object> getRecyclerViewItems() {
        if (recyclerViewItems == null) {
            recyclerViewItems = new ArrayList<>();
        }
        return recyclerViewItems;
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

    public void setFileModelListForWaterMark(List<FileModel> fileModelListForWaterMark) {
        /*this.fileModelListForWaterMark = fileModelListForWaterMark;*/
        if (!getFileModelListForWaterMark().isEmpty()) {
            getFileModelListForWaterMark().clear();
        }
        getFileModelListForWaterMark().addAll(fileModelListForWaterMark);
    }

    private List<FileModel> getFetchedFileList() {
        if (fetchedFolderNamesList == null) {
            fetchedFolderNamesList = new ArrayList<>();
        }
        return fetchedFolderNamesList;
    }

    private String getPdfFileNameForMultipleDocs() {
        if (TextUtils.isEmpty(pdfFileNameForMultipleDocs)) {
            pdfFileNameForMultipleDocs = flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name);
        }
        return pdfFileNameForMultipleDocs;
    }

    private void setPdfFileNameForMultipleDocs(String pdfFileNameForMultipleDocs) {
        this.pdfFileNameForMultipleDocs = pdfFileNameForMultipleDocs;
    }

    private List<FileModel> getDocumentsListIncludingAds() {
        if (totalDocListIncludingAds == null) {
            totalDocListIncludingAds = new ArrayList<>();
        }
        return totalDocListIncludingAds;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*AppController appController = AppController.getINSTANCE();
        SdkFactory SdkFactory = new AppSdkFactory(appController);
        SdkFactory.Routine routine = SdkFactory.createRoutine();*/

        /*boolean isLoad = ImageSdkLibrary.load(getApplication());

        Log.i(TAG, "Load lib: " + isLoad);*/

        /*mIdentity = new ViewModelProvider(this).get(MainIdentity.class);
        mIdentity.loadSettings();*/

        findViewIds();
        setClickListeners();
        initObjects();
        fetchFiles();

        manageSearchedFolders();
        prefManager.saveFoldersSortingOrder(prefManager.getAppSortingOrder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefManager.isPremium()) {
            if (ad_view_banner_container != null) {
                ad_view_banner_container.setVisibility(View.GONE);
            }
        }
    }

    private void manageSearchedFolders() {
        etSearch.addTextChangedListener(new TextWatcher() {
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

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
            ll_no_document.setVisibility(View.GONE);
            tv_scanned_docs.setVisibility(View.GONE);
            ll_floating.setVisibility(View.VISIBLE);
            for (FileModel fileModel : getFetchedFileList()) {
                if (fileModel.getName().toLowerCase().contains(text.toLowerCase())) {
                    filterFileList.add(fileModel);
                }
            }
            if (fileModelAdapter != null && !filterFileList.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                tvNoFile.setVisibility(View.GONE);
                fileModelAdapter.filterList(filterFileList);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvNoFile.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            ll_no_document.setVisibility(View.VISIBLE);
            tv_scanned_docs.setVisibility(View.GONE);
            ll_floating.setVisibility(View.GONE);
        }
    }

    private void fetchFiles() {
        // get files and set adapter here
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                prefManager.getAppSortingOrder()).execute();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_CODE_DRIVE_SIGN_IN:
                progress_lay.setVisibility(View.GONE);
                if (resultCode == Activity.RESULT_OK && data != null) {
                    flashScanUtil.handleSignInResult(context, data);
                    /*Log.d(TAG, "onActivityResult: isGetAllDataFromDrive = " + isGetAllDataFromDrive);
                    if (isGetAllDataFromDrive){
                        getAllAppUserDataFromGoogleDrive();
                        isGetAllDataFromDrive = false;
                    }else*/
                    if (mFileModelForSaveToDrive != null) {
                        checkDriveSignIn(mFileModelForSaveToDrive, positionForSaveToDrive);
                    }
                }
                break;
            case PERMISSIONS_SETTING_REQUEST_CODE: {
                finish();
                break;
            }
            case REQUEST_CODE_FOR_SINGLE_DOCUMENT:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelForWaterMark() != null) {
                            actionSavePdf(getFileModelForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelForWaterMark() != null) {
                            actionSavePdf(getFileModelForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelForWaterMark() != null)
                            actionSavePdf(getFileModelForWaterMark(), true);
                        break;
                }
                break;
            case REQUEST_CODE_FOR_MULTIPLE_DOCUMENT:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelListForWaterMark() != null) {
                            saveAsPdfSelectedDocuments(getFileModelListForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelListForWaterMark() != null) {
                            saveAsPdfSelectedDocuments(getFileModelListForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelListForWaterMark() != null)
                            saveAsPdfSelectedDocuments(getFileModelListForWaterMark(), true);
                        break;
                }
                break;
            case REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForSharingSingleDocument(getFileModelForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForSharingSingleDocument(getFileModelForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelForWaterMark() != null)
                            createPdfForSharingSingleDocument(getFileModelForWaterMark(), true);
                        break;
                }
                break;
            case REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForSharingMultipleDocuments(getFileModelListForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForSharingMultipleDocuments(getFileModelListForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        if (getFileModelListForWaterMark() != null)
                            createPdfForSharingMultipleDocuments(getFileModelListForWaterMark(), true);
                        break;
                }
                break;
            case REQUEST_CODE_FETCH_ALL_DOCUMENTS:
                isNativeAdAlreadyLoaded = true;
                fetchFiles();
                break;
            case REQUEST_CODE_GET_PDF_FILES:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            importPdf(uri);
                        }
                    }
                }
                break;
            case REQUEST_GET_IMAGES_USING_LIBRARY:
                if (resultCode == RESULT_OK) {
                    if (data != null) {

                        int corruptFileCount = 0;

                        ArrayList<String> selectedImagesPathList;
                        if (FlashScanUtil.isOsLessThanR()) {
                            selectedImagesPathList = new ArrayList<>(Matisse.obtainPathResult(data));
                        } else {
                            selectedImagesPathList = new ArrayList<>(FlashScanUtil.getClipData(data, this));
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
                            flashScanUtil.showSnackBar(findViewById(android.R.id.content), String.format(getString(R.string.corrupted_file_error) + "", corruptFileCount));
                        }
                        if (selectedImagesPathList.size() > 0) {
                            copyFiles(selectedImagesPathList);
                        }
                        /*Intent intent = new Intent(context, ImageCropActivity.class);
                        intent.putExtra(Constants.PutExtraConstants.FROM_SOURCE, Constants.FROM_MEDIA_FILES);
                        intent.putParcelableArrayListExtra(Constants.PutExtraConstants.SELECTED_IMAGES_LIST, selectedImagesPathList);
                        startActivity(intent);*/

                        //Todo commented by Harshit
                        /*if (selectedImagesPathList != null && !selectedImagesPathList.isEmpty()) {
                            if (selectedImagesPathList.size() == 1) {
                                intentToScanActivity(selectedImagesPathList);
                            } else {
                                ArrayList<String> selectedImagesList = new ArrayList<>(selectedImagesPathList);
                                intentToCropFilterActivity(selectedImagesList);
                            }
                        }*/
                    }
                }
                break;
            case TAKE_PHOTO:
                if (RESULT_OK == resultCode) {
                    if (data == null) return;
                    ArrayList<String> capturedPaths = (ArrayList<String>) data.getSerializableExtra("cam_paths");
                    copyFiles(capturedPaths);
                }
                break;
            case REQUEST_PERM_DELETE:
                Log.e(TAG, "RESULT_OK" + RESULT_OK);
                if (resultCode == RESULT_OK) {
                    Log.e(TAG, "Folder deleted succesfully!");
                } else {
                    Log.e(TAG, "Folder not deleted");
                }
                break;
            case LOCATION_REQUEST:
                if (resultCode == RESULT_OK) {
                    Log.e(TAG, "RESULT_OK!" + RESULT_OK);
                    if (data != null) {
                        Uri uri = data.getData();
                        Log.e(TAG, "uri " + uri);
                        if (uri != null) {
                            /* Got the path uri */
                            deletefile(uri, fileName);
                        }
                    }
                } else {
                    Log.e(TAG, "Folder not deleted");
                }
                break;
        }
    }

    private void copyFiles(ArrayList<String> selectedImagesPathList) {
        String folderName = flashScanUtil.getFolderCurrentTime();

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
                        Intent intent = new Intent(context, ScanResultActivity.class);
                        intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_HOME_SCREEN);
                        intent.putExtra(ScanConstants.PutExtraConstants.FOLDER_NAME, folderName);
                        startActivityForResult(intent, REQUEST_CODE_FETCH_ALL_DOCUMENTS);

                        if (selectedImagesPathList.size() == 1) {
                            ArrayList<String> pathLists = new ArrayList<>();
                            File file = new File(selectedImagesPathList.get(0));
                            File processedPath = new File(dstFolderName, "0_" + file.getName());
                            pathLists.add(processedPath.getAbsolutePath());

                            Intent intent1 = new Intent(context, ImageCropActivity.class);
                            intent1.putExtra("folder_name", folderName);
                            intent1.putExtra("is_bmp", true);
                            intent1.putStringArrayListExtra(Constants.PutExtraConstants.SELECTED_IMAGES_LIST, pathLists);
                            intent1.putExtra("pos", 0);

                            startActivity(intent1);
                        } else
                            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                }, true).execute();
    }

   /* private void loadLargeBannerAd() {
        if (!prefManager.isAppAdFree() && Constants.SHOW_MEDIUM_BANNER_ADS.FOR_DOC_SCANNER_ACTIVITY) {
            if (flashScanUtil.isConnectingToInternet()) {
                ll_adView.setVisibility(View.VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            } else {
                ll_adView.setVisibility(View.GONE);
            }
        }
    }*/

    private void importPdf(Uri uri) {
        Intent intent = new Intent(context, PdfReaderActivity.class);
        intent.putExtra(Constants.PutExtraConstants.URI, uri);
        intent.putExtra(Constants.IS_IMPORT_PDF_FROM_WITHIN_APP, true);
        startActivityForResult(intent, REQUEST_CODE_FETCH_ALL_DOCUMENTS);
    }

    private void initObjects() {
        context = MainActivity.this;
        flashScanUtil = new FlashScanUtil(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        /*DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);*/
        prefManager = new PrefManager(context);
        /*if (!prefManager.isAppRewardAdFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
            loadRewardedAd();
        }*/
        permissionUtils = new PermissionUtils(this, this, this);
    }

    private void setClickListeners() {
        iv_menu.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        fl_camera.setOnClickListener(this);
        fl_media.setOnClickListener(this);
        iv_more_menu.setOnClickListener(this);
        fab_camera.setOnClickListener(this);
        fab_media.setOnClickListener(this);
        iv_home.setOnClickListener(this);
        tv_save_as_pdf.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
        tv_select_all_files.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_media.setOnClickListener(this);
    }

    private void findViewIds() {
        recyclerView = findViewById(R.id.rv_scanner_files);
        tvNoFile = findViewById(R.id.tv_no_file);
        iv_menu = findViewById(R.id.iv_menu);
        /*drawerLayout = findViewById(R.id.drawer_layout);*/
        toolbarTitle = findViewById(R.id.toolbar_title);
        etSearch = findViewById(R.id.et_search);
        etSearch.setRawInputType(InputType.TYPE_CLASS_TEXT);
        iv_search = findViewById(R.id.iv_search);
        fl_camera = findViewById(R.id.fl_camera);
        fl_media = findViewById(R.id.fl_media);
        iv_more_menu = findViewById(R.id.iv_more_menu);
        iv_more_menu.setVisibility(View.VISIBLE);
        fab_camera = findViewById(R.id.fab_camera);
        fab_media = findViewById(R.id.fab_media);
        iv_home = findViewById(R.id.iv_home);
        progress_lay = findViewById(R.id.progress_lay);
        ll_bottom_bar = findViewById(R.id.ll_bottom_bar);
        tv_delete = findViewById(R.id.tv_delete);
        tv_share = findViewById(R.id.tv_share);
        tv_save_as_pdf = findViewById(R.id.tv_save_as_pdf);
        ll_no_document = findViewById(R.id.ll_no_document);
        ll_floating = findViewById(R.id.ll_floating);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        //ll_adView = findViewById(R.id.ll_adView);
        adView = findViewById(R.id.adView);
        ll_select_all_files = findViewById(R.id.ll_select_all_files);
        tv_total_file_count = findViewById(R.id.tv_total_file_count);
        tv_select_all_files = findViewById(R.id.tv_select_all_files);
        //ll_native_ad_view = findViewById(R.id.ll_native_ad_view);
        fl_native_ad_view = findViewById(R.id.fl_native_ad);
        iv_media = findViewById(R.id.iv_media);
        iv_camera = findViewById(R.id.iv_camera);
        tv_scanned_docs = findViewById(R.id.tv_scanned_docs);
        ad_view_banner_container = findViewById(R.id.ad_view_banner_container);
        nativeSmallAdNoDoc = findViewById(R.id.nativeSmallAdNoDoc);
    }

    private void showAskPdfNameDialogForMultiDoc(List<FileModel> selectedFileModelList, int pdfVia) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_ask_pdf_name);

        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        et_pdf_name.setText(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));

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
                setPdfFileNameForMultipleDocs(et_pdf_name.getText().toString().trim());
                if (rbOriginal.isChecked()) {
                    isMultiplePdfCreationWithCompression = false;
                    handleMultipleDocPdfCreation(selectedFileModelList, pdfVia);
                } else if (rbCompressed.isChecked()) {
                    isMultiplePdfCreationWithCompression = true;
                    handleMultipleDocPdfCreation(selectedFileModelList, pdfVia);
                }

                hideCheckBoxAndRemoveBottomBar();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /*private void showRewardedAdDialogForMultipleSelectedDocuments(List<FileModel> selectedFileModelList) {
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
                rewardedAd.show(MainActivity.this, rewardedAdCallback);
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

    private void handleMultipleDocPdfCreation(List<FileModel> selectedFileModelList, int pdfVia) {
        switch (pdfVia) {
            case PDF_BY_DIRECT:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelListForWaterMark(selectedFileModelList);
                    goToWaterMarkActivityForMultipleDoc();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /* || prefManager.isPremiumQuarterly()*/) {
                        saveAsPdfSelectedDocuments(selectedFileModelList, false);
                    } else {
                        saveAsPdfSelectedDocuments(selectedFileModelList, true);
                    }
                }
                break;
            case PDF_VIA_SHARE:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelListForWaterMark(selectedFileModelList);
                    goToWaterMarkActivityForShareMultipleDoc();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly()  /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForSharingMultipleDocuments(selectedFileModelList, false);
                    } else {
                        createPdfForSharingMultipleDocuments(selectedFileModelList, true);
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_menu) {
            openDrawer();
        } else if (id == R.id.iv_search) {
            handleSearchBarVisibility();
        } else if (id == R.id.fab_camera || id == R.id.fl_camera || id == R.id.iv_camera) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();

//                startScan(ScanConstants.OPEN_CAMERA);
            final File fileSink = getExternalCacheDir();

            if (fileSink.exists() || fileSink.mkdirs()) {
                Intent captureIntent = new Intent(this, CaptureImagesActivity.class);
                startActivityForResult(captureIntent, TAKE_PHOTO);
            }
        } else if (id == R.id.iv_home) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            goToHome();
        } else if (id == R.id.tv_save_as_pdf) {
            List<FileModel> selectedFileModelList = null;
            if (fileModelAdapter != null) {
                selectedFileModelList = fileModelAdapter.getSelectedFileModelList();
            }
            if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                if (selectedFileModelList.size() == 1) {
                    selectedFileModel = selectedFileModelList.get(0);
                    createPDF();
                    hideCheckBoxAndRemoveBottomBar();
                } else {
                    if (Constants.IS_CREATE_PDF_DIRECT) {
                        setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));
                        handleMultipleDocPdfCreation(selectedFileModelList, PDF_BY_DIRECT);
                        hideCheckBoxAndRemoveBottomBar();
                    } else {
                        showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_BY_DIRECT);
                    }
                }

            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
            }
        } else if (id == R.id.tv_share) {
            List<FileModel> fileModelList = null;
            if (fileModelAdapter != null) {
                fileModelList = fileModelAdapter.getSelectedFileModelList();
            }
            if (fileModelList != null && !fileModelList.isEmpty()) {
                showShareDialog();
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
            }
        } else if (id == R.id.tv_delete) {
            List<FileModel> selectedFiles = null;
            if (fileModelAdapter != null) {
                selectedFiles = fileModelAdapter.getSelectedFileModelList();
            }
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                showDeleteDialog();
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
            }
        } else if (id == R.id.fab_media || id == R.id.fl_media || id == R.id.iv_media) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
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
        } else if (id == R.id.btn_progress_lay) {
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

    private void deSelectAllDocuments() {
        if (fileModelAdapter != null) {
            fileModelAdapter.deSelectAllDocuments(new OnDeselectAllFiles() {
                @Override
                public void onDeselect() {
                    tv_total_file_count.setText(fileModelAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
                }
            });
            manageSelectAllText();
        }
    }

    private void goToWaterMarkActivityForMultipleDoc() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_MULTIPLE_DOCUMENT);
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
        popupMenu.getMenu().findItem(R.id.menu_import_pdf).setVisible(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_sort_by) {
                    showSortingDialog();
                } else if (itemId == R.id.menu_select_all) {/*selectAllDocuments();*/
                } else if (itemId == R.id.menu_import_pdf) {
                    openStorageAccessFrameWorkForPdfFiles();
                }
                return true;
            }
        });

        popupMenu.show();
    }

    private void openStorageAccessFrameWorkForPdfFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_CODE_GET_PDF_FILES);
    }

    private void selectAllDocuments() {
        if (fileModelAdapter != null) {
            fileModelAdapter.selectAllFiles(new OnSelectAllFiles() {
                @Override
                public void onSelectedAllFiles() {
                    tv_total_file_count.setText(fileModelAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
                }
            });
            manageSelectAllText();
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
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                Constants.SORT_BY.nameZtoA).execute();
    }

    private void fetchDocumentsBySortingAtoZ() {
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                Constants.SORT_BY.nameAtoZ).execute();
    }

    private void fetchDocumentsByModificationTimeDescending() {
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                Constants.SORT_BY.modificationTimeDescending).execute();
    }

    /*private void showRewardedAdDialogForShareMultipleDocuments(List<FileModel> selectedFileModelList) {
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
                        createPdfForSharingMultipleDocuments(selectedFileModelList, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForSharingMultipleDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForSharingMultipleDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(MainActivity.this, rewardedAdCallback);
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

    private void fetchDocumentsByModificationTimeAscending() {
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                Constants.SORT_BY.modificationTimeAscending).execute();
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

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                hideCheckBoxAndRemoveBottomBar();
            }
        });

        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
            List<FileModel> finalSelectedFileModelList = null;
            if (fileModelAdapter != null) {
                finalSelectedFileModelList = fileModelAdapter.getSelectedFileModelList();
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

    private void showShareDialog() {

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
                List<FileModel> selectedFileModelList = null;
                if (fileModelAdapter != null) {
                    selectedFileModelList = fileModelAdapter.getSelectedFileModelList();
                }
                if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                    if (Constants.IS_CREATE_PDF_DIRECT) {
                        sharePdfDirectWithoutOpen = false;
                        setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));
                        handleMultipleDocPdfCreation(selectedFileModelList, PDF_VIA_SHARE);
                        hideCheckBoxAndRemoveBottomBar();
                    } else {
                        showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_VIA_SHARE);
                    }

                }
            }
        });

        ll_share_as_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                List<FileModel> selectedFileModelList = null;
                if (fileModelAdapter != null) {
                    selectedFileModelList = fileModelAdapter.getSelectedFileModelList();
                }
                if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                    if (Constants.IS_CREATE_PDF_DIRECT) {
                        sharePdfDirectWithoutOpen = true;
                        setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));
                        handleMultipleDocPdfCreation(selectedFileModelList, PDF_VIA_SHARE);
                        hideCheckBoxAndRemoveBottomBar();
                    } else {
                        showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_VIA_SHARE);
                    }

                }
            }
        });

        ll_share_as_image.setOnClickListener(v -> {
            dialog.dismiss();
            List<FileModel> selectedFileModelList = null;
            if (fileModelAdapter != null) {
                selectedFileModelList = fileModelAdapter.getSelectedFileModelList();
            }
            if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                ArrayList<Uri> uriList = new ArrayList<>();
                for (FileModel fileModel : selectedFileModelList) {
                    if (fileModel != null) {
                        File fileOrDirectory = new File(fileModel.getPath());

                        if (fileOrDirectory.isDirectory()) {
                            File[] files = fileOrDirectory.listFiles();
                            if (files != null && files.length > 0) {
                                for (File file : files) {
                                    if (file.isFile() && file.exists()) {
                                        if (!TextUtils.isEmpty(file.getName()) && file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                            continue;
                                        }
                                    }
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
            }
            hideCheckBoxAndRemoveBottomBar();
        });
        dialog.show();
    }

    private void goToWaterMarkActivityForShareMultipleDoc() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS);
    }

    private void createPdfForSharingMultipleDocuments(List<FileModel> selectedFileModelList, boolean isWaterMarkToBeShown) {
        if (isMultiplePdfCreationWithCompression) {
            ArrayList<String> selectedFoldersPathList = new ArrayList<>();
            for (FileModel fileModel : selectedFileModelList) {
                selectedFoldersPathList.add(fileModel.getPath());
            }
            if (!selectedFoldersPathList.isEmpty()) {
                new GetTempCompressBitmapFolders(context, selectedFoldersPathList, new CreateMultipleTempBitmapListener() {
                    @Override
                    public void onCompressBitmapStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCompressBitmapComplete(ArrayList<String> foldersList) {
                        progress_lay.setVisibility(View.GONE);
                        if (foldersList != null && !foldersList.isEmpty()) {
                            List<String> filePathList = new ArrayList<>();
                            for (String folderPath : foldersList) {
                                File fileOrDirectory = new File(folderPath);
                                if (fileOrDirectory.isDirectory()) {
                                    File[] files = fileOrDirectory.listFiles();
                                    if (files != null && files.length > 0) {
                                        flashScanUtil.sortFilesByNameAtoZ(files);
                                        /*int appSortingOrder = prefManager.getAppSortingOrder();
                                        switch (appSortingOrder) {
                                            case Constants.SORT_BY.defaultOrder:
                                            case Constants.SORT_BY.modificationTimeDescending:
                                                flashScanUtil.sortFilesByDescendingLastModified(files);
                                                break;
                                            case Constants.SORT_BY.modificationTimeAscending:
                                                flashScanUtil.sortFilesByAscendingLastModified(files);
                                                break;
                                            case Constants.SORT_BY.nameAtoZ:
                                                flashScanUtil.sortFilesByNameAtoZ(files);
                                                break;
                                            case Constants.SORT_BY.nameZtoA:
                                                flashScanUtil.sortFilesByNameZtoA(files);
                                                break;
                                        }*/
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
                            if (!filePathList.isEmpty()) {
                                isPdfCreationForSharing = true;
                                // not in use
                                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
                            } else {
                                // show warning message
                                showNoFilesInDocumentDialog();
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
                            flashScanUtil.sortFilesByNameAtoZ(files);
                            /*int appSortingOrder = prefManager.getAppSortingOrder();
                            switch (appSortingOrder) {
                                case Constants.SORT_BY.defaultOrder:
                                case Constants.SORT_BY.modificationTimeDescending:
                                    flashScanUtil.sortFilesByDescendingLastModified(files);
                                    break;
                                case Constants.SORT_BY.modificationTimeAscending:
                                    flashScanUtil.sortFilesByAscendingLastModified(files);
                                    break;
                                case Constants.SORT_BY.nameAtoZ:
                                    flashScanUtil.sortFilesByNameAtoZ(files);
                                    break;
                                case Constants.SORT_BY.nameZtoA:
                                    flashScanUtil.sortFilesByNameZtoA(files);
                                    break;
                            }*/
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
                isPdfCreationForSharing = true;
                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
            } else {
                // show warning message
                showNoFilesInDocumentDialog();
            }
        }

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

    private void saveAsPdfSelectedDocuments(List<FileModel> selectedFileModelList, boolean isWaterMarkToBeShown) {
        if (selectedFileModelList == null || selectedFileModelList.isEmpty())
            return;
        if (isMultiplePdfCreationWithCompression) {
            ArrayList<String> selectedFoldersPathList = new ArrayList<>();
            for (FileModel fileModel : selectedFileModelList) {
                selectedFoldersPathList.add(fileModel.getPath());
            }
            if (!selectedFoldersPathList.isEmpty()) {
                new GetTempCompressBitmapFolders(context, selectedFoldersPathList, new CreateMultipleTempBitmapListener() {
                    @Override
                    public void onCompressBitmapStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCompressBitmapComplete(ArrayList<String> foldersList) {
                        progress_lay.setVisibility(View.GONE);
                        if (foldersList != null && !foldersList.isEmpty()) {
                            List<String> filePathList = new ArrayList<>();
                            for (String folderPath : foldersList) {
                                File fileOrDirectory = new File(folderPath);
                                if (fileOrDirectory.isDirectory()) {
                                    File[] files = fileOrDirectory.listFiles();
                                    if (files != null && files.length > 0) {
                                        flashScanUtil.sortFilesByNameAtoZ(files);
                                        /*int appSortingOrder = prefManager.getAppSortingOrder();
                                        switch (appSortingOrder) {
                                            case Constants.SORT_BY.defaultOrder:
                                            case Constants.SORT_BY.modificationTimeDescending:
                                                flashScanUtil.sortFilesByDescendingLastModified(files);
                                                break;
                                            case Constants.SORT_BY.modificationTimeAscending:
                                                flashScanUtil.sortFilesByAscendingLastModified(files);
                                                break;
                                            case Constants.SORT_BY.nameAtoZ:
                                                flashScanUtil.sortFilesByNameAtoZ(files);
                                                break;
                                            case Constants.SORT_BY.nameZtoA:
                                                flashScanUtil.sortFilesByNameZtoA(files);
                                                break;
                                        }*/
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
                            if (!filePathList.isEmpty()) {
                                isPdfCreationForSharing = false;
                                // not in use now
                                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
                            } else {
                                // show warning dialog
                                showNoFilesInDocumentDialog();
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
                            flashScanUtil.sortFilesByNameAtoZ(files);
                            /*int appSortingOrder = prefManager.getAppSortingOrder();
                            switch (appSortingOrder) {
                                case Constants.SORT_BY.defaultOrder:
                                case Constants.SORT_BY.modificationTimeDescending:
                                    flashScanUtil.sortFilesByDescendingLastModified(files);
                                    break;
                                case Constants.SORT_BY.modificationTimeAscending:
                                    flashScanUtil.sortFilesByAscendingLastModified(files);
                                    break;
                                case Constants.SORT_BY.nameAtoZ:
                                    flashScanUtil.sortFilesByNameAtoZ(files);
                                    break;
                                case Constants.SORT_BY.nameZtoA:
                                    flashScanUtil.sortFilesByNameZtoA(files);
                                    break;
                            }*/
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
                isPdfCreationForSharing = false;
                createPdf(filePathList, getPdfFileNameForMultipleDocs(), isWaterMarkToBeShown);
            } else {
                // show warning message
                showNoFilesInDocumentDialog();
            }
        }

    }

    private void handleSearchBarVisibility() {
        if (toolbarTitle.getVisibility() == View.VISIBLE) {
            toolbarTitle.setVisibility(View.GONE);
            etSearch.setVisibility(View.VISIBLE);

            etSearch.requestFocus();

            /*if(etSearch.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }*/
            iv_search.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_white));
            showKeyboard();
        } else {
            clearSearchView();
        }
    }

    private void clearSearchView() {
        toolbarTitle.setVisibility(View.VISIBLE);
        etSearch.setText("");
        etSearch.setVisibility(View.GONE);
        iv_search.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_search));
        hideKeyboard();
        if (fileModelAdapter != null) {
            fileModelAdapter.clearFilterList(getDocumentsListIncludingAds());  // for showing ads
        }
    }

    /*private void navigateToDocumentScanActivity() {
        startActivity(new Intent(context, DocumentScanActivity.class));
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }*/

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void openDrawer() {
        /*drawerLayout.openDrawer(GravityCompat.START);*/
    }

    private void closeDrawer() {
        /*drawerLayout.closeDrawer(GravityCompat.START);*/
    }

  /*  @Override
    public void onCompletedShowList(List<FileModel> fileModelList) {
        if (fileModelList != null && !fileModelList.isEmpty()) {
            // set Adapter
            showRecyclerView(fileModelList);
        } else {
            hideRecyclerView();
        }
    }*/

    @Override
    public void onBackPressed() {
        if (fileModelAdapter != null && fileModelAdapter.isVisibleAllCheckbox()) {
            /*iv_more_menu.setVisibility(View.GONE);*/
            hideCheckBoxAndRemoveBottomBar();

        } else if (etSearch.getVisibility() == View.VISIBLE) {
            clearSearchView();
        } else {
            goToHome();
        }
    }

    private void hideCheckBoxAndRemoveBottomBar() {
        fileModelAdapter.hideAllCheckBoxes();
        ll_bottom_bar.setVisibility(View.GONE);
        ll_select_all_files.setVisibility(View.GONE);
        iv_more_menu.setVisibility(View.VISIBLE);
        ll_floating.setVisibility(View.VISIBLE);
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

    /*private WaterMark getWaterMark() {
        WaterMark waterMark = new WaterMark();
        waterMark.setWaterMarkText(getString(R.string.flash_scan));
        waterMark.setTextColor(getColor(R.color.transparent_color));
        waterMark.setBaseColor(new BaseColor(Color.red(getColor(android.R.color.black)),
                Color.green(getColor(android.R.color.black)),
                Color.blue(getColor(android.R.color.black)),
                Color.alpha(getColor(android.R.color.black))));
        waterMark.setTextSize(20);
        waterMark.setFontFamily(Font.FontFamily.TIMES_ROMAN);
        waterMark.setFontStyle(Font.BOLD);
        waterMark.setRotationAngle(0);
        return waterMark;
    }*/

    private void intentToHomeDashBoard() {
        Intent intent = new Intent(context, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /*private void loadBottomNativeAd() {

        boolean showNative = AppController.getINSTANCE().dbHandler.showNative();

//        if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_DOC_SCANNER_ACTIVITY) {
        if (!prefManager.isAppAdFree() && showNative) {
            if (flashScanUtil.isConnectingToInternet()) {
                ll_native_ad_view.setVisibility(View.VISIBLE);
                //loadNativeAd();
            } else {
                ll_native_ad_view.setVisibility(View.GONE);
            }
        } else {
            ll_native_ad_view.setVisibility(View.GONE);
        }
    }*/

   /* private void loadNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(context, BuildConfig.NATIVE_AD_ID)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        if (isDestroyed()) {
                            unifiedNativeAd.destroy();
                            return;
                        }
                        if (nativeAd != null) {
                            nativeAd.destroy();
                        }
                        nativeAd = unifiedNativeAd;
                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.item_view_native_ad, null, false);
                        if (unifiedNativeAdView != null) {
                            mapUnifiedNativeAdToLayout(unifiedNativeAd, unifiedNativeAdView);
                            fl_native_ad_view.removeAllViews();
                            fl_native_ad_view.addView(unifiedNativeAdView);
                        }

                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }*/

  /*  private void mapUnifiedNativeAdToLayout(UnifiedNativeAd adFromGoogle, UnifiedNativeAdView myAdView) {
        MediaView mediaView = myAdView.findViewById(R.id.ad_media);
        myAdView.setMediaView(mediaView);
        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_headline));
        myAdView.setBodyView(myAdView.findViewById(R.id.ad_body));
        myAdView.setCallToActionView(myAdView.findViewById(R.id.ad_call_to_action));
        CardView cardView = myAdView.findViewById(R.id.cv_app_icon);
        myAdView.setIconView(myAdView.findViewById(R.id.ad_app_icon));
        myAdView.setPriceView(myAdView.findViewById(R.id.ad_price));
        myAdView.setStarRatingView(myAdView.findViewById(R.id.ad_stars));
        myAdView.setStoreView(myAdView.findViewById(R.id.ad_store));
        myAdView.setAdvertiserView(myAdView.findViewById(R.id.ad_advertiser));

        *//*myAdView.setImageView();
        myAdView.setClickConfirmingView();
        myAdView.setAdChoicesView();*//*

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        myAdView.getMediaView().setMediaContent(adFromGoogle.getMediaContent());
        Log.e(TAG, "ad headline :" + adFromGoogle.getHeadline());
        ((TextView) myAdView.getHeadlineView()).setText(adFromGoogle.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        Log.e(TAG, "ad body :" + adFromGoogle.getBody());
        if (adFromGoogle.getBody() == null) {
            myAdView.getBodyView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getBodyView()).setText(adFromGoogle.getBody());
        }

        Log.e(TAG, "ad call to action :" + adFromGoogle.getCallToAction());
        if (adFromGoogle.getCallToAction() == null) {
            myAdView.getCallToActionView().setVisibility(View.GONE);
        } else {
            ((Button) myAdView.getCallToActionView()).setText(adFromGoogle.getCallToAction());
        }

        if (adFromGoogle.getIcon() == null) {
            myAdView.getIconView().setVisibility(View.GONE);
            cardView.setVisibility(View.GONE);
        } else {
            cardView.setVisibility(View.VISIBLE);
            myAdView.getIconView().setVisibility(View.VISIBLE);
            ((ImageView) myAdView.getIconView()).setImageDrawable(adFromGoogle.getIcon().getDrawable());
        }

        Log.e(TAG, "ad price :" + adFromGoogle.getPrice());
        if (adFromGoogle.getPrice() == null) {
            myAdView.getPriceView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getPriceView()).setText(adFromGoogle.getPrice());
        }

        if (adFromGoogle.getStarRating() == null) {
            myAdView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) myAdView.getStarRatingView()).setRating(adFromGoogle.getStarRating().floatValue());
        }

        Log.e(TAG, "ad store :" + adFromGoogle.getStore());
        if (adFromGoogle.getStore() == null) {
            myAdView.getStoreView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getStoreView()).setText(adFromGoogle.getStore());
        }

        Log.e(TAG, "ad advertiser :" + adFromGoogle.getAdvertiser());
        if (adFromGoogle.getAdvertiser() == null) {
            myAdView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getAdvertiserView()).setText(adFromGoogle.getAdvertiser());
        }

        myAdView.setNativeAd(adFromGoogle);
    }*/

    @Override
    public void onFetchingComplete(List<FileModel> fileModelList) {
        progress_lay.setVisibility(View.GONE);
        if (!isFetchingFilesForPdfConvert) {
            if (fileModelList != null && !fileModelList.isEmpty()) {
                if (!getFetchedFileList().isEmpty()) {
                    getFetchedFileList().clear();
                }
                getFetchedFileList().addAll(fileModelList);  // for search filter working
                showRecyclerView(fileModelList);
            } else {
                hideRecyclerView();
            }
        } else {
            // not in use now
       /*     if (fileModelList != null && !fileModelList.isEmpty()) {
                List<String> imagesUriList = new ArrayList<>();
                for (FileModel fileModel : fileModelList) {
                    imagesUriList.add(fileModel.getPath());
                }
                if (!imagesUriList.isEmpty()) {
                    createPdf(imagesUriList, pdfFileName);
                }
            }*/
        }
    }

    @Override
    public void onFetchingStart() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    private void createPdf(List<String> imagesUriList, String pdfFileName, boolean isWaterMarkToBeShown) {
        ImageToPdfOptions imageToPdfOptions = new ImageToPdfOptions();
        imageToPdfOptions.setPageSize(Constants.PdfConstants.DEFAULT_PDF_PAGE_SIZE);
        imageToPdfOptions.setPageColor(Constants.PdfConstants.DEFAULT_PDF_PAGE_COLOR);
        imageToPdfOptions.setMargins(0, 0, 0, 0);
        imageToPdfOptions.setPdfQuality(Constants.PdfConstants.DEFAULT_PDF_QUALITY);
        imageToPdfOptions.setBorderWidth(Constants.PdfConstants.DEFAULT_BORDER_WIDTH);
        // TODO: 01-05-2020 commneted for build 2.4 to be live
        imageToPdfOptions.setWaterMarkAdded(isWaterMarkToBeShown);
        imageToPdfOptions.setWaterMark(flashScanUtil.getWaterMark());


        new CreatePdfTask(context, pdfFileName, imageToPdfOptions, imagesUriList, this, true).execute();
    }

    private void hideRecyclerView() {
        recyclerView.setVisibility(View.GONE);
        ll_no_document.setVisibility(View.VISIBLE);
        tv_scanned_docs.setVisibility(View.GONE);
        ll_floating.setVisibility(View.GONE);

        if (!prefManager.isAppAdFree() && flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_DOC_EMPTY_LIST.getValue())) {
            ad_view_banner_container.setVisibility(View.VISIBLE);
            callNativeAd(nativeSmallAdNoDoc);
        } else {
            Log.e("Mobibuz : ", "Ad Not Showing");
            ad_view_banner_container.setVisibility(View.GONE);
        }

        /*loadLargeBannerAd();
        if (!isNativeAdAlreadyLoaded) {
            Log.e(TAG, "loadBottomNativeAd called");
            //loadBottomNativeAd();
        }*/
    }

    private void showRecyclerView(List<FileModel> fileModelList) {
        ll_no_document.setVisibility(View.GONE);
        tv_scanned_docs.setVisibility(View.GONE);
        ad_view_banner_container.setVisibility(View.GONE);
        //ll_adView.setVisibility(View.GONE);
        //ll_native_ad_view.setVisibility(View.GONE);
        ll_floating.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

        // for showing ad
        List<FileModel> finalFileModelList = new ArrayList<>();

        boolean showNative = AppController.getINSTANCE().dbHandler.showNative();

        /*if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
                && Constants.SHOW_NATIVE_ADS.FOR_DOC_SCANNER_ACTIVITY) {*/
        if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
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
        }
        //==============

        if (!getDocumentsListIncludingAds().isEmpty()) {
            getDocumentsListIncludingAds().clear();
        }
        getDocumentsListIncludingAds().addAll(finalFileModelList);
        fileModelAdapter = new FileModelAdapter(context, finalFileModelList, this, this);
        recyclerView.setAdapter(fileModelAdapter);
    }

    @Override
    public void onItemSelect(Object o) {
        if (fileModelAdapter != null && fileModelAdapter.isVisibleAllCheckbox()) {
            tv_total_file_count.setText(fileModelAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
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
            Intent intent = new Intent(context, ScanResultActivity.class);
            intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_MAIN_SCREEN);
            intent.putExtra(ScanConstants.PutExtraConstants.FOLDER_NAME, fileModel.getName());
            intent.putExtra(ScanConstants.PutExtraConstants.DATE_TAKEN, fileModel.getDateTaken());
            intent.putExtra(ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE, fileModel.isSavedOnGoogleDrive());
            intent.putExtra(ScanConstants.PutExtraConstants.GOOGLE_DRIVE_FOLDER_ID, fileModel.getGoogleDriveFolderId());
            startActivityForResult(intent, REQUEST_CODE_FETCH_ALL_DOCUMENTS);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }

    }

    @Override
    public void onItemLongPress(Object o) {
        if (fileModelAdapter != null && fileModelAdapter.isVisibleAllCheckbox()) {
            ll_bottom_bar.setVisibility(View.VISIBLE);
            iv_more_menu.setVisibility(View.GONE);
            ll_floating.setVisibility(View.GONE);
            ll_select_all_files.setVisibility(View.VISIBLE);
            tv_total_file_count.setText(fileModelAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
            manageSelectAllText();
        }
    }

    private void manageSelectAllText() {
        if (fileModelAdapter != null && fileModelAdapter.getSelectedFileModelList().size() == getFetchedFileList().size()) {
            tv_select_all_files.setText(getString(R.string.deselect_all));
            selectionAction = Constants.DESELECT_ALL;
        } else {
            tv_select_all_files.setText(getString(R.string.select_all));
            selectionAction = Constants.SELECT_ALL;
        }

    }

    /*private void showRewardedAdDialogForShareSingleDocument(FileModel fileModel) {
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
                        createPdfForSharingSingleDocument(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForSharingSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForSharingSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(MainActivity.this, rewardedAdCallback);
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
    public void onItemAction(Object o, View view) {
      /*  FileModel fileModel = null;
        if (o == null && view == null)
            return;
        if (o instanceof FileModel) {
            fileModel = (FileModel) o;
        }
        if (fileModel != null) {
            showPopUpMenu(fileModel, view);
        }*/
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
                    fileModel.setPdfFileName(fileModel.getName());
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
                    fileModel.setPdfFileName(fileModel.getName());
                    handlePdfCreation(PDF_VIA_SHARE, fileModel);
                } else {
                    showAskPdfNameDialog(fileModel, PDF_VIA_SHARE);
                }

            }
        });

        ll_share_as_image.setOnClickListener(v -> {
            dialog.dismiss();
            File fileOrDirectory = new File(fileModel.getPath());
            ArrayList<Uri> uriList = new ArrayList<>();
            if (fileOrDirectory.isDirectory()) {
                File[] files = fileOrDirectory.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isFile() && file.exists()) {
                            if (!TextUtils.isEmpty(file.getName()) && file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                continue;
                            }
                        }
                        Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                        if (uriForFile != null) uriList.add(uriForFile);
                    }
                    if (!uriList.isEmpty()) {
                        shareMultiple(uriList);
                    } else {
                        showNoFileToShareDialog();
                    }
                } else {
                    showNoFileToShareDialog();
                }
            } else {
                Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", fileOrDirectory);
                if (uriForFile != null) uriList.add(uriForFile);
                if (!uriList.isEmpty()) {
                    shareMultiple(uriList);
                } else {
                    showNoFileToShareDialog();
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

    private void goToWaterMarkActivityForShareDoc() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT);
    }

   /* private void requestDeletePermission(List<Uri> uriList) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            PendingIntent pi = MediaStore.createDeleteRequest(context.getContentResolver(), uriList);

            try {
                startIntentSenderForResult(pi.getIntentSender(), REQUEST_PERM_DELETE, null, 0, 0,
                        0);
            } catch (IntentSender.SendIntentException e) {
            }
        }
    }*/

    private void createPdfForSharingSingleDocument(FileModel fileModel, boolean isWaterMarkToBeShown) {
        if (fileModel.isCompressedPdf()) {
            new GetTempCompressedBitmapPath(context, fileModel.getPath(), new CreateTempBitmapListener() {
                @Override
                public void onCompressingStart() {
                    progress_lay.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCompressingComplete(File compressedFile) {
                    progress_lay.setVisibility(View.GONE);
                    if (compressedFile != null) {
                        sharePdfFromDir(compressedFile.getPath(), fileModel, isWaterMarkToBeShown);
                    }
                }
            }).execute();
        } else {
            sharePdfFromDir(fileModel.getPath(), fileModel, isWaterMarkToBeShown);
        }

    }

    private void sharePdfFromDir(String path, FileModel fileModel, boolean isWaterMarkToBeShown) {
        File fileOrDirectory = new File(path);
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null && files.length > 0) {
                flashScanUtil.sortFilesByNameAtoZ(files);
                /*int appSortingOrder = prefManager.getAppSortingOrder();
                switch (appSortingOrder) {
                    case Constants.SORT_BY.defaultOrder:
                    case Constants.SORT_BY.modificationTimeDescending:
                        flashScanUtil.sortFilesByDescendingLastModified(files);
                        break;
                    case Constants.SORT_BY.modificationTimeAscending:
                        flashScanUtil.sortFilesByAscendingLastModified(files);
                        break;
                    case Constants.SORT_BY.nameAtoZ:
                        flashScanUtil.sortFilesByNameAtoZ(files);
                        break;
                    case Constants.SORT_BY.nameZtoA:
                        flashScanUtil.sortFilesByNameZtoA(files);
                        break;
                }*/
                List<String> filePathList = new ArrayList<>();
                for (File file : files) {
                    if (file.isFile() && file.exists()) {
                        if (file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                            continue;
                        }
                        filePathList.add(file.getPath());
                    }

                }
                if (!filePathList.isEmpty()) {
                    isPdfCreationForSharing = true;
                    createPdf(filePathList, fileModel.getPdfFileName(), isWaterMarkToBeShown);
                } else {
                    // show warning message
                    showNoFilesInDocumentDialog();
                }
            } else {
                showNoFilesInDocumentDialog();
            }
        }
    }

    private void shareMultiple(ArrayList<Uri> uriList) {
        if (uriList == null || uriList.isEmpty()) return;
        flashScanUtil.shareMultiple(uriList, this);
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
                    String folderName = et_pdf_name.getText().toString().trim();
                    if (TextUtils.isEmpty(folderName)) {
                        Toast.makeText(context, getString(R.string.please_name_file), Toast.LENGTH_SHORT).show();
                        return;
                    } else if (folderName.equalsIgnoreCase(fileModel.getName())) {
                        Toast.makeText(context, getString(R.string.file_name_same_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }
              /*      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                         extUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
                       // String relativeLocation = Environment.DIRECTORY_DOCUMENTS + File.separator + "AppFolder";

                        //File tempFile = new File(fileModel.getPath());
                        //Uri  Uri_one = Uri.fromFile(new File(tempFile.getPath()));
                       boolean isRenamed = renameFile(context,folderName ,fileModel.getName());
                        //Uri Uri_one = MediaStore.Files.getContentUri("external");
                       // Uri Uri_one = MediaStore.Files.getContentUri( MediaStore.);
                        //Uri Uri_one = getUri(tempFile.getPath());
                        //Uri  Uri_one = Uri.parse(fileModel.getPath());

                        //Uri  Uri_one = getImageContentUri(context, tempFile);
                        //Uri contentUri = FileProvider.getUriForFile(getContext(), "com.mydomain.fileprovider", newFile);
                        Log.e(TAG, "isRenamed "+isRenamed);
                        //Log.e(TAG, "Uri_one "+Uri_one);
                        //rename(Uri_one,folderName);
                    }
                    else{
                        renameFolder(folderName, fileModel);
                    }*/
                    renameFolder(folderName, fileModel);
                    dialog.dismiss();
                    break;
                case Constants.FileOperations.ACTION_DELETE:
                    /*File dir = new File(fileModel.getPath());*/
                    /*deleteRecursive(dir);*/

                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        List<Uri> uriPathList = new ArrayList<>();
                        if (fileModel != null) {

                            File file = new File(Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "FlashScanDocScanner"+File.separator+fileModel.getName());
                            //file.mkdirs();
                            if(file.isDirectory()) {
                                file.delete();
                               // deleteDir(file);
                            }
                            //deleteFileUsingDisplayName(context, fileModel.getName());
                        //    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"FlashScanDocScanner"+File.separator+fileModel.getName());
                            //File filePath = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+"FlashScanDocScanner"+File.separator+fileModel.getName());
                        //    Log.e(TAG, "filePath "+file);

                            //boolean isFileDeleted = delete(context, file);
                            //Log.e(TAG, "isFileDeleted "+isFileDeleted);
                            //File tempFile = new File(filePath.getPath());
                            //Uri uri_one = Uri.fromFile(tempFile);
                            //filePath.delete();
                            //boolean isFileDeleted =  deleteDir(filePath);
                            //Log.e(TAG, "isFileDeleted "+isFileDeleted);
                            //deleteDir(filePath);
                            //uriPathList.add(uri_one);
                            //requestDeletePermission(uriPathList);
                            //deletefile(uri_one, fileModel.getName());
                            //fileName = fileModel.getName();
                            //choosePath();

                            //deleteFileUsingDisplayName(context, fileModel.getName());
                        }
                    } else {
                        new DeleteFolderOrFileTask(context, fileModel.getPath(), this).execute();

                    }*/

                    new DeleteFolderOrFileTask(MainActivity.this, fileModel.getPath(), this).execute();
                    AppController.getINSTANCE().dbHandler.deleteApplyFilterFolder(fileModel.getName());
                    dialog.dismiss();
                    break;
            }

        });

        dialog.show();
    }

    private Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Log.e(TAG, "filePath " + filePath);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = context.getContentResolver();
                    Uri picCollection = MediaStore.Images.Media
                            .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    ContentValues picDetail = new ContentValues();
                    picDetail.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
                    picDetail.put(MediaStore.Images.Media.MIME_TYPE, "text/doc");
                    picDetail.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/" + UUID.randomUUID().toString());
                    picDetail.put(MediaStore.Images.Media.IS_PENDING, 1);
                    Uri finaluri = resolver.insert(picCollection, picDetail);
                    picDetail.clear();
                    picDetail.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(picCollection, picDetail, null, null);
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
                    flashScanUtil.saveFileInGoogleDrive(context, Constants.ROOT_FOLDER_NAME, fileModel, false, context.getResources().getString(R.string.updating_file_metadata), folderId -> {
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

    @Override
    public void onPdfCreationStarted() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPdfCreated(String savedPdfPath) {
        progress_lay.setVisibility(View.GONE);
        if (Constants.IS_SHOWING_CREATED_PDF_IN_OWN_APP) {
            if (!sharePdfDirectWithoutOpen) {
                Intent intent = new Intent(context, PdfEditorActivity.class);
                intent.putExtra(Constants.PutExtraConstants.SAVED_PDF_PATH, savedPdfPath);
                startActivity(intent);
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
            if (!isPdfCreationForSharing) {
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


        /*util.showSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_created_successfully));*/
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
        dialog.show();
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
    public void actionAddToDrive(Object o, int position) {
        if (!flashScanUtil.isConnectingToInternet()) {
            Toast.makeText(context, context.getResources().getString(R.string.connect_to_internet), Toast.LENGTH_LONG).show();
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
                TAG,
                com.itl.commonres.utils.Constants.CLICK_GOOGLE_DRIVE_ICON
        );
        checkDriveSignIn(fileModel, position);
    }

    @Override
    public void actionShare(Object o) {
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel != null)
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_SHARE_ICON
            );
        showShareDialog(fileModel);
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
        if (fileModel != null)
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_DELETE_ICON
            );
        showCommonDialog(fileModel, Constants.FileOperations.ACTION_DELETE);
    }

    @Override
    public void actionSaveAsPdf(Object o) {
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
                selectedFileModel = fileModel;
            }
        }
        CommonMethods.logCustomFireBaseEvents(
                TAG,
                com.itl.commonres.utils.Constants.CLICK_PDF_ICON_CREATE_PDF
        );
        /*if (!prefManager.isAppAdFree() && flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow) {
            // show(this);
            // load interstitial Ad for PDF creation
            Constants.isAppInBackground = false;
            loadInterstitialAd(context, BuildConfig.AD_UNIT_ID_PDF_ICON_INTERSTITIAL_AD, this);
        } else {
            Log.e(TAG, "PhoneMate : Ad Not Showing");
            createPDF();
        }*/
        //Log.e(TAG, "PhoneMate : Ad Not Showing");
        createPDF();
    }

    private void showAskPdfNameDialog(FileModel fileModel, int pdfVia) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_ask_pdf_name);

        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        Button btn_done = dialog.findViewById(R.id.btn_done);

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


        et_pdf_name.setText(fileModel.getName());
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

    /*private void showRewardedAdDialogForSingleDocument(FileModel fileModel) {
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
                        actionSavePdf(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        actionSavePdf(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        actionSavePdf(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(MainActivity.this, rewardedAdCallback);
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

    private void handlePdfCreation(int pdfVia, FileModel fileModel) {
        switch (pdfVia) {
            case PDF_BY_DIRECT:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelForWaterMark(fileModel);
                    goToWaterMarkActivityForSingleDoc();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        actionSavePdf(fileModel, false);
                    } else {
                        actionSavePdf(fileModel, true);
                    }

                }
                break;
            case PDF_VIA_SHARE:
                if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    setFileModelForWaterMark(fileModel);
                    goToWaterMarkActivityForShareDoc();
                } else {
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForSharingSingleDocument(fileModel, false);
                    } else {
                        createPdfForSharingSingleDocument(fileModel, true);
                    }

                }
                break;
        }
    }

    private void goToWaterMarkActivityForSingleDoc() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SINGLE_DOCUMENT);
    }

    private void actionSavePdf(FileModel fileModel, boolean isWaterMarkToBeShown) {
        if (fileModel.isCompressedPdf()) {
            new GetTempCompressedBitmapPath(context, fileModel.getPath(), new CreateTempBitmapListener() {
                @Override
                public void onCompressingStart() {
                    progress_lay.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCompressingComplete(File compressedFile) {
                    progress_lay.setVisibility(View.GONE);
                    if (compressedFile != null) {
                        createPdfFromDirectory(compressedFile.getPath(), fileModel, isWaterMarkToBeShown);
                    }
                }
            }).execute();
        } else {
            createPdfFromDirectory(fileModel.getPath(), fileModel, isWaterMarkToBeShown);
        }

    }

    private void createPdfFromDirectory(String path, FileModel fileModel, boolean isWaterMarkToBeShown) {
        File fileOrDirectory = new File(path);
        List<String> filePathList = new ArrayList<>();
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null && files.length > 0) {
                flashScanUtil.sortFilesByNameAtoZ(files);
                /*Collections.reverse(Arrays.asList(files));*/
                /*int appSortingOrder = prefManager.getAppSortingOrder();
                switch (appSortingOrder) {
                    case Constants.SORT_BY.defaultOrder:
                    case Constants.SORT_BY.modificationTimeDescending:
                        flashScanUtil.sortFilesByDescendingLastModified(files);
                        break;
                    case Constants.SORT_BY.modificationTimeAscending:
                        flashScanUtil.sortFilesByAscendingLastModified(files);
                        break;
                    case Constants.SORT_BY.nameAtoZ:
                        flashScanUtil.sortFilesByNameAtoZ(files);
                        break;
                    case Constants.SORT_BY.nameZtoA:
                        flashScanUtil.sortFilesByNameZtoA(files);
                        break;
                }*/
                for (File file : files) {
                    if (file.isFile() && file.exists()) {
                        if (file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                            continue;
                        }
                        filePathList.add(file.getPath());
                    }
                }

            }
            if (!filePathList.isEmpty()) {
                isPdfCreationForSharing = false;
                createPdf(filePathList, fileModel.getPdfFileName(), isWaterMarkToBeShown);
            } else {
                // show warning message
                showNoFilesInDocumentDialog();
            }
        }

    }

    @Override
    public void makeFavourite(Object o) {
        FileModel fileModel = null;
        if (o == null)
            return;
        if (o instanceof FileModel) {
            fileModel = (FileModel) o;
        }
        if (fileModel == null)
            return;

        // use fileModel object here for functionality
        Toast.makeText(context, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
        flashScanUtil.readUpdateCreateMetaDataJson(fileModel);
    }

    @Override
    public void removeFavourite(Object o) {
        FileModel fileModel = null;
        if (o == null)
            return;
        if (o instanceof FileModel) {
            fileModel = (FileModel) o;
        }
        if (fileModel == null)
            return;

        // use fileModel object here for functionality
        Toast.makeText(context, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
        flashScanUtil.readUpdateCreateMetaDataJson(fileModel);
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        if (fileModelAdapter != null) {
            fileModelAdapter.destroyAdapterNativeAd();
        }
        super.onDestroy();


    }

    public void rename(Uri uri, String rename) {

        //create content values with new name and update
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, rename);
        //context.getContentResolver().update(uri, contentValues, null);

    }

    public Uri getUri(String path) {
        Uri mediaUri = MediaStore.Files.getContentUri("external");
        Cursor ca = context.getContentResolver().query(mediaUri, new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?",
                new String[]{path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Files.getContentUri("external", id);
        }
        if (ca != null) {
            ca.close();
        }
        return null;
    }

    boolean renameFile(Context context, String newName, String displayName) {

        try {
            Long id = getIdFromDisplayName(displayName);
            ContentResolver contentResolver = context.getContentResolver();
            Uri mUri = ContentUris.withAppendedId(extUri, id);
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1);
            contentResolver.update(mUri, contentValues, null, null);

            contentValues.clear();
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, newName);
            // contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "files/pdf");
            // contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativeLocation);
            // contentValues.put(MediaStore.Files.FileColumns.TITLE, "SomeName");
            // contentValues.put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
            // contentValues.put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0);
            contentResolver.update(mUri, contentValues, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    boolean deleteFile(Context context, String newName, String displayName) {

        try {

            Long id = getIdFromDisplayName(displayName);
            ContentResolver contentResolver = context.getContentResolver();
            Uri mUri = ContentUris.withAppendedId(extUri, id);
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1);
            contentResolver.delete(mUri, null, null);

            contentValues.clear();
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, newName);
            // contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "files/pdf");
            // contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativeLocation);
            // contentValues.put(MediaStore.Files.FileColumns.TITLE, "SomeName");
            // contentValues.put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
            // contentValues.put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0);
            contentResolver.delete(mUri, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    Long getIdFromDisplayName(String displayName) {
        String[] projection;
        projection = new String[]{MediaStore.Files.FileColumns._ID};

        // TODO This will break if we have no matching item in the MediaStore.
        Cursor cursor = getContentResolver().query(extUri, projection,
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?", new String[]{displayName}, null);
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

    private void choosePath() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(intent, LOCATION_REQUEST);
    }

    private void deletefile(Uri uri, String filename) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        DocumentFile file = pickedDir.findFile(filename);
        if (file.delete())
            Log.d("Log ID", "Delete successful");
        else
            Log.d("Log ID", "Delete unsuccessful");
    }

    private void callNativeAd(FrameLayout nativeSmallAdNoDoc) {
        if (AppController.nativeAdDoc == null) {
            AdLoader customEventNativeLoader = new AdLoader.Builder(context,
                    BuildConfig.AD_UNIT_ID_DOC_SCREEN_EMPTY_LIST_NATIVE_AD)
                    .forNativeAd(nativeAd -> {
                        Log.e("HOME_NO_DOC_NATIVE_AD ", "onUnifiedNativeAdLoaded G `> " + "");
                        AppController.nativeAdDoc = nativeAd;
                        smallDocNativeAdSet(nativeAd, nativeSmallAdNoDoc, false);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.e("HOME_NO_DOC_NATIVE_AD ", "onAdFailedToLoad G > " + loadAdError.getMessage());
                        }
                    }).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                            .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build())
                    .build();
            customEventNativeLoader.loadAd(new AdRequest.Builder().build());
        } else {
            smallDocNativeAdSet(AppController.nativeAdDoc, nativeSmallAdNoDoc, false);
        }
    }

    @Override
    public void onAdClosed() {
        createPDF();
    }

    @Override
    public void onAdLoadedOrFailed(boolean isLoaded) {
        createPDF();
    }

    private void createPDF() {
        if (selectedFileModel != null) {
            if (Constants.IS_CREATE_PDF_DIRECT) {
                selectedFileModel.setPdfFileName(selectedFileModel.getName());
                handlePdfCreation(PDF_BY_DIRECT, selectedFileModel);
            } else {
                showAskPdfNameDialog(selectedFileModel, PDF_BY_DIRECT);
            }

        }
    }

    private void checkDriveSignIn(FileModel fileModel, int position) {
        if (flashScanUtil.isDriveSignedIn()) {
            Log.e(TAG, "Drive signed in " + flashScanUtil.isDriveSignedIn());
            if (fileModel.isSavedOnGoogleDrive()) {
                //showDeleteDialogGoogleDrive(fileModel);
                Toast.makeText(context, context.getResources().getString(R.string.doc_already_synced), Toast.LENGTH_LONG).show();
            } else {
                if (fileModel.getSize() > 0) {
                    flashScanUtil.saveFileInGoogleDrive(context, Constants.ROOT_FOLDER_NAME, fileModel, false, context.getResources().getString(R.string.uploading_files_to_drive), folderId -> {
                        fileModel.setSavedOnGoogleDrive(true);
                        fileModel.setGoogleDriveFolderId(folderId);
                        fileModelAdapter.notifyItemChanged(position);
                    });
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.empty_folder), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil.isDriveSignedIn());
            progress_lay.setVisibility(View.VISIBLE);
            mFileModelForSaveToDrive = fileModel;
            positionForSaveToDrive = position;
            startActivityForResult((flashScanUtil.requestSignIn(context)).getSignInIntent(), REQUEST_CODE_DRIVE_SIGN_IN);
        }

    }

    private void showDeleteDialogGoogleDrive(FileModel fileModel) {
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
        msgHeading.setText(context.getResources().getString(R.string.delete_from_drive));
        btn_cancel.setText(R.string.keep_it);
        btn_ok.setText(R.string.yes_btn_dialog);

        btn_cancel.setOnClickListener(v -> dialog.dismiss());

        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
            deleteFromGoogleDriveById(fileModel);
        });
        dialog.show();
    }

    private void deleteFromGoogleDriveById(FileModel fileModel) {
        if (flashScanUtil.isDriveSignedIn()) {
            Log.e(TAG, "Drive signed in " + flashScanUtil.isDriveSignedIn());
            flashScanUtil.deleteFolderByIdFromGoogleDrive(context, fileModel.getGoogleDriveFolderId(), context.getResources().getString(R.string.delete_files_from_drive), () -> {
                prefManager.deleteFolderFromGoogleDriveDataList(fileModel.getGoogleDriveFolderId());
                fileModel.setSavedOnGoogleDrive(false);
                fileModel.setGoogleDriveFolderId("");
                fetchFiles();
            });
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil.isDriveSignedIn());
            progress_lay.setVisibility(View.VISIBLE);
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
            startActivityForResult((flashScanUtil.requestSignIn(context)).getSignInIntent(), REQUEST_CODE_DRIVE_SIGN_IN);
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
            fetchFiles();
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
