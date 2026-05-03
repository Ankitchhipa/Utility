package com.cam.scanner.scantopdf.android.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.DocumentListAdapter;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.DeleteFolderOrFileTask;
import com.cam.scanner.scantopdf.android.asynctasks.GetFilesTask;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.FileOrFolderDeleteListener;
import com.cam.scanner.scantopdf.android.interfaces.OnFetchingCompleted;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DocumentsListActivity extends AppCompatActivity implements OnFetchingCompleted,
        OnItemSelectListener, View.OnClickListener, CopyOperationListener, FileOrFolderDeleteListener {

    private Context context;
    private RecyclerView recyclerView;
    private TextView toolbar_title, tv_no_file;
    private EditText et_search;
    private ImageView iv_search, iv_back, iv_home;
    private DocumentListAdapter documentListAdapter;
    private FlashScanUtil util;

    public List<FileModel> getFetchedFoldersList() {
        if (fetchedFoldersList == null) {
            fetchedFoldersList = new ArrayList<>();
        }
        return fetchedFoldersList;
    }

    public void setFetchedFoldersList(List<FileModel> fetchedFoldersList) {
        this.fetchedFoldersList = fetchedFoldersList;
    }

    private List<FileModel> fetchedFoldersList = new ArrayList<>();
    private int fileOperationAction;
    private String sourceFilePath;
    private ArrayList<String> filePathList;
    private String sourceFolderPath;
    private LinearLayout ll_no_document;
    private View progress_lay;
    private Button btn_progress_lay;
    private PrefManager prefManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents_list);

        findIds();
        setOnClickListeners();
        initObjects();
        fetchFiles();
        manageSearchedFolders();
        getIntentData();
    }

    private void getIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FILE_OPERATION_ACTION)) {
            fileOperationAction = getIntent().getIntExtra(Constants.PutExtraConstants.FILE_OPERATION_ACTION, 0);
        }
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FILE_PATH)) {
            sourceFilePath = getIntent().getStringExtra(Constants.PutExtraConstants.FILE_PATH);
        }
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FILE_PATH_LIST)) {
            filePathList = getIntent().getStringArrayListExtra(Constants.PutExtraConstants.FILE_PATH_LIST);
        }

        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FOLDER_PATH)) {
            sourceFolderPath = getIntent().getStringExtra(Constants.PutExtraConstants.FOLDER_PATH);
        }
    }

    private void setOnClickListeners() {
        iv_search.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        btn_progress_lay.setOnClickListener(this);
    }

    private void fetchFiles() {
        new GetFilesTask(context, "", this, Constants.RECENT_DOCS_COUNT_LIMITLESS, prefManager.getAppSortingOrder()).execute();
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

    private void filter(String text) {
        List<FileModel> filterFileList = new ArrayList<>();
        if (getFetchedFoldersList() != null && !getFetchedFoldersList().isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            ll_no_document.setVisibility(View.GONE);
            for (FileModel fileModel : getFetchedFoldersList()) {
                if (fileModel.getName().toLowerCase().contains(text.toLowerCase())) {
                    filterFileList.add(fileModel);
                }
            }
            if (documentListAdapter != null && !filterFileList.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                tv_no_file.setVisibility(View.GONE);
                documentListAdapter.filterList(filterFileList);
            } else {
                recyclerView.setVisibility(View.GONE);
                tv_no_file.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            ll_no_document.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
        }
    }

    private void findIds() {
        recyclerView = findViewById(R.id.rv_scanner_files);
        toolbar_title = findViewById(R.id.toolbar_title);
        et_search = findViewById(R.id.et_search);
        tv_no_file = findViewById(R.id.tv_no_file);
        iv_search = findViewById(R.id.iv_search);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setVisibility(View.VISIBLE);
        iv_home = findViewById(R.id.iv_home);
        iv_home.setVisibility(View.GONE);
        ll_no_document = findViewById(R.id.ll_no_document);
        progress_lay = findViewById(R.id.progress_lay);
        btn_progress_lay = findViewById(R.id.btn_progress_lay);
    }

    private void initObjects() {
        context = this;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        /*DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);*/
        toolbar_title.setText(getString(R.string.select_target_document));
        util = new FlashScanUtil(context);
        prefManager = new PrefManager(context);
    }

    @Override
    public void onFetchingComplete(List<FileModel> fileModelList) {
        progress_lay.setVisibility(View.GONE);
        if (fileModelList != null && !fileModelList.isEmpty()) {
            if (!getFetchedFoldersList().isEmpty()) {
                getFetchedFoldersList().clear();
            }
            getFetchedFoldersList().addAll(fileModelList);
            showRecyclerView(fileModelList);
        } else {
            hideRecyclerView();
        }
    }

    @Override
    public void onFetchingStart() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    private void showRecyclerView(List<FileModel> fileModelList) {
        ll_no_document.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);


        documentListAdapter = new DocumentListAdapter(context, fileModelList, this);
        recyclerView.setAdapter(documentListAdapter);
    }

    private void hideRecyclerView() {
        recyclerView.setVisibility(View.GONE);
        ll_no_document.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelect(Object o) {
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }
        if (fileModel == null)
            return;

        switch (fileOperationAction) {
            case Constants.FileOperations.ACTION_COPY:
                if (!TextUtils.isEmpty(sourceFolderPath)) {
                    if (sourceFolderPath.equalsIgnoreCase(fileModel.getPath())) {
                        util.showSnackBar(findViewById(android.R.id.content), getString(R.string.unable_to_copy_in_same_folder));
                        return;
                    }
                }
                copyOperation(fileModel.getPath());
                break;
            case Constants.FileOperations.ACTION_MOVE:
                if (!TextUtils.isEmpty(sourceFolderPath)) {
                    if (sourceFolderPath.equalsIgnoreCase(fileModel.getPath())) {
                        util.showSnackBar(findViewById(android.R.id.content), getString(R.string.unable_to_move_in_same_folder));
                        return;
                    }
                }
                moveOperation(fileModel.getPath());
                break;
        }
    }

    private void moveOperation(String destFilePath) {

        File dstOriginalFolderName = new File(util.getDocOriginalPath(context), new File(destFilePath).getName());

        if (!dstOriginalFolderName.exists())
            dstOriginalFolderName.mkdirs();

        if (filePathList != null && !filePathList.isEmpty()) {
            if (!TextUtils.isEmpty(destFilePath)) {
                new CopyFileTask(context, filePathList, destFilePath, dstOriginalFolderName.getAbsolutePath(), this, Constants.FileOperations.ACTION_MOVE, true).execute();
            }
        } else {
            if (!TextUtils.isEmpty(sourceFilePath) || !TextUtils.isEmpty(destFilePath)) {
                new CopyFileTask(context, sourceFilePath, destFilePath, dstOriginalFolderName.getAbsolutePath(), this, Constants.FileOperations.ACTION_MOVE, true).execute();
            }
        }

    }

    private void copyOperation(String destFilePath) {
        File dstOriginalFolderName = new File(util.getDocOriginalPath(context), new File(destFilePath).getName());

        if (!dstOriginalFolderName.exists())
            dstOriginalFolderName.mkdirs();

        if (filePathList != null && !filePathList.isEmpty()) {
            if (!TextUtils.isEmpty(destFilePath)) {
                new CopyFileTask(context, filePathList, destFilePath, dstOriginalFolderName.getAbsolutePath(), this, Constants.FileOperations.ACTION_COPY, true).execute();
            }
        } else {
            if (!TextUtils.isEmpty(sourceFilePath) || !TextUtils.isEmpty(destFilePath)) {
                new CopyFileTask(context, sourceFilePath, destFilePath, dstOriginalFolderName.getAbsolutePath(), this, Constants.FileOperations.ACTION_COPY, true).execute();
            }
        }

    }

    @Override
    public void onItemLongPress(Object o) {
        FileModel fileModel = null;
        if (o != null) {
            if (o instanceof FileModel) {
                fileModel = (FileModel) o;
            }
        }

    }

    @Override
    public void onItemAction(Object o, View view) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_search) {
            handleSearchBarVisibility();
        } else if (id == R.id.iv_back) {
            onBackPressed();
        } else if (id == R.id.btn_progress_lay) {
        }
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
        if (documentListAdapter != null) {
            documentListAdapter.clearFilterList(getFetchedFoldersList());
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (et_search != null && et_search.getVisibility() == View.VISIBLE) {
            clearSearchView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCopyStart() {

    }

    @Override
    public void onCopyComplete(int fileOperationAction) {
        if (fileOperationAction == 0)
            return;
        switch (fileOperationAction) {
            case Constants.FileOperations.ACTION_COPY:
                Toast.makeText(context, "" + getString(R.string.file_copied_successfully), Toast.LENGTH_SHORT).show();
                navigateToMainScreen();
                break;
            case Constants.FileOperations.ACTION_MOVE:
                if (filePathList != null && !filePathList.isEmpty()) {
                    new DeleteFolderOrFileTask(context,filePathList, this).execute();
                } else {
                    if (!TextUtils.isEmpty(sourceFilePath))
                        new DeleteFolderOrFileTask(DocumentsListActivity.this,sourceFilePath, this).execute();
                }

                break;
        }

    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(context, MainActivity.class);
        /*intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
        startActivity(intent);
        finish();
    }

    @Override
    public void onFileOrFolderDeleted() {
        Toast.makeText(context, "" + getString(R.string.file_moved_successfully), Toast.LENGTH_SHORT).show();
        navigateToMainScreen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideKeyboard();
    }

}
