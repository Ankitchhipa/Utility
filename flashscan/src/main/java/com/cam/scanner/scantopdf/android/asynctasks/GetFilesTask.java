package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.cam.scanner.scantopdf.android.interfaces.OnFetchingCompleted;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.models.GoogleDriveChildFileModel;
import com.cam.scanner.scantopdf.android.models.GoogleDriveFolderModel;
import com.cam.scanner.scantopdf.android.models.enums.DocumentTypeEnum;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GetFilesTask extends AsyncTask<Void, Void, List<FileModel>> {

    Context context;
    private OnFetchingCompleted onFetchingCompleted;
    private WeakReference<Context> contextRef;
    private static final String TAG = GetFilesTask.class.getSimpleName();
    private String folderName;
    private FlashScanUtil flashScanUtil;
    private int limitedCount = -1;
    private int sortBy = -1;

    public GetFilesTask(Context context, String folderName, OnFetchingCompleted onFetchingCompleted, int limitedCount, int sortBy) {
        this.context = context;
        contextRef = new WeakReference<>(context);
        this.onFetchingCompleted = onFetchingCompleted;
        this.folderName = folderName;
        flashScanUtil = new FlashScanUtil(context);
        this.limitedCount = limitedCount;
        this.sortBy = sortBy;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (onFetchingCompleted != null) {
            onFetchingCompleted.onFetchingStart();
        }
    }

    @Override
    protected List<FileModel> doInBackground(Void... voids) {
        return getFiles();
    }

    private List<FileModel> getFiles() {

        GoogleDriveFolderModel googleDriveFolderModel = new PrefManager(contextRef.get()).isFolderExistOnGoogleDrive(folderName);
        List<GoogleDriveChildFileModel> googleDriveChildFileModelList = null;
        int cloudFilesCount = 0;
        if (googleDriveFolderModel != null) {
            googleDriveChildFileModelList = googleDriveFolderModel.getGoogleDriveChildFileModelList();
            Log.e(TAG, "folderName getFiles: " + new Gson().toJson(googleDriveFolderModel));
        }

        List<FileModel> list = new ArrayList<>();
//        String relativePath = Environment.DIRECTORY_PICTURES + "/" + Constants.ROOT_FOLDER_NAME + "/" + folderName;
        String imagesDir;
        try {
            imagesDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + folderName;
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                imagesDir = contextRef.get().getExternalFilesDir(relativePath).toString();
            } else {
                imagesDir = Environment.getExternalStoragePublicDirectory(relativePath).toString();
            }*/
            File directory = new File(imagesDir);

            Log.e(TAG, "getFiles: directory = " + directory.getAbsolutePath());

            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (sortBy != Constants.SORT_BY.noChange) {
                    Collections.sort(Arrays.asList(files));     // sort files in ascending order to avoid shuffling from external storage
                }

                if (files != null && files.length != 0) {
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
                    int fileNumber = 0;
                    Log.e(TAG, "getFiles: files.length = " + files.length);
                    for (File file : files) {
                        if (file.getName().contains(Constants.originalFolderName))
                            continue;
                        // file may be a file or directory
                        if (file.exists()) {
                            if (file.isFile()) {
                                if (!TextUtils.isEmpty(file.getName()) && file.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                    continue;
                                }
                            }

                            Log.e(TAG, "getFiles: Name = " + file.getName());
                            FileModel fileModel = new FileModel();
                            if (file.isFile()) {
                                fileNumber++;
                                fileModel.setFileNumber(fileNumber);
                            }
                            fileModel.setPath(file.getPath());
                            fileModel.setFileExtension(flashScanUtil.getExtensionFromFileName(file.getName()));
                            fileModel.setName(file.getName());
                            /*fileModel.setCreatedTime(flashScanUtil.getFileCreationTime(Paths.get(file.getAbsolutePath())));*/
                            fileModel.setDateTaken(file.lastModified());
                            fileModel.setSize(file.length());
                            fileModel.setFolder(file.getParent());
                            fileModel.setType(DocumentTypeEnum.Document.getValue());
                            fileModel.setStarred(isFav(file.getParent(), file.getName()));

                            if (googleDriveFolderModel != null && googleDriveChildFileModelList != null) {
                                for (GoogleDriveChildFileModel childFileModel : googleDriveChildFileModelList) {
                                    if (file.getName().equalsIgnoreCase(childFileModel.getFileName())) {
                                        fileModel.setSavedOnGoogleDrive(childFileModel.isFileAlreadyExist());
                                        fileModel.setGoogleDriveFolderId(childFileModel.getFileId());
                                        break;
                                    }
                                }
                            }

                            Log.e(TAG, "getFiles: " + new Gson().toJson(fileModel));

                            if (fileModel.isSavedOnGoogleDrive()) {
                                cloudFilesCount++;
                            }
                            Log.e(TAG, "cloudFilesCount = " + cloudFilesCount);

                            if (file.isDirectory()) {  // only for main launch activity for root folder
                                File[] listFiles = file.listFiles();
                                if (listFiles != null && listFiles.length > 0) {
                                    for (File eachFile : listFiles) {
                                        if (eachFile != null && eachFile.isFile() && eachFile.exists()) {
                                            if (eachFile.length() <= 0) {
                                                eachFile.delete();
                                            }
                                        }
                                    }

                                    /*switch (sortBy) {
                                        case Constants.SORT_BY.defaultOrder:
                                        case Constants.SORT_BY.modificationTimeDescending:
                                            setFilesByDescendingLastModified(listFiles);
                                            break;
                                        case Constants.SORT_BY.modificationTimeAscending:
                                            setFilesByAscendingLastModified(listFiles);
                                            break;
                                        case Constants.SORT_BY.nameAtoZ:
                                            sortFilesByNameAtoZ(listFiles);
                                            break;
                                        case Constants.SORT_BY.nameZtoA:
                                            sortFilesByNameZtoA(listFiles);
                                            break;
                                    }*/
                                    for (File eachFile : listFiles) {
                                        if (eachFile != null && eachFile.isFile() && eachFile.exists()) {
                                            /*fileModel.setThumbnailPath(eachFile.getPath());
                                            break;*/
                                           /* if(eachFile.length()<=0){
                                                eachFile.delete();
                                            }*/
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
                                    boolean isMetaDataJsonFileExist = false;
                                    for (File eachFile : listFiles) {
                                        if (eachFile != null && eachFile.isFile() && eachFile.exists()) {
                                            if (!TextUtils.isEmpty(eachFile.getName()) && eachFile.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                                isMetaDataJsonFileExist = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (isMetaDataJsonFileExist)
                                        fileModel.setFileCountInFolder(listFiles.length - 1);
                                    else fileModel.setFileCountInFolder(listFiles.length);
                                }

                                GoogleDriveFolderModel parentGoogleDriveFolderModel = new PrefManager(contextRef.get()).isFolderExistOnGoogleDrive(fileModel.getName());
                                if (parentGoogleDriveFolderModel != null) {
                                    fileModel.setSavedOnGoogleDrive(true);
                                    fileModel.setGoogleDriveFolderId(parentGoogleDriveFolderModel.getId());
                                    Log.e(TAG, "getFiles: googleDriveFolderModel!=null { "+fileModel.isSavedOnGoogleDrive()+", "+ fileModel.getGoogleDriveFolderId()+" }");
                                }
                                Log.e(TAG, "File Name" + file.getName());
                            }

                            list.add(fileModel);
                        }
                    }

                    /*switch (sortBy) {
                        case Constants.SORT_BY.defaultOrder:
                        case Constants.SORT_BY.modificationTimeDescending:
                            setFilesByDescendingLastModified(list);
                            break;
                        case Constants.SORT_BY.creationTimeAscending:
                            *//*setFilesByAscendingCreatedTime(list);*//*
                            break;
                        case Constants.SORT_BY.creationTimeDescending:
                            *//*setFilesByDescendingCreatedTime(list);*//*
                            break;
                        case Constants.SORT_BY.modificationTimeAscending:
                            setFilesByAscendingLastModified(list);
                            break;
                        case Constants.SORT_BY.nameAtoZ:
                            sortFilesByNameAtoZ(list);
                            break;
                        case Constants.SORT_BY.nameZtoA:
                            sortFilesByNameZtoA(list);
                            break;
                    }*/
                } else {
                    directory.delete();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void sortFilesByNameZtoA(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });
    }

    private void sortFilesByNameAtoZ(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
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
                return Long.compare(o1.lastModified(), o2.lastModified());
            }
        });
        Collections.reverse(Arrays.asList(files)); // to reverse the ascending array
    }

    private void sortFilesByNameZtoA(List<FileModel> files) {
        Collections.sort(files, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel f1, FileModel f2) {
                return f2.getName().compareToIgnoreCase(f1.getName());
            }
        });
    }

    private void sortFilesByNameAtoZ(List<FileModel> files) {
        Collections.sort(files, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel f1, FileModel f2) {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
    }

    private void setFilesByAscendingLastModified(List<FileModel> files) {
        Collections.sort(files, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel f1, FileModel f2) {
                return Long.compare(f1.getDateTaken(), f2.getDateTaken());    // last modified ascending
            }
        });
    }

    private void setFilesByDescendingLastModified(List<FileModel> files) {
        Collections.sort(files, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel f1, FileModel f2) {
                return Long.compare(f2.getDateTaken(), f1.getDateTaken()); // last modified descending
            }
        });
    }

    private List<FileModel> getFilesUsingMediaStoreQuery() {
        Context context = contextRef.get();
        List<FileModel> list = new ArrayList<>();
        /*Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;*/
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        /*String[] projection = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};*/
        String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_TAKEN, MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH};
        Cursor cursor = null;
        try {
            if (context != null) {
                String relativePath = "%" + Constants.ROOT_FOLDER_NAME + "/" + folderName + "%";
                /*cursor = context.getContentResolver().query(uri, projection, MediaStore.Images.Media.DATA + " like ? ",
                        new String[]{relativePath}, null);*/
                cursor = context.getContentResolver().query(uri, projection, MediaStore.MediaColumns.RELATIVE_PATH + " like ? ",
                        new String[]{relativePath}, null);
            }
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    /*int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    int dateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);*/
                    /*int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);*/
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    int dateTaken = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                    int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
                    int relativePath = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH);

                    FileModel fileModel = new FileModel();
                    /*fileModel.setPath(cursor.getString(dataColumn));*/
                    fileModel.setName(cursor.getString(nameColumn));
                    fileModel.setDateTaken(cursor.getLong(dateTaken));
                    fileModel.setSize(cursor.getLong(sizeColumn));
                    fileModel.setFolder(cursor.getString(folderColumn));
                    list.add(fileModel);

                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.e(TAG, "list size : " + list.size());
        return list;

    }

    @Override
    protected void onPostExecute(List<FileModel> fileList) {
        super.onPostExecute(fileList);
        if (onFetchingCompleted != null) {

            onFetchingCompleted.onFetchingComplete(fileList);
        }

    }

    private boolean isFav(String parent, String name) {
        boolean isFav = false;
        File file = new File(parent + "/" + name + "/" + Constants.JSON_FILE_NAME);
        if (!file.exists()) {
            return false;
        }
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {

                sb.append(line);
//                sb.append(System.lineSeparator());
            }
            Log.i(TAG, "sb: " + sb.toString());

            String jsonStr = sb.toString();

            try {

                JSONObject obj = new JSONObject(jsonStr);

                isFav = obj.getBoolean(Constants.NODE_ISFAV);
                String tags = obj.getString("Tags");

                Log.i(TAG, "isFav: " + isFav);
                Log.i(TAG, "tags: " + tags);

            } catch (Throwable t) {
                Log.e(TAG, "Could not parse malformed JSON: \"" + jsonStr + "\"");
            }
        } catch (FileNotFoundException e) {
            isFav = false;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isFav;
    }
}
