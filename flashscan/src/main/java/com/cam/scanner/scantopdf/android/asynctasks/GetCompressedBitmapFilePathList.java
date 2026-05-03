package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.cam.scanner.scantopdf.android.interfaces.CreateMultipleTempBitmapListener;
import com.cam.scanner.scantopdf.android.util.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GetCompressedBitmapFilePathList extends AsyncTask<Void, Void, ArrayList<String>> {

    private Context context;
    private ArrayList<String> filesPathList;
    private CreateMultipleTempBitmapListener createMultipleTempBitmapListener;

    public GetCompressedBitmapFilePathList(Context context, ArrayList<String> filesPathList, CreateMultipleTempBitmapListener createMultipleTempBitmapListener) {
        this.context = context;
        this.filesPathList = filesPathList;
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
        ArrayList<String> filesList = new ArrayList<>();
        File outFile;
        for (String filePath : filesPathList) {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if (bitmap != null) {
//                    outFile = new File(context.getExternalFilesDir(null), file.getName());
                    outFile = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY, file.getName());
                    try {
                        boolean isFileCreated;
                        if (!outFile.exists()) {
                            isFileCreated = outFile.createNewFile();
                        } else {
                            isFileCreated = true;
                        }
                        if (isFileCreated) {
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

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    filesList.add(outFile.getPath());
                }
            }
        }
        return filesList;
    }

    @Override
    protected void onPostExecute(ArrayList<String> filesPathList) {
        super.onPostExecute(filesPathList);
        if (createMultipleTempBitmapListener != null) {
            createMultipleTempBitmapListener.onCompressBitmapComplete(filesPathList);
        }
    }
}
