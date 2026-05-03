package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.cam.scanner.scantopdf.android.interfaces.MoveDirectoryListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class MoveDirectoryTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> contextRef;
    private String srcDirPath, destDirPath;
    private MoveDirectoryListener moveDirectoryListener;

    public MoveDirectoryTask(Context context, String srcDirPath, String destDirPath, MoveDirectoryListener moveDirectoryListener) {
        contextRef = new WeakReference<>(context);
        this.srcDirPath = srcDirPath;
        this.destDirPath = destDirPath;
        this.moveDirectoryListener = moveDirectoryListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (moveDirectoryListener != null) {
            moveDirectoryListener.onMovingStart();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        File srcDir = new File(srcDirPath);

        if (srcDir.exists() && srcDir.isDirectory()) {
            File destDir = new File(destDirPath, srcDir.getName());
            boolean isDirectoryCreated = false;
            if (!destDir.exists()) {
                isDirectoryCreated = destDir.mkdirs();
            } else {
                if (destDir.isDirectory()) {
                    isDirectoryCreated = true;
                }
            }
            if (isDirectoryCreated) {
                File[] files = srcDir.listFiles();
                if (files != null && files.length > 0) {
                    for (File srcFile : files) {
                        if (srcFile != null && srcFile.isFile() && srcFile.exists()) {
                            File dest = new File(destDir, srcFile.getName());
                            copy(srcFile, dest);
                        }
                    }
                }
            }
        }

        return null;
    }

    private void copy(File src, File dest) {
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
        if (moveDirectoryListener != null) {
            moveDirectoryListener.onMoveCompleted();
        }
    }
}
