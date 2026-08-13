package com.cam.scanner.scantopdf.android.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.widget.Toast;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.models.WaterMark;
import com.cam.scanner.scantopdf.android.util.PdfImageObject;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PdfUtils {

    public void isPdfEncrypted(Context context, String path, String fromSource, PdfEncryptionCallBack pdfEncryptionCallBack) {
        boolean isEncrypted = false;
        PdfReader pdfReader = null;
        PrefManager sharedPref = new PrefManager(context);
        String masterPwd = sharedPref.getMasterPassword();
        byte[] ownerPassword = masterPwd.getBytes();
        try {
            pdfReader = new PdfReader(path, ownerPassword);
            if (pdfReader != null) {
                isEncrypted = pdfReader.isEncrypted();

                if (pdfEncryptionCallBack != null) {
                    pdfEncryptionCallBack.isCompletedWithSuccess(true, isEncrypted);
                }
            }
        } catch (IOException|NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(context, context.getResources().getString(R.string.corrupted_file), Toast.LENGTH_LONG).show();
            /*if(fromSource.equalsIgnoreCase("Reader")) {
                pdfEncryptionCallBack.isCompletedWithSuccess(false, true);
            }*/
        } finally {
            if (pdfReader != null) {
                pdfReader.close();
            }

        }
    }

    public boolean doEncryption(Context context, String pdfPath, String enteredPassword, String masterPassword) throws IOException, DocumentException {
        boolean isEncryptedSuccessfully = false;
        String encryptedPdfFilePath = null;
        PdfReader reader = null;
        PdfStamper stamper = null;
        /*PdfUtils pdfUtils = new PdfUtils();*/
        try {
            reader = new PdfReader(new FileInputStream(pdfPath));
            /*encryptedPdfFilePath = pdfUtils.getUniqueFileName(pdfPath.replace(".pdf", "_encrypted.pdf"));*/
            encryptedPdfFilePath = replaceAlreadyCreatedPdfWithEncryptedPdf(pdfPath);
            stamper = new PdfStamper(reader, new FileOutputStream(encryptedPdfFilePath));
            stamper.setEncryption(enteredPassword.getBytes(), masterPassword.getBytes(),
                    PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128);
            isEncryptedSuccessfully = true;
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
        if (stamper != null) {
            stamper.close();
        }
        if (reader != null) {
            reader.close();
        }
        return isEncryptedSuccessfully;
    }

    private String replaceAlreadyCreatedPdfWithEncryptedPdf(String pdfPath) {
        String outputPath;
        File file = new File(pdfPath);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
        outputPath = pdfPath;
        return outputPath;
    }

    String getUniqueFileName(String fileName) {
        String outputFileName = fileName;
        File file = new File(outputFileName);

        return file.getPath();
        /*if (!isFileExist(file.getName()))
            return outputFileName;*/

        /*File parentFile = file.getParentFile();
        if (parentFile != null) {
            File[] listFiles = parentFile.listFiles();

            if (listFiles != null) {
                int append = checkRepeat(outputFileName, Arrays.asList(listFiles));
                outputFileName = outputFileName.replace(mContext.getString(R.string.pdf_ext),
                        append + mContext.getResources().getString(R.string.pdf_ext));
            }
        }

        return outputFileName;*/
    }

    public void compressPdf(String inputPath, int quality, PdfCompressionCallback pdfCompressionCallback) {
        new ComPressPdfAsync(inputPath, quality, pdfCompressionCallback).execute();
    }

    public boolean checkEnteredPasswordIsCorrect(Context context, String filePath, String[] inputPassword) {
        boolean isPasswordCorrect = false;
        PrefManager sharedPref = new PrefManager(context);
        String masterPwd = sharedPref.getMasterPassword();
        try {
            PdfReader reader = new PdfReader(filePath, masterPwd.getBytes());
            byte[] pdfPassword = reader.computeUserPassword();
            byte[] enteredPassword = inputPassword[0].getBytes();
            if (Arrays.equals(pdfPassword, enteredPassword)) {
                isPasswordCorrect = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isPasswordCorrect;
    }


    public String removeDefPasswordForImages(Context context, final String file,
                                             final String[] inputPassword) {
        PrefManager sharedPref = new PrefManager(context);
        String finalOutputFile;
        try {
            String masterPwd = sharedPref.getMasterPassword();
            PdfReader reader = new PdfReader(new FileInputStream(file), masterPwd.getBytes());
            byte[] password;
            /*finalOutputFile = mFileUtils.getUniqueFileName
                    (file.replace(mContext.getResources().getString(R.string.pdf_ext),
                            mContext.getString(R.string.decrypted_file)));*/

            password = reader.computeUserPassword();
            byte[] input = inputPassword[0].getBytes();
            if (Arrays.equals(input, password)) {
                finalOutputFile = replaceAlreadyCreatedPdfWithEncryptedPdf(file);
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
                stamper.close();
                reader.close();
                return finalOutputFile;
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String addAntiCounterFeitToPdf(WaterMark waterMark, String savedPdfPath) {
        String outputFilePath = null;
        try {
            PdfReader pdfReader = new PdfReader(new FileInputStream(savedPdfPath));
            outputFilePath = replaceAlreadyCreatedPdfWithEncryptedPdf(savedPdfPath);
            PdfStamper stamper = new PdfStamper(pdfReader, new FileOutputStream(outputFilePath));
            Font font = new Font(waterMark.getFontFamily(), waterMark.getTextSize(), waterMark.getFontStyle(), waterMark.getTextColor());
            Phrase phrase = new Phrase(waterMark.getWaterMarkText(), font);
            PdfContentByte over;
            Rectangle pageSize;
            float x, y;
            int n = pdfReader.getNumberOfPages();
            for (int i = 1; i <= n; i++) {

                // get page size and position
                pageSize = pdfReader.getPageSizeWithRotation(i);
                x = (pageSize.getLeft() + pageSize.getRight()) / 2;
                y = (pageSize.getTop() + pageSize.getBottom()) / 2;
                over = stamper.getOverContent(i);

                if (waterMark.getImage() != null) {
                    try {
                        Image image = Image.getInstance(waterMark.getImage());
                        // Scale accordingly
                        image.scaleToFit(100, 20);
                        image.setAbsolutePosition(x - (image.getScaledWidth() / 2), y);
                        over.addImage(image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                ColumnText.showTextAligned(over, Element.ALIGN_CENTER, phrase, x, y, waterMark.getRotationAngle());
            }
            stamper.close();
            pdfReader.close();
        } catch (NoSuchMethodError | IOException | DocumentException e) {
            e.printStackTrace();
        }
        return outputFilePath;
    }

    public String getPdfFilePassword(Context context, String originalPdfPath, String masterPassword) {
        String pdfFilePassword = null;
        try {
            PdfReader pdfReader = new PdfReader(originalPdfPath, masterPassword.getBytes());
            byte[] bytes = pdfReader.computeUserPassword();
            if (bytes != null && bytes.length > 0) {
                pdfFilePassword = new String(bytes);
            }
//            pdfReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfFilePassword;
    }

    private class ComPressPdfAsync extends AsyncTask<String, String, String> {

        private String inputPath;
        private int quality;
        private PdfCompressionCallback pdfCompressionCallback;
        private boolean success;

        public ComPressPdfAsync(String inputPath, int quality, PdfCompressionCallback pdfCompressionCallback) {
            this.inputPath = inputPath;
            this.quality = quality;
            this.pdfCompressionCallback = pdfCompressionCallback;
            success = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (pdfCompressionCallback != null) {
                pdfCompressionCallback.onCompressionStart();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String outputPath = null;

            PdfReader reader = null;
            try {
                reader = new PdfReader(new FileInputStream(inputPath));
            } catch (IOException|NullPointerException e) {
                e.printStackTrace();
            }
            if (reader == null) {
                success = false;
                return "";
            }
            int n = reader.getXrefSize();
            PdfObject object;
            PRStream stream;

            for (int i = 0; i < n; i++) {
                object = reader.getPdfObject(i);
                if (object == null || !object.isStream())
                    continue;
                stream = (PRStream) object;
                PdfObject pdfSubType = stream.get(PdfName.SUBTYPE);
                System.out.println(stream.type());
                if (pdfSubType != null && pdfSubType.toString().equals(PdfName.IMAGE.toString())) {
                    PdfImageObject image = null;
                    try {
                        image = new PdfImageObject(stream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] imageBytes = image.getImageAsBytes();
                    Bitmap bmp;
                    bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bmp == null) continue;

                    int width = bmp.getWidth();
                    int height = bmp.getHeight();

                    Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas outCanvas = new Canvas(outBitmap);
                    outCanvas.drawBitmap(bmp, 0f, 0f, null);

                    ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                    outBitmap.compress(Bitmap.CompressFormat.JPEG, quality, imgBytes);

                    // Change from itext to open pdf
                    //stream.clear();
                    stream.getKeys().clear();

                    stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
                    stream.put(PdfName.TYPE, PdfName.XOBJECT);
                    stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                    stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                    stream.put(PdfName.WIDTH, new PdfNumber(width));
                    stream.put(PdfName.HEIGHT, new PdfNumber(height));
                    stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                    stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                }
            }

            reader.removeUnusedObjects();

            // Save altered PDF
            outputPath = replaceAlreadyCreatedPdfWithEncryptedPdf(inputPath);
            try {
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
                int total = reader.getNumberOfPages() + 1;
                for (int i = 1; i < total; i++) {
                    reader.setPageContent(i + 1, reader.getPageContent(i + 1));
                }
                stamper.setFullCompression();
                stamper.close();
                reader.close();

                success = true;
            } catch (DocumentException | IOException e) {
                success = false;
                e.printStackTrace();
            }
            return outputPath;
        }

        @Override
        protected void onPostExecute(String outputPath) {
            super.onPostExecute(outputPath);
            if (pdfCompressionCallback != null) {
                pdfCompressionCallback.onCompressionCompleted(success, outputPath);
            }
        }
    }

}
