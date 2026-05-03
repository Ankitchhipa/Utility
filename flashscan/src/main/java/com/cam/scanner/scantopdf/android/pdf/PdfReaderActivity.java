package com.cam.scanner.scantopdf.android.pdf;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.ScanResultActivity;
import com.cam.scanner.scantopdf.android.adapters.PdfToImagesImportAdapter;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.interfaces.PdfToImageCallback;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.ScanConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfReaderActivity extends AppCompatActivity implements OnItemSelectListener, View.OnClickListener {

    private FlashScanUtil flashScanUtil;
    private Context context;
    private static final String TAG = PdfReaderActivity.class.getSimpleName();
    private PdfUtils pdfUtils;
    private View progress_lay;
    private TextView tv_folder_name, tv_import, tv_total_file_count;
    private RecyclerView recyclerView;
    private PdfToImagesImportAdapter pdfToImagesImportAdapter;
    private Button btn_select_all, btn_progress_lay;
    private List<FileModel> fetchedImagesList = new ArrayList<>();
    private CheckBox chkBoxSelectAll;
    private String[] mInputPassword = null;
    private boolean isImportPdfFromWithInApp = false;


    public List<FileModel> getFetchedImagesList() {
        if (fetchedImagesList == null) {
            fetchedImagesList = new ArrayList<>();
        }
        return fetchedImagesList;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);

        init();
        findIds();
        setClickListeners();


        if (getIntent() != null && getIntent().hasExtra(Constants.IS_IMPORT_PDF_FROM_WITHIN_APP)) {
            isImportPdfFromWithInApp = getIntent().getBooleanExtra(Constants.IS_IMPORT_PDF_FROM_WITHIN_APP, false);
        } else {
            isImportPdfFromWithInApp = false;
        }
        if (!isImportPdfFromWithInApp) {    // from outside of app (i.e. from bottom launcher popup)
            if (getIntent() != null) {
                Uri uri = getIntent().getData();
                if (uri != null) {
                    Log.e(TAG, "uri : " + uri);
                    String path = flashScanUtil.getRealPdfPathFromUri(uri);
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (file.isFile() && file.exists()) {
                            String extensionFromFileName = flashScanUtil.getExtensionFromFileName(file.getName());
                            if (!TextUtils.isEmpty(extensionFromFileName) && extensionFromFileName.equalsIgnoreCase(Constants.FileExtensions.PDF)) {
                                createImages(path, uri);
                            } else {
                                finishAffinity();
                            }
                        } else {
                            finishAffinity();
                        }

                    } else {
                        finishAffinity();
                    }
                } else {
                    finishAffinity();
                }
            }
        } else {
            Uri uri = null;
            if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.URI)) {
                uri = getIntent().getParcelableExtra(Constants.PutExtraConstants.URI);
            }
            if (uri != null) {
                String path = flashScanUtil.getRealPdfPathFromUri(uri);
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (file.isFile() && file.exists()) {
                        String extensionFromFileName = flashScanUtil.getExtensionFromFileName(file.getName());
                        if (!TextUtils.isEmpty(extensionFromFileName) && extensionFromFileName.equalsIgnoreCase(Constants.FileExtensions.PDF)) {
                            createImages(path, uri);
                        } else {
                            finish();
                        }
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            } else {
                finish();
            }
        }

    }

    private void setClickListeners() {
        tv_import.setOnClickListener(this);
        btn_select_all.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
    }

    private void findIds() {
        progress_lay = findViewById(R.id.progress_lay);
        tv_folder_name = findViewById(R.id.tv_folder_name);
        tv_import = findViewById(R.id.tv_import);
        recyclerView = findViewById(R.id.recyclerView);
        setUpRecyclerView(recyclerView);
        btn_select_all = findViewById(R.id.btn_select_all);
        chkBoxSelectAll = findViewById(R.id.chkBoxSelectAll);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
        tv_total_file_count = findViewById(R.id.tv_total_file_count);
    }

    private void setUpRecyclerView(RecyclerView recyclerView) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void createImages(String path, Uri uri) {

        pdfUtils.isPdfEncrypted(context,path, "Reader",new PdfEncryptionCallBack() {
            @Override
            public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                if (isSuccess && isEncrypted) {
                    mInputPassword = new String[1];
                    Toast.makeText(context, getString(R.string.pdf_is_pswd_ptd), Toast.LENGTH_SHORT).show();
                    showPasswordProtectedPdfDialog(path, uri, mInputPassword);

                } else {
                    if (isSuccess) {
                        pdfToImage(mInputPassword, path, uri);
                    } else {
                        if (isEncrypted) {
                            mInputPassword = new String[1];
                            Toast.makeText(context, getString(R.string.pdf_is_pswd_ptd), Toast.LENGTH_SHORT).show();
                            showPasswordProtectedPdfDialog(path, uri, mInputPassword);
                        }

                    }
                }
            }
        });
    }

    private void showPasswordProtectedPdfDialog(String path, Uri uri, String[] mInputPassword) {
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
                if (!isImportPdfFromWithInApp) {
                    finishAffinity();
                } else {
                    finish();
                }

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
                    pdfToImage(mInputPassword, path, uri);
                } else {
                    Toast.makeText(context, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
                }
                /*pdfToImage(mInputPassword, path, uri);*/
            }
        });

        dialog.show();
    }

    private void pdfToImage(String[] enteredPassword, String path, Uri uri) {
        new PdfToImagesAsyncTask(context, enteredPassword, path, uri, new PdfToImageCallback() {
            @Override
            public void onConversionStart() {
                Log.e(TAG, "onConversionStart called");
                progress_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onConversionCompleted(String savedDirPath, boolean isSuccess) {
                Log.e(TAG, "onConversionCompleted called");
                progress_lay.setVisibility(View.GONE);
                if (isSuccess && !TextUtils.isEmpty(savedDirPath)) {
                    setFolderNameInToolbar(savedDirPath);
                    fetchFilesFromDir(savedDirPath);
                } else {
                    Toast.makeText(context, getString(R.string.pdf_is_pswd_ptd), Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            }
        }).execute();
    }

    private void fetchFilesFromDir(String savedDirPath) {
        File dir = new File(savedDirPath);
        if (dir.isDirectory() && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                List<FileModel> list = new ArrayList<>();
                for (File file : files) {
                    FileModel fileModel = new FileModel();
                    fileModel.setPath(file.getPath());
                    fileModel.setChecked(true);
                    list.add(fileModel);
                }
                if (!list.isEmpty()) {
                    if (!getFetchedImagesList().isEmpty()) {
                        getFetchedImagesList().clear();
                    }
                    getFetchedImagesList().addAll(list);
                    populateRV(getFetchedImagesList());
                }
            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_image_found_in_this_pdf));
            }
        }

    }

    private void populateRV(List<FileModel> list) {
        pdfToImagesImportAdapter = new PdfToImagesImportAdapter(context, list, this);
        pdfToImagesImportAdapter.setSelectedImagesList(list);
        recyclerView.setAdapter(pdfToImagesImportAdapter);
        handleImportImagesCount();
        handleSelectAllView();
    }

    private void handleSelectAllView() {
        if (pdfToImagesImportAdapter != null) {
            if (getFetchedImagesList().size() == pdfToImagesImportAdapter.getSelectedImagesList().size()) {
                chkBoxSelectAll.setChecked(true);
            } else {
                chkBoxSelectAll.setChecked(false);
            }
        }

    }

    private void handleImportImagesCount() {
        if (pdfToImagesImportAdapter != null) {
            tv_import.setText(getString(R.string.str_import));
            tv_total_file_count.setText("" + pdfToImagesImportAdapter.getSelectedImagesList().size() + " " + getString(R.string.selected));
        }

    }

    private void setFolderNameInToolbar(String savedDirPath) {
        File dir = new File(savedDirPath);
        if (dir.isDirectory() && dir.exists()) {
            tv_folder_name.setText(dir.getName());
        }
    }

    private void init() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        pdfUtils = new PdfUtils();
    }

    @Override
    public void onItemSelect(Object o) {
        handleImportImagesCount();
        handleSelectAllView();
    }

    @Override
    public void onItemLongPress(Object o) {

    }

    @Override
    public void onItemAction(Object o, View view) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_import) {
            importSelectedImages();
        } else if (id == R.id.btn_select_all) {
            if (!chkBoxSelectAll.isChecked()) {
                selectAllImages();
            } else {
                deSelectAllImages();
            }
        } else if (id == R.id.btn_progress_lay) {
        }
    }

    private void deSelectAllImages() {
        if (pdfToImagesImportAdapter != null) {
            pdfToImagesImportAdapter.deSelectAllImages();
        }
        handleImportImagesCount();
        handleSelectAllView();
    }

    private void selectAllImages() {
        if (pdfToImagesImportAdapter != null) {
            pdfToImagesImportAdapter.selectAllImages();
        }
        handleImportImagesCount();
        handleSelectAllView();
    }

    private void importSelectedImages() {
        if (pdfToImagesImportAdapter != null) {
            List<FileModel> selectedImagesList = pdfToImagesImportAdapter.getSelectedImagesList();
            if (selectedImagesList != null && !selectedImagesList.isEmpty()) {
                ArrayList<String> pathList = new ArrayList<>();
                for (FileModel fileModel : selectedImagesList) {
                    pathList.add(fileModel.getPath());
                }
                if (!pathList.isEmpty()) {
                    moveSelectedImagesToDocument(pathList);
                }

            } else {
                flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.please_select_images));
            }
        }
    }

    private void moveSelectedImagesToDocument(ArrayList<String> pathList) {

        String folderName = flashScanUtil.getFolderCurrentTime();

        File dstFolderName = new File(flashScanUtil.getDocProcessingPath(context), folderName);
        File dstOriginalFolderName = new File(flashScanUtil.getDocOriginalPath(context), folderName);

        if (!dstFolderName.exists())
            dstFolderName.mkdirs();

        if (!dstOriginalFolderName.exists())
            dstOriginalFolderName.mkdirs();

        new CopyFileTask(this, pathList, dstFolderName.getAbsolutePath(), dstOriginalFolderName.getAbsolutePath(),
                new CopyOperationListener() {
                    @Override
                    public void onCopyStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCopyComplete(int fileOperation) {
                        progress_lay.setVisibility(View.GONE);
                        moveToScanResultActivity(folderName);
                    }
                }, true).execute();

        /*String rootPath = Environment.DIRECTORY_PICTURES + File.separator + Constants.ROOT_FOLDER_NAME;
        String relativePath = Environment.getExternalStoragePublicDirectory(rootPath).toString();
        String directoryName = tv_folder_name.getText().toString().trim();
        boolean isDirectoryCreated = false;
        File destDir = null;
        if (!TextUtils.isEmpty(directoryName)) {
            destDir = new File(relativePath, directoryName);

            *//*if (!destDir.exists()) {
                isDirectoryCreated = destDir.mkdirs();
            } else {
                if (destDir.isDirectory()) {
                    isDirectoryCreated = true;
                }
            }*//*
            int i = 1;
            while (destDir.exists()) {
                destDir = new File(relativePath, directoryName + "(" + i + ")");
                i++;
            }
            isDirectoryCreated = destDir.mkdirs();
        }
        String destinationPath = null;
        if (isDirectoryCreated) {
            if (destDir.isDirectory() && destDir.exists()) {
                destinationPath = destDir.getPath();
            }
        }
        if (!TextUtils.isEmpty(destinationPath)) {
            String finalDestinationPath = destinationPath;
            new CopyFileTask(pathList, finalDestinationPath, new CopyOperationListener() {
                @Override
                public void onCopyStart() {
                    progress_lay.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCopyComplete(int fileOperation) {
                    progress_lay.setVisibility(View.GONE);
                    long dateTaken = 0;
                    String folderName = null;
                    if (!TextUtils.isEmpty(finalDestinationPath)) {
                        File destDir = new File(finalDestinationPath);
                        if (destDir.isDirectory() && destDir.exists()) {
                            folderName = destDir.getName();
                            dateTaken = destDir.lastModified();
                        }
                    }
                    moveToScanResultActivity(folderName, dateTaken);
                }
            }, Constants.FileOperations.ACTION_MOVE).execute();
        }*/
    }

    private void moveToScanResultActivity(String folderName) {
        Intent intent = new Intent(context, ScanResultActivity.class);
        if (!isImportPdfFromWithInApp) {
            intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_PDF_TO_IMAGES_IMPORT);
        }
        intent.putExtra(ScanConstants.PutExtraConstants.FOLDER_NAME, folderName);
//        intent.putExtra(ScanConstants.PutExtraConstants.DATE_TAKEN, dateTaken);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

}
