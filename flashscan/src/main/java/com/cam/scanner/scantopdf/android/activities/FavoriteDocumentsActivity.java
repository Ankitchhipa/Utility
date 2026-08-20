package com.cam.scanner.scantopdf.android.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.SingleTon.PdfSettings;
import com.cam.scanner.scantopdf.android.adapters.FileModelAdapter;
import com.cam.scanner.scantopdf.android.adapters.PageSizesAdapter;
import com.cam.scanner.scantopdf.android.asynctasks.CreatePdfTask;
import com.cam.scanner.scantopdf.android.asynctasks.DeleteFolderOrFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetFilesTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetTempCompressBitmapFolders;
import com.cam.scanner.scantopdf.android.asynctasks.GetTempCompressedBitmapPath;
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
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.ScanConstants;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.itl.commonres.utils.CommonMethods;
import com.itl.commonres.utils.PermissionInterface;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDocumentsActivity extends AppCompatActivity implements View.OnClickListener, OnFetchingCompleted,
        OnItemSelectListener, FileOperationListener, PDFCreationCallback, FileOrFolderDeleteListener, PermissionInterface {

    private static final int REQUEST_CODE_FOR_SINGLE_DOCUMENT = 101;
    private static final int REQUEST_CODE_FOR_MULTIPLE_DOCUMENT = 102;
    private static final int REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT = 103;
    private static final int REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS = 104;
    private static final int REQUEST_CODE_FOR_ALL_DOCUMENTS = 301;
    private static final int PDF_VIA_SHARE = 1;
    private static final int PDF_BY_DIRECT = 2;
    private ImageView iv_home, iv_search, iv_more_menu;
    private TextView toolbar_title, tv_save_as_pdf, tv_share, tv_delete, tv_no_file, tv_total_file_count, tv_select_all_files;
    private EditText et_search;
    private Context context;
    private RecyclerView rv_favorite_documents;
    private FileModelAdapter fileModelAdapter;
    private LinearLayout ll_bottom_bar, ll_no_document, ll_select_all_files;
    private RelativeLayout progress_lay;
    private boolean isPdfCreationForSharing;
    private FlashScanUtil flashScanUtil;
    private Button btn_progress_lay;
    private PrefManager prefManager;
    private RewardedAd rewardedAd;
    private static final String TAG = FavoriteDocumentsActivity.class.getSimpleName();
    private List<FileModel> favoritesDocsList;

    /*private List<FileModel> fileModelListGlobal;*/
    private long lastClickedTime = 0;
    private int selectionAction = -1;
    private boolean isMultiplePdfCreationWithCompression = false;
    private String selectedPageSize;

    public List<FileModel> getFetchedFilesList() {
        if (fetchedFilesList == null) {
            fetchedFilesList = new ArrayList<>();
        }
        return fetchedFilesList;
    }

    private List<FileModel> fetchedFilesList = new ArrayList<>();

    public FileModel getFileModelForWaterMark() {
        return fileModelForWaterMark;
    }

    public void setFileModelForWaterMark(FileModel fileModelForWaterMark) {
        this.fileModelForWaterMark = fileModelForWaterMark;
    }

    private FileModel fileModelForWaterMark;

    public List<FileModel> getFileModelListForWaterMark() {
        if (fileModelListForWaterMark == null) {
            fileModelListForWaterMark = new ArrayList<>();
        }
        return fileModelListForWaterMark;
    }

    public void setFileModelListForWaterMark(List<FileModel> fileModelListForWaterMark) {
        if (!getFileModelListForWaterMark().isEmpty()) {
            getFileModelListForWaterMark().clear();
        }
        getFileModelListForWaterMark().addAll(fileModelListForWaterMark);
        /*this.fileModelListForWaterMark = fileModelListForWaterMark;*/
    }

    private List<FileModel> fileModelListForWaterMark = new ArrayList<>();

    public String getPdfFileNameForMultipleDocs() {
        if (TextUtils.isEmpty(pdfFileNameForMultipleDocs)) {
            pdfFileNameForMultipleDocs = flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name);
        }
        return pdfFileNameForMultipleDocs;
    }

    public void setPdfFileNameForMultipleDocs(String pdfFileNameForMultipleDocs) {
        this.pdfFileNameForMultipleDocs = pdfFileNameForMultipleDocs;
    }

    private String pdfFileNameForMultipleDocs;
    private boolean sharePdfDirectWithoutOpen = false;

    private List<FileModel> totalDocListIncludingAds = new ArrayList<>();

    private List<FileModel> getDocumentsListIncludingAds() {
        if (totalDocListIncludingAds == null) {
            totalDocListIncludingAds = new ArrayList<>();
        }
        return totalDocListIncludingAds;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_documents);

        findViewIds();
        setClickListeners();
        initObjects();
        fetchFiles();
        manageSearchedFolders();
        prefManager.saveFoldersSortingOrder(prefManager.getAppSortingOrder());
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

        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void filter(String searchedText) {
        List<FileModel> filterFileList = new ArrayList<>();
        if (getFetchedFilesList() != null && !getFetchedFilesList().isEmpty()) {
            rv_favorite_documents.setVisibility(View.VISIBLE);
            ll_no_document.setVisibility(View.GONE);
            for (FileModel fileModel : getFetchedFilesList()) {
                if (fileModel.getName().toLowerCase().contains(searchedText.toLowerCase())) {
                    filterFileList.add(fileModel);
                }
            }
            if (fileModelAdapter != null && !filterFileList.isEmpty()) {
                rv_favorite_documents.setVisibility(View.VISIBLE);
                tv_no_file.setVisibility(View.GONE);
                fileModelAdapter.filterList(filterFileList);
            } else {
                rv_favorite_documents.setVisibility(View.GONE);
                tv_no_file.setVisibility(View.VISIBLE);
            }
        } else {
            rv_favorite_documents.setVisibility(View.GONE);
            ll_no_document.setVisibility(View.VISIBLE);
        }
    }

    private void fetchFiles() {
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                prefManager.getAppSortingOrder()).execute();
    }

    private void initObjects() {
        context = this;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_favorite_documents.setLayoutManager(linearLayoutManager);
        rv_favorite_documents.setHasFixedSize(true);
        flashScanUtil = new FlashScanUtil(context);
        prefManager = new PrefManager(context);
       /* if (!prefManager.isAppRewardAdFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
            loadRewardedAd();
        }*/
    }

   /* private void loadRewardedAd() {
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

    private void setClickListeners() {
        iv_home.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        tv_save_as_pdf.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
        iv_more_menu.setOnClickListener(this);
        tv_select_all_files.setOnClickListener(this);
    }

    private void findViewIds() {
        iv_home = findViewById(R.id.iv_home);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.my_favourites));
        iv_search = findViewById(R.id.iv_search);
        et_search = findViewById(R.id.et_search);
        rv_favorite_documents = findViewById(R.id.rv_favorite_documents);
        ll_bottom_bar = findViewById(R.id.ll_bottom_bar);
        progress_lay = findViewById(R.id.progress_lay);
        tv_delete = findViewById(R.id.tv_delete);
        tv_share = findViewById(R.id.tv_share);
        tv_save_as_pdf = findViewById(R.id.tv_save_as_pdf);
        ll_no_document = findViewById(R.id.ll_no_document);
        tv_no_file = findViewById(R.id.tv_no_file);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        iv_more_menu = findViewById(R.id.iv_more_menu);
        iv_more_menu.setVisibility(View.VISIBLE);
        ll_select_all_files = findViewById(R.id.ll_select_all_files);
        tv_total_file_count = findViewById(R.id.tv_total_file_count);
        tv_select_all_files = findViewById(R.id.tv_select_all_files);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_home) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                return;
            }
            lastClickedTime = SystemClock.elapsedRealtime();
            goToHome();
        } else if (id == R.id.iv_search) {
            handleSearchBarVisibility();
        } else if (id == R.id.tv_save_as_pdf) {
            List<FileModel> selectedFileModelList = null;
            if (fileModelAdapter != null) {
                selectedFileModelList = fileModelAdapter.getSelectedFileModelList();
            }
            if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {

                if (Constants.IS_CREATE_PDF_DIRECT) {
                    setPdfFileNameForMultipleDocs(flashScanUtil.getFileDateFormatName() + "_" + getString(R.string.suffix_app_name));
                    handleMultipleDocPdfCreation(selectedFileModelList, PDF_BY_DIRECT);
                    hideCheckBoxAndRemoveBottomBar();
                } else {
                    showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_BY_DIRECT);
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
            /*hideCheckBoxAndRemoveBottomBar();*/
        } else if (id == R.id.tv_delete) {
            List<FileModel> selectedFileModelList1 = null;
            if (fileModelAdapter != null) {
                selectedFileModelList1 = fileModelAdapter.getSelectedFileModelList();
            }
            if (selectedFileModelList1 != null && !selectedFileModelList1.isEmpty()) {
                showDeleteDialog(selectedFileModelList1);
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_files));
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
                } else if (rbCompressed.isChecked()) {
                    isMultiplePdfCreationWithCompression = true;
                }
                handleMultipleDocPdfCreation(selectedFileModelList, pdfVia);
                hideCheckBoxAndRemoveBottomBar();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void handleMultipleDocPdfCreation(List<FileModel> selectedFileModelList, int pdfVia) {
        switch (pdfVia) {
            case PDF_BY_DIRECT:
                if (isStoragePermissionGranted()) {
                    if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                        setFileModelListForWaterMark(selectedFileModelList);
                        goToWaterMarkRemoveActivityForMultipleDocuments();
                    } else {
                        if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                            saveAsPdfSelectedDocuments(selectedFileModelList, false);
                        } else {
                            saveAsPdfSelectedDocuments(selectedFileModelList, true);
                        }

                    }
                }
                break;
            case PDF_VIA_SHARE:
                if (isStoragePermissionGranted()) {
                    if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                        setFileModelListForWaterMark(selectedFileModelList);
                        goToWaterMarkRemoveActivityForShareMultipleDocuments();
                    } else {
                        if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                            createPdfForShareSelectedDocuments(selectedFileModelList, false);
                        } else {
                            createPdfForShareSelectedDocuments(selectedFileModelList, true);
                        }

                    }
                }
                break;
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

    private void goToWaterMarkRemoveActivityForMultipleDocuments() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_MULTIPLE_DOCUMENT);
    }

    /*private void showRewardAdDialogForSelectedDocuments(List<FileModel> selectedFileModelList) {
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
                rewardedAd.show(FavoriteDocumentsActivity.this, rewardedAdCallback);
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

    private void manageSelectAllText() {
        if (favoritesDocsList == null || favoritesDocsList.isEmpty())
            return;
        if (fileModelAdapter != null && fileModelAdapter.getSelectedFileModelList().size() == favoritesDocsList.size()) {
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

    private void fetchDocumentsByModificationTimeAscending() {
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS,
                Constants.SORT_BY.modificationTimeAscending).execute();
    }

    private void showDeleteDialog(List<FileModel> selectedFileModelList1) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextView msgHeading = dialog.findViewById(R.id.msg_heading);
        TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);
        TextView btn_ok = dialog.findViewById(R.id.btn_ok);
        btn_cancel.setText(R.string.keep_it);
        btn_ok.setText(R.string.yes_btn_dialog);
        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        et_pdf_name.setVisibility(View.GONE);

        dialogTitle.setText(getString(R.string.delete));
        msgHeading.setText(getString(R.string.delete_msg));


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
            for (FileModel fileModel : selectedFileModelList1) {
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

    private void goToWaterMarkRemoveActivityForShareMultipleDocuments() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS);
    }

    /*private void showRewardAdDialogForShareSelectedDocuments(List<FileModel> selectedFileModelList) {
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
                        createPdfForShareSelectedDocuments(selectedFileModelList, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForShareSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForShareSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(FavoriteDocumentsActivity.this, rewardedAdCallback);
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

    private void createPdfForShareSelectedDocuments(List<FileModel> selectedFileModelList, boolean isWaterMarkToBeShown) {
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
                                        int appSortingOrder = prefManager.getAppSortingOrder();
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
                                        }
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
                                // not in use  now
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
                            int appSortingOrder = prefManager.getAppSortingOrder();
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
                            }
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
                                        int appSortingOrder = prefManager.getAppSortingOrder();
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
                                        }
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
                            int appSortingOrder = prefManager.getAppSortingOrder();
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
                            }
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
                // show warning dialog
                showNoFilesInDocumentDialog();
            }
        }

    }

    private void hideCheckBoxAndRemoveBottomBar() {
        fileModelAdapter.hideAllCheckBoxes();
        ll_bottom_bar.setVisibility(View.GONE);
        iv_more_menu.setVisibility(View.VISIBLE);
        ll_select_all_files.setVisibility(View.GONE);
    }

    private void handleSearchBarVisibility() {
        if (toolbar_title.getVisibility() == View.VISIBLE) {
            toolbar_title.setVisibility(View.GONE);
            et_search.setVisibility(View.VISIBLE);
            iv_search.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_white));
            showKeyboard();
        } else {
            clearSearchView();
        }
    }

    private void clearSearchView() {
        toolbar_title.setVisibility(View.VISIBLE);
        et_search.setText("");
        et_search.setVisibility(View.GONE);
        iv_search.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_search));
        hideKeyboard();
        if (fileModelAdapter != null) {
            fileModelAdapter.clearFilterList(getDocumentsListIncludingAds());
        }
    }

    private void hideKeyboard() {

    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void onFetchingComplete(List<FileModel> fileModelList) {
        progress_lay.setVisibility(View.GONE);
        if (fileModelList != null && !fileModelList.isEmpty()) {

            /*fileModelListGlobal = fileModelList;*/

            if (!getFetchedFilesList().isEmpty()) {
                getFetchedFilesList().clear();
            }
            getFetchedFilesList().addAll(fileModelList);
            setUpRecyclerView();
        }
    }

    @Override
    public void onFetchingStart() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    private void setUpRecyclerView() {


        favoritesDocsList = new ArrayList<>();
        /*favoritesDocsList.clear();*/

        for (FileModel fileModel : getFetchedFilesList()) {
            if (fileModel.isStarred()) {
                favoritesDocsList.add(fileModel);
            }
        }

        if (favoritesDocsList.isEmpty()) {
            Toast.makeText(context, getString(R.string.no_favorite_documents), Toast.LENGTH_LONG).show();
            onBackPressed();
            return;
        }
        // for showing ad
        List<FileModel> finalFileModelList = new ArrayList<>();

        boolean showNative = AppController.getINSTANCE().dbHandler.showNative();

        /*if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
                && Constants.SHOW_NATIVE_ADS.FOR_FAVORITES_DOCUMENTS_ACTIVITY) {*/
        if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
                && showNative) {
            if (favoritesDocsList.size() <= Constants.AD_PER_ITEM) {
                finalFileModelList.addAll(favoritesDocsList);
                FileModel fileModel = new FileModel();
                fileModel.setAdView(true);
                finalFileModelList.add(fileModel);
            } else {
                for (int i = 0; i < favoritesDocsList.size(); i++) {
                    if (Constants.AdAfterItems.FOR_MULTIPLE_ITEMS) {
                        if (i != 0 && i % Constants.AD_PER_ITEM == 0) {
                            FileModel fileModel = new FileModel();
                            fileModel.setAdView(true);
                            finalFileModelList.add(fileModel);
                        }
                        finalFileModelList.add(favoritesDocsList.get(i));
                    } else if (Constants.AdAfterItems.FOR_SINGLE_ITEM) {
                        if (i == Constants.AD_PER_ITEM) {
                            FileModel fileModel = new FileModel();
                            fileModel.setAdView(true);
                            finalFileModelList.add(fileModel);
                        }
                        finalFileModelList.add(favoritesDocsList.get(i));
                    }
                }
            }
        } else {
            finalFileModelList.addAll(favoritesDocsList);
        }
        //==============

        if (!getDocumentsListIncludingAds().isEmpty()) {
            getDocumentsListIncludingAds().clear();
        }
        getDocumentsListIncludingAds().addAll(finalFileModelList);
        fileModelAdapter = new FileModelAdapter(context, finalFileModelList, this, this);
        rv_favorite_documents.setAdapter(fileModelAdapter);
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
            intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_FAVORITES_SCREEN);
            intent.putExtra(ScanConstants.PutExtraConstants.FOLDER_NAME, fileModel.getName());
            intent.putExtra(ScanConstants.PutExtraConstants.DATE_TAKEN, fileModel.getDateTaken());
            intent.putExtra(ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE, fileModel.isSavedOnGoogleDrive());
            intent.putExtra(ScanConstants.PutExtraConstants.GOOGLE_DRIVE_FOLDER_ID, fileModel.getGoogleDriveFolderId());
            startActivityForResult(intent, REQUEST_CODE_FOR_ALL_DOCUMENTS);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }

    }

    @Override
    public void onItemLongPress(Object o) {
        if (fileModelAdapter != null && fileModelAdapter.isVisibleAllCheckbox()) {
            ll_bottom_bar.setVisibility(View.VISIBLE);
            iv_more_menu.setVisibility(View.GONE);
            ll_select_all_files.setVisibility(View.VISIBLE);
            tv_total_file_count.setText(fileModelAdapter.getSelectedFileModelList().size() + " " + getString(R.string.selected));
            manageSelectAllText();
        }
    }

    @Override
    public void onItemAction(Object o, View view) {

    }

    @Override
    public void actionAddToDrive(Object o, int position) {

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
            showShareDialog(fileModel);
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

    private void goToWaterMarkRemoveActivityForShareSingleDocument() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT);
    }

    /*private void showRewardAdDialogForShareSingleDocument(FileModel fileModel) {
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
                        createPdfForShareSingleDocument(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForShareSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForShareSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(FavoriteDocumentsActivity.this, rewardedAdCallback);
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

    private void createPdfForShareSingleDocument(FileModel fileModel, boolean isWaterMarkToBeShown) {
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
                        createPdfForShareFromDirPath(compressedFile.getPath(), fileModel, isWaterMarkToBeShown);
                    }
                }
            }).execute();
        } else {
            createPdfForShareFromDirPath(fileModel.getPath(), fileModel, isWaterMarkToBeShown);
        }

    }

    private void createPdfForShareFromDirPath(String path, FileModel fileModel, boolean isWaterMarkToBeShown) {
        File fileOrDirectory = new File(path);
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null && files.length > 0) {
                int appSortingOrder = prefManager.getAppSortingOrder();
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
                }
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

    private void shareMultiple(ArrayList<Uri> uriList) {
        if (uriList == null || uriList.isEmpty()) return;
        flashScanUtil.shareMultiple(uriList, context);
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
                    renameFolder(folderName, fileModel);
                    dialog.dismiss();
                    break;
                case Constants.FileOperations.ACTION_DELETE:
                    /*File dir = new File(fileModel.getPath());*/
                    /*deleteRecursive(dir);*/
                    new DeleteFolderOrFileTask(FavoriteDocumentsActivity.this, fileModel.getPath(), this).execute();
                    AppController.getINSTANCE().dbHandler.deleteApplyFilterFolder(fileModel.getName());
                    dialog.dismiss();
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

            fetchFiles();
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.rename_success_msg));
        } else {
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.something_went_wrong));
        }

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
            showCommonDialog(fileModel, Constants.FileOperations.ACTION_DELETE);
    }

    @Override
    public void actionSaveAsPdf(Object o) {  //     pdf for single document
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel != null) {

            // show ask pdf name dialog
            if (Constants.IS_CREATE_PDF_DIRECT) {
                fileModel.setPdfFileName(fileModel.getName());
                handlePdfCreation(PDF_BY_DIRECT, fileModel);
            } else {
                showAskPdfNameDialog(fileModel, PDF_BY_DIRECT);
            }

        }
    }

    private void showAskPdfNameDialog(FileModel fileModel, int pdfVia) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_ask_pdf_name);

        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        et_pdf_name.setText(fileModel.getName());

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
                if (isStoragePermissionGranted()) {
                    if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                        setFileModelForWaterMark(fileModel);
                        goToWaterMarkRemoveActivityForSingleDocument();
                    } else {
                        if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                            createPdfForSingleDocument(fileModel, false);
                        } else {
                            createPdfForSingleDocument(fileModel, true);
                        }
                    }
                }
                break;
            case PDF_VIA_SHARE:
                if (isStoragePermissionGranted()) {
                    if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                        setFileModelForWaterMark(fileModel);
                        goToWaterMarkRemoveActivityForShareSingleDocument();
                    } else {
                        if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly()/* || prefManager.isPremiumQuarterly()*/) {
                            createPdfForShareSingleDocument(fileModel, false);
                        } else {
                            createPdfForShareSingleDocument(fileModel, true);
                        }

                    }
                }
                break;
        }
    }

    private void goToWaterMarkRemoveActivityForSingleDocument() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_SINGLE_DOCUMENT);
    }

    /*private void showRewardAdDialogForSingleDocument(FileModel fileModel) {
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
                        createPdfForSingleDocument(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(FavoriteDocumentsActivity.this, rewardedAdCallback);
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

    private void createPdfForSingleDocument(FileModel fileModel, boolean isWaterMarkToBeShown) {
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
                int appSortingOrder = prefManager.getAppSortingOrder();
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
                }
                for (File file : files) {
                    if (file.isFile() && file.exists()) {
                        if (file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                            continue;
                        }
                        filePathList.add(file.getPath());
                    }

                }
                if (!filePathList.isEmpty()) {
                    isPdfCreationForSharing = false;
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
        Toast.makeText(context, "" + getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
        flashScanUtil.readUpdateCreateMetaDataJson(fileModel);
        setUpRecyclerView();
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
        setUpRecyclerView();
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
    protected void onStop() {
        super.onStop();
        hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        if (fileModelAdapter != null && fileModelAdapter.isVisibleAllCheckbox()) {
            hideCheckBoxAndRemoveBottomBar();
        } else if (et_search.getVisibility() == View.VISIBLE) {
            clearSearchView();
        } else {
            goToHome();
        }
    }

    private void goToHome() {
        // TODO: 29-06-2020 commented for avoiding reload ad on HomeActivity
        if (Constants.ALWAYS_RELOAD_AD_ON_HOME_SCREEN) {
            Intent intent = new Intent(context, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_FOR_SINGLE_DOCUMENT:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForSingleDocument(getFileModelForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForSingleDocument(getFileModelForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        Log.i(TAG, "RESULT_AD_CANCELLED called");
                        if (getFileModelForWaterMark() != null) {
                            createPdfForSingleDocument(getFileModelForWaterMark(), true);
                        }
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
                        Log.i(TAG, "RESULT_AD_CANCELLED called");
                        if (getFileModelListForWaterMark() != null) {
                            saveAsPdfSelectedDocuments(getFileModelListForWaterMark(), true);
                        }
                        break;

                }
                break;
            case REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForShareSingleDocument(getFileModelForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelForWaterMark() != null) {
                            createPdfForShareSingleDocument(getFileModelForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        Log.i(TAG, "RESULT_AD_CANCELLED called");
                        if (getFileModelForWaterMark() != null) {
                            createPdfForShareSingleDocument(getFileModelForWaterMark(), true);
                        }
                        break;
                }
                break;
            case REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS:
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForShareSelectedDocuments(getFileModelListForWaterMark(), false);
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK:
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForShareSelectedDocuments(getFileModelListForWaterMark(), false);
                            Toast.makeText(context, "" + getString(R.string.water_mark_free_success_msg, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED:
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE:
                        Log.i(TAG, "RESULT_AD_CANCELLED called");
                        if (getFileModelListForWaterMark() != null) {
                            createPdfForShareSelectedDocuments(getFileModelListForWaterMark(), true);
                        }
                        break;
                }
                break;
            case REQUEST_CODE_FOR_ALL_DOCUMENTS:
                fetchFiles();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (fileModelAdapter != null) {
            fileModelAdapter.destroyAdapterNativeAd();
        }
        super.onDestroy();
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return true;
            } else {
                new CommonMethods(this).showPermissionDialog("All_Files_Access", this, false);
                return false;
            }
        } else {
            return true;
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
