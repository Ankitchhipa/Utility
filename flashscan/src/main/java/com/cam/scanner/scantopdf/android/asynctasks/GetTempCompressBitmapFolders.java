package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.cam.scanner.scantopdf.android.interfaces.CreateMultipleTempBitmapListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GetTempCompressBitmapFolders extends AsyncTask<Void, Void, ArrayList<String>> {

    private WeakReference<Context> contextRef;
    private ArrayList<String> selectedFoldersPathList;
    private CreateMultipleTempBitmapListener createMultipleTempBitmapListener;

    public GetTempCompressBitmapFolders(Context context, ArrayList<String> selectedFoldersPathList, CreateMultipleTempBitmapListener createMultipleTempBitmapListener) {
        contextRef = new WeakReference<>(context);
        this.selectedFoldersPathList = selectedFoldersPathList;
        this.createMultipleTempBitmapListener = createMultipleTempBitmapListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (createMultipleTempBitmapListener != null) {
            createMultipleTempBitmapListener.onCompressBitmapStart();
        }
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        File tempFile;
        ArrayList<String> tempFoldersList = new ArrayList<>();
        for (String dirPath : selectedFoldersPathList) {
            File dir = new File(dirPath);
            if (dir.exists()) {
                if (dir.isDirectory()) {
                    tempFile = new File(contextRef.get().getExternalFilesDir(null), dir.getName());
                    File[] files = dir.listFiles();
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
                    tempFoldersList.add(tempFile.getPath());
                }
            }
        }
        return tempFoldersList;
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
    protected void onPostExecute(ArrayList<String> foldersList) {
        super.onPostExecute(foldersList);
        if (createMultipleTempBitmapListener != null) {
            createMultipleTempBitmapListener.onCompressBitmapComplete(foldersList);
        }

    }
}
