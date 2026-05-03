package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.SelectedImagesListAdapter;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.MoveDirectoryTask;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.MoveDirectoryListener;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.ScanConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// currently not in use
public class SelectedImagesListActivity extends AppCompatActivity implements OnItemSelectListener, CopyOperationListener, View.OnClickListener {

    private static final int REQUEST_CODE_EDIT_SELECTED_IMAGE = 101;
    private Context context;
    private static final String TAG = SelectedImagesListActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private ArrayList<String> selectedImagesPathList;
    private FlashScanUtil flashScanUtil;
    private File tempDir;
    private View progress_lay;
    private String tempFolderName;
    private ImageView iv_done;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_images_list);

        init();
        findIds();
        setClickListeners();
        getSelectedImagesList();
    }

    private void setClickListeners() {
        iv_done.setOnClickListener(this);
    }

    private void getSelectedImagesList() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.SELECTED_IMAGES_LIST)) {
            selectedImagesPathList = getIntent().getStringArrayListExtra(Constants.PutExtraConstants.SELECTED_IMAGES_LIST);
        }
        if (selectedImagesPathList != null && !selectedImagesPathList.isEmpty()) {
            //clear previous temp files before creating  new temp files

            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                flashScanUtil.clearTempFiles(externalFilesDir);
            }
            //----------
            tempFolderName = flashScanUtil.getFolderCurrentTime();
            tempDir = new File(context.getExternalFilesDir(null), tempFolderName);
            boolean isDirectoryCreated = false;
            if (!tempDir.exists()) {
                isDirectoryCreated = tempDir.mkdirs();
            } else {
                if (tempDir.isDirectory()) {
                    isDirectoryCreated = true;
                }
            }
            if (isDirectoryCreated) {
                String destinationDirPath = tempDir.getPath();
                if (!TextUtils.isEmpty(destinationDirPath)) {
                    new CopyFileTask(selectedImagesPathList, destinationDirPath, this, Constants.FileOperations.ACTION_COPY).execute();
                }

            }
        }

    }


    private void populateRV(List<FileModel> selectedImagesList) {
        SelectedImagesListAdapter selectedImagesListAdapter = new SelectedImagesListAdapter(context, selectedImagesList, this);
        recyclerView.setAdapter(selectedImagesListAdapter);
    }

    private void findIds() {
        recyclerView = findViewById(R.id.recyclerView);
        setUpRecyclerView();
        progress_lay = findViewById(R.id.progress_lay);
        iv_done = findViewById(R.id.iv_done);
        iv_done.setVisibility(View.VISIBLE);
    }

    private void setUpRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void init() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
    }

    @Override
    public void onItemSelect(Object o) {
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel != null) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_SELECTED_IMAGE:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e(TAG, "RESULT_OK");
                        String folderName = null;
                        if (data != null && data.hasExtra(Constants.PutExtraConstants.FOLDER_NAME)) {
                            folderName = data.getStringExtra(Constants.PutExtraConstants.FOLDER_NAME);
                        }
                        if (!TextUtils.isEmpty(folderName)) {
                            refreshList(folderName);
                        }
                        break;
                    case RESULT_CANCELED:
                        Log.e(TAG, "RESULT_CANCELED");
                        break;
                }
                break;
        }
    }

    private void refreshList(String folderName) {
        tempFolderName = folderName;
        String relativePath = getExternalFilesDir(null) + File.separator + tempFolderName;
        File file = new File(relativePath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                List<FileModel> selectedImagesList = new ArrayList<>();
                for (File eachFile : files) {
                    FileModel fileModel = new FileModel();
                    fileModel.setPath(eachFile.getPath());
                    selectedImagesList.add(fileModel);
                }
                if (!selectedImagesList.isEmpty()) {
                    populateRV(selectedImagesList);
                }
            }
        }
    }

    @Override
    public void onItemLongPress(Object o) {

    }

    @Override
    public void onItemAction(Object o, View view) {

    }

    @Override
    public void onCopyStart() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCopyComplete(int fileOperation) {
        progress_lay.setVisibility(View.GONE);
        if (tempDir != null && tempDir.exists() && tempDir.isDirectory()) {
            File[] files = tempDir.listFiles();
            if (files != null && files.length > 0) {
                List<FileModel> selectedImagesList = new ArrayList<>();
                for (File file : files) {
                    if (file != null && file.exists() && file.isFile()) {
                        FileModel fileModel = new FileModel();
                        fileModel.setPath(file.getPath());
                        selectedImagesList.add(fileModel);
                    }
                }
                if (!selectedImagesList.isEmpty()) {
                    populateRV(selectedImagesList);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_done) {
            if (!TextUtils.isEmpty(tempFolderName)) {
                String srcDirPath = getExternalFilesDir(null) + File.separator + tempFolderName;
                String relativePath = Environment.DIRECTORY_PICTURES + File.separator + Constants.ROOT_FOLDER_NAME;
                String destDirPath = Environment.getExternalStoragePublicDirectory(relativePath).toString();
                new MoveDirectoryTask(context, srcDirPath, destDirPath, new MoveDirectoryListener() {
                    @Override
                    public void onMovingStart() {
                        progress_lay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onMoveCompleted() {
                        progress_lay.setVisibility(View.GONE);
                        File externalFilesDir = context.getExternalFilesDir(null);
                        if (externalFilesDir != null) {
                            flashScanUtil.clearTempFiles(externalFilesDir);
                        }
                        String documentPath = destDirPath + File.separator + tempFolderName;
                        File file = new File(documentPath);
                        if (file.isDirectory() && file.exists()) {
                            intentToScanResultActivity(file, tempFolderName);
                        }
                    }
                }).execute();
            }
        }
    }

    private void intentToScanResultActivity(File file, String tempFolderName) {
        Intent intent = new Intent(context, ScanResultActivity.class);
        intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_SELECTED_IMAGES_LIST_SCREEN);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_NAME, tempFolderName);
        intent.putExtra(ScanConstants.PutExtraConstants.DATE_TAKEN, file.lastModified());
        startActivity(intent);
        finish();
    }
}
