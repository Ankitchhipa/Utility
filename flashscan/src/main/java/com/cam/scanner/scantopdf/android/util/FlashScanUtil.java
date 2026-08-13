package com.cam.scanner.scantopdf.android.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.palette.graphics.Palette;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.barcodereader.model.MultiplePhones;
import com.cam.scanner.scantopdf.android.drive.DriveServiceHelper;
import com.cam.scanner.scantopdf.android.drive.GoogleDriveFileHolder;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.GetDominantColorListener;
import com.cam.scanner.scantopdf.android.interfaces.GoogleDriveDataDeleteListener;
import com.cam.scanner.scantopdf.android.interfaces.GoogleDriveDataDownloadListener;
import com.cam.scanner.scantopdf.android.interfaces.GoogleDriveDataUploadListener;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.models.GoogleDriveChildFileModel;
import com.cam.scanner.scantopdf.android.models.GoogleDriveFolderModel;
import com.cam.scanner.scantopdf.android.models.PageSize;
import com.cam.scanner.scantopdf.android.models.WaterMark;
import com.cam.scanner.scantopdf.android.pdf.ColorModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.itl.commonres.utils.CommonMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.openpdf.text.Font;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/*import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgcodecs.Imgcodecs.imread;*/

public class FlashScanUtil {

    private static final String TAG = FlashScanUtil.class.getSimpleName();
    private static final String HOME_SCREEN_CLICKED_EVENT = "HOME_SCREEN_CLICKED_EVENT";
    private static final String QR_BARCODE_RESULT_SCREEN_EVENT = "QR_BARCODE_RESULT_SCREEN_EVENT";
    private static final String OCR_RESULT_SCREEN_EVENT = "OCR_RESULT_SCREEN_EVENT";
    private static final String PDF_CREATION_EVENT = "PDF_CREATION_EVENT";
    private static final String EVENT_NAME = "EVENT_NAME";
    private Context context;
    private static final double SPACE_KB = 1024;
    private static final double SPACE_MB = 1024 * SPACE_KB;
    private static final double SPACE_GB = 1024 * SPACE_MB;
    private static final double SPACE_TB = 1024 * SPACE_GB;
    private DriveServiceHelper mDriveServiceHelper;
    private int dominantColor = 0;
    private GetDominantColorListener getDominantColorListener;
    private static Dialog loadingDialog;

    public FlashScanUtil(Context context) {
        this.context = context;
    }

    public FlashScanUtil() {

    }

    public boolean isPermissionGranted(String permission) {
        boolean isPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true;
        }
        return isPermissionGranted;
    }

    public AlertDialog showAlertDialog(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnClick
            , String negativeLabel, DialogInterface.OnClickListener negativeOnClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        builder.setNeutralButton(negativeLabel, negativeOnClick);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }


    public String getDateFromTimeStamp(long dateAdded) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateAdded);
        DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm a", Locale.getDefault());
        return df.format(cal.getTime());
    }

    public String convertSizeToHumanReadable(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KB", bytes / 0x1p10)
                : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MB", bytes / 0x1p20)
                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GB", bytes / 0x1p30)
                : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TB", bytes / 0x1p40)
                : b <= 0xfffccccccccccccL ? String.format("%.1f PB", (bytes >> 10) / 0x1p40)
                : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }

    public String byteToString(long sizeInBytes) {
        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        try {
            if (sizeInBytes < SPACE_KB) {
                return nf.format(sizeInBytes) + " Byte(s)";
            } else if (sizeInBytes < SPACE_MB) {
                return nf.format(sizeInBytes / SPACE_KB) + " KB";
            } else if (sizeInBytes < SPACE_GB) {
                return nf.format(sizeInBytes / SPACE_MB) + " MB";
            } else if (sizeInBytes < SPACE_TB) {
                return nf.format(sizeInBytes / SPACE_GB) + " GB";
            } else {
                return nf.format(sizeInBytes / SPACE_TB) + " TB";
            }
        } catch (Exception e) {
            return sizeInBytes + " Byte(s)";
        }

    }

    public String getDefaultStorageLocationForPdf(Context context) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.ITL_PDF_DIRECTORY;
    }
    public String getCacheStorageLocationForPdf(Context context) {
        return context.getCacheDir().getAbsolutePath() + "/" + Constants.ITL_PDF_DIRECTORY;
    }

    public String removeExtensionFromFileName(String fileName) {
        String fileNameWithoutExtension = null;
        if (fileName.indexOf(".") > 0)
            fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        return fileNameWithoutExtension;
    }

    public void showSnackBar(View view, String message) {
        /*Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();*/

        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(context.getResources().getColor(com.itl.commonres.R.color.black_text_color));
        snackbar.show();
    }

    public void showSnackOnTop(View view, String msg, int duration) {
        Snackbar snackbar = Snackbar.make(view, msg, duration);
        View snackbarView = snackbar.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        snackbarView.setLayoutParams(params);
        snackbarView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    public void openFile(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            if (uri != null) {
                context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        if (uri != null) {
            String mimeType = getMimeType(file.getAbsolutePath());
            if (!TextUtils.isEmpty(mimeType)) {
                intent.setDataAndType(uri, mimeType);
            } else {
                if (file.toString().contains(".jpg") || file.toString().contains(".jpeg") || file.toString().contains(".png")) {
                    intent.setDataAndType(uri, "image/*");
                } else if (file.toString().contains(".pdf")) {
                    intent.setDataAndType(uri, "application/pdf");
                } else if (file.toString().contains(".txt")) {
                    intent.setDataAndType(uri, "text/plain");
                }
            }
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "" + context.getString(R.string.please_install_pdf_reader), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getMimeType(String absolutePath) {
        String type = null;
        /*String extension = MimeTypeMap.getFileExtensionFromUrl(absolutePath);*/
        String extension = getExtensionFromFileName(absolutePath);
        if (extension != null) {
            extension = extension.toLowerCase();
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getExtensionFromFileName(String fileName) {
        String extension = null;
        if (!TextUtils.isEmpty(fileName)) {
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1);
            }
        }
        return extension;
    }

    public String getOnlyDateFromTimeStamp(long dateAdded) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateAdded);
        DateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return df.format(cal.getTime());
    }

    private void createMetaDataJson(FileModel fileModel, boolean isFav) {
        // JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
        FileWriter fileWriter = null;
        JSONObject obj = new JSONObject();
        try {
            obj.put(Constants.NODE_ISFAV, isFav);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JSONArray tags = new JSONArray();
        tags.put("Tag: idcards");
        tags.put("Tag: office_work");
        tags.put("Tag: personal_docs");

        try {
            obj.put("Tags", tags);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            // Constructs a FileWriter given a file name, using the platform's default charset
            fileWriter = new FileWriter(fileModel.getFolder() + "/" + fileModel.getName() + "/" + Constants.JSON_FILE_NAME);
            fileWriter.write(obj.toString());

        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void readUpdateCreateMetaDataJson(FileModel fileModel) {
        try {
            FileReader fileReader = new FileReader(fileModel.getFolder() + "/" + fileModel.getName() + "/" + Constants.JSON_FILE_NAME);
            BufferedReader br = new BufferedReader(fileReader);

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {

                sb.append(line);
//                sb.append(System.lineSeparator());
            }
            Log.i(TAG, "sb: " + sb.toString());

            String jsonStr = sb.toString();

            try {

                JSONObject obj = new JSONObject(jsonStr);

                boolean isFav = obj.getBoolean(Constants.NODE_ISFAV);
                String tags = obj.getString("Tags");

                Log.i(TAG, "isFav: " + isFav);
                Log.i(TAG, "tags: " + tags);

                //update existing json file
                createMetaDataJson(fileModel, !isFav);
            } catch (Throwable t) {
                Log.e(TAG, "Could not parse malformed JSON: \"" + jsonStr + "\"");
            }
        } catch (FileNotFoundException e) {
            //First time (json file not created yet)
            createMetaDataJson(fileModel, true);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String uriNameFromUri(ContentResolver resolver, Uri uri) {
        String name = null;
        Cursor returnCursor = null;
        try {
            returnCursor = resolver.query(uri, null, null, null, null);
            int nameIndex;
            if (returnCursor != null && returnCursor.moveToFirst()) {
                nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                name = returnCursor.getString(nameIndex);
            }
        } finally {
            if (returnCursor != null) {
                returnCursor.close();
            }
        }
        return name;
    }

    public File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //deletePreviousCreatedTempFiles(storageDir);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        // Save a file: path for use with ACTION_VIEW intents
        String absolutePath = image.getAbsolutePath();
        return image;
    }

    private void deletePreviousCreatedTempFiles(File storageDir) {
        if (storageDir != null && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    deletePreviousCreatedTempFiles(file);
                }
            }
        }
        if (storageDir != null && storageDir.isFile()) {
            storageDir.delete();
        }
    }

    public Bitmap getBitmapFromUri(Uri imageUri) {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;
        Bitmap bitmap = null;
        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, newOptions);
            newOptions.inSampleSize = calculateInSampleSize(newOptions, MAX_WIDTH, MAX_HEIGHT);
            newOptions.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, newOptions);
            if (bitmap != null) {
                bitmap = rotateImageIfRequired(bitmap, imageUri);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap rotateImageIfRequired(Bitmap imageBitmap, Uri imageUri) {
        Bitmap bitmap;
        try {
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            ExifInterface exifInterface = null;
            if (Build.VERSION.SDK_INT > 23) {
                if (input != null) {
                    exifInterface = new ExifInterface(input);
                }
            } else {
                String path = imageUri.getPath();
                if (path != null) {
                    exifInterface = new ExifInterface(path);
                }
            }
            if (exifInterface != null) {
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap = rotateImage(imageBitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap = rotateImage(imageBitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap = rotateImage(imageBitmap, 270);
                        break;
                    default:
                        bitmap = imageBitmap;
                        break;
                }
            } else {
                bitmap = imageBitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            bitmap = imageBitmap;
        }
        return bitmap;
    }

    public Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        if (img != null && !img.isRecycled()) {
            try {
                img.recycle();
                img = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rotatedImg;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
     /*   // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;*/
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio);

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public String getFileDateFormatName() {
        String fileName;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault());
        fileName = simpleDateFormat.format(calendar.getTime());
        return fileName;
    }

    public String getFolderCurrentTime() {
        String folderName;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.getDefault());
        String format = simpleDateFormat.format(calendar.getTime());
        folderName = "New Doc" + " " + format + "_" + context.getString(R.string.suffix_app_name);
        return folderName;
    }

    public void shareMultiple(Uri uris, Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.here_are_some_files, context.getString(R.string.app_name)));
        String shareMessage = context.getString(R.string.app_share_msg);
        shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uris);
        intent.setClipData(ClipData.newRawUri("", uris)); // Use setClipData
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uris, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

    public void intentToBrowser(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "" + context.getString(R.string.no_app_handle), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "" + context.getString(R.string.no_app_handle), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void shareTextContent(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.ocr_result_text));
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share));
        context.startActivity(shareIntent);
    }

    public void playBeepSound() {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 150);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
    }

    public int getCurrentAppVersionCode() {
        int appVersionCode = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (packageInfo != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    appVersionCode = (int) packageInfo.getLongVersionCode(); // avoid huge version numbers and you will be ok
                } else {
                    appVersionCode = packageInfo.versionCode;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersionCode;
    }

    public String getCurrentAppVersionName() {
        String appVersionName = null;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (packageInfo != null) {
                appVersionName = packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersionName;
    }

    public void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        /*String shareMessage = "\nLet me recommend you this application\n\n";*/
        String shareMessage = context.getString(R.string.app_share_msg);
        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        context.startActivity(Intent.createChooser(shareIntent, "choose one"));
    }

    public void rateUs() {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (Exception e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public void intentToEmail(String toEmail, String subject, String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, text);
        try {
            context.startActivity(Intent.createChooser(i, context.getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, context.getString(R.string.no_email_client), Toast.LENGTH_SHORT).show();
        }
    }

    public String getCurrentDateAndTime() {
        String currentDateAndTime;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy, hh:mm aaa", Locale.getDefault());
        currentDateAndTime = simpleDateFormat.format(calendar.getTime());
        return currentDateAndTime;
    }

    public void copyToClipboard(String textToBeCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("OCR text", textToBeCopy);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            Toast.makeText(context, context.getString(R.string.text_copied_clipboard), Toast.LENGTH_SHORT).show();
        }
    }

    public void queryTextOnBrowser(String textToBeSearch) {
        boolean isValidUrl;
        isValidUrl = isValidUrl(textToBeSearch);
        if (isValidUrl) {
            intentToBrowser(textToBeSearch);
        } else {
            String escapedQuery;
            try {
                escapedQuery = URLEncoder.encode(textToBeSearch, "UTF-8");

                if (!TextUtils.isEmpty(escapedQuery)) {
                    Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
                    Intent intent = new Intent(Intent.ACTION_SEARCH, uri);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        // No app can handle the intent, show an error message
                        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, textToBeSearch); // query contains search string
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    // No app can handle the intent, show an error message
                    Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public boolean isValidUrl(String textToBeSearch) {
        boolean isValidUrl = false;
        if (Patterns.WEB_URL.matcher(textToBeSearch.toLowerCase()).matches() && URLUtil.isValidUrl(textToBeSearch)) {
            isValidUrl = true;
        }
        return isValidUrl;
    }

  /*  public long getFileCreationTime(Path path) {
        long fileCreationTime = 0;
        try {
            if (path != null) {
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                fileCreationTime = attr.creationTime().to(TimeUnit.MILLISECONDS);
            }
        } catch (Exception ex) {
            fileCreationTime = 0;
        }
        return fileCreationTime;
    }*/

    public void logHomeScreenClickEvent(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_NAME, eventName);
        AppController.getFirebaseAnalytics().logEvent(HOME_SCREEN_CLICKED_EVENT, bundle);
    }

    public void logHomeScreenClickEventNew(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_NAME, eventName);
        AppController.getFirebaseAnalytics().logEvent(eventName, bundle);
    }

    public void logFirebaseEvent(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_NAME, eventName);
        AppController.getFirebaseAnalytics().logEvent(eventName, bundle);
    }

    public void logQrBarCodeResultEvents(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_NAME, eventName);
        AppController.getFirebaseAnalytics().logEvent(QR_BARCODE_RESULT_SCREEN_EVENT, bundle);
    }

    public void logOcrResultEvents(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_NAME, eventName);
        AppController.getFirebaseAnalytics().logEvent(OCR_RESULT_SCREEN_EVENT, bundle);
    }

    public boolean isConnectingToInternet() {
        boolean isConnectingToInternet = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                isConnectingToInternet = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
            }
        }
        return isConnectingToInternet;
    }

    public void logPdfCreation(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_NAME, eventName);
        AppController.getFirebaseAnalytics().logEvent(PDF_CREATION_EVENT, bundle);
    }

    public WaterMark getWaterMark() {
        WaterMark waterMark = new WaterMark();
        waterMark.setWaterMarkText(context.getString(R.string.scanned_by_flashscan, context.getString(R.string.app_name)));
        waterMark.setImage(getLogoByte());
        int color = Color.parseColor("#000000");
        waterMark.setTextColor(new BaseColor(color));
        /*waterMark.setBaseColor(new BaseColor(Color.red(ContextCompat.getColor(context, android.R.color.black)),
                Color.green(ContextCompat.getColor(context, android.R.color.black)),
                Color.blue(ContextCompat.getColor(context, android.R.color.black)),
                Color.alpha(ContextCompat.getColor(context, android.R.color.black))));*/
        waterMark.setTextSize(20);
        waterMark.setFontFamily(Font.HELVETICA);
        waterMark.setFontStyle(Font.NORMAL);
        waterMark.setRotationAngle(0);
        return waterMark;
    }

    public void addToContacts(String phoneNumber) {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        context.startActivity(intent);
    }

    public void dialCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public void sendSms(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));  // This ensures only SMS apps respond
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public void openGoogleMap(double lat, double lng) {
        String uri = "geo:" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "" + context.getString(R.string.no_app_handle), Toast.LENGTH_SHORT).show();
        }

    }

    public void openWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public void addEventToCalendar(String title, String location, long begin,
                                   long end, String description, String organizer, String status) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.putExtra(CalendarContract.Events.ORGANIZER, organizer);
        intent.putExtra(CalendarContract.Events.STATUS, status);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public long getTimeInMillis(int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTimeInMillis();
    }

    public void insertContactInfo(String name, ArrayList<String> emails, String organization, String title,
                                  String address, String[] urls, ArrayList<MultiplePhones> multiplePhones) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        ArrayList<ContentValues> data = new ArrayList<>();
        if (multiplePhones != null && !multiplePhones.isEmpty()) {
            /*ArrayList<ContentValues> data = new ArrayList<>();*/
            for (MultiplePhones multiplePhone : multiplePhones) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, multiplePhone.getNumber());
                contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, multiplePhone.getType());
                data.add(contentValues);
            }
            /*intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);*/
        }

        if (urls != null && urls.length > 0) {
            /*ArrayList<ContentValues> data = new ArrayList<>();*/
            for (String url : urls) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
                contentValues.put(ContactsContract.CommonDataKinds.Website.URL, url.trim());
                data.add(contentValues);
            }
            /*intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);*/
        }

        if (emails != null && !emails.isEmpty()) {
            /*ArrayList<ContentValues> data = new ArrayList<>();*/
            for (String email : emails) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                contentValues.put(ContactsContract.CommonDataKinds.Email.ADDRESS, email.trim());
                data.add(contentValues);
            }
            /*intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);*/
        }
        if (!data.isEmpty()) {
            intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
        }

        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        /*intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email);*/
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, organization);
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title);
        intent.putExtra(ContactsContract.Intents.Insert.POSTAL, address);
        /*intent.putExtra(ContactsContract.Intents.Insert.NOTES, url);*/
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public Bitmap imageViewToBitmap(ImageView imageView) {
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }

    public Bitmap overLay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    public Bitmap combineImageIntoOne(ArrayList<Bitmap> bitmap) {
        int w = 0, h = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            if (i < bitmap.size() - 1) {
                w = Math.max(bitmap.get(i).getWidth(), bitmap.get(i + 1).getWidth());
            }
            h += bitmap.get(i).getHeight();
        }

        Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        int top = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            Log.d("HTML", "Combine: " + i + "/" + bitmap.size() + 1);

            top = (i == 0 ? 0 : top + bitmap.get(i).getHeight());
            canvas.drawBitmap(bitmap.get(i), 0f, top, null);
        }
        return temp;
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs;

        int width, height;

        if (c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/

        return cs;
    }

    public Bitmap makeTransparentBitmap(Bitmap bmp, int alpha) {
        Bitmap transBmp = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(transBmp);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bmp, 0, 0, null);
        return transBmp;
    }

    public String getRealPathFromUri(Uri uri) {
        String realPath = null;
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int columnIndexOrThrow = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                realPath = cursor.getString(columnIndexOrThrow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return realPath;
    }

   /* public Bitmap tryMagicColor(Bitmap bitmap){

        Mat adaptiveTh = new Mat();
       // Mat adaptiveTh = new Mat(bitmap.getHeight(),bitmap.getWidth(), CvType.CV_16SC1);
        Utils.bitmapToMat(bitmap, adaptiveTh);
        //Imgproc.cvtColor(adaptiveTh, adaptiveTh, Imgproc.CV_BGR2YCrCb);
        Imgproc.cvtColor(adaptiveTh, adaptiveTh, Imgproc.COLOR_BGR2RGB);
        Imgproc.medianBlur(adaptiveTh, adaptiveTh, 3);
        Imgproc.adaptiveThreshold(adaptiveTh, adaptiveTh, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
        Bitmap bmp = Bitmap.createBitmap(adaptiveTh.cols(), adaptiveTh.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(adaptiveTh, bmp);

        return bmp;
    }*/

    public Bitmap createTransparentBitmap(Bitmap bitmap, int replaceThisColor) {
        if (bitmap != null) {
            int picw = bitmap.getWidth();
            int pich = bitmap.getHeight();
            int[] pix = new int[picw * pich];
            bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);

            for (int y = 0; y < pich; y++) {
                // from left to right
                for (int x = 0; x < picw; x++) {
                    int index = y * picw + x;
                    int r = (pix[index] >> 16) & 0xff;
                    int g = (pix[index] >> 8) & 0xff;
                    int b = pix[index] & 0xff;

                    if (pix[index] == replaceThisColor) {
                        pix[index] = Color.TRANSPARENT;
                    } else {
                        break;
                    }
                }

                // from right to left
                for (int x = picw - 1; x >= 0; x--) {
                    int index = y * picw + x;
                    int r = (pix[index] >> 16) & 0xff;
                    int g = (pix[index] >> 8) & 0xff;
                    int b = pix[index] & 0xff;

                    if (pix[index] == replaceThisColor) {
                        pix[index] = Color.TRANSPARENT;
                    } else {
                        break;
                    }
                }
            }

            Bitmap bmp = Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_4444);
            return bmp;

        }
        return null;
    }


   /* private Mat img;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");

                    img = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };*/


    /*public Bitmap removeBackground(Bitmap bitmap) {
        //GrabCut part

        Utils.bitmapToMat(bitmap, img);

        int r = img.rows();
        int c = img.cols();
        *//*Point p1 = new Point(c / 100, r / 100);
        Point p2 = new Point(c - c / 100, r - r / 100);*//*
        Point p1 = new Point(c / 5, r / 5);
        Point p2 = new Point(c - c / 5, r - r / 8);
        Rect rect = new Rect(p1, p2);

        Mat mask = new Mat();
        mask.setTo(new Scalar(125));
        Mat fgdModel = new Mat();
        fgdModel.setTo(new Scalar(255, 255, 255));
        Mat bgdModel = new Mat();
        bgdModel.setTo(new Scalar(255, 255, 255));

        Mat imgC3 = new Mat();
        Imgproc.cvtColor(img, imgC3, Imgproc.COLOR_RGBA2RGB);

        Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 5, Imgproc.GC_INIT_WITH_RECT);

        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
        Core.compare(mask, source*//* GC_PR_FGD *//*, mask, Core.CMP_EQ);

        //This is important. You must use Scalar(255,255, 255,255), not Scalar(255,255,255)
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        img.copyTo(foreground, mask);

        //  convert matrix to output bitmap
        bitmap = Bitmap.createBitmap((int) foreground.size().width, (int) foreground.size().height,
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(foreground, bitmap);
        *//*findEdges(bitmap);*//*
        return bitmap;
    }*/

    public void initOpenCv() {
        if (!OpenCVLoader.initLocal()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initLocal(OpenCVLoader.OPENCV_VERSION, context, mLoaderCallback);
            OpenCVLoader.initLocal();
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public Bitmap eraseBackground(Bitmap bitmap) {
        Bitmap dummyBmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        dummyBmp.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(dummyBmp);
        canvas.drawBitmap(bitmap, 0f, 0f, null);
        if (bitmap != null && !bitmap.isRecycled()) {
            try {
                bitmap.recycle();
                bitmap = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dummyBmp;
    }

    public Bitmap replaceColor(Bitmap src, int fromColor, int targetColor) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int x = 0; x < pixels.length; ++x) {
            /*pixels[x] = Color.TRANSPARENT;*/
            pixels[x] = (pixels[x] == fromColor) ? targetColor : Color.BLACK;
        }

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public int getDominantColor(Bitmap bitmap, GetDominantColorListener getDominantColorListener) {
        this.getDominantColorListener = getDominantColorListener;
        if (!bitmap.isRecycled()) {

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    // by default find 16 colors from the image

                    if (palette != null) {
                        Palette.Swatch dominantSwatch = palette.getDominantSwatch();
                        dominantColor = palette.getDominantColor(0x000000);
                        if (dominantSwatch == null) {
                            return;
                        }
                        dominantColor = dominantSwatch.getRgb();
                        dominantColor = dominantSwatch.getBodyTextColor();
                        float[] hsl = dominantSwatch.getHsl();
                        int population = dominantSwatch.getPopulation();
                        dominantColor = dominantSwatch.getTitleTextColor();

                        if (getDominantColorListener != null) {
                            getDominantColorListener.onGettingDominantColor(dominantColor);
                        }
                    } else {
                        if (getDominantColorListener != null) {
                            getDominantColorListener.onFailureToGetDominantColor();
                        }
                    }

                }
            });

        } else {
            if (getDominantColorListener != null) {
                getDominantColorListener.onFailureToGetDominantColor();
            }
        }
        return dominantColor;
    }

    /*public int getDominantColor(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        int color = scaledBitmap.getPixel(0, 0);
        scaledBitmap.recycle();
        return color;
    }*/

    public List<PageSize> getPageSizeList() {
        List<PageSize> pageSizeList = new ArrayList<>();
        pageSizeList.add(new PageSize(context.getString(R.string.A4), String.valueOf(org.openpdf.text.PageSize.A4)));
        pageSizeList.add(new PageSize(context.getString(R.string.letter), String.valueOf(org.openpdf.text.PageSize.LETTER)));
        pageSizeList.add(new PageSize(context.getString(R.string.legal), String.valueOf(org.openpdf.text.PageSize.LEGAL)));
        pageSizeList.add(new PageSize(context.getString(R.string.executive), String.valueOf(org.openpdf.text.PageSize.EXECUTIVE)));
        pageSizeList.add(new PageSize(context.getString(R.string.ledger), String.valueOf(org.openpdf.text.PageSize.LEDGER)));
        pageSizeList.add(new PageSize(context.getString(R.string.tabloid), String.valueOf(org.openpdf.text.PageSize.TABLOID)));
      /*  pageSizeList.add(new PageSize(context.getString(R.string.a0_841_1189), context.getString(R.string.A0)));
        pageSizeList.add(new PageSize(context.getString(R.string.a1_594_841), context.getString(R.string.A1)));
        pageSizeList.add(new PageSize(context.getString(R.string.a2_420_594), context.getString(R.string.A2)));*/
        pageSizeList.add(new PageSize(context.getString(R.string.A3), String.valueOf(org.openpdf.text.PageSize.A3)));
        /*pageSizeList.add(new PageSize(context.getString(R.string.a4_210_297), context.getString(R.string.A4)));*/
        pageSizeList.add(new PageSize(context.getString(R.string.A5), String.valueOf(org.openpdf.text.PageSize.A5)));
       /* pageSizeList.add(new PageSize(context.getString(R.string.a6_105_148), context.getString(R.string.A6)));
        pageSizeList.add(new PageSize(context.getString(R.string.a7_74_105), context.getString(R.string.A7)));
        pageSizeList.add(new PageSize(context.getString(R.string.a8_52_74), context.getString(R.string.A8)));
        pageSizeList.add(new PageSize(context.getString(R.string.a9_37_52), context.getString(R.string.A9)));
        pageSizeList.add(new PageSize(context.getString(R.string.a10_26_37), context.getString(R.string.A10)));
        pageSizeList.add(new PageSize(context.getString(R.string.b0_1000_1414), context.getString(R.string.B0)));
        pageSizeList.add(new PageSize(context.getString(R.string.b1_707_1000), context.getString(R.string.B1)));
        pageSizeList.add(new PageSize(context.getString(R.string.b2_500_707), context.getString(R.string.B2)));
        pageSizeList.add(new PageSize(context.getString(R.string.b3_353_500), context.getString(R.string.B3)));
        pageSizeList.add(new PageSize(context.getString(R.string.b4_250_353), context.getString(R.string.B4)));
        pageSizeList.add(new PageSize(context.getString(R.string.b5_176_250), context.getString(R.string.B5)));
        pageSizeList.add(new PageSize(context.getString(R.string.b6_125_176), context.getString(R.string.B6)));
        pageSizeList.add(new PageSize(context.getString(R.string.b7_88_125), context.getString(R.string.B7)));
        pageSizeList.add(new PageSize(context.getString(R.string.b8_62_88), context.getString(R.string.B8)));
        pageSizeList.add(new PageSize(context.getString(R.string.b9_44_62), context.getString(R.string.B9)));
        pageSizeList.add(new PageSize(context.getString(R.string.b10_31_44), context.getString(R.string.B10)));*/
        return pageSizeList;
    }

    public void clearTempFiles(File externalFilesDir) {
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

    public String getSavedBitmapFileName(Context context, Bitmap bitmap) {
        String fileName = "bitmap.png";
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            if (fileOutputStream != null)
                fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public File getTempCameraPictureFile() {
        File tempFile = null;
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null)
            return null;
        File file = new File(externalFilesDir, "CameraPictures");
        boolean isDirectoryCreated = false;
        if (!file.exists()) {
            isDirectoryCreated = file.mkdirs();
        } else {
            if (file.isDirectory()) {
                isDirectoryCreated = true;
            }
        }
        if (isDirectoryCreated) {
            String imageFileName = "temp_";
            try {
                tempFile = File.createTempFile(imageFileName, ".jpg", file);
                if (tempFile.isFile()) {
                    return tempFile;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tempFile;
    }

    public String getRealPdfPathFromUri(Uri uri) {

        String fileName = getFileNameFromUri(uri);
        if (!TextUtils.isEmpty(fileName)) {
            File externalFilesDir = context.getExternalFilesDir(null);
            /*if (externalFilesDir != null) {
                clearTempFiles(externalFilesDir);
            }*/

            File copyFile = new File(externalFilesDir + File.separator + fileName);
            copy(context, uri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;

        /*String realPath = null;
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                realPath = cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return realPath;*/
    }

    private void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (!TextUtils.isEmpty(uri.getScheme()) && uri.getScheme().equals("content")) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (TextUtils.isEmpty(fileName)) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    fileName = path.substring(cut + 1);
                }
            }

        }
        return fileName;
    }

    public String saveImageToTempDirectory(File tempDir, String fileName, Bitmap bitmap) {
        String savedImageFilePath = null;
        boolean isTempDirectoryCreated = false;
        if (!tempDir.exists()) {
            isTempDirectoryCreated = tempDir.mkdirs();
        } else {
            if (tempDir.exists()) {
                if (tempDir.isDirectory()) {
                    isTempDirectoryCreated = true;
                }
            }
        }
        File file = null;
        if (isTempDirectoryCreated) {
            file = new File(tempDir, fileName);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
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
        if (file != null && file.isFile() && file.exists()) {
            savedImageFilePath = file.getPath();
        }
        return savedImageFilePath;
    }

    public String getFormattedFileSize(double size) {
        return String.format("%.2f", size / (1024 * 1024));
    }

    public String getFormattedFileSize(File file) {
        return String.format("%.2f", (double) file.length() / (1024 * 1024));
    }

    public String differenceInFormattedFileSize(File originalFile, File compressedFile) {
        double originalSize = (double) originalFile.length() / (1024 * 1024);
        double compressedSize = (double) compressedFile.length() / (1024 * 1024);
        double differenceSize = originalSize - compressedSize;
        return String.format("%.2f MB", differenceSize);
    }

    public List<ColorModel> getColorsList() {
        List<ColorModel> colorList = new ArrayList<>();
        colorList.add(new ColorModel("#000000", false));
        colorList.add(new ColorModel("#FFFFFF", false));
        colorList.add(new ColorModel("#FF0000", false));
        colorList.add(new ColorModel("#006B00", false));
        colorList.add(new ColorModel("#0000FF", false)); // default color for anticounterfeit watermark
        colorList.add(new ColorModel("#FFFF00", false));
        colorList.add(new ColorModel("#FFA200", false));
        colorList.add(new ColorModel("#21708D", false));
        colorList.add(new ColorModel("#B7BEC8", false));
        return colorList;
    }

    public boolean replaceImageFileWithNewBitmap(String oldFilePath, Bitmap bitmap) {
        boolean isFileReplaced = false;
        File oldFile = new File(oldFilePath);
        File newFile = new File(oldFilePath);
        if (oldFile.isFile() && oldFile.exists()) {
            oldFile.delete();
        }
        boolean isNewFileCreated = false;
        try {
            if (!newFile.exists())
                isNewFileCreated = newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isNewFileCreated) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(newFile);
                isFileReplaced = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
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
        return isFileReplaced;
    }


    public void sortFilesByDescendingLastModified(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o2.lastModified(), o1.lastModified()); // last modified descending
            }
        });
    }

    public void sortFilesByAscendingLastModified(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o1.lastModified(), o2.lastModified()); // last modified descending
            }
        });
    }

    public void sortFilesByNameAtoZ(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public void sortFilesByNameZtoA(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });
    }

    public boolean urlValidOrNot(String urls) {
        boolean isOnline = false;
        try {
            URL url = new URL(urls);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout(2000);
            isOnline = conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            isOnline = false;
        }
        Log.i(TAG, "url valid: " + isOnline);
        return isOnline;
    }

    public static double roundUpto2Decimal(double input) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        df2.setRoundingMode(RoundingMode.UP);
        double output = Double.parseDouble(df2.format(input));
        return output;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static File getDocOriginalPath(Context context) {
        File DOC_ORIGINAL_PATH = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_ORIGINAL_DIRECTORY);
       /* File DOC_ORIGINAL_PATH;
        // change Storage Path in Android 11 and above devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DOC_ORIGINAL_PATH = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + File.separator + Constants.ROOT_FOLDER_NAME + File.separator + Constants.originalFolderName);
        } else {
            DOC_ORIGINAL_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    Constants.ROOT_FOLDER_NAME + File.separator + Constants.originalFolderName);
        }*/
        return DOC_ORIGINAL_PATH;
    }

    public static File getDocProcessingPath(Context context) {
        File DOC_PROCESSING_PATH = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY);
        /*File DOC_PROCESSING_PATH;
        // change Storage Path in Android 11 and above devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DOC_PROCESSING_PATH = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + File.separator + Constants.ROOT_FOLDER_NAME);
        } else {
            DOC_PROCESSING_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    Constants.ROOT_FOLDER_NAME);
        }*/
        return DOC_PROCESSING_PATH;
    }

    public static File getPdfOriginalPath(Context context) {
        File PDF_ORIGINAL_PATH = new File(context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_ORIGINAL_DIRECTORY);
       /* File DOC_ORIGINAL_PATH;
        // change Storage Path in Android 11 and above devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DOC_ORIGINAL_PATH = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + File.separator + Constants.ROOT_FOLDER_NAME + File.separator + Constants.originalFolderName);
        } else {
            DOC_ORIGINAL_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    Constants.ROOT_FOLDER_NAME + File.separator + Constants.originalFolderName);
        }*/
        return PDF_ORIGINAL_PATH;
    }

    public static File getPdfProcessingPath(Context context) {
        File PDF_PROCESSING_PATH = new File(context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY);
        /*File DOC_PROCESSING_PATH;
        // change Storage Path in Android 11 and above devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DOC_PROCESSING_PATH = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + File.separator + Constants.ROOT_FOLDER_NAME);
        } else {
            DOC_PROCESSING_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    Constants.ROOT_FOLDER_NAME);
        }*/
        return PDF_PROCESSING_PATH;
    }



    public String getDefaultPathForOcr() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + Constants.FLASH_SCAN_OCR;
        } else {
            return Environment.DIRECTORY_PICTURES + "/" + Constants.FLASH_SCAN_OCR;
        }
    }

    public static File getOcrProcessingPath(Context context) {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.ITL_OCR_DIRECTORY);
    }

    public String getDefaultPathForOcr(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + Constants.FLASH_SCAN_OCR;
        } else {
            return Environment.DIRECTORY_PICTURES + "/" + Constants.FLASH_SCAN_OCR;
        }
    }


    public boolean isDriveSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(context) != null;
    }

    public GoogleSignInClient requestSignIn(Context context) {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        return GoogleSignIn.getClient(context, signInOptions);

    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #}.
     */
    public void handleSignInResult(Context context, Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    context, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

   /* public static File getPath(Context context, String directory, String rootFolderName){
        File DOC_PROCESSING_PATH;
        // change Storage Path in Android 11 and above devices
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            DOC_PROCESSING_PATH  = new File(context.getExternalFilesDir(directory)
                    + File.separator + rootFolderName);
        }else{
            DOC_PROCESSING_PATH  = new File(Environment.getExternalStoragePublicDirectory(directory),
                    rootFolderName);
        }
        return DOC_PROCESSING_PATH;
    }*/


    public void saveFileInGoogleDrive(Context context, String folderName, FileModel fileModel, boolean isUpdate, String dialogMsg, GoogleDriveDataUploadListener listener) {
        Dialog progressDialog = new Dialog(context);
        progressDialog.setContentView(R.layout.progress_dialog_centered);
        progressDialog.setCancelable(false);

        TextView tvMsg = progressDialog.findViewById(R.id.tv_msg);
        tvMsg.setText(dialogMsg);
        progressDialog.show();

        Log.d(TAG, "saveFileInDrive: " + new Gson().toJson(fileModel));
        if (mDriveServiceHelper == null) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                return;
            }
            mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(context, account, "appName"));
        }

        // you can provide  folder id in case you want to save this file inside some folder.
        // if folder id is null, it will save file to the root
        mDriveServiceHelper.createFolderIfNotExist(folderName, null)
                .addOnSuccessListener(googleDriveParentFileHolder -> {
                    // create sub folder to save files

                    String subFolderName = fileModel.getName();
                    mDriveServiceHelper.createFolderIfNotExist(subFolderName, googleDriveParentFileHolder.getId())
                            .addOnSuccessListener(googleDriveSubFileHolder -> {
                                Log.d(TAG, "onSuccess: " + new Gson().toJson(googleDriveSubFileHolder));
                                if (!googleDriveSubFileHolder.isAlreadyExist()) {
                                    // Creates a new File instance by converting the given pathname string
                                    // into an abstract pathname
                                    File folder = new File(fileModel.getPath());
                                    List<GoogleDriveChildFileModel> googleDriveChildFileModelList = new ArrayList<>();

                                    int currentFileCount = 0;
                                    int totalFileCount = Objects.requireNonNull(folder.list()).length;
                                    boolean isDismissLoader = false;

                                    // Populates the array with names of files
                                    for (String fileName : Objects.requireNonNull(folder.list())) {
                                        // Upload all files of sub directory
                                        File file = new File(fileModel.getPath() + "/" + fileName);
                                        currentFileCount++;
                                        if (currentFileCount == totalFileCount) {
                                            isDismissLoader = true;
                                        }
                                        Log.e(TAG, "googleDriveSubFileHolder.getId() " + googleDriveSubFileHolder.getId());
                                        Log.e(TAG, "subFolderName " + subFolderName);
                                        Log.e(TAG, "file " + file.getName());
                                        Log.e(TAG, "currentFileCount " + currentFileCount);
                                        Log.e(TAG, "totalFileCount " + totalFileCount);
                                        Log.e(TAG, "isDismissLoader " + isDismissLoader);

                                        String subChildFileName = file.getName();


                                        uploadFile(googleDriveSubFileHolder.getId(), subFolderName, subChildFileName, googleDriveChildFileModelList, file, getMimeType(file.getAbsolutePath()), progressDialog, isDismissLoader, isUpdate, listener);
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, context.getResources().getString(R.string.doc_already_exist_on_drive), Toast.LENGTH_SHORT).show();
                                    listener.onUploadFinish(googleDriveSubFileHolder.getId());
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "onFailure up3: " + e.getMessage());
                                Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                // Toast.makeText(context, "save fail 3 "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure up1: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(context, "upload fail 1 "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    public void uploadFile(String folderId, String folderName, String subChildFileName, List<GoogleDriveChildFileModel> googleDriveChildFileModelList, File file, String mimeType, Dialog progressDialog, boolean isDismissDialog, boolean isUpdate, GoogleDriveDataUploadListener listener) {
        if (mDriveServiceHelper == null) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                return;
            }
            mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(context, account, "appName"));
        }

        mDriveServiceHelper.uploadFile(file, mimeType, folderId)
                .addOnSuccessListener(googleDriveFileHolder -> {
                    Log.d(TAG, "onSuccess: " + new Gson().toJson(googleDriveFileHolder));
                    Log.d(TAG, "isDismissDialog: " + isDismissDialog);

                    googleDriveChildFileModelList.add(new GoogleDriveChildFileModel(googleDriveFileHolder.getId(), subChildFileName, true));

                    if (isDismissDialog) {
                        Log.e(TAG, "" + googleDriveChildFileModelList.size());
                        if (isUpdate) {
                            new PrefManager(context).updateChildOfGoogleDriveFolder(folderId, googleDriveChildFileModelList);
                        } else {
                            new PrefManager(context).addFolderToGoogleDriveDataList(folderId, folderName, googleDriveChildFileModelList);
                        }
                        Toast.makeText(context, context.getResources().getString(R.string.doc_uploaded_successfully), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "heere finish: " + isDismissDialog);
                        progressDialog.dismiss();
                        listener.onUploadFinish(folderId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure up2: " + e.getMessage());
                    Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(context, "upload fail2 "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    public static Drive getGoogleDriveService(Context context, GoogleSignInAccount account, String appName) {
        Log.d(TAG, "Signed in as drive " + account.getEmail());
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName(appName)
                .build();
    }

    public void queryFolderOnGoogleDrive(Context context, String msg, String folderName, File parent, GoogleDriveDataDownloadListener listener) {

        //Show progress loader dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(msg);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(1);
        progressDialog.show();

        //If mDriveServiceHelper is not initialize first initialize it with last signed in google account
        if (mDriveServiceHelper == null) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                return;
            }
            mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(context, account, "appName"));
        }

        //It search folder in google drive
        mDriveServiceHelper.searchFolder(folderName).addOnSuccessListener(googleDriveFileHolders -> {
            PrefManager prefManager = new PrefManager(context);
            Log.e(TAG, "queryFolderOnGoogleDrive: googleDriveFileHolders size= " + googleDriveFileHolders.size());

            //Check particular folder found or not, if result list is greater than 0 means we found the folder
            if (googleDriveFileHolders.size() > 0) {
                AtomicInteger downloadFileCount = new AtomicInteger();
                AtomicInteger totalFileCount = new AtomicInteger();

                //Iterate over all folder
                //Get all files or folder in the specified folder
                mDriveServiceHelper.queryFiles(googleDriveFileHolders.get(0).getId())
                        .addOnSuccessListener(googleDriveParentFileHolders -> {
                            Log.d(TAG, "onSuccess: " + new Gson().toJson(googleDriveParentFileHolders));
                            if (googleDriveParentFileHolders.size() > 0) {
                                List<GoogleDriveFolderModel> googleDriveFolderModelList = new ArrayList<>();
                                AtomicInteger totalChildFileCount = new AtomicInteger();

                                for (GoogleDriveFileHolder parentFolder : googleDriveParentFileHolders) {
                                    File dstFolderName = new File(parent, parentFolder.getName());

                                    if (!dstFolderName.exists()) {
                                        dstFolderName.mkdirs();
                                    }
                                    GoogleDriveFolderModel googleDriveFolderModel = new GoogleDriveFolderModel();

                                    googleDriveFolderModel.setFolderName(parentFolder.getName());
                                    googleDriveFolderModel.setId(parentFolder.getId());

                                    googleDriveFolderModelList.add(googleDriveFolderModel);
                                    totalChildFileCount.getAndIncrement();
                                    AtomicInteger totalSubChildFileCount = new AtomicInteger();

                                    //Get all files or folder of sub-folder
                                    mDriveServiceHelper.queryFiles(parentFolder.getId())
                                            .addOnSuccessListener(googleDriveChildFileHolders -> {
                                                Log.d(TAG, "onSuccess: " + new Gson().toJson(googleDriveChildFileHolders));

                                                List<GoogleDriveChildFileModel> googleDriveChildFileModelList = new ArrayList<>();

                                                ////Start iterate over folder and download files one by one
                                                for (GoogleDriveFileHolder childFolder : googleDriveChildFileHolders) {
                                                    totalFileCount.getAndIncrement();

                                                    File file = new File(dstFolderName.getAbsolutePath() + "/" + childFolder.getName());
                                                    Log.d(TAG, "queryFolder: file = " + file.getAbsolutePath());

                                                    mDriveServiceHelper.downloadFile(file, childFolder.getId())
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d(TAG, "onSuccess: file download" + childFolder.getId());
                                                                Log.d(TAG, "onSuccess: file download" + childFolder.getName());
                                                                Log.d(TAG, "onSuccess: file parentFolder" + parentFolder.getName());
                                                                downloadFileCount.getAndIncrement();
                                                                totalSubChildFileCount.getAndIncrement();

                                                                int progress = ((downloadFileCount.get()) * 100) / totalFileCount.get();
                                                                progressDialog.setProgress(progress);

                                                                //progressDialog.setProgress(10);

                                                                Log.d(TAG, "onSuccess: totalSubChildFileCount=" + totalSubChildFileCount.get());
                                                                Log.d(TAG, "onSuccess: googleDriveChildFileHolders=" + googleDriveChildFileHolders.size());

                                                                GoogleDriveChildFileModel googleDriveChildFileModel = new GoogleDriveChildFileModel(childFolder.getId(), childFolder.getName(), true);
                                                                googleDriveChildFileModelList.add(googleDriveChildFileModel);
                                                                if (totalSubChildFileCount.get() == googleDriveChildFileHolders.size()) {
                                                                    int index = getIndexById(googleDriveFolderModelList, parentFolder.getId());
                                                                    googleDriveFolderModelList.get(index).setGoogleDriveChildFileModelList(googleDriveChildFileModelList);
                                                                }
                                                                if (downloadFileCount.get() == totalFileCount.get()) {
                                                                    List<GoogleDriveFolderModel> updatedGoogleDriveFolderModelList = prefManager.getGoogleDriveDataList();
                                                                    updatedGoogleDriveFolderModelList.addAll(googleDriveFolderModelList);
                                                                    prefManager.saveGoogleDriveDataList(updatedGoogleDriveFolderModelList);
                                                                    listener.onFinishDownload();
                                                                    progressDialog.dismiss();
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.d(TAG, "onFailure1: " + e.getMessage());
                                                                Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                                                //Toast.makeText(context, "1 "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                List<GoogleDriveFolderModel> updatedGoogleDriveFolderModelList = prefManager.getGoogleDriveDataList();
                                                                updatedGoogleDriveFolderModelList.addAll(googleDriveFolderModelList);
                                                                prefManager.saveGoogleDriveDataList(updatedGoogleDriveFolderModelList);
                                                                listener.onFinishDownload();
                                                                progressDialog.dismiss();
                                                            });
                                                }

                                            })
                                            .addOnFailureListener(e -> {
                                                Log.d(TAG, "onFailure2: " + e.getMessage());
                                                Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                                //Toast.makeText(context, "2 "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                listener.onFinishDownload();
                                                progressDialog.dismiss();
                                            });
                                }
                            } else {
                                listener.onFinishDownload();
                                progressDialog.dismiss();

                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAG, "onFailure3: " + e.getMessage());
                            Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(context, "3 "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            listener.onFinishDownload();
                            progressDialog.dismiss();
                        });
            } else {
                listener.onFinishDownload();
                progressDialog.dismiss();
                if (folderName.equalsIgnoreCase(Constants.ROOT_FOLDER_NAME)) {
                    Toast.makeText(context, context.getResources().getString(R.string.no_docs_on_drive), Toast.LENGTH_SHORT).show();
                } else if (folderName.equalsIgnoreCase(Constants.FLASH_SCAN_OCR)) {
                    Toast.makeText(context, context.getResources().getString(R.string.no_ocr_on_drive), Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(context, "No Data Synced on Drive.", Toast.LENGTH_SHORT).show();
            }

        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure4: " + e.getMessage());
            Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            //Toast.makeText(context, "4 "+e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFinishDownload();
            progressDialog.dismiss();
        });

    }

    private int getIndexById(List<GoogleDriveFolderModel> googleDriveFolderModelArrayList, String folderId) {
        for (int i = 0; i < googleDriveFolderModelArrayList.size(); i++) {
            GoogleDriveFolderModel googleDriveFolderModel = googleDriveFolderModelArrayList.get(i);
            if (googleDriveFolderModel.getId().equals(folderId)) {
                return i;
            }
        }
        return 0;
    }

    public void deleteFolderFromGoogleDrive(Context context, String folderName) {
        Log.d(TAG, "deleteFolderFromGoogleDrive: ");
        //Show progress loader dialog
        Dialog progressDialog = new Dialog(context);
        progressDialog.setContentView(R.layout.progress_dialog_centered);
        progressDialog.setCancelable(false);

        TextView tvMsg = progressDialog.findViewById(R.id.tv_msg);
        tvMsg.setText(context.getResources().getString(R.string.delete_files_from_drive));
        progressDialog.show();


        //If mDriveServiceHelper is not initialize first initialize it with last signed in google account
        if (mDriveServiceHelper == null) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                return;
            }
            mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(context, account, "appName"));
        }

        //It search folder in google drive
        mDriveServiceHelper.searchFolder(folderName).addOnSuccessListener(googleDriveFileHolders -> {
            Log.d(TAG, "onSuccess: " + new Gson().toJson(googleDriveFileHolders));

            //Delete folder if found
            if (googleDriveFileHolders.size() > 0) {
                mDriveServiceHelper.deleteFolderFile(googleDriveFileHolders.get(0).getId()).addOnSuccessListener(unused -> {
                    Log.d(TAG, "delete folder onSuccess: ");
                    progressDialog.dismiss();
                    new PrefManager(context).deleteFolderFromGoogleDriveDataList(googleDriveFileHolders.get(0).getId());
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                    Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
            } else {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: " + e.getMessage());
            Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    public void deleteFolderByIdFromGoogleDrive(Context context, String folderId, String dialogMsg, GoogleDriveDataDeleteListener listener) {
        Log.d(TAG, "deleteFolderByIdFromGoogleDrive: ");
        //Show progress loader dialog
        Dialog progressDialog = new Dialog(context);
        progressDialog.setContentView(R.layout.progress_dialog_centered);
        progressDialog.setCancelable(false);

        TextView tvMsg = progressDialog.findViewById(R.id.tv_msg);
        tvMsg.setText(dialogMsg);
        progressDialog.show();

        //If mDriveServiceHelper is not initialize first initialize it with last signed in google account
        if (mDriveServiceHelper == null) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                return;
            }
            mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(context, account, "appName"));
        }

        //Delete folder from google drive
        mDriveServiceHelper.deleteFolderFile(folderId).addOnSuccessListener(unused -> {
            Log.d(TAG, "delete folder onSuccess: ");
            progressDialog.dismiss();
            listener.onDeleted();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: " + e.getMessage());
            Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    public static void newShowLoading(Context mContext, String msg) {
        /*loadingDialog = new Dialog(mContext);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        loadingDialog.setContentView(R.layout.custom_progress);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        loadingDialog.getWindow().setGravity(Gravity.CENTER);

        TextView txtMsg = loadingDialog.findViewById(R.id.txtMsg);
        //AVLoadingIndicatorView AVLoadingIndicatorView = loadingDialog.findViewById(R.id.loadingView);
        ProgressBar progress_lay = loadingDialog.findViewById(R.id.progress_lay);
        txtMsg.setText(msg);
        //AVLoadingIndicatorView.smoothToShow();
        progress_lay.setVisibility(View.VISIBLE);
        loadingDialog.show();*/
        CommonMethods.showLoading(mContext);
    }

    public static void newHideLoading() {
        /*if (loadingDialog != null) {
            loadingDialog.dismiss();
        }*/
        CommonMethods.hideLoading();
    }

    public void renameFile(String fileId, String newTitle) {
        Log.e(TAG, "rename");

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account == null) {
            return;
        }
        Drive service = getGoogleDriveService(context, account, "appName");
        com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
        file.setName(newTitle);
        try {
            service.files().update(fileId, file).execute();
            Log.e(TAG, "renameFile: successful");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getClipData(Intent data, Context context) {
        List<String> filePaths = new ArrayList<>();
        if (data != null && data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                java.lang.String filePath = getPathFromUri(uri, context);
                if (filePath != null) {
                    filePaths.add(filePath);
                }
            }
        } else if (data != null && data.getData() != null) {
            Uri uri = data.getData();
            java.lang.String filePath = getPathFromUri(uri, context);
            if (filePath != null) {
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }

    private static String getPathFromUri(Uri uri, Context context) {
        String[] projection = {MediaStore.Images.Media.DATA};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public static List<Uri> obtainResult(Intent data) {
        List<Uri> uris = new ArrayList<>();
        if (data != null && data.getClipData() != null) {
            android.content.ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                uris.add(uri);
            }
        } else if (data != null && data.getData() != null) {
            Uri uri = data.getData();
            uris.add(uri);
        }
        return uris;
    }

    private byte[] getLogoByte() {
        Drawable drawable = ContextCompat.getDrawable(context,R.drawable.watermark);
        Bitmap bitmap = drawableToBitmap(drawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean isOsLessThanR() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R;
    }

}
