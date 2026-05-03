package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.cam.scanner.scantopdf.android.interfaces.DocumentCreationListener;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class CreateOcrDocument extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> contextRef;
    private String folderName;
    private Bitmap bitmap;
    private DocumentCreationListener documentCreationListener;
    private FlashScanUtil flashScanUtil;

    public CreateOcrDocument(Context context, String folderName, Bitmap bitmapFromUri, DocumentCreationListener documentCreationListener) {
        contextRef = new WeakReference<>(context);
        this.folderName = folderName;
        this.bitmap = bitmapFromUri;
        this.documentCreationListener = documentCreationListener;
        flashScanUtil = new FlashScanUtil(contextRef.get());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (documentCreationListener != null) {
            documentCreationListener.onDocumentCreationStart();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        String folderPath = null;
        if (!TextUtils.isEmpty(folderName)) {
            folderPath = setFolderPath(folderName);
        }
        if (!TextUtils.isEmpty(folderPath)) {
            OutputStream outputStream = null;
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver contentResolver = contextRef.get().getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, flashScanUtil.getFileDateFormatName() + ".jpg");
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, folderPath);

                Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri != null) {
                    try {
                        outputStream = contentResolver.openOutputStream(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        contentResolver.delete(imageUri, null, null);
                    }
                }
            } else {*/
                /*String imagesDir;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imagesDir = contextRef.get().getExternalFilesDir(folderPath).toString();
                }
                else{
                    imagesDir = Environment.getExternalStoragePublicDirectory(folderPath).toString();
                }*/
                //String imagesDir = Environment.getExternalStoragePublicDirectory(folderPath).toString();
                File directory = new File(folderPath);
                boolean isDirectoryCreated;
                if (!directory.exists()) {
                    isDirectoryCreated = directory.mkdirs();
                } else {
                    isDirectoryCreated = true;
                }
            Log.e("TAG", "doInBackground: " + directory.getAbsolutePath());
                if (isDirectoryCreated) {
                    File savedFile = new File(directory, flashScanUtil.getFileDateFormatName() + ".jpg");
                    if (savedFile.exists()) savedFile.delete();
                    try {
                        outputStream = new FileOutputStream(savedFile);
                        Log.e("TAG", "doInBackground: savedFile = " + savedFile.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            //}
            try {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                }
            } catch (Exception e) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return folderPath;
    }

    private String setFolderPath(String folderName) {
        String folderPath;
        //String defaultPathForOcr = flashScanUtil.getDefaultPathForOcr();
        String defaultPathForOcr = FlashScanUtil.getOcrProcessingPath(contextRef.get()).getAbsolutePath();
        folderPath = defaultPathForOcr + "/" + folderName;
        return folderPath;
    }

    @Override
    protected void onPostExecute(String folderPath) {
        super.onPostExecute(folderPath);
        if (!TextUtils.isEmpty(folderPath)) {
            if (documentCreationListener != null) {
                documentCreationListener.onDocumentCreated(folderPath);
            }
        }
    }
}
