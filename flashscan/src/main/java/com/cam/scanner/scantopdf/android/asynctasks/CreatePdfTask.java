package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.SingleTon.PdfSettings;
import com.cam.scanner.scantopdf.android.interfaces.PDFCreationCallback;
import com.cam.scanner.scantopdf.android.models.ImageToPdfOptions;
import com.cam.scanner.scantopdf.android.models.WaterMark;
import com.cam.scanner.scantopdf.android.pdf.PdfEncryptionCallBack;
import com.cam.scanner.scantopdf.android.pdf.PdfUtils;
import com.cam.scanner.scantopdf.android.util.BaseColor;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.WatermarkPageEvent;
import org.openpdf.text.Document;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class CreatePdfTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = CreatePdfTask.class.getSimpleName();
    private PDFCreationCallback pdfCreationCallback;
    private FlashScanUtil flashScanUtil;
    private String fileName;
    private String pageSize;
    private int pageColor;
    private List<String> selectedImagesList;
    private int marginTop;
    private int marginBottom;
    private int marginLeft;
    private int marginRight;
    private int pdfQuality;
    private int borderWidth;
    private WeakReference<Context> contextRef;
    private boolean isWaterMarkAdded;
    private WaterMark waterMark;
    private PdfUtils pdfUtils;
    private boolean isPdfEncrypted;
    private String mMasterPwd;
    private PrefManager prefManager;
    private byte[] pdfPassword = null;
    private boolean isOriginalPdfImagesSaveToTempDir = false;
    private Context context;

    String pdfFileName;
    boolean isFromPdfEditor = false;

    public CreatePdfTask(Context context, String fileName, ImageToPdfOptions imageToPdfOptions,
                         List<String> selectedImagesList, PDFCreationCallback pdfCreationCallback, boolean isOriginalPdfImagesSaveToTempDir) {
        this.context = context;
        contextRef = new WeakReference<>(context);
        this.pdfCreationCallback = pdfCreationCallback;
        flashScanUtil = new FlashScanUtil(context);
        this.fileName = fileName;
        this.pageSize = imageToPdfOptions.getPageSize();
        this.pageColor = imageToPdfOptions.getPageColor();
        this.selectedImagesList = selectedImagesList;
        this.marginTop = imageToPdfOptions.getMarginTop();
        this.marginBottom = imageToPdfOptions.getMarginBottom();
        this.marginLeft = imageToPdfOptions.getMarginLeft();
        this.marginRight = imageToPdfOptions.getMarginRight();
        this.pdfQuality = imageToPdfOptions.getPdfQuality();
        this.borderWidth = imageToPdfOptions.getBorderWidth();
        this.isWaterMarkAdded = imageToPdfOptions.isWaterMarkAdded();
        this.waterMark = imageToPdfOptions.getWaterMark();
        pdfUtils = new PdfUtils();
        isPdfEncrypted = false;
        prefManager = new PrefManager(context);
        mMasterPwd = prefManager.getMasterPassword();
        this.isOriginalPdfImagesSaveToTempDir = isOriginalPdfImagesSaveToTempDir;
        pdfFileName = fileName;
    }

    public CreatePdfTask(Context context, String fileName, ImageToPdfOptions imageToPdfOptions,
                         List<String> selectedImagesList, PDFCreationCallback pdfCreationCallback, boolean isOriginalPdfImagesSaveToTempDir, Boolean isFromPdfEditor) {
        this.context = context;
        contextRef = new WeakReference<>(context);
        this.pdfCreationCallback = pdfCreationCallback;
        flashScanUtil = new FlashScanUtil(context);
        this.fileName = fileName;
        this.pageSize = imageToPdfOptions.getPageSize();
        this.pageColor = imageToPdfOptions.getPageColor();
        this.selectedImagesList = selectedImagesList;
        this.marginTop = imageToPdfOptions.getMarginTop();
        this.marginBottom = imageToPdfOptions.getMarginBottom();
        this.marginLeft = imageToPdfOptions.getMarginLeft();
        this.marginRight = imageToPdfOptions.getMarginRight();
        this.pdfQuality = imageToPdfOptions.getPdfQuality();
        this.borderWidth = imageToPdfOptions.getBorderWidth();
        this.isWaterMarkAdded = imageToPdfOptions.isWaterMarkAdded();
        this.waterMark = imageToPdfOptions.getWaterMark();
        pdfUtils = new PdfUtils();
        isPdfEncrypted = false;
        prefManager = new PrefManager(context);
        mMasterPwd = prefManager.getMasterPassword();
        this.isOriginalPdfImagesSaveToTempDir = isOriginalPdfImagesSaveToTempDir;
        pdfFileName = fileName;
        this.isFromPdfEditor = isFromPdfEditor;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (pdfCreationCallback != null)
            pdfCreationCallback.onPdfCreationStarted();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String pdfFilePath = setFilePath();
        Log.i("pdfPath", pdfFilePath);
        if (!TextUtils.isEmpty(pdfFilePath)) {
            File file = new File(pdfFilePath);
            if (file.isFile() && file.exists()) {
                if (Constants.IS_CREATE_ALREADY_ENCRYPTED_PDF_WITH_PASSWORD) {
                    pdfUtils.isPdfEncrypted(context, file.getPath(), "CreateTask", new PdfEncryptionCallBack() {
                        @Override
                        public void isCompletedWithSuccess(boolean isSuccess, boolean isEncrypted) {
                            if (isEncrypted) {
                /*{
                   delete old and create new with password protection
                }*/
                                isPdfEncrypted = true;
                                try {
                                    PdfReader pdfReader = new PdfReader(file.getPath(), prefManager.getMasterPassword().getBytes());
                                    pdfPassword = pdfReader.computeUserPassword();
//                                    pdfReader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }

                file.delete();
            }
            Rectangle rectanglePageSize = null;
            try {
                String pageSizeString;
                if (Constants.IS_CREATE_PDF_DIRECT) {
                    pageSizeString = prefManager.getSelectedPdfSizeForWholeApp();
                } else {
                    pageSizeString = PdfSettings.getInstance().getSelectedPdfPageSize();
                }
                if (pageSizeString.contains("RECTANGLEREADONLY")) {
                    rectanglePageSize = PageSize.A4;
                } else {
                    rectanglePageSize = PageSize.getRectangle(pageSizeString);
                }
            } catch (Exception e) {
                rectanglePageSize = PageSize.A4;
                e.printStackTrace();
            }
            if (rectanglePageSize != null) {
                Rectangle rectangle = new Rectangle(rectanglePageSize);
                rectangle.setBackgroundColor(getBaseColor(pageColor));
                Document document = new Document(rectangle, marginLeft, marginRight, marginTop, marginBottom);
                document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
                Rectangle pageSize = document.getPageSize();
                boolean hasPages = false;

                try {
                    PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFilePath));
                    if (isPdfEncrypted && pdfPassword != null) {
                        pdfWriter.setEncryption(pdfPassword, mMasterPwd.getBytes(),
                                PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                                PdfWriter.ENCRYPTION_AES_128);

                    }
                    if (isWaterMarkAdded) {
                        WatermarkPageEvent watermarkPageEvent = new WatermarkPageEvent();
                        if (Constants.IS_CREATE_PDF_DIRECT) {
                            watermarkPageEvent.setWatermark(context, waterMark, prefManager.getSelectedPdfSizeForWholeApp());
                        } else {
                            watermarkPageEvent.setWatermark(context, waterMark, PdfSettings.getInstance().getSelectedPdfPageSize());
                        }
                        pdfWriter.setPageEvent(watermarkPageEvent);
                        flashScanUtil.logPdfCreation(Constants.FirebaseClickEvents.PDF_CREATION_WITH_WATERMARK);
                    } else {
                        flashScanUtil.logPdfCreation(Constants.FirebaseClickEvents.PDF_CREATION_WITHOUT_WATERMARK);
                    }
                    document.open();

                    if (selectedImagesList != null && !selectedImagesList.isEmpty()) {


                        for (String imageUri : selectedImagesList) {
                            File eachFile = new File(imageUri);
                            if (eachFile.isFile() && eachFile.exists()) {
                                if (!TextUtils.isEmpty(eachFile.getName()) && eachFile.getName().equalsIgnoreCase(Constants.JSON_FILE_NAME)) {
                                    continue;
                                }
                            }
                            Image image = getImageForPdf(imageUri);
                            int qualityMod = (int) (pdfQuality * 0.09);
                            image.setCompressionLevel(qualityMod); // 2 by calculating default
                            image.setBorder(Rectangle.BOX);
                            image.setBorderWidth(borderWidth);
                            float pageWidth = document.getPageSize().getWidth() - (marginLeft + marginRight);
                            float pageHeight = document.getPageSize().getHeight() - (marginBottom + marginTop);
                            image.scaleToFit(pageWidth, pageHeight);
                            image.setAbsolutePosition(
                                    (pageSize.getWidth() - image.getScaledWidth()) / 2,
                                    (pageSize.getHeight() - image.getScaledHeight()) / 2);
                            /*document.newPage();*/
                            document.add(image);
                            hasPages = true;
                            document.newPage();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to create PDF at " + pdfFilePath, e);
                    pdfFilePath = null;
                } finally {
                    if (document != null && document.isOpen()) {
                        if (hasPages) {
                            document.close();
                        } else {
                            try {
                                document.close();
                            } catch (Exception closeException) {
                                Log.w(TAG, "Skipping empty PDF close after creation failure", closeException);
                            }
                        }
                    }
                }
                if (!hasPages) {
                    File failedFile = new File(file.getPath());
                    if (failedFile.exists() && !failedFile.delete()) {
                        Log.w(TAG, "Unable to delete empty PDF file " + failedFile.getAbsolutePath());
                    }
                    pdfFilePath = null;
                }
            }

        }
        return pdfFilePath;
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
            } catch (Exception e) {
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

    private String setFilePath() {
        String pdfFilePath = null;
        String path;
        if (isFromPdfEditor) {
            path = flashScanUtil.getCacheStorageLocationForPdf(context);
        } else {
            path = flashScanUtil.getDefaultStorageLocationForPdf(context);
        }
        File dir = new File(path);
        boolean isDirectoryCreated = false;
        if (!dir.exists()) {
            isDirectoryCreated = dir.mkdirs();
        } else {
            if (dir.isDirectory()) {
                isDirectoryCreated = true;
            }
        }
        if (isDirectoryCreated) {
            if (!TextUtils.isEmpty(fileName)) {
                pdfFilePath = path + "/" + fileName + Constants.PDF_FILE_EXTENSION;
            } else {
                pdfFilePath = path + "/" + contextRef.get().getString(R.string.prefix_document) + System.currentTimeMillis() + Constants.PDF_FILE_EXTENSION;
                pdfFileName = contextRef.get().getString(R.string.prefix_document) + System.currentTimeMillis();
            }
        }
        return pdfFilePath;
    }

    @Override
    protected void onPostExecute(String saveFilePath) {
        super.onPostExecute(saveFilePath);
      /*  File externalFilesDir = contextRef.get().getExternalFilesDir(null);
        if (externalFilesDir != null) {
            clearTempFiles(externalFilesDir);
        }*/
        if (!TextUtils.isEmpty(saveFilePath)) {
            if (pdfCreationCallback != null)
                pdfCreationCallback.onPdfCreated(saveFilePath);
        }

    }

    private void clearTempFiles(File externalFilesDir) {
        if (externalFilesDir.isDirectory()) {
            File[] files = externalFilesDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    clearTempFiles(file);
                }
            }
        }
        if (externalFilesDir.isFile()) {
            externalFilesDir.delete();
        }
    }

    private Image getImageForPdf(String imageUri) throws IOException {
        Bitmap bitmap = decodeBitmap(imageUri);
        if (bitmap == null) {
            throw new IOException("Unable to decode bitmap for PDF image: " + imageUri);
        }

        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            byte[] imageData = new byte[width * height * 3];
            for (int index = 0; index < pixels.length; index++) {
                int pixel = pixels[index];
                int offset = index * 3;
                imageData[offset] = (byte) Color.red(pixel);
                imageData[offset + 1] = (byte) Color.green(pixel);
                imageData[offset + 2] = (byte) Color.blue(pixel);
            }

            return Image.getInstance(width, height, 3, 8, imageData);
        } catch (Exception e) {
            throw new IOException("Unable to convert bitmap into PDF image: " + imageUri, e);
        } finally {
            bitmap.recycle();
        }
    }

    private Bitmap decodeBitmap(String imageUri) throws IOException {
        if (TextUtils.isEmpty(imageUri)) {
            return null;
        }

        if (imageUri.startsWith("content://")) {
            try (InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(imageUri))) {
                return BitmapFactory.decodeStream(inputStream);
            }
        }

        return BitmapFactory.decodeFile(imageUri);
    }

    private java.awt.Color getBaseColor(int color) {
        return new BaseColor(Color.red(color), Color.green(color), Color.blue(color));
    }
}
