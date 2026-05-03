package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.cam.scanner.scantopdf.android.interfaces.CreateTempBitmapListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class GetTempCompressedBitmapPath extends AsyncTask<Void, Void, File> {

    private String fileOrDirectoryPath;
    private WeakReference<Context> contextRef;
    private CreateTempBitmapListener createTempBitmapListener;

    public GetTempCompressedBitmapPath(Context context, String path, CreateTempBitmapListener createTempBitmapListener) {
        contextRef = new WeakReference<>(context);
        this.fileOrDirectoryPath = path;
        this.createTempBitmapListener = createTempBitmapListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (createTempBitmapListener != null) {
            createTempBitmapListener.onCompressingStart();
        }
    }

    @Override
    protected File doInBackground(Void... voids) {
        File tempFile = null;
        File fileOrDirectory = new File(fileOrDirectoryPath);
        if (fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                tempFile = new File(contextRef.get().getExternalFilesDir(null), fileOrDirectory.getName());
                File[] files = fileOrDirectory.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        if (bitmap != null) {
                            try {
                                writeBitmapToTempFile(bitmap, tempFile, file.getName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
        return tempFile;
    }

    private void writeBitmapToTempFile(Bitmap bitmap, File tempFile, String imageName) throws IOException {
        boolean isTempFileCreated;
        if (!tempFile.exists()) {
            isTempFileCreated = tempFile.mkdirs();
        } else {
            isTempFileCreated = true;
        }
        if (isTempFileCreated) {
            File outFile = new File(tempFile, imageName);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
                fileOutputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        }
    }

    @Override
    protected void onPostExecute(File tempCompressedBitmapPath) {
        super.onPostExecute(tempCompressedBitmapPath);
        if (createTempBitmapListener != null) {
            createTempBitmapListener.onCompressingComplete(tempCompressedBitmapPath);
        }
    }

}
