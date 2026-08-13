package com.cam.scanner.scantopdf.android.pdf;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.AskEmailActivity;
import com.cam.scanner.scantopdf.android.activities.BaseActivity;
import com.cam.scanner.scantopdf.android.activities.PremiumActivity;
import com.cam.scanner.scantopdf.android.activities.WaterMarkRemoveActivity;
import com.cam.scanner.scantopdf.android.ads.AdClosed;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.CreatePdfTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetFilesTask;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.OnFetchingCompleted;
import com.cam.scanner.scantopdf.android.interfaces.PDFCreationCallback;
import com.cam.scanner.scantopdf.android.interfaces.PdfToImageCallback;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.models.ImageToPdfOptions;
import com.cam.scanner.scantopdf.android.models.WaterMark;
import com.cam.scanner.scantopdf.android.util.BaseColor;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.itl.commonres.utils.CommonMethods;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PdfEditorActivity extends BaseActivity implements OnPageChangeListener, View.OnClickListener, AdClosed, OnFetchingCompleted {

    private static final String TAG = PdfEditorActivity.class.getSimpleName();
    private static final int REQUEST_CODE_FOR_REMOVE_WATERMARK = 101;
    private static final int UPDATE_PDF_WITH_NO_WATERMARK = 1;
    private static final int UPDATE_PDF_WITH_EDITED_COUNTERFEIT = 2;
    private static final int UPDATE_PDF_WITH_NO_COUNTERFEIT = 3;
    private static final int REQUEST_CODE_FOR_PDF_SIGNATURE = 102;
    private static final int UPDATE_PDF_WITH_SIGNATURE = 4;
    private int selectedPdfCompressionSize = Constants.COMPRESS_PDF_QUALITY_REGULAR;
    private boolean isPdfCompressed = false;
    private boolean showCreatePdfMsg = false;
    private PDFView pdfView;
    private ImageView iv_back_toolbar, iv_share, ivPremium, iv_rename;
    private TextView tv_toolbar, tv_pdf_page_count, tv_tut_ok, btn_got_it;
    private FrameLayout tv_pdf_password, tv_pdf_compress, tv_anti_counterfeit, tv_pdf_signature;
    private FlashScanUtil flashScanUtil;
    private Context context;
    private String savedPdfPath;
    private String originalSavedPdfPath;
    private PdfUtils pdfUtils;
    private PrefManager prefManager;
    private View progress_lay;

    private String[] mInputPassword;
    private TextColorAdapter textColorAdapter = null;
    private boolean isEncryptPdfAfterCreating = false;
    private boolean antiCounterFeitAlreadyApplied = false;

    private ImageView ivCrownPwd, ivCrownSign, ivCrownCounterfeit;
    private WaterMark editedCounterfeitWatermark = null;
    private View pdf_editor_tutorial_view;
    private boolean isAdVideoAlreadyWatched = false;
    private FrameLayout flMain;
    private DBHandler dbHandler;
    private boolean isAdShowedForWatermark = false;
    private boolean isAdShowedForSignature = false;
    private boolean isAdShowedForPassword = false;
    private int featureClickedType = -1;
    private String folderName = "";

    public WaterMark getEditedCounterfeitWatermark() {
        return editedCounterfeitWatermark;
    }

    public void setEditedCounterfeitWatermark(WaterMark editedCounterfeitWatermark) {
        this.editedCounterfeitWatermark = editedCounterfeitWatermark;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_editor);

        initObjects();
        findIds();
        setClickListeners();
        handlePremiumIconVisibility();
        getSavedPdfPath();
        handleTutorialView();

        //pdfCreationSuccessfulMsg();
    }

    private void pdfCreationSuccessfulMsg() {

        showPdfCreatedDialog();
        /*Toast toast = Toast.makeText(getApplicationContext(),
                getString(R.string.pdf_created_successfully), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();*/
    }

    private void showPdfCreatedDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog_yes_no);

        TextView tv_dialog_title = dialog.findViewById(R.id.tv_dialog_title);
        tv_dialog_title.setText(getString(R.string.pdf_created_successfully));

        TextView msg_heading = dialog.findViewById(R.id.msg_heading);
        msg_heading.setText(getString(R.string.continue_editing));

        TextView btn_ok = dialog.findViewById(R.id.btn_ok);
        btn_ok.setText(context.getResources().getString(R.string.btn_continue));

        TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setVisibility(View.GONE);


        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void handleTutorialView() {
        if (prefManager.isPdfEditorTutorialWatched()) {
            pdf_editor_tutorial_view.setVisibility(View.GONE);
        } else {
            pdf_editor_tutorial_view.setVisibility(View.VISIBLE);
            prefManager.setPdfEditorTutorialWatched(true);
        }
    }

    private void handlePremiumIconVisibility() {
//        if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly()) {
        if (/*prefManager.isPremiumYearly() || prefManager.isPremiumQuarterly()*/true) {
            ivPremium.setVisibility(View.GONE);
            ivCrownSign.setVisibility(View.GONE);
            ivCrownPwd.setVisibility(View.GONE);
            ivCrownCounterfeit.setVisibility(View.GONE);
        } else {
            ivPremium.setVisibility(View.VISIBLE);
            ivCrownSign.setVisibility(View.VISIBLE);
            ivCrownPwd.setVisibility(View.VISIBLE);
            ivCrownCounterfeit.setVisibility(View.VISIBLE);
        }
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        pdfUtils = new PdfUtils();
        prefManager = new PrefManager(context);
        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        iv_rename.setOnClickListener(this);
        tv_pdf_password.setOnClickListener(this);
        tv_pdf_compress.setOnClickListener(this);
        ivPremium.setOnClickListener(this);
        tv_anti_counterfeit.setOnClickListener(this);
        tv_pdf_signature.setOnClickListener(this);
        btn_got_it.setOnClickListener(this);
        pdf_editor_tutorial_view.setOnClickListener(this);
    }

    private void findIds() {
        pdfView = findViewById(R.id.pdfView);
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_pdf_page_count = findViewById(R.id.tv_pdf_page_count);
        iv_share = findViewById(R.id.iv_share);
        iv_share.setVisibility(View.VISIBLE);
        iv_rename = findViewById(R.id.iv_rename);
        iv_rename.setVisibility(View.VISIBLE);
        tv_pdf_password = findViewById(R.id.tv_pdf_password);
        tv_pdf_compress = findViewById(R.id.tv_pdf_compress);
        progress_lay = findViewById(R.id.progress_lay);
        ivPremium = findViewById(R.id.iv_premium);
        tv_anti_counterfeit = findViewById(R.id.tv_anti_counterfeit);
        tv_pdf_signature = findViewById(R.id.tv_pdf_signature);
        pdf_editor_tutorial_view = findViewById(R.id.pdf_editor_tutorial_view);
        btn_got_it = findViewById(R.id.btn_got_it);

        flMain = findViewById(R.id.fl_main);

        ivCrownCounterfeit = findViewById(R.id.iv_counterfeit_crown);
        ivCrownPwd = findViewById(R.id.iv_pwd_crown);
        ivCrownSign = findViewById(R.id.iv_sign_crown);
    }

    private void getSavedPdfPath() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.SAVED_PDF_PATH)) {
            savedPdfPath = getIntent().getStringExtra(Constants.PutExtraConstants.SAVED_PDF_PATH);
//            proceedWithPdfPath(true);
            createPdfDocumentsInCache();
        }
    }

    private void proceedWithPdfPath(Boolean isFromOnCreate) {
        if (!TextUtils.isEmpty(savedPdfPath)) {
            tv_toolbar.setText(folderName + ".pdf");
            pdfUtils.isPdfEncrypted(context, savedPdfPath, "Editor", (isSuccess, isEncrypted) -> {
                if (isEncrypted) {
                    mInputPassword = new String[1];
                    Log.e(TAG, "PDF is password encrypted");
                    showRemovePdfPasswordDialog(savedPdfPath, mInputPassword);
                } else {
                    Log.e(TAG, "PDF is not encrypted");
                    showCreatePdfMsg = isFromOnCreate;
                    loadPdf(savedPdfPath, false, null, null, false);
                }
            });

        }
    }

    private void showRemovePdfPasswordDialog(String savedPdfPath, String[] mInputPassword) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_pdf_password_protected);

        EditText et_password = dialog.findViewById(R.id.et_password);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        TextView tv_error_msg = dialog.findViewById(R.id.tv_error_msg);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredPassword = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(enteredPassword)) {
                    showErrorMsg(tv_error_msg, getString(R.string.please_enter_password));
                    return;
                }

                mInputPassword[0] = enteredPassword;
                String decryptedPdfPath = pdfUtils.removeDefPasswordForImages(context, savedPdfPath, mInputPassword);
                if (!TextUtils.isEmpty(decryptedPdfPath)) {
                    loadPdf(decryptedPdfPath, true, enteredPassword, prefManager.getMasterPassword(), false);
                } else {
                    Toast.makeText(context, getString(R.string.unable_to_decrypt_pdf_file), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private String getPdfNameFromPath(String savedPdfPath) {
        String pdfFileName = null;
        File file = new File(savedPdfPath);
        if (file.isFile() && file.exists()) {
            pdfFileName = file.getName();
        }
        if (!TextUtils.isEmpty(pdfFileName)) {
            pdfFileName = flashScanUtil.removeExtensionFromFileName(pdfFileName);
        }

        return pdfFileName;
    }

    private void loadPdf(String savedPdfPath, boolean encryptAfterLoading, String pdfPassword, String masterpassword, boolean isPdfLoadedForAntiCounterfeit) {
        File file = new File(savedPdfPath);
        Log.e(TAG, "file size..." + file.length());
        if (file.isFile() && file.exists()) {
            pdfView.fromFile(file)
                    .defaultPage(0)
                    .spacing(10)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .onPageChange(this)
                    .swipeHorizontal(false)
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            Log.e(TAG, "onError called : " + t.getMessage());
                            Toast.makeText(context, getString(R.string.pdf_may_be_pwd_ptd), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            Log.e(TAG, "loadComplete called : " + nbPages);
                            if (encryptAfterLoading) {
                                if (!TextUtils.isEmpty(pdfPassword) && !TextUtils.isEmpty(masterpassword)) {
                                    try {
                                        pdfUtils.doEncryption(context, savedPdfPath, pdfPassword, masterpassword);
                                    } catch (IOException | DocumentException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (isPdfLoadedForAntiCounterfeit) {
                                antiCounterFeitAlreadyApplied = true;
                            } else {
                                antiCounterFeitAlreadyApplied = false;
                            }
                        }
                    })
                    .load();
            if (showCreatePdfMsg) {
                pdfCreationSuccessfulMsg();
                showCreatePdfMsg = false;
            }
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        tv_pdf_page_count.setVisibility(View.VISIBLE);
        tv_pdf_page_count.setText((page + 1) +"/"+pageCount);
    }

    @Override
    public void onClick(View v) {
        if (CommonMethods.multipleClicked())
            return;
        int id = v.getId();
        if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.iv_share) {
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_PDF_SHARE_ICON
            );
            if (!TextUtils.isEmpty(savedPdfPath)) {
                sharePdfFile(savedPdfPath);
            }
        } else if (id == R.id.tv_pdf_password) {
            featureClickedType = OnFeatureClicked.Password.getValue();
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_PDF_PASSWORD
            );
            if (CommonMethods.isConnectingToInternet(this) && com.itl.commonres.utils.Constants.isAdShow && isAdShowedForPassword) {
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
            } else {
                Log.e(TAG, "Mobibuz : Ad Not Showing");
                isAdShowedForPassword = false;
                if (!TextUtils.isEmpty(savedPdfPath)) {
                    File file = new File(savedPdfPath);
                    if (file.isFile() && file.exists()) {
                        pdfUtils.isPdfEncrypted(context, file.getPath(), "Editor", new PdfEncryptionCallBack() {
                            @Override
                            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                                if (isSuccess && !isEncrypted) {
                                    showPdfPasswordDialog(file.getPath());
                                } else {
                                    Toast.makeText(context, getString(R.string.pdf_is_pswd_ptd), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
            /*if (*//*prefManager.isPremiumYearly() || prefManager.isPremiumQuarterly()*//**//*isAdShowedForPassword*//* CommonMethods.isConnectingToInternet(this) && !com.itl.commonres.utils.Constants.isAdShow) {
                isAdShowedForPassword = false;
                if (!TextUtils.isEmpty(savedPdfPath)) {
                    File file = new File(savedPdfPath);
                    if (file.isFile() && file.exists()) {
                        pdfUtils.isPdfEncrypted(context, file.getPath(), "Editor", new PdfEncryptionCallBack() {
                            @Override
                            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                                if (isSuccess && !isEncrypted) {
                                    showPdfPasswordDialog(file.getPath());
                                } else {
                                    Toast.makeText(context, getString(R.string.pdf_is_pswd_ptd), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            } else {
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
//                askToBePremium();
            }*/
        } else if (id == R.id.tv_pdf_compress) {
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_FILE_COMPRESSION
            );
            showPdfCompressionPopup(Constants.COMPRESS_PDF_QUALITY_VALUE);

               /* if(!isPdfCompressed) {
                    showPdfCompressionSizeDialog();
                    isPdfCompressed = true;
                }
                else {
                    Toast.makeText(context, getString(R.string.pdf_already_compressed), Toast.LENGTH_LONG).show();
                }*/
        } else if (id == R.id.iv_premium) {
            askToBePremium();

                /*if (prefManager.isAppWatermarkFree()) {
                    askToBePremium();
                } else {
                    goToWaterMarkRemoveActivity();
                }*/

            //Previous code (rishav)
                /*if (!prefManager.isAppWatermarkFree() && Constants.SHOW_REWARDED_ADS.FOR_PDF_EDITOR_ACTIVITY) {
                    // case when remove watermark is done by both i.e. Watching Ad or Payment
                    goToWaterMarkRemoveActivity();
                } else {
                    if (!prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly()) {
                        // case when remove watermark is only done by payment
                    }
                }*/
            ////
        } else if (id == R.id.tv_anti_counterfeit) {
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_ANTI_COUNTERFEIT
            );
            featureClickedType = OnFeatureClicked.Watermark.getValue();

            if (CommonMethods.isConnectingToInternet(this) && com.itl.commonres.utils.Constants.isAdShow && isAdShowedForWatermark) {
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
            } else {
                Log.e(TAG, "Mobibuz : Ad Not Showing");
                isAdShowedForWatermark = false;

                if (!antiCounterFeitAlreadyApplied) {
                    showAntiCounterFeitDialog();
                } else {
                    removeAntiCounterfeitDialog();
                }
            }
        } else if (id == R.id.tv_pdf_signature) {
            CommonMethods.logCustomFireBaseEvents(
                    TAG,
                    com.itl.commonres.utils.Constants.CLICK_PDF_SIGNATURE
            );
            featureClickedType = OnFeatureClicked.Signature.getValue();
            if (CommonMethods.isConnectingToInternet(this) && com.itl.commonres.utils.Constants.isAdShow && isAdShowedForSignature) {
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
            } else {
                Log.e(TAG, "Mobibuz : Ad Not Showing");
                isAdShowedForSignature = false;
                if (!TextUtils.isEmpty(savedPdfPath)) {
                    File file = new File(savedPdfPath);
                    if (file.isFile() && file.exists()) {
                        pdfUtils.isPdfEncrypted(context, file.getPath(), "Editor", new PdfEncryptionCallBack() {
                            @Override
                            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                                if (isSuccess && isEncrypted) {
                                    // file is encrypted
                                    mInputPassword = new String[1];
                                    String pdfFilePassword = pdfUtils.getPdfFilePassword(context, file.getPath(), prefManager.getMasterPassword());
                                    if (!TextUtils.isEmpty(pdfFilePassword)) {
                                        mInputPassword[0] = pdfFilePassword;
                                        String directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                                        String tempDirPath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + directoryName;
                                        File tempDir = new File(tempDirPath);
                                        if (tempDir.isDirectory() && tempDir.exists()) {
                                            // check password before applying signature on password protected file
                                            showPdfPasswordProtectedDialog(file, tempDir, mInputPassword);
                                            //intentToPdfSignatureActivity(tempDir.getPath());
                                        } else {
                                            createImagesFromPdf(savedPdfPath, mInputPassword, pdfFilePassword, 0);
                                        }

                                    }
                                } else {
                                    if (isSuccess) {
                                        /*createImagesFromPdf(file.getPath(), null, null);*/
                                        String directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                                        String tempDirPath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + directoryName;
                                        File tempDir = new File(tempDirPath);
                                        if (tempDir.isDirectory() && tempDir.exists()) {
                                            intentToPdfSignatureActivity(tempDir.getPath());
                                        } else {
                                            createImagesFromPdf(savedPdfPath, null, null, 0);
                                        }
                                    } else {
                                        if (isEncrypted) {
                                            // file is encrypted
                                            mInputPassword = new String[1];
                                            /*showEncryptionDialogForPdfSignature(file.getPath(), mInputPassword);*/
                                            String pdfFilePassword = pdfUtils.getPdfFilePassword(context, file.getPath(), prefManager.getMasterPassword());
                                            if (!TextUtils.isEmpty(pdfFilePassword)) {
                                                mInputPassword[0] = pdfFilePassword;
                                                String directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                                                String tempDirPath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + directoryName;
                                                File tempDir = new File(tempDirPath);
                                                if (tempDir.isDirectory() && tempDir.exists()) {
                                                    // check password before applying signature on password protected file
                                                    showPdfPasswordProtectedDialog(file, tempDir, mInputPassword);
                                                    //intentToPdfSignatureActivity(tempDir.getPath());
                                                } else {
                                                    createImagesFromPdf(savedPdfPath, mInputPassword, pdfFilePassword, 0);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }

            /*if (*//*prefManager.isPremiumYearly() || prefManager.isPremiumQuarterly()*//**//*isAdShowedForSignature*//* !com.itl.commonres.utils.Constants.isAdShow) {
                isAdShowedForSignature = false;
                if (!TextUtils.isEmpty(savedPdfPath)) {
                    File file = new File(savedPdfPath);
                    if (file.isFile() && file.exists()) {
                        pdfUtils.isPdfEncrypted(context, file.getPath(), "Editor", new PdfEncryptionCallBack() {
                            @Override
                            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                                if (isSuccess && isEncrypted) {
                                    // file is encrypted
                                    mInputPassword = new String[1];
                                    String pdfFilePassword = pdfUtils.getPdfFilePassword(context, file.getPath(), prefManager.getMasterPassword());
                                    if (!TextUtils.isEmpty(pdfFilePassword)) {
                                        mInputPassword[0] = pdfFilePassword;
                                        String directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                                        String tempDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + directoryName;
                                        File tempDir = new File(tempDirPath);
                                        if (tempDir.isDirectory() && tempDir.exists()) {
                                            // check password before applying signature on password protected file
                                            showPdfPasswordProtectedDialog(file, tempDir, mInputPassword);
                                            //intentToPdfSignatureActivity(tempDir.getPath());
                                        } else {
                                            createImagesFromPdf(savedPdfPath, mInputPassword, pdfFilePassword, 0);
                                        }

                                    }
                                } else {
                                    if (isSuccess) {
                                        *//*createImagesFromPdf(file.getPath(), null, null);*//*
                                        String directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                                        String tempDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + directoryName;
                                        File tempDir = new File(tempDirPath);
                                        if (tempDir.isDirectory() && tempDir.exists()) {
                                            intentToPdfSignatureActivity(tempDir.getPath());
                                        } else {
                                            createImagesFromPdf(savedPdfPath, null, null, 0);
                                        }
                                    } else {
                                        if (isEncrypted) {
                                            // file is encrypted
                                            mInputPassword = new String[1];
                                            *//*showEncryptionDialogForPdfSignature(file.getPath(), mInputPassword);*//*
                                            String pdfFilePassword = pdfUtils.getPdfFilePassword(context, file.getPath(), prefManager.getMasterPassword());
                                            if (!TextUtils.isEmpty(pdfFilePassword)) {
                                                mInputPassword[0] = pdfFilePassword;
                                                String directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                                                String tempDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + directoryName;
                                                File tempDir = new File(tempDirPath);
                                                if (tempDir.isDirectory() && tempDir.exists()) {
                                                    // check password before applying signature on password protected file
                                                    showPdfPasswordProtectedDialog(file, tempDir, mInputPassword);
                                                    //intentToPdfSignatureActivity(tempDir.getPath());
                                                } else {
                                                    createImagesFromPdf(savedPdfPath, mInputPassword, pdfFilePassword, 0);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            } else {
                Constants.isAppInBackground = false;
                loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
//                askToBePremium();
            }*/
        } else if (id == R.id.btn_got_it) {
            pdf_editor_tutorial_view.setVisibility(View.GONE);
        } else if (id == R.id.pdf_editor_tutorial_view) {
        } else if (id == R.id.iv_rename) {
            fetchFiles();
        }
    }

    private void showPdfCompressionSizeDialog() {

        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_compress_pdf_by_sizes);

        EditText et_pdf_name = dialog.findViewById(R.id.et_pdf_name);
        et_pdf_name.setText("" + getPdfNameFromPath(savedPdfPath));

        TextView tv_compress = dialog.findViewById(R.id.tv_compress);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_low = dialog.findViewById(R.id.tv_low);
        TextView tv_medium = dialog.findViewById(R.id.tv_medium);
        TextView tv_regular = dialog.findViewById(R.id.tv_regular);
        TextView tv_max = dialog.findViewById(R.id.tv_max);

        RadioButton rb_low = dialog.findViewById(R.id.rb_low);
        RadioButton rb_medium = dialog.findViewById(R.id.rb_medium);
        RadioButton rb_regular = dialog.findViewById(R.id.rb_regular);
        RadioButton rb_max = dialog.findViewById(R.id.rb_max);


        rb_low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_low.setChecked(true);
                rb_medium.setChecked(false);
                rb_regular.setChecked(false);
                rb_max.setChecked(false);
                tv_low.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_checked));
                tv_medium.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_regular.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_max.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                selectedPdfCompressionSize = Constants.COMPRESS_PDF_QUALITY_LOW;
            }
        });
        rb_medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_low.setChecked(false);
                rb_medium.setChecked(true);
                rb_regular.setChecked(false);
                rb_max.setChecked(false);
                tv_low.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_medium.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_checked));
                tv_regular.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_max.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                selectedPdfCompressionSize = Constants.COMPRESS_PDF_QUALITY_MEDIUM;
            }
        });
        rb_regular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_low.setChecked(false);
                rb_medium.setChecked(false);
                rb_regular.setChecked(true);
                rb_max.setChecked(false);
                tv_low.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_medium.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_regular.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_checked));
                tv_max.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                selectedPdfCompressionSize = Constants.COMPRESS_PDF_QUALITY_REGULAR;
            }
        });
        rb_max.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_low.setChecked(false);
                rb_medium.setChecked(false);
                rb_regular.setChecked(false);
                rb_max.setChecked(true);
                tv_low.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_medium.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_regular.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_unchecked));
                tv_max.setTextColor(context.getResources().getColor(R.color.pdf_size_radio_checked));
                selectedPdfCompressionSize = Constants.COMPRESS_PDF_QUALITY_MAX;
            }
        });

        tv_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if (TextUtils.isEmpty(et_pdf_name.getText().toString().trim())) {
                    Toast.makeText(context, "" + getString(R.string.please_name_the_pdf), Toast.LENGTH_SHORT).show();
                    return;
                }*/
                String folderName = et_pdf_name.getText().toString().trim();
                if (TextUtils.isEmpty(folderName)) {
                    Toast.makeText(context, getString(R.string.please_name_file), Toast.LENGTH_SHORT).show();
                    return;
                }
                showPdfCompressionPopup(selectedPdfCompressionSize);
                dialog.dismiss();
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        /*btn_done.setOnClickListener(new View.OnClickListener() {
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
                    // working same as previous
                } else if (rbCompressed.isChecked()) {
                    // new working for compressoion
                    fileModel.setCompressedPdf(true);
                }
                handlePdfCreation(fileModel, pdfVia);
                dialog.dismiss();
            }
        });*/

        dialog.show();
    }

    private void showPdfCompressionPopup(int compressionSize) {

        if (!TextUtils.isEmpty(savedPdfPath)) {
            File file = new File(savedPdfPath);

            if (file.isFile() && file.exists()) {
                double originalFileSize = file.length();
                Log.e("originalFileSize", "" + flashScanUtil.getFormattedFileSize(file));
                //String originalFileSize = flashScanUtil.getFormattedFileSize(file);
                //Toast.makeText(PdfEditorActivity.this,"file Path: "+file.getPath().toString()+" originalFileSize: " +originalFileSize, Toast.LENGTH_SHORT).show();
                pdfUtils.compressPdf(file.getPath(), compressionSize, new PdfCompressionCallback() {
                    @Override
                    public void onCompressionStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCompressionCompleted(boolean isSuccess, String outputPath) {
                        progress_lay.setVisibility(View.GONE);
                        if (isSuccess) {
                            if (!isFinishing() || !isDestroyed()) {
                                File outputFile = new File(outputPath);
                                if (outputFile.isFile() && outputFile.exists()) {
                                    double compressedFileSize = outputFile.length();
//                                            String compressedFileSize = flashScanUtil.getFormattedFileSize(outputFile);
                                    double originalSize = originalFileSize;
                                    double compressedSize = compressedFileSize;
                                    double differenceSize = originalSize - compressedSize;
                                    if (differenceSize <= Constants.COMPRESS_PDF_THRESHOLD) {
                                        Toast.makeText(context, getString(R.string.pdf_already_compressed), Toast.LENGTH_LONG).show();
                                    } else {
                                        showCompressionSuccessDialog(flashScanUtil.getFormattedFileSize(originalFileSize),
                                                flashScanUtil.getFormattedFileSize(compressedFileSize));
                                    }

                                }

                            }
                        } else {
                            Toast.makeText(context, "" + getString(R.string.cant_compress_pdf_pw_ptd), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
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

    private void openAskEmailActivity(int whichPlanActivity) {
        Intent intent = new Intent(PdfEditorActivity.this, AskEmailActivity.class);
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity);
        startActivity(intent);
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(PdfEditorActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    /*private void showEncryptionDialogForPdfSignature(String path, String[] mInputPassword) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_pdf_password_protected);

        EditText et_password = dialog.findViewById(R.id.et_password);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredPassword = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(enteredPassword))
                    return;
                mInputPassword[0] = enteredPassword;
                boolean isPasswordCorrect = pdfUtils.checkEnteredPasswordIsCorrect(context, path, mInputPassword);
                if (isPasswordCorrect) {
                    dialog.dismiss();
                    String decryptedPdfpath = pdfUtils.removeDefPasswordForImages(context, path, mInputPassword);
                    createImagesFromPdf(decryptedPdfpath, enteredPassword);
                } else {
                    Toast.makeText(context, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }*/

    private void createImagesFromPdf(String path, String[] password, String pdfFilePassword, int action) { //action- 0:signature, 1:rename, 2:anti-counterFeit
        new PdfToImagesAsyncTask(context, password, path, new PdfToImageCallback() {
            @Override
            public void onConversionStart() {
                progress_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onConversionCompleted(String savedDirPath, boolean isSuccess) {
                progress_lay.setVisibility(View.GONE);
                if (isSuccess && !TextUtils.isEmpty(savedDirPath)) {
                    if (action == 0) {
                        intentToPdfSignatureActivity(savedDirPath);
                        if (password != null && password.length > 0 && !TextUtils.isEmpty(pdfFilePassword)) {
                            try {
                                pdfUtils.doEncryption(context, path, pdfFilePassword, prefManager.getMasterPassword());
                            } catch (IOException | DocumentException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (action == 1) {
                        fetchFiles();
                    } else if (action == 2) {
                        if (!antiCounterFeitAlreadyApplied) {
                            showAntiCounterFeitDialog();
                        } else {
                            removeAntiCounterfeitDialog();
                        }
                    } else {
                        copyFiles(savedDirPath);
                    }
                } else {
                    Toast.makeText(context, getString(R.string.pdf_is_pswd_ptd), Toast.LENGTH_SHORT).show();
                }
            }
        }).execute();
    }

    private void intentToPdfSignatureActivity(String savedDirPath) {
        Intent intent = new Intent(context, PdfSignatureActivity.class);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_PATH, savedDirPath);
        startActivityForResult(intent, REQUEST_CODE_FOR_PDF_SIGNATURE);
    }

    private void removeAntiCounterfeitDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_remove_anti_counterfeit);

        LinearLayout tvEditCounterfeit = dialog.findViewById(R.id.tv_edit_counterfeit_content);
        LinearLayout tvClearCounterfeit = dialog.findViewById(R.id.tv_clear_counterfeit_content);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        tvEditCounterfeit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showEditCounterfeitDialog();
            }
        });

        tvClearCounterfeit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create new pdf from temporary images
                dialog.dismiss();

                String imagesDirectoryPath = flashScanUtil.getPdfProcessingPath(context) + File.separator + folderName;

                if (!TextUtils.isEmpty(imagesDirectoryPath) && !TextUtils.isEmpty(savedPdfPath)) {
                    updatePdfWithSignatures(savedPdfPath, imagesDirectoryPath, UPDATE_PDF_WITH_NO_COUNTERFEIT);
                }
            }
        });
        dialog.show();
    }

    private void showEditCounterfeitDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_anti_counterfeit);

        EditText et_watermark_txt = dialog.findViewById(R.id.et_watermark_txt);
        et_watermark_txt.setText(prefManager.getAntiCounterfeitTxt());
        RecyclerView rv_colors = dialog.findViewById(R.id.rv_colors);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rv_colors.setLayoutManager(linearLayoutManager);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = dialog.findViewById(R.id.tv_ok);
        SeekBar seekBar = dialog.findViewById(R.id.seekBar);
        seekBar.setProgress(prefManager.getAntiCounterFeitTextSize());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int min = 50;
                if (progress < min) {
                    seekBar.setProgress(min);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        List<ColorModel> colorsList = flashScanUtil.getColorsList();
        if (colorsList != null && !colorsList.isEmpty()) {
            for (ColorModel colorModel : colorsList) {
                if (colorModel.getColorCode().equalsIgnoreCase(prefManager.getAntiCounterfeitTxtColor())) {
                    colorModel.setChecked(true);
                }
            }
            textColorAdapter = new TextColorAdapter(context, colorsList);
            rv_colors.setAdapter(textColorAdapter);
        }

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String waterMarkTxt = et_watermark_txt.getText().toString().trim();
                if (TextUtils.isEmpty(waterMarkTxt)) {
                    Toast.makeText(context, getString(R.string.please_enter_watermark_content), Toast.LENGTH_SHORT).show();
                    return;
                }
                int watermarkFontSize = seekBar.getProgress();
                dialog.dismiss();
                String watermarkTextColor = getSelectedColorFromAdapter();
                if (TextUtils.isEmpty(watermarkTextColor)) {
                    Toast.makeText(context, getString(R.string.please_choose_color), Toast.LENGTH_SHORT).show();
                    return;
                }
                prefManager.saveAntiCounterFeitText(waterMarkTxt);
                prefManager.saveAntiCounterFeitTextSize(watermarkFontSize);
                prefManager.saveAntiConterFeitTextColor(watermarkTextColor);
                WaterMark waterMark = setWaterMark(waterMarkTxt, watermarkFontSize, watermarkTextColor);
                setEditedCounterfeitWatermark(waterMark);
                String imagesDirectoryPath = flashScanUtil.getPdfProcessingPath(context) + File.separator + folderName;

                if (!TextUtils.isEmpty(imagesDirectoryPath) && !TextUtils.isEmpty(savedPdfPath)) {
                    updatePdfWithSignatures(savedPdfPath, imagesDirectoryPath, UPDATE_PDF_WITH_EDITED_COUNTERFEIT);
                }
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showAntiCounterFeitDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_anti_counterfeit);

        EditText et_watermark_txt = dialog.findViewById(R.id.et_watermark_txt);

        et_watermark_txt.setText(prefManager.getAntiCounterfeitTxt());

        RecyclerView rv_colors = dialog.findViewById(R.id.rv_colors);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rv_colors.setLayoutManager(linearLayoutManager);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = dialog.findViewById(R.id.tv_ok);
        SeekBar seekBar = dialog.findViewById(R.id.seekBar);
        seekBar.setProgress(prefManager.getAntiCounterFeitTextSize());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int min = 50;
                if (progress < min) {
                    seekBar.setProgress(min);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        List<ColorModel> colorsList = flashScanUtil.getColorsList();
        if (colorsList != null && !colorsList.isEmpty()) {
            for (ColorModel colorModel : colorsList) {
                if (colorModel.getColorCode().equalsIgnoreCase(prefManager.getAntiCounterfeitTxtColor())) {
                    colorModel.setChecked(true);
                }
            }
            textColorAdapter = new TextColorAdapter(context, colorsList);
            rv_colors.setAdapter(textColorAdapter);
        }

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String waterMarkTxt = et_watermark_txt.getText().toString().trim();
                if (TextUtils.isEmpty(waterMarkTxt)) {
                    Toast.makeText(context, getString(R.string.please_enter_watermark_content), Toast.LENGTH_SHORT).show();
                    return;
                }
                int watermarkFontSize = seekBar.getProgress();
                String watermarkTextColor = getSelectedColorFromAdapter();
                if (TextUtils.isEmpty(watermarkTextColor)) {
                    Toast.makeText(context, getString(R.string.please_choose_color), Toast.LENGTH_SHORT).show();
                    return;
                }
                prefManager.saveAntiCounterFeitText(waterMarkTxt);
                prefManager.saveAntiCounterFeitTextSize(watermarkFontSize);
                prefManager.saveAntiConterFeitTextColor(watermarkTextColor);
                WaterMark waterMark = setWaterMark(waterMarkTxt, watermarkFontSize, watermarkTextColor);
                if (!TextUtils.isEmpty(savedPdfPath)) {
                    File file = new File(savedPdfPath);
                    if (file.isFile() && file.exists()) {
                        pdfUtils.isPdfEncrypted(context, file.getPath(), "Editor", new PdfEncryptionCallBack() {
                            @Override
                            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                                if (isSuccess && isEncrypted) {
                                    // file is encrypted
                                    mInputPassword = new String[1];
                                    showPwdDialogForCounterFeit(file.getPath(), mInputPassword, waterMark);
                                } else {
                                    if (isSuccess) {
                                        String antiCounterFeitPdfPath = pdfUtils.addAntiCounterFeitToPdf(waterMark, file.getPath());
                                        if (!TextUtils.isEmpty(antiCounterFeitPdfPath)) {
                                            loadPdf(antiCounterFeitPdfPath, false, null, null, true);
                                        }
                                    } else {
                                        if (isEncrypted) {
                                            // file is encrypted
                                            mInputPassword = new String[1];
                                            showPwdDialogForCounterFeit(file.getPath(), mInputPassword, waterMark);
                                        }
                                    }
                                }
                            }
                        });
                    }


                }
                dialog.dismiss();

            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showPwdDialogForCounterFeit(String path, String[] mInputPassword, WaterMark waterMark) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_pdf_password_protected);

        EditText et_password = dialog.findViewById(R.id.et_password);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        TextView tv_error_msg = dialog.findViewById(R.id.tv_error_msg);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredPassword = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(enteredPassword)) {
                    showErrorMsg(tv_error_msg, getString(R.string.please_enter_password));
                    return;
                }

                mInputPassword[0] = enteredPassword;
                boolean isPasswordCorrect = pdfUtils.checkEnteredPasswordIsCorrect(context, path, mInputPassword);
                if (isPasswordCorrect) {
                    dialog.dismiss();
                    String decryptedPdfpath = pdfUtils.removeDefPasswordForImages(context, path, mInputPassword);
                    String antiCounterFeitPdfPath = pdfUtils.addAntiCounterFeitToPdf(waterMark, decryptedPdfpath);
                    if (!TextUtils.isEmpty(antiCounterFeitPdfPath)) {
                        loadPdf(antiCounterFeitPdfPath, true, enteredPassword, prefManager.getMasterPassword(), true);
                    }
                } else {
                    showErrorMsg(tv_error_msg, getString(R.string.incorrect_password));
                }
            }
        });

        dialog.show();
    }


    private WaterMark setWaterMark(String waterMarkTxt, int watermarkFontSize, String watermarkTextColor) {
        WaterMark waterMark = new WaterMark();
        waterMark.setWaterMarkText(waterMarkTxt);
        waterMark.setFontFamily(Font.TIMES_ROMAN);
        waterMark.setFontStyle(Font.NORMAL);
        waterMark.setRotationAngle(45);
        waterMark.setTextSize(watermarkFontSize);
        int color = Color.parseColor(watermarkTextColor);
        int colorWithAlpha = getColorWithAlpha(color, 0.3f);
        if (colorWithAlpha != 0) {
            waterMark.setTextColor(new BaseColor(colorWithAlpha));
        } else {
            waterMark.setTextColor(new BaseColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color)));
        }
        return waterMark;
    }

    private int getColorWithAlpha(int color, float ratio) {
        int newColor;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;

    }

    private String getSelectedColorFromAdapter() {
        String selectedColor = null;
        if (textColorAdapter != null) {
            selectedColor = textColorAdapter.getSelectedColor();
        }
        return selectedColor;
    }

    private void goToWaterMarkRemoveActivity() {
        Intent intent = new Intent(context, WaterMarkRemoveActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_REMOVE_WATERMARK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_FOR_REMOVE_WATERMARK: {
                switch (resultCode) {
                    case Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED: {
                        // when user cancels the ad in between
                        Log.e(TAG, "RESULT_AD_CANCELLED");
                    }
                    break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD: {
                        // update pdf without watermark
                        if (!TextUtils.isEmpty(savedPdfPath)) {
                            isAdVideoAlreadyWatched = true;
                            updatePdfWithOriginalImages(savedPdfPath, UPDATE_PDF_WITH_NO_WATERMARK);
                        }

                    }
                    break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_IGNORE: {
                        // when user cancel watermark activity
                        Log.e(TAG, "RESULT_IGNORE");
                    }
                    break;
                    case Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK: {
                        Log.e(TAG, "RESULT_PURCHASE_WATERMARK");
                        reCreatePdfWithoutWaterMark();
                        handlePremiumIconVisibility();
                    }
                    break;
                }
            }
            break;
            case REQUEST_CODE_FOR_PDF_SIGNATURE:
                switch (resultCode) {
                    case RESULT_OK: {
                        String imagesDirectoryPath = null;
                        if (data != null && data.hasExtra(Constants.PutExtraConstants.FOLDER_PATH)) {
                            imagesDirectoryPath = data.getStringExtra(Constants.PutExtraConstants.FOLDER_PATH);
                        }
                        if (!TextUtils.isEmpty(imagesDirectoryPath) && !TextUtils.isEmpty(savedPdfPath)) {
                            updatePdfWithSignatures(savedPdfPath, imagesDirectoryPath, UPDATE_PDF_WITH_SIGNATURE);
                        }
                    }
                    break;
                }
                break;
            case Constants.REQUEST_CODE_PREMIUM_YEALY:
                Log.i(TAG, "onActivityResult REQUEST_CODE_PREMIUM_YEALY");
                if (resultCode == RESULT_OK) {

                    reCreatePdfWithoutWaterMark();
                    //PREMIUM taken
                    handlePremium();
                    if (prefManager.getPurchasedPlanName() == Constants.BUY_NOW_YEARLY) {
                        flashScanUtil.showSnackBar(flMain, getString(R.string.premium_yearly_success_msg, getString(R.string.app_name)));
                    } /*else {
                        flashScanUtil.showSnackBar(flMain, getString(R.string.premium_quarterly_success_msg));
                    }*/
                    //flashScanUtil.showSnackBar(flMain, getString(R.string.premium_yearly_success_msg));
                    try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void reCreatePdfWithoutWaterMark() {
        if (!TextUtils.isEmpty(savedPdfPath)) {
            updatePdfWithOriginalImages(savedPdfPath, UPDATE_PDF_WITH_NO_WATERMARK);
        }
    }

    private void handlePremium() {
        //Do required UI or any changes
        handlePremiumIconVisibility();
    }

    private void reCreate() {
        //No need to reCreate here
        /*finish();
        startActivity(getIntent());*/
    }

    private void updatePdfWithSignatures(String savedPdfPath, String imagesDirectoryPath, int updatePdfForAction) {
        pdfUtils.isPdfEncrypted(context, savedPdfPath, "Editor", new PdfEncryptionCallBack() {
            @Override
            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                String pdfFilePassword = null;
                if (isEncrypted) {
                    isEncryptPdfAfterCreating = true;
                    pdfFilePassword = pdfUtils.getPdfFilePassword(context, savedPdfPath, prefManager.getMasterPassword());
                } else {
                    isEncryptPdfAfterCreating = false;
                }
                File file = new File(savedPdfPath);
                if (file.isFile() && file.exists()) {
                    String fileName = flashScanUtil.removeExtensionFromFileName(file.getName());
                    if (!TextUtils.isEmpty(fileName)) {
                        fetchSignaturedImagesFromTempDir(fileName, imagesDirectoryPath, isEncryptPdfAfterCreating, pdfFilePassword, updatePdfForAction);
                    }
                }
            }
        });

    }


    private void updatePdfWithOriginalImages(String savedPdfPath, int updatePdfForAction) {
        // fetch images from pdf and then create pdf and then load it into pdf editor

        pdfUtils.isPdfEncrypted(context, savedPdfPath, "Editor", new PdfEncryptionCallBack() {
            @Override
            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                String pdfFilePassword = null;
                if (isEncrypted) {
                    isEncryptPdfAfterCreating = true;
                    pdfFilePassword = pdfUtils.getPdfFilePassword(context, savedPdfPath, prefManager.getMasterPassword());
                } else {
                    isEncryptPdfAfterCreating = false;
                }
                File file = new File(savedPdfPath);
                if (file.isFile() && file.exists()) {
                    String fileName = flashScanUtil.removeExtensionFromFileName(file.getName());
                    if (!TextUtils.isEmpty(fileName)) {
                        fetchOriginalImagesFromTempDir(fileName, isEncryptPdfAfterCreating, pdfFilePassword, updatePdfForAction);
                    }
                }
            }
        });


    }

    private void fetchSignaturedImagesFromTempDir(String pdfFileName, String imagesDirectoryPath, boolean isEncryptPdfAfterCreating,
                                                  String pdfFilePassword, int updatePdfForAction) {
        File tempDir = new File(imagesDirectoryPath);
        if (tempDir.isDirectory() && tempDir.exists()) {
            File[] files = tempDir.listFiles();
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
                ArrayList<String> imagesPathList = new ArrayList<>();
                for (File file : files) {
                    if (file != null && file.isFile() && file.exists()) {
                        imagesPathList.add(file.getPath());
                    }
                }
                if (!imagesPathList.isEmpty()) {
                    boolean isWaterMarkToBeShown;
                    if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() /*|| prefManager.isPremiumQuarterly()*/) {
                        isWaterMarkToBeShown = false;
                    } else {
                        if (isAdVideoAlreadyWatched) {
                            isWaterMarkToBeShown = false;
                        } else {
                            isWaterMarkToBeShown = true;
                        }
                    }
                    createPdfForImages(imagesPathList, pdfFileName, isWaterMarkToBeShown, isEncryptPdfAfterCreating,
                            pdfFilePassword, updatePdfForAction);
                }
            }
        }

    }

    private void fetchOriginalImagesFromTempDir(String directoryName, boolean isEncryptPdfAfterCreating, String pdfFilePassword,
                                                int updatePdfForAction) {
        String tempDirPath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_ORIGINAL_DIRECTORY + File.separator + directoryName;
        File tempDir = new File(tempDirPath);
        if (tempDir.isDirectory() && tempDir.exists()) {
            File[] files = tempDir.listFiles();
            if (files != null && files.length > 0) {
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
                ArrayList<String> imagesPathList = new ArrayList<>();
                for (File file : files) {
                    if (file != null && file.isFile() && file.exists()) {
                        imagesPathList.add(file.getPath());
                    }
                }
                boolean isWaterMarkToBeShown = true;
                if (!imagesPathList.isEmpty()) {
                    /*switch (updatePdfForAction) {
                        case UPDATE_PDF_WITH_EDITED_COUNTERFEIT:
                        case UPDATE_PDF_WITH_NO_COUNTERFEIT:
                            if (prefManager.isAppWatermarkFree() || prefManager.isPremiumYearly() *//*|| prefManager.isPremiumQuarterly()*//*) {
                                isWaterMarkToBeShown = false;
                            } else {
                                if (isAdVideoAlreadyWatched) {
                                    isWaterMarkToBeShown = false;
                                } else {
                                    isWaterMarkToBeShown = true;
                                }

                            }
                            break;
                        case UPDATE_PDF_WITH_NO_WATERMARK:
                            isWaterMarkToBeShown = false;
                            break;

                    }*/


                    createPdfForImages(imagesPathList, directoryName, isWaterMarkToBeShown, isEncryptPdfAfterCreating,
                            pdfFilePassword, updatePdfForAction);
                }
            }

        }
    }

    /*private void pdfToImage(String[] enteredPassword, String savedPdfPath) {
        new PdfToImagesAsyncTask(context, enteredPassword, savedPdfPath, new PdfToImageCallback() {
            @Override
            public void onConversionStart() {
                progress_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onConversionCompleted(String savedDirPath, boolean isSuccess) {
                progress_lay.setVisibility(View.GONE);
                if (isSuccess && !TextUtils.isEmpty(savedDirPath)) {
                    File file = new File(savedDirPath);
                    if (file.isDirectory() && file.exists()) {
                        File[] files = file.listFiles();
                        if (files != null && files.length > 0) {
                            ArrayList<String> imagesPathList = new ArrayList<>();
                            for (File eachFile : files) {
                                if (eachFile != null && eachFile.isFile() && eachFile.exists()) {
                                    imagesPathList.add(eachFile.getPath());
                                }
                            }
                            if (!imagesPathList.isEmpty()) {
                                String pdfFileName = null;
                                File pdfFile = new File(savedPdfPath);
                                if (pdfFile.isFile() && pdfFile.exists()) {
                                    pdfFileName = flashScanUtil.removeExtensionFromFileName(pdfFile.getName());
                                }
                                createPdfForImages(imagesPathList, pdfFileName, false); // (jugad) true because temporarily we transparent watermark , false otherwise
                            }
                        }
                    }
                } else {
                    // ask  password first then create images
                    Log.e(TAG, "ask password first then create images");
                }
            }
        }).execute();
    }*/

    private void createPdfForImages(ArrayList<String> imagesPathList, String pdfFileName, boolean isWaterMarkToBeShown,
                                    boolean isEncryptPdfAfterCreating, String pdfFilePassword, int updatePdfForAction) {
        ImageToPdfOptions imageToPdfOptions = new ImageToPdfOptions();
        imageToPdfOptions.setPageSize(Constants.PdfConstants.DEFAULT_PDF_PAGE_SIZE);
        imageToPdfOptions.setPageColor(Constants.PdfConstants.DEFAULT_PDF_PAGE_COLOR);
        imageToPdfOptions.setMargins(0, 0, 0, 0);
        imageToPdfOptions.setPdfQuality(Constants.PdfConstants.DEFAULT_PDF_QUALITY);
        imageToPdfOptions.setBorderWidth(Constants.PdfConstants.DEFAULT_BORDER_WIDTH);
        imageToPdfOptions.setWaterMarkAdded(isWaterMarkToBeShown);
        imageToPdfOptions.setWaterMark(flashScanUtil.getWaterMark());
        new CreatePdfTask(context, pdfFileName, imageToPdfOptions, imagesPathList, new PDFCreationCallback() {
            @Override
            public void onPdfCreationStarted() {
                progress_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPdfCreated(String savedPdfPath) {
                progress_lay.setVisibility(View.GONE);
                //load pdf with without watermark
                if (!TextUtils.isEmpty(savedPdfPath)) {
                    if (isEncryptPdfAfterCreating) {
                        if (!TextUtils.isEmpty(pdfFilePassword)) {
                            switch (updatePdfForAction) {
                                case UPDATE_PDF_WITH_NO_WATERMARK:
                                case UPDATE_PDF_WITH_SIGNATURE:
                                    if (antiCounterFeitAlreadyApplied) {
                                        String antiCounterfeitTxt = prefManager.getAntiCounterfeitTxt();
                                        int antiCounterFeitTextSize = prefManager.getAntiCounterFeitTextSize();
                                        String antiCounterfeitTxtColor = prefManager.getAntiCounterfeitTxtColor();
                                        WaterMark waterMark = setWaterMark(antiCounterfeitTxt, antiCounterFeitTextSize, antiCounterfeitTxtColor);
                                        String antiCounterfeitPdfPath = pdfUtils.addAntiCounterFeitToPdf(waterMark, savedPdfPath);
                                        if (!TextUtils.isEmpty(antiCounterfeitPdfPath)) {
                                            loadPdf(savedPdfPath, true, pdfFilePassword, prefManager.getMasterPassword(), true);
                                        }
                                    } else {
                                        loadPdf(savedPdfPath, true, pdfFilePassword, prefManager.getMasterPassword(), false);
                                    }

                                    break;
                                case UPDATE_PDF_WITH_NO_COUNTERFEIT:
                                    loadPdf(savedPdfPath, true, pdfFilePassword, prefManager.getMasterPassword(), false);
                                    break;
                                case UPDATE_PDF_WITH_EDITED_COUNTERFEIT:
                                    WaterMark waterMark = getEditedCounterfeitWatermark();
                                    if (waterMark != null) {
                                        String antiCounterfeitPdfPath = pdfUtils.addAntiCounterFeitToPdf(waterMark, savedPdfPath);
                                        if (!TextUtils.isEmpty(antiCounterfeitPdfPath)) {
                                            loadPdf(savedPdfPath, true, pdfFilePassword, prefManager.getMasterPassword(), true);
                                        }
                                    }
                                    break;
                            }

                        }
                    } else {
                        switch (updatePdfForAction) {
                            case UPDATE_PDF_WITH_NO_WATERMARK:
                            case UPDATE_PDF_WITH_SIGNATURE:
                                if (antiCounterFeitAlreadyApplied) {
                                    String antiCounterfeitTxt = prefManager.getAntiCounterfeitTxt();
                                    int antiCounterFeitTextSize = prefManager.getAntiCounterFeitTextSize();
                                    String antiCounterfeitTxtColor = prefManager.getAntiCounterfeitTxtColor();
                                    WaterMark waterMark = setWaterMark(antiCounterfeitTxt, antiCounterFeitTextSize, antiCounterfeitTxtColor);
                                    String antiCounterfeitPdfPath = pdfUtils.addAntiCounterFeitToPdf(waterMark, savedPdfPath);
                                    if (!TextUtils.isEmpty(antiCounterfeitPdfPath)) {
                                        loadPdf(savedPdfPath, false, null, null, true);
                                    }

                                } else {
                                    loadPdf(savedPdfPath, false, null, null, false);
                                }

                                break;
                            case UPDATE_PDF_WITH_NO_COUNTERFEIT:
                                loadPdf(savedPdfPath, false, null, null, false);
                                break;

                            case UPDATE_PDF_WITH_EDITED_COUNTERFEIT:
                                WaterMark waterMark = getEditedCounterfeitWatermark();
                                if (waterMark != null) {
                                    String antiCounterfeitPdfPath = pdfUtils.addAntiCounterFeitToPdf(waterMark, savedPdfPath);
                                    if (!TextUtils.isEmpty(antiCounterfeitPdfPath)) {
                                        loadPdf(savedPdfPath, false, null, null, true);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }, false, true).execute();
    }

    private void showCompressionSuccessDialog(String originalFileSize, String compressedFileSize) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.pdf_compression_success_dialog);

        TextView tv_original_pdf_size = dialog.findViewById(R.id.tv_original_pdf_size);
        TextView tv_compressed_pdf_size = dialog.findViewById(R.id.tv_compressed_pdf_size);
        TextView tv_difference_in_size = dialog.findViewById(R.id.tv_difference_in_size);
        TextView tv_compressed_percentage = dialog.findViewById(R.id.tv_compressed_percentage);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);

        tv_original_pdf_size.setText(String.format(getString(R.string.original_pdf_size), originalFileSize));
        tv_compressed_pdf_size.setText(String.format(getString(R.string.compressed_pdf_size), compressedFileSize));

        double originalSize = 0;
        if (originalFileSize.contains(",")) {
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number = null;
            try {
                number = format.parse(originalFileSize);
                if (number != null) {
                    originalSize = number.doubleValue();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            originalSize = Double.parseDouble(originalFileSize);
        }

        double compressedSize = 0;
        if (originalFileSize.contains(",")) {
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number = null;
            try {
                number = format.parse(compressedFileSize);
                if (number != null) {
                    compressedSize = number.doubleValue();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            compressedSize = Double.parseDouble(compressedFileSize);
        }

        double difference = originalSize - compressedSize;
        String differenceInString = String.format("%.2f", difference);
        tv_difference_in_size.setText(String.format(getString(R.string.difference_pdf_size), differenceInString));

        double percentage = (difference / originalSize) * 100;

        int percentageTxt = (int) percentage;
        if (percentageTxt > 0) {
            tv_compressed_percentage.setVisibility(View.VISIBLE);
            tv_compressed_percentage.setText(String.format(getString(R.string.file_is_compressed_by) + "%%", percentageTxt));
        } else {
            tv_compressed_percentage.setVisibility(View.GONE);
        }
        btn_ok.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPdfPasswordDialog(String pdfPath) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_pdf_password);

        EditText editText = dialog.findViewById(R.id.et_password);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        TextView tv_error_msg = dialog.findViewById(R.id.tv_error_msg);


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = editText.getText().toString().trim();
                if (TextUtils.isEmpty(enteredPassword)) {
                    String message = getString(R.string.please_enter_password);
                    showErrorMsg(tv_error_msg, message);
                    return;
                }
                if (enteredPassword.length() < 4) {
                    String message = getString(R.string.password_min_length);
                    showErrorMsg(tv_error_msg, message);
                    return;
                }
                try {
                    boolean isEncryptedSuccessfully = pdfUtils.doEncryption(context, pdfPath, enteredPassword, prefManager.getMasterPassword());
                    if (isEncryptedSuccessfully) {
                        Toast.makeText(context, getString(R.string.pdf_file_password_protected_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "" + getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showErrorMsg(TextView tv_error_msg, String message) {
        tv_error_msg.setVisibility(View.VISIBLE);
        tv_error_msg.setText(message);
    }

    private void sharePdfFile(String savedPdfPath) {
        File file = new File(savedPdfPath);
        if (file.isFile() && file.exists()) {
            ArrayList<Uri> uris = new ArrayList<>();
            Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            if (uriForFile != null) {

//                    uris.add(uriForFile);
                flashScanUtil.shareMultiple(uriForFile, PdfEditorActivity.this);
            }
                /*if (!uris.isEmpty()) {
                    flashScanUtil.shareMultiple(uris);
                }*/
        }
    }

    private void showPdfPasswordProtectedDialog(File file, File tempDir, String[] mInputPassword) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_pdf_password_protected);

        EditText et_password = dialog.findViewById(R.id.et_password);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        TextView tv_error_msg = dialog.findViewById(R.id.tv_error_msg);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredPassword = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(enteredPassword)) {
                    showErrorMsg(tv_error_msg, getString(R.string.please_enter_password));
                    return;
                }
                mInputPassword[0] = enteredPassword;
                boolean isPasswordCorrect = pdfUtils.checkEnteredPasswordIsCorrect(context, file.getPath(), mInputPassword);
                if (isPasswordCorrect) {
                    intentToPdfSignatureActivity(tempDir.getPath());
                    dialog.dismiss();

                } else {
                    showErrorMsg(tv_error_msg, getString(R.string.incorrect_password));
                    return;
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onAdClosed() {
        processAfterAd();
    }

    @Override
    public void onAdLoadedOrFailed(boolean isLoaded) {
        processAfterAd();
    }

    private void processAfterAd() {
        switch (featureClickedType) {
            case 0: {
                isAdShowedForWatermark = true;
                tv_anti_counterfeit.performClick();
                break;
            }
            case 1: {
                isAdShowedForSignature = true;
                tv_pdf_signature.performClick();
                break;
            }
            case 2: {
                isAdShowedForPassword = true;
                tv_pdf_password.performClick();
                break;
            }
        }
    }

    private void showRenameDialog(FileModel fileModel) {
        Dialog dialog = new Dialog(this);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.common_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextView msgHeading = dialog.findViewById(R.id.msg_heading);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextView btnOk = dialog.findViewById(R.id.btn_ok);
        EditText etPdfName = dialog.findViewById(R.id.et_pdf_name);

        dialogTitle.setText(getString(R.string.rename_file));
        msgHeading.setText(""); // Set to empty string
        etPdfName.setText(fileModel.getName() + ".pdf");
        etPdfName.setSelection(etPdfName.getText().length());


        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            String folderName = etPdfName.getText().toString().trim();
            if (folderName.startsWith(".") || folderName.contains("null")) {
                etPdfName.setError(getString(R.string.pdf_extension_name_error));
                etPdfName.requestFocus();
                return;
            }
            String extension = flashScanUtil.getExtensionFromFileName(folderName);
            if (extension != null) {
                if (!extension.equalsIgnoreCase("pdf")) {
                    etPdfName.setError(getString(R.string.pdf_extension_error));
                    etPdfName.requestFocus();
                    return;
                }
            }
            String folderNameWithoutExtension = folderName.replace(".pdf", "");

            if (TextUtils.isEmpty(folderNameWithoutExtension)) {
                Toast.makeText(PdfEditorActivity.this, getString(R.string.please_name_file), Toast.LENGTH_SHORT).show();
                return;
            } else if (folderNameWithoutExtension.equalsIgnoreCase(fileModel.getName())) {
                Toast.makeText(PdfEditorActivity.this, getString(R.string.file_name_same_msg), Toast.LENGTH_SHORT).show();
                return;
            }

            renameFolder(folderNameWithoutExtension, fileModel);
            //renameFromGoogleDriveById(fileModel, folderName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void renameFolder(String newFolderName, FileModel fileModel) {
        File oldFolder = new File(fileModel.getFolder(), fileModel.getName());
        File newFolder = new File(fileModel.getFolder(), newFolderName);
        boolean isRenamed = oldFolder.renameTo(newFolder);

        if (isRenamed) {
            AppController.getINSTANCE().dbHandler.updateFolderName(fileModel.getName(), newFolderName);
            AppController.getINSTANCE().dbHandler.updateApplyFilterFolder(newFolderName, fileModel.getName());

            String originalName = fileModel.getName();

            File dstOriginalFolderName = new File(FlashScanUtil.getDocOriginalPath(this), originalName);
            File tempOriginal = new File(FlashScanUtil.getDocOriginalPath(this), newFolderName);
            dstOriginalFolderName.renameTo(tempOriginal);

            File dstOriginalFolderName3 = new File(context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_DIRECTORY, originalName + ".pdf");
            File tempOriginal3 = new File(context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_DIRECTORY, newFolderName + ".pdf");
            dstOriginalFolderName3.renameTo(tempOriginal3);
            folderName = newFolderName;

            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.rename_success_msg));
            fileModel.setName(newFolderName);
            fileModel.setPath(newFolder.getPath());
            if (fileModel.isSavedOnGoogleDrive()) {
                String strFileId = fileModel.getGoogleDriveFolderId();
                flashScanUtil.deleteFolderByIdFromGoogleDrive(context, strFileId, getResources().getString(R.string.updating_files_to_google_drive), () -> {
                    prefManager.deleteFolderFromGoogleDriveDataList(strFileId);

                    flashScanUtil.saveFileInGoogleDrive(context, Constants.ROOT_FOLDER_NAME, fileModel, false, getResources().getString(R.string.updating_file_metadata), folderId -> {
                        savedPdfPath = tempOriginal3.getAbsolutePath();
                        proceedWithPdfPath(false);
                    });
                });
            } else {
                savedPdfPath = tempOriginal3.getAbsolutePath();
                proceedWithPdfPath(false);
            }
        } else {
            flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.same_folder_already_exist));
        }
    }

    @Override
    public void onFetchingComplete(List<FileModel> fileModelList) {
        progress_lay.setVisibility(View.GONE);
        Optional<FileModel> fileModelOptional = fileModelList.stream().filter(it -> it.getName().equalsIgnoreCase(folderName)).findFirst();
        FileModel fileModel = fileModelOptional.orElse(null);
        if (fileModel != null) {
            if (fileModel.getName().equalsIgnoreCase(folderName)) {
                showRenameDialog(fileModel);
            }
        }
    }

    @Override
    public void onFetchingStart() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    private void fetchFiles() {
        new GetFilesTask(
                this,
                "",
                this,
                Constants.RECENT_DOCS_COUNT_LIMIT,
                Constants.SORT_BY.modificationTimeDescending
        ).execute();
    }

    enum OnFeatureClicked {
        Watermark(0),
        Signature(1),
        Password(2);

        private int value;

        OnFeatureClicked(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private void createPdfDocumentsInCache() {
        File file = new File(savedPdfPath);
        if (file != null && file.exists()) {
            String directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
            String tempDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + directoryName;
            File tempDir = new File(tempDirPath);
            if (!tempDir.exists()) {
                createImagesFromPdf(savedPdfPath, null, null, 3);
            } else {
                copyFiles(tempDirPath);
            }
        }
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    @Override
    protected void onDestroy() {
        saveFinalPdf();
        super.onDestroy();
    }

    private void saveFinalPdf() {
        try {
            if (!TextUtils.isEmpty(savedPdfPath)) {

                String destinationPdfPath = flashScanUtil.getDefaultStorageLocationForPdf(context) + File.separator + folderName + ".pdf";
                File destinationPdf = new File(destinationPdfPath);

                File file = new File(savedPdfPath);
                if (destinationPdf.exists()) {
                    destinationPdf.delete();
                }
                if (!destinationPdf.exists()) {
                    destinationPdf.createNewFile();
                    copyFile(file, destinationPdf);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFiles(String path) {
        if (!TextUtils.isEmpty(savedPdfPath)) {
            String pdfNameFromPath = getPdfNameFromPath(savedPdfPath);
            folderName = pdfNameFromPath;

            File cacheMainDirectory = new File(flashScanUtil.getCacheStorageLocationForPdf(context));
            if (cacheMainDirectory.exists()) {
                deleteRecursive(cacheMainDirectory);
            }

            File dstFolderName = new File(flashScanUtil.getPdfProcessingPath(context), folderName);
            File dstOriginalFolderName = new File(flashScanUtil.getPdfOriginalPath(context), folderName);

            if (!dstFolderName.exists()) {
                dstFolderName.mkdirs();
            }
            if (!dstOriginalFolderName.exists())
                dstOriginalFolderName.mkdirs();

            String newSavedPdfPath = flashScanUtil.getCacheStorageLocationForPdf(context) + File.separator + folderName + ".pdf";
            File newPdfPath = new File(newSavedPdfPath);
            try {
                if (newPdfPath != null && !newPdfPath.exists()) {
                    newPdfPath.createNewFile();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            copyFile(new File(savedPdfPath), newPdfPath);
            savedPdfPath = newSavedPdfPath;

            File sourceFile = new File(path);
            if (sourceFile.exists() && sourceFile.isDirectory()) {
                File[] filesList = sourceFile.listFiles();
                if (filesList != null && filesList.length > 0) {
                    ArrayList<String> filesPath = new ArrayList<>();
                    for (File file : filesList) {
                        filesPath.add(file.getAbsolutePath());
                    }
                    new CopyFileTask(this, filesPath, dstFolderName.getAbsolutePath(), dstOriginalFolderName.getAbsolutePath(),
                            new CopyOperationListener() {
                                @Override
                                public void onCopyStart() {
                                    progress_lay.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onCopyComplete(int fileOperation) {
                                    progress_lay.setVisibility(View.GONE);
                                    proceedWithPdfPath(true);
                                }
                            }, false, false).execute();
                }
            }
        }
    }

    private static void copyFile(File sourceFile, File destinationFile) {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.i(TAG, "copyFiles::IOException::" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
