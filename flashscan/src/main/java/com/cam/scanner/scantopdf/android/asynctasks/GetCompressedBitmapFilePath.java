package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.cam.scanner.scantopdf.android.interfaces.CreateTempBitmapListener;
import com.cam.scanner.scantopdf.android.util.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetCompressedBitmapFilePath extends AsyncTask<Void, Void, File> {

    private Context context;
    private String filePath;
    private CreateTempBitmapListener createTempBitmapListener;

    public GetCompressedBitmapFilePath(Context context, String path, CreateTempBitmapListener createTempBitmapListener) {
        this.context = context;
        this.filePath = path;
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
        File outFile = null;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            if (bitmap != null) {
                outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ Constants.ITL_PDF_DOCS_DIRECTORY, file.getName());
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

            }
        }
        return outFile;
    }

    @Override
    protected void onPostExecute(File compressedFile) {
        super.onPostExecute(compressedFile);
        if (createTempBitmapListener != null) {
            createTempBitmapListener.onCompressingComplete(compressedFile);
        }
    }
}
