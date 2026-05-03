package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.cam.scanner.scantopdf.android.interfaces.FetchOcrDocumentsListener;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.models.GoogleDriveFolderModel;
import com.cam.scanner.scantopdf.android.models.enums.DocumentTypeEnum;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GetOcrDocuments extends AsyncTask<Void, Void, List<FileModel>> {
    private static final String TAG = GetOcrDocuments.class.getSimpleName();
    private WeakReference<Context> contextRef;
    private String directoryPath;
    private FetchOcrDocumentsListener fetchOcrDocumentsListener;
    private FlashScanUtil flashScanUtil;
    private int sortBy = -1;

    public GetOcrDocuments(Context context, String directoryPath, FetchOcrDocumentsListener fetchOcrDocumentsListener, int sortBy) {
        contextRef = new WeakReference<>(context);
        this.directoryPath = directoryPath;
        this.fetchOcrDocumentsListener = fetchOcrDocumentsListener;
        flashScanUtil = new FlashScanUtil(contextRef.get());
        this.sortBy = sortBy;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (fetchOcrDocumentsListener != null) {
            fetchOcrDocumentsListener.onFetchingStartOcr();
        }
    }

    @Override
    protected List<FileModel> doInBackground(Void... voids) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getFiles();
        } else {
            return getFiles();
        }
    }

    private List<FileModel> getFiles() {
        List<FileModel> list = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.isDirectory() && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                switch (sortBy) {
                    case Constants.SORT_BY.defaultOrder:
                    case Constants.SORT_BY.modificationTimeDescending:
                        setFilesByDescendingLastModified(files);
                        break;
                    case Constants.SORT_BY.modificationTimeAscending:
                        setFilesByAscendingLastModified(files);
                        break;
                    case Constants.SORT_BY.nameAtoZ:
                        sortFilesByNameAtoZ(files);
                        break;
                    case Constants.SORT_BY.nameZtoA:
                        sortFilesByNameZtoA(files);
                        break;
                }
                /*Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));*/
                for (File file : files) {
                    if (file != null && file.exists()) {
                        Log.e(TAG, "getFiles: "+ file.getPath());
                        FileModel fileModel = new FileModel();
                        fileModel.setPath(file.getPath());
                        fileModel.setFileExtension(flashScanUtil.getExtensionFromFileName(file.getName()));
                        fileModel.setName(file.getName());
                        fileModel.setDateTaken(file.lastModified());
                        fileModel.setSize(file.length());
                        fileModel.setFolder(file.getParent());
                        fileModel.setType(DocumentTypeEnum.OCR.getValue());
                        if (file.isDirectory()) {
                            File[] listFiles = file.listFiles();
                            if (listFiles != null && listFiles.length > 0) {
                                for (File eachFile : listFiles) {
                                    if (eachFile != null && eachFile.isFile() && eachFile.exists()) {
                                        /*fileModel.setThumbnailPath(eachFile.getPath());
                                        break;*/
                                        String extensionFromFileName = flashScanUtil.getExtensionFromFileName(eachFile.getName());
                                        if (!TextUtils.isEmpty(extensionFromFileName)) {
                                            if (extensionFromFileName.equalsIgnoreCase(Constants.FileExtensions.JPG) || extensionFromFileName.equalsIgnoreCase(Constants.FileExtensions.PNG)
                                                    || extensionFromFileName.equalsIgnoreCase(Constants.FileExtensions.JPEG)
                                                    || extensionFromFileName.equalsIgnoreCase(Constants.FileExtensions.GIF)) {
                                                fileModel.setThumbnailPath(eachFile.getPath());
                                                break;
                                            }
                                        }
                                    }
                                }
                                fileModel.setFileCountInFolder(listFiles.length);
                            }
                            GoogleDriveFolderModel googleDriveFolderModel = new PrefManager(contextRef.get()).isFolderExistOnGoogleDrive(fileModel.getName());
                            if (googleDriveFolderModel!=null){
                                fileModel.setSavedOnGoogleDrive(true);
                                fileModel.setGoogleDriveFolderId(googleDriveFolderModel.getId());
                            }
                        }
                        list.add(fileModel);
                    }
                }
            }
        }
        return list;
    }

    private void sortFilesByNameZtoA(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o2.getName().compareToIgnoreCase(o1.getName());
            }
        });
    }

    private void sortFilesByNameAtoZ(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }

    private void setFilesByAscendingLastModified(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o1.lastModified(), o2.lastModified()); // last modified descending
            }
        });
    }

    private void setFilesByDescendingLastModified(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o2.lastModified(), o1.lastModified()); // last modified descending
            }
        });
    }

    @Override
    protected void onPostExecute(List<FileModel> fileModels) {
        super.onPostExecute(fileModels);
        if (fetchOcrDocumentsListener != null) {
            fetchOcrDocumentsListener.onFetchingCompleted(fileModels);
        }

    }
}
