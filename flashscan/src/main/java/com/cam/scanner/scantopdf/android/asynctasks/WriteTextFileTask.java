package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.interfaces.WriteFileTaskListener;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class WriteTextFileTask extends AsyncTask<Void, Void, String> {

    private String text;
    private WriteFileTaskListener writeFileTaskListener;
    private WeakReference<Context> contextRef;
    private FlashScanUtil flashScanUtil;
    private String folderPath;
    private boolean shouldDlgShow;

    public WriteTextFileTask(Context context, String resultText, String folderPath, WriteFileTaskListener writeFileTaskListener, boolean shouldDlgShow) {
        contextRef = new WeakReference<>(context);
        this.text = resultText;
        this.writeFileTaskListener = writeFileTaskListener;
        flashScanUtil = new FlashScanUtil(contextRef.get());
        this.folderPath = folderPath;
        this.shouldDlgShow = shouldDlgShow;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (writeFileTaskListener != null) {
            writeFileTaskListener.onWriteStart();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        /*if (!TextUtils.isEmpty(folderPath)) {
            deleteAllTextFilesExceptJpgFile(folderPath);
        }*/
        String filePath = setFilePath();
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (!file.exists()) {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(text.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
        return filePath;
    }

    private void deleteAllTextFilesExceptJpgFile(String folderPath) {
        String directoryPath = Environment.getExternalStoragePublicDirectory(folderPath).toString();
        if (!TextUtils.isEmpty(directoryPath)) {
            File dir = new File(directoryPath);
            if (dir.isDirectory() && dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file != null && file.isFile() && file.exists()) {
                            if (flashScanUtil.getExtensionFromFileName(file.getName()).equalsIgnoreCase(Constants.FileExtensions.JPG)) {
                                continue;
                            }
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String savedFilePath) {
        super.onPostExecute(savedFilePath);
        if (writeFileTaskListener != null) {
            writeFileTaskListener.onWriteCompleted(savedFilePath, shouldDlgShow);
        }
    }

    private String setFilePath() {
        String txtFilePath = null;
        String dirPath;
        if (!TextUtils.isEmpty(folderPath)) {
            dirPath = folderPath;
        } else {
            //dirPath = flashScanUtil.getDefaultPathForOcr() + "/" + flashScanUtil.getFolderCurrentTime();
            dirPath = FlashScanUtil.getOcrProcessingPath(contextRef.get()) + "/" + flashScanUtil.getFolderCurrentTime();
        }
        if (!TextUtils.isEmpty(dirPath)) {
            //String directoryPath = Environment.getExternalStoragePublicDirectory(dirPath).toString();
            File dir = new File(dirPath);
            boolean isDirectoryCreated;
            if (!dir.exists()) {
                isDirectoryCreated = dir.mkdirs();
            } else {
                isDirectoryCreated = true;
            }
            if (isDirectoryCreated) {
                txtFilePath = dirPath + "/" + flashScanUtil.getFileDateFormatName() + "_" + contextRef.get().getString(R.string.suffix_app_name) + Constants.TXT_FILE_EXTENSION;
            }
        }
        return txtFilePath;
    }
}
