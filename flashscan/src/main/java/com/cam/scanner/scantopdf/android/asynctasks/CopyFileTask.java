package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import kotlin.Pair;

public class CopyFileTask extends AsyncTask<Void, Void, Void> {

    private String sourceFilePath;
    private String destFilePath;
    private CopyOperationListener copyOperationListener;
    private int fileOperationAction;
    private ArrayList<String> filePathList;
    private boolean isCompress = false;
    private boolean updateImageName = true;
    private String originalFilePath;

    public CopyFileTask(String sourceFilePath, String destFilePath, CopyOperationListener copyOperationListener, int fileOperationAction) {
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
        this.copyOperationListener = copyOperationListener;
        this.fileOperationAction = fileOperationAction;
    }

    public CopyFileTask(ArrayList<String> filePathList, String destFilePath, CopyOperationListener copyOperationListener, int fileOperationAction) {
        this.filePathList = filePathList;
        this.destFilePath = destFilePath;
        this.copyOperationListener = copyOperationListener;
        this.fileOperationAction = fileOperationAction;
    }

    private Context context;

    public CopyFileTask(Context context, ArrayList<String> filePathList, String destFilePath, String originalFilePath, CopyOperationListener copyOperationListener, boolean isCompress) {
        this.filePathList = filePathList;
        this.destFilePath = destFilePath;
        this.copyOperationListener = copyOperationListener;
        this.isCompress = isCompress;
        this.context = context;
        this.originalFilePath = originalFilePath;
    }

    public CopyFileTask(Context context, ArrayList<String> filePathList, String destFilePath, String originalFilePath, CopyOperationListener copyOperationListener, int fileOperationAction, boolean isCompress) {
        this.filePathList = filePathList;
        this.destFilePath = destFilePath;
        this.copyOperationListener = copyOperationListener;
        this.fileOperationAction = fileOperationAction;
        this.context = context;
        this.isCompress = isCompress;
        this.originalFilePath = originalFilePath;
    }

    public CopyFileTask(Context context, String sourceFilePath, String destFilePath, String originalFilePath, CopyOperationListener copyOperationListener, int fileOperationAction, boolean isCompress) {
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
        this.copyOperationListener = copyOperationListener;
        this.fileOperationAction = fileOperationAction;
        this.context = context;
        this.isCompress = isCompress;
        this.originalFilePath = originalFilePath;
    }

    public CopyFileTask(Context context, ArrayList<String> filePathList, String destFilePath, String originalFilePath, CopyOperationListener copyOperationListener, boolean isCompress, boolean updateImageName) {
        this.filePathList = filePathList;
        this.destFilePath = destFilePath;
        this.copyOperationListener = copyOperationListener;
        this.isCompress = isCompress;
        this.context = context;
        this.originalFilePath = originalFilePath;
        this.updateImageName = updateImageName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (copyOperationListener != null) copyOperationListener.onCopyStart();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (filePathList != null && !filePathList.isEmpty()) {
            Pair<Boolean, Integer> destFilesDetail = checkIfDestinationExists();
            for (String sourceFilePath : filePathList) {
                if (updateImageName) {
                    int index = filePathList.indexOf(sourceFilePath);
                    if (destFilesDetail.getFirst()) {
                        index = destFilesDetail.getSecond();
                    }
                    File src = new File(sourceFilePath);
                    File dest = new File(destFilePath, index + "_" + src.getName());
                    File destOriginal = new File(originalFilePath, index + "_" + src.getName());
                    copy(context, isCompress, src, dest);
                    copy(context, isCompress, src, destOriginal);
                } else {
                    File src = new File(sourceFilePath);
                    File dest = new File(destFilePath, src.getName());
                    File destOriginal = new File(originalFilePath, src.getName());
                    copy(context, isCompress, src, dest);
                    copy(context, isCompress, src, destOriginal);
                }
            }
        } else {
            if (updateImageName) {
                File src = new File(sourceFilePath);
                File dest = new File(destFilePath, "0_" + src.getName());
                File destOriginal = new File(originalFilePath, "0_" + src.getName());
                copy(context, isCompress, src, dest);
                copy(context, isCompress, src, destOriginal);
            } else {
                File src = new File(sourceFilePath);
                File dest = new File(destFilePath, src.getName());
                File destOriginal = new File(originalFilePath, src.getName());
                copy(context, isCompress, src, dest);
                copy(context, isCompress, src, destOriginal);
            }
        }

        return null;
    }

    public static void copy(Context context, boolean isCompress, File src, File dest) {
        boolean isNewFileCreated = false;
        try {
            if (dest.exists())
                dest.delete();
            isNewFileCreated = dest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isNewFileCreated) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(src);
                outputStream = new FileOutputStream(dest);

                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }
                if (isCompress) {
                    try {
                        FutureTarget<Bitmap> futureTarget =
                                Glide.with(context)
                                        .asBitmap()
                                        .load(dest)
                                        .submit(800, 800);

                        Bitmap bitmap = futureTarget.get();
                        Glide.with(context).clear(futureTarget);

                        FileOutputStream out = new FileOutputStream(dest);
                        try {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        out.flush();
                        out.close();

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (copyOperationListener != null)
            copyOperationListener.onCopyComplete(fileOperationAction);
    }

    private Pair<Boolean, Integer> checkIfDestinationExists() {
        int sizeOfFiles = 0;
        boolean exist = false;
        try {
            File destFile = new File(destFilePath);
            if (destFile != null && destFile.exists() && destFile.isDirectory()) {
                File[] listFiles = destFile.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    new FlashScanUtil(context).sortFilesByNameAtoZ(listFiles);
                    File lastIndexFile = listFiles[listFiles.length - 1];
                    String lastIndex = lastIndexFile.getName().split("_")[0];
                    sizeOfFiles = Integer.parseInt(lastIndex) + 1;
                    exist = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<>(exist, sizeOfFiles);
    }
}
