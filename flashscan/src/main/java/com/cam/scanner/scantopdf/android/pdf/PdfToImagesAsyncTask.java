package com.cam.scanner.scantopdf.android.pdf;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.cam.scanner.scantopdf.android.interfaces.PdfToImageCallback;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.io.File;
import java.lang.ref.WeakReference;

public class PdfToImagesAsyncTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> contextRef;
    private Context context;
    private String[] password;
    private String path;
    private Uri uri;
    private FlashScanUtil flashScanUtil;
    private static final String TAG = PdfToImagesAsyncTask.class.getSimpleName();
    private PdfToImageCallback pdfToImageCallback;
    private String mDecryptedPath;
    private PdfUtils pdfUtils;
    private boolean success;

    public PdfToImagesAsyncTask(Context context, String[] enteredPassword, String path, Uri uri, PdfToImageCallback pdfToImageCallback) {
        this.context = context;
        contextRef = new WeakReference<>(context);
        this.password = enteredPassword;
        this.path = path;
        this.uri = uri;
        flashScanUtil = new FlashScanUtil(context);
        this.pdfToImageCallback = pdfToImageCallback;
        pdfUtils = new PdfUtils();
        success = false;
    }

    // for pdf editor screen
    public PdfToImagesAsyncTask(Context context, String[] enteredPassword, String savedPdfPath, PdfToImageCallback pdfToImageCallback) {
        this.context = context;
        contextRef = new WeakReference<>(context);
        this.password = enteredPassword;
        this.path = savedPdfPath;
        flashScanUtil = new FlashScanUtil(context);
        this.pdfToImageCallback = pdfToImageCallback;
        pdfUtils = new PdfUtils();
        success = false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (pdfToImageCallback != null) {
            pdfToImageCallback.onConversionStart();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {

        if (password != null && password.length > 0) {
            mDecryptedPath = pdfUtils.removeDefPasswordForImages(contextRef.get(), path, password);
        }
        File tempDir = null;
        File originalTempDir = null;
        ParcelFileDescriptor fileDescriptor = null;
        String directoryName = null;
        try {
            if (!TextUtils.isEmpty(mDecryptedPath)) {
                File file = new File(mDecryptedPath);
                if (file.isFile() && file.exists()) {
                    directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                    fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
                }


            } else {
            /*    if (uri != null) {
                    fileDescriptor = contextRef.get().getContentResolver().openFileDescriptor(uri, "r");
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (file.isFile() && file.exists()) {
                            directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                        }
                    }
                } else*/
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (file.isFile() && file.exists()) {
                        directoryName = flashScanUtil.removeExtensionFromFileName(file.getName());
                        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                    }

                }

            }
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                int pageCount = renderer.getPageCount();

                tempDir = new File(FlashScanUtil.getDocProcessingPath(context) + File.separator + directoryName);
                originalTempDir = new File(FlashScanUtil.getDocOriginalPath(context) + File.separator + directoryName);
                if (tempDir.isDirectory() && tempDir.exists()) {
                    File[] files = tempDir.listFiles();
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            file.delete();
                        }
                    }

                }
                if (originalTempDir.isDirectory() && originalTempDir.exists()) {
                    File[] files = originalTempDir.listFiles();
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            file.delete();
                        }
                    }

                }
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    if (bitmap != null) {
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawColor(Color.WHITE);
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        // say we render for showing on the screen
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        // close the page
                        page.close();

                        // save image to temp file in temp directory
                        if (!TextUtils.isEmpty(directoryName)) {
                            /*tempDir = new File(contextRef.get().getExternalFilesDir(null), directoryName);*/

                            String fileName = directoryName + "_" + (i + 1) + ".jpg";
                            String savedImagePath = flashScanUtil.saveImageToTempDirectory(tempDir, fileName, bitmap);
                            String savedOriginalImagePath = flashScanUtil.saveImageToTempDirectory(originalTempDir, fileName, bitmap);
                            if (!TextUtils.isEmpty(savedImagePath)) {
                                /*outputFilesPath.add(savedImagePath);*/
                                Log.e(TAG, "savedImagePath : " + savedImagePath);
                                Log.e(TAG, "savedOriginalImagePath : " + savedOriginalImagePath);
                            }
                        }


                    }
                }
                renderer.close();
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        if (tempDir != null && tempDir.isDirectory() && tempDir.exists()) {
            return tempDir.getPath();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String directoryPath) {
        super.onPostExecute(directoryPath);
        if (pdfToImageCallback != null) {
            pdfToImageCallback.onConversionCompleted(directoryPath, success);
        }
    }
}
