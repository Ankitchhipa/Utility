package com.cam.scanner.scantopdf.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.cam.scanner.scantopdf.android.BuildConfig;

public class Constants {
    public static boolean isAppInBackground = false;
    public static final String URL_BASE = "http://srv.theflashscan.com/Service1.svc/";
    public static final String URL_GET_PLANS = URL_BASE + "getPlans";
    public static final String URL_REGISTER = URL_BASE + "register";
    public static final String URL_UPDATE_CREDIT = URL_BASE + "updateCredit";
    public static final String URL_UPDATE_Email = URL_BASE + "updateEmail";
    public static final String URL_ORDER = URL_BASE + "order";

    public static final String ROOT_FOLDER_NAME = BuildConfig.APPLICATION_NAME + "DocScanner";
    public static final long SPLASH_TIME_OUT = 500;
    public static final long NO_AD_SPLASH_TIME_OUT = 500;
    public static final int RECENT_DOCS_COUNT_LIMIT = 3;
    public static final int RECENT_DOCS_COUNT_LIMITLESS = -1;

    public static final int FAVORITE_DOCS_COUNT_LIMIT = 5;
    public static final int FAVORITE_DOCS_COUNT_LIMITLESS = -1;
    public static final String FLASH_SCAN_OCR = BuildConfig.APPLICATION_NAME + "Ocr";
    public static final String TXT_FILE_EXTENSION = ".txt";
    public static final String TXT_FILE_EXTENSION_WITHOUT_DOT = "txt";
    public static final long OCR_RESCAN_DELAY = 3000;
    public static final long NAV_DRAWER_CLOSE_TIME = 300;
    public static final long TICK_ANIMATION_DELAY = 1000;
    public static final int AD_PER_ITEM = 1;
    public static final int AD_PER_ITEM_RECENT = 0;
    public static final long AD_HOLDING_TIME = 5500;
    public static final String AUTHORITY_APP = BuildConfig.APPLICATION_ID + ".fileprovider";
    public static final String IS_IMPORT_PDF_FROM_WITHIN_APP = "is_import_pdf_from_within_app";
    public static final String originalFolderName = "original";
    public static  File DOC_PROCESSING_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Constants.ROOT_FOLDER_NAME);

    public static  File DOC_ORIGINAL_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Constants.ROOT_FOLDER_NAME + File.separator + originalFolderName);


    public static final double COMPRESS_PDF_THRESHOLD = 1024.0;
    public static final int PLAN_FREE = 1;
    public static final int PLAN_OCR_MONTHLY = 3;
    public static final int PLAN_PEMIUM_YEARLY = 2;
    public static final int PLAN_PEMIUM_MONTHLY = 7;
    public static final int PLAN_FREE_TEST = 4;
    public static final int PLAN_OCR_MONTHLY_TEST = 6;
    public static final int PLAN_PEMIUM_YEARLY_TEST = 5;
    public static final int PLAN_PEMIUM_MONTHLY_TEST = 8;
    public static final String FROM_NAV_CHOOSE_PLAN = "from_nav_choose_plan";
    public static final String FROM_NAV = "from_nav";
    public static final int NAV_FROM_OCRACTIVITY = 801;
    public static final int FROM_SPLASH = 802;
    public static final int FROM_NOTIF = 803;
    public static final String PLANS_FILE = "plans.json";
    public static final String EXTRA_NAV_TO_PLAN = "extra_nav_to_plan";
    public static final String EXTRA_BACKSTACKOFFER = "backstackOffer";
    public static final String EXTRA_BACKSTACK = "backstack";
    public static final String EXTRA_PLAN_ID_IN_NOTIF = "plan_id_in_notif";
    public static final int PLAN_ACTIVITY_PREMIUM = 651;
    public static final int PLAN_ACTIVITY_OCR_MONTHLY = 652;
    public static final int PLAN_ACTIVITY_CHOOSE_PLAN = 653;
    public static final String EXTRA_PLAN_PAID_SUCCESS = "extra_plan_paid_success";
    public static String NOTIFY_TOKEN = "notify_token";

    public static boolean YEARLY_PLAN_RESTORED = false;
    public static boolean QUARTERLY_PLAN_RESTORED = false;

    public static boolean isSplashAdLoad = false;
    public static String docs = "DOCS";

    public static final String ITL_PDF_DIRECTORY = BuildConfig.APPLICATION_NAME + "/" + BuildConfig.MODULE_NAME + "/" + BuildConfig.APPLICATION_NAME + "PDF";
    public static final String ITL_PDF_DOCS_DIRECTORY = ITL_PDF_DIRECTORY + File.separator + docs;
    public static final String ITL_PDF_ORIGINAL_DIRECTORY = ITL_PDF_DIRECTORY + File.separator + docs + File.separator + originalFolderName;
    public static final String ITL_OCR_DIRECTORY = BuildConfig.APPLICATION_NAME + "/" + BuildConfig.MODULE_NAME + "/" + BuildConfig.APPLICATION_NAME + "OCR";
    public static final String PDF_FILE_EXTENSION = ".pdf";

    public interface PutExtraConstants {
        String FILE_OPERATION_ACTION = "file_operation_action";
        String FILE_PATH = "file_path";
        String FILE_NAME = "file_name";
        String FILE_PATH_LIST = "file_path_list";
        String FOLDER_PATH = "folder_path";
        String URI = "uri";
        String OCR_RESULT_TEXT = "ocr_result_text";
        String OCR_RESULT_FROM_SCREEN = "ocr_result_screen_from";
        String BAR_QR_CODE_RESULT = "bar_qr_code_result";
        String OCR_SAVED_FILE_PATH = "ocr_saved_file_path";
        String URL = "url";
        String DISPLAY_INTERSTITIAL_AD_ON_LAUNCH = "display_interstitial_ad_on_launch";
        String BAR_QR_VALUE_FORMAT = "bar_qr_value_format";
        String SCANNED_BARCODE = "scanned_barcode";
        String IS_COMING_FROM_HOME_DASHBOARD = "is_coming_from_home_dashboard";
        String FOLDER_NAME = "folder_name";
        String IS_COMING_FROM_CUSTOM_GALLERY = "is_coming_from_custom_gallery";
        String SELECTED_IMAGES_LIST = "selected_images_list";
        String IS_COMING_FROM_CROPPED_IMAGE_ACTIVITY = "is_coming_from_cropped_image_activity";
        String SCANNED_RESULT = "scannedResult";
        String IS_COMING_FROM_SELECTED_IMAGES_ACTIVITY = "is_coming_from_selected_images_activity";
        String FROM_SOURCE = "from_source";
        String SAVED_PDF_PATH = "saved_pdf_path";
        String OCR_IS_NET_WORKING = "ocr_is_net_working";
    }

    public interface PdfConstants {
        String DEFAULT_PDF_PAGE_SIZE = "A4";
        int DEFAULT_PDF_PAGE_COLOR = Color.WHITE;
        int DEFAULT_PDF_QUALITY = 30;
        int DEFAULT_BORDER_WIDTH = 0;
    }

    public interface FileOperations {
        int ACTION_RENAME = 1;
        int ACTION_DELETE = 2;
        int ACTION_MOVE = 3;
        int ACTION_COPY = 4;
        int ACTION_SHARE = 5;
    }

    public interface FileExtensions {
        String PDF = "pdf";
        String JPG = "jpg";
        String PNG = "png";
        String JPEG = "jpeg";
        String GIF = "gif";
        String WEBP = "webp";
    }

    public static final String JSON_FILE_NAME = "metajson.txt";
    public static final String NODE_ISFAV = "isFav";

    public interface OcrResultScreenFrom {
        int FROM_PREVIEW = 1;
        int FROM_DOCUMENT = 2;
    }

    public interface URLs {
        String PRIVACY_POLICY = "http://theflashscan.com/privacy-policy/";
        String APP_WEBSITE_URL = "https://theflashscan.com/";
        String CUSTOMER_SUPPORT_URL = "apps@innovanatechlabs.com";
    }

    public interface SORT_BY {
        int creationTimeAscending = 1;
        int creationTimeDescending = 2;
        int modificationTimeAscending = 3;
        int modificationTimeDescending = 4;
        int nameAtoZ = 5;
        int nameZtoA = 6;
        int defaultOrder = 7;
        int noChange = 8;
    }

    // ads management by flags
    public interface SHOW_INTERSTITIAL_ADS {
        boolean FOR_SPLASH = false; //replaced with firebase remote config
        boolean FOR_SCAN_RESULT_ACTIVITY = false; // applied only on OcrPreviewActivity
        boolean FOR_OCR_RESULT_ACTIVITY = false; //replaced with firebase remote config
        boolean FOR_EXIT_APP_DIALOG = true; //replaced with firebase remote config
    }

    public interface SHOW_MEDIUM_BANNER_ADS {
        boolean FOR_HOME_ACTIVITY = false;
        boolean FOR_RECYCLERVIEW_LIST = false;
        boolean FOR_DOC_SCANNER_ACTIVITY = false;
        boolean FOR_OCR_ACTIVITY = false;
        boolean FOR_FAVORITES_DOCUMENTS_ACTIVITY = false;
    }

    public interface SHOW_NATIVE_ADS {
        boolean FOR_HOME_ACTIVITY = false; //replaced with firebase remote config
        boolean FOR_RECYCLERVIEW_LIST = true;
        boolean FOR_DOC_SCANNER_ACTIVITY = false; //replaced with firebase remote config
        boolean FOR_OCR_ACTIVITY = false; //replaced with firebase remote config
        boolean FOR_FAVORITES_DOCUMENTS_ACTIVITY = false; //replaced with firebase remote config
    }

    public interface SHOW_REWARDED_ADS {
        boolean FOR_SAVE_AS_PDF = false;  // if false then no ad and pdf created with watermark
        boolean FOR_PDF_EDITOR_ACTIVITY = true;
    }

    /*public static final String DOWNLOAD_URL = "https://www.theparentalcontrol.com/tracking/getip";
    public static final String TRACKING_URL = "https://www.theparentalcontrol.com/tracking/installandr?";
    public static final String GET_COUNTRY_URL = "http://cc.theparentalcontrol.com/productprice.svc/getccode/";*/

    public static final String DOWNLOAD_URL = "http://ins.innovanatechlabs.com/getip";
    public static final String TRACKING_URL = "http://ins.innovanatechlabs.com/install/flash-scan/?";
    public static final String GET_COUNTRY_URL = "http://cc.innovanatechlabs.com/productprice.svc/getccode/";
    public static final int CONNECTION_TIMEOUT = 60;
    public static final boolean LOG_TRACKING_URL = false;

    public static final String TRACKING_RECEIVER_DATA = "https://www.innovanatechlabs.com/post.html?";
    public static final String SOURCE_INSTALL = "flshscndflt";
    public static final String DEFAULT_PIXEL = "FLS5207_FLS5093_FLS2640";

    public interface SubscribeToTopic {
        String DEBUG_HOME = "DEBUG_HOME";
        String HOME = "HOME";
        String FREE = "free";
        String FREE_TEST = "free_test";
        String PREMIUM_YEARLY = "premium_yearly";
        String PREMIUM_YEARLY_TEST = "premium_yearly_test";
        String PREMIUM_MONTHLY = "premium_monthly";
        String PREMIUM_MONTHLY_TEST = "premium_monthly_test";
        String OCR_MONTHLY = "ocr_monthly";
        String OCR_MONTHLY_TEST = "ocr_monthly_test";
        String BOTH_PLANS = "both_plans";
        String BOTH_PLANS_TEST = "both_plans_test";
        String BOTH_PREMIUM = "both_premium";
        String BOTH_PREMIUM_TEST = "both_premium_test";
    }

    public interface FirebaseClickEvents {
        // home screen
        String HOME_SCREEN_DOC_SCANNER = "HOME_SCREEN_DOC_SCANNER";
        String HOME_SCREEN_OCR = "HOME_SCREEN_OCR";
        String HOME_SCREEN_QR_BARCODE = "HOME_SCREEN_QR_BARCODE";

        // qr bar code result screen
        String QR_BARCODE_RESULT_OPEN_URL = "QR_BARCODE_RESULT_OPEN_URL";
        String QR_BARCODE_RESULT_WEB_SEARCH = "QR_BARCODE_RESULT_WEB_SEARCH";
        String QR_BARCODE_RESULT_COPY = "QR_BARCODE_RESULT_COPY";
        String QR_BARCODE_RESULT_SHARE = "QR_BARCODE_RESULT_SHARE";
        String QR_BARCODE_RESULT_RESCAN = "QR_BARCODE_RESULT_RESCAN";

        // ocr result screen
        String OCR_RESULT_SAVE_AS_TEXT = "OCR_RESULT_SAVE_AS_TEXT";
        String OCR_RESULT_SHARE_AS_TEXT_FILE = "OCR_RESULT_SHARE_AS_TEXT_FILE";
        String OCR_RESULT_SHARE_AS_PLAIN_TEXT = "OCR_RESULT_SHARE_AS_PLAIN_TEXT";
        String OCR_RESULT_COPY = "OCR_RESULT_COPY";
        String OCR_RESULT_TRANSLATE = "OCR_RESULT_TRANSLATE";
        String OCR_RESULT_RESCAN = "OCR_RESULT_RESCAN";

        String OCR_ON_DEVICE = "OCR_ON_DEVICE";
        String OCR_CLOUD_VISION = "OCR_CLOUD_VISION";

        // pdf creation
        String PDF_CREATION_WITH_WATERMARK = "PDF_CREATION_WITH_WATERMARK";
        String PDF_CREATION_WITHOUT_WATERMARK = "PDF_CREATION_WITHOUT_WATERMARK";

    }

    public static final String PRODUCT_ID_AD_FREE_TEST = "test.flashscan.adfree";
    public static final String PRODUCT_ID_AD_FREE = "com.utilify.boost.cleaner.android.adfree";
    public static final String PRODUCT_ID_WATERMARK_FREE_TEST = "test.flashscan.watermarkfree";
    public static final String PRODUCT_ID_WATERMARK_FREE = "com.utilify.boost.cleaner.android.watermarkfreepdf";
    public static final String PRODUCT_ID_MONTHLY_TEST = "test.flashscan.premium_monthly";
    public static final String PRODUCT_ID_MONTHLY = "com.utilify.boost.cleaner.android.premium_monthly";
    public static final String PRODUCT_ID_PREMIUM_TEST = "test.flashscan.premium_yearly";
    public static final String PRODUCT_ID_PREMIUM = "com.utilify.boost.cleaner.android.premium_yearly";
//    public static final String PRODUCT_ID_PREMIUM_QUARTELY = "com.utilify.boost.cleaner.android.premium_yearly";
    public static final String PRODUCT_ID_OCR_MONTH_TEST = "test.flashscan.ocr_month";
    public static final String PRODUCT_ID_OCR_MONTH = "com.utilify.boost.cleaner.android.ocr_month";

    public static final int REQUEST_CODE_PREMIUM_YEALY = 125;
    public static final int REQUEST_CODE_OCR_MONTHLY = 126;

    public interface WaterMarkActivityResultCodes {
        int RESULT_EARNED_REWARD = 1;
        int RESULT_AD_CANCELLED = 2;
        int RESULT_PURCHASE_WATERMARK = 3;
        int RESULT_IGNORE = 4;
    }

    public interface AdAfterItems {
        boolean FOR_SINGLE_ITEM = true;
        boolean FOR_MULTIPLE_ITEMS = false;
    }

    public static final int SELECT_ALL = 1;
    public static final int DESELECT_ALL = 2;

    public interface PremiumFeatures {
        boolean IS_ADD_SIGNATURE_ENABLED = true;
    }

    public static final boolean ALWAYS_RELOAD_AD_ON_HOME_SCREEN = false;
    public static final boolean ALWAYS_RELOAD_AD_ON_MAIN_SCREEN = false;
    public static final boolean ALWAYS_RELOAD_AD_ON_OCR_SCREEN = false;


    // flag for showing gallery images list in own app
    public static final boolean IS_SHOWING_IMAGES_IN_OWN_APP = false;
    public static final boolean IS_SHOWING_IMAGES_USING_LIBRARY = false;

    public static final boolean IS_SHOWING_CAMERA_IN_OWN_APP = false;

    public static final int FROM_MEDIA_FILES = 1;
    public static final int FROM_CAMERA_FILES = 2;

    public static final boolean IS_SHOWING_CREATED_PDF_IN_OWN_APP = true;
    public static final boolean IS_CREATE_PDF_DIRECT = true;   // for creating pdf direct without asking dialog

    public static final int COMPRESS_PDF_QUALITY_VALUE = 20;
    public static final int COMPRESS_PDF_QUALITY_LOW = 25;
    public static final int COMPRESS_PDF_QUALITY_MEDIUM = 50;
    public static final int COMPRESS_PDF_QUALITY_REGULAR = 75;
    public static final int COMPRESS_PDF_QUALITY_MAX = 99;

    public static final boolean IS_CREATE_ALREADY_ENCRYPTED_PDF_WITH_PASSWORD = false;

    public static final String REMOTE_CONFIG_PIXEL_NETICA_LICENSE_KEY = "android_license_key_pixel_netica";
    public static final String REMOTE_CONFIG_SHOW_INTERS_SPLASH = "show_inters_splash";
    public static final String REMOTE_CONFIG_SHOW_APP_OPEN_AD = "show_app_open_ad";
    public static final String REMOTE_CONFIG_SHOW_INTERS_EXIT = "show_inters_exit";
    public static final String REMOTE_CONFIG_SHOW_INTERS_CREATION = "show_inters_creation";
    public static final String REMOTE_CONFIG_INTERS_SPLASH_AFTER = "inters_splash_after";
    public static final String REMOTE_CONFIG_INTERS_CREATE_FREQ_IN_SESSION = "inters_create_freq_in_session";
    public static final String REMOTE_CONFIG_SHOW_NATIVE = "show_native";
    public static final String REMOTE_CONFIG_PACKAGE_NAME = "package_name";
    public static final String REMOTE_CONFIG_OCR_RULES = "ocr_rules";
    public static final String REMOTE_CONFIG_DATA = "data";
    public static final String JSON_NODE_DEVICES = "devices";
    public static final String JSON_NODE_FREE = "free";
    public static final String JSON_NODE_PREMIUM_YEARLY = "premium_yearly";
    public static final String JSON_NODE_OCR_MONTHLY = "ocr_monthly";
    public static final String JSON_NODE_TEST_FREE = "test_free";
    public static final String JSON_NODE_TEST_PREMIUM_YEARLY = "test_premium_yearly";
    public static final String JSON_NODE_TEST_OCR_MONTHLY = "test_ocr_monthly";

    public static final int FAB_DEFAULT = 50;
    public static final int FAB_CAMERA = 51;
    public static final int FAB_MEDIA = 52;

    public static final boolean IS_CLOUD_VISION_ALLOW = true;

    public static final boolean IS_REMOTE_CONFIG_FROM_OWN_API = false;
    public static final boolean IS_OWN_API_IMPLEMENT = false;
    public static final boolean IS_ORDER_REAL = true;

    public static final boolean IN_DUMMY_AT_SPLASH_PREMIUM_YEARLY_FOUND = true;
    public static final String LAST_TESTED_ORDER_ID_PREMIUM_YEARLY = "ITL.5678-2923-6539-12348";;
    public static final String LAST_TESTED_ORDER_ID_OCR_MONTHLY = "ITL.5678-2923-6539-12347";;
    public static final boolean IN_DUMMY_AT_SPLASH_OCR_MONTHLY_FOUND = true;

    public static final String WEBSITE_URL = "https://www.theflashscan.com/";

    public static final int RESTORE_PREMIUM_NONE = 184;
    public static final int RESTORE_PREMIUM_YEARLY = 185;
    public static final int RESTORE_PREMIUM_MONTHLY = 186;
    public static final int RESTORE_PREMIUM_BOTH = 187;

    public static int BUY_NOW_MONTHLY = 195;
    public static int BUY_NOW_YEARLY = 196;

    public static String Storage_and_Camera="Storage_and_Camera";
    public static String Camera ="Camera";

    public static String getDateFromTimeStamp(long dateAdded) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateAdded);
        DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm a", Locale.getDefault());
        return df.format(cal.getTime());
    }

    public static String getFileDateFormatName() {
        String fileName;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault());
        fileName = simpleDateFormat.format(calendar.getTime());
        return fileName;
    }

    public static String saveBitmapToFolder(Context context, Bitmap bitmap, String relativePath, String fileName) throws IOException {
        File savedFile;
        OutputStream outputStream = null;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver contentResolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

            Uri imageUri = null;
            try {
                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (imageUri != null) {
                try {
                    outputStream = contentResolver.openOutputStream(imageUri);
                } catch (FileNotFoundException e) {
                    contentResolver.delete(imageUri, null, null);
                }
            }

        } else {*/

        File directory;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            directory =  new File(context.getExternalFilesDir(relativePath).toString());

        }
        else{
            directory = new File(Environment.getExternalStoragePublicDirectory(relativePath).toString());
        }
           // String imagesDir = Environment.getExternalStoragePublicDirectory(relativePath).toString();
           // File directory = new File(imagesDir);
            boolean isDirectoryCreated;
            if (!directory.exists()) {
                isDirectoryCreated = directory.mkdirs();
            } else {
                isDirectoryCreated = true;
            }
            if (isDirectoryCreated) {
                savedFile = new File(directory, fileName);
                if (savedFile.exists()) savedFile.delete();
                outputStream = new FileOutputStream(savedFile);
            }
        //}
        try {
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
            }
        } catch (Exception e) {
            outputStream.close();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return relativePath;
    }

    public static Bitmap bwOpenCvMain2(Bitmap original) { // third build
        Mat adaptiveTh = new Mat();
        Utils.bitmapToMat(original, adaptiveTh);
        Imgproc.cvtColor(adaptiveTh, adaptiveTh, Imgproc.COLOR_BGR2GRAY);

//        Imgproc.medianBlur(adaptiveTh, adaptiveTh, 3);
        Imgproc.threshold(adaptiveTh, adaptiveTh, 0, 255, Imgproc.THRESH_OTSU);

        Imgproc.adaptiveThreshold(adaptiveTh, adaptiveTh, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);

//        Imgproc.Canny(adaptiveTh, adaptiveTh, 80, 100);

//        Imgproc.equalizeHist(adaptiveTh, adaptiveTh);


        int kernelCols = 3;
        int kernelRows = 3;
        Mat destination = new Mat(adaptiveTh.rows(),adaptiveTh.cols(),adaptiveTh.type());

        Mat kernel = new Mat(kernelRows,kernelCols, CvType.CV_32F){

            {


                put(0,0,0);
                put(0,1,1);
                put(0,2,0);

                put(1,0,1);
                put(1,1,-4);
                put(1,2,1);

                put(2,0,0);
                put(2,1,1);
                put(2,2,0);

            }
        };

        Imgproc.filter2D(adaptiveTh, destination, -1, kernel);

        /*Bitmap imgBitmap = Bitmap.createBitmap(destination.cols(), destination.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(destination, imgBitmap);*/


        Bitmap imgBitmap = Bitmap.createBitmap(adaptiveTh.cols(), adaptiveTh.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(adaptiveTh, imgBitmap);

//        imgBitmap = setGamma(imgBitmap,0.5,0.5,0.5);

        return imgBitmap;
    }
}
