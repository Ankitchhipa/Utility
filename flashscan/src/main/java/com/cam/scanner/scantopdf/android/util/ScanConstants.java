package com.cam.scanner.scantopdf.android.util;

import android.graphics.Color;

/**
 * Created by jhansi on 15/03/15.
 */
public class ScanConstants {

    public static final String ROOT_FOLDER_NAME = "FlashScanDocScanner";
    public static final long AD_HOLDING_TIME = 5500;


    static final String ITL_PDF_DIRECTORY = "FlashScanPDF";
    public final static int PICKFILE_REQUEST_CODE = 1;
    public final static int START_CAMERA_REQUEST_CODE = 2;
    public final static String OPEN_INTENT_PREFERENCE = "selectContent";
    public final static int OPEN_CAMERA = 4;
    public final static int OPEN_MEDIA = 5;
    public final static String SCANNED_RESULT = "scannedResult";

    public final static String SELECTED_BITMAP = "selectedBitmap";
    public static final long SCAN_ANIMATION_DURATION = 2500;

    public static final String PDF_FILE_EXTENSION = ".pdf";

    public interface PutExtraConstants {
        String FROM_SCREEN = "from_screen";
        String FOLDER_NAME = "folder_name";
        String DATE_TAKEN = "date_taken";
        String FILE_URI = "file_uri";
        String FILE_PATH = "file_path";
        String IS_COMING_FROM_CUSTOM_GALLERY = "is_coming_from_custom_gallery";
        String IS_COMING_FROM_CROPPED_IMAGE_ACTIVITY = "is_coming_from_cropped_image_activity";
        String URI = "uri";
        String IS_COMING_FROM_SELECTED_IMAGES_ACTIVITY = "is_coming_from_selected_images_activity";
        String SELECTED_IMAGES_LIST = "selected_images_list";
        String FROM_SOURCE = "from_source";
        String IS_FOLDER_EXISTS_ON_DRIVE = "is_folder_exists_on_drive";
        String GOOGLE_DRIVE_FOLDER_ID = "google_drive_folder_id";
    }

    public interface ScreenConstants {
        int FROM_SCAN_RESULT = 1;
        int FROM_HOME_SCREEN = 2;
        int FROM_MAIN_SCREEN = 3;
        int FROM_FAVORITES_SCREEN = 4;
        int FROM_MODIFY_SCAN = 5;
        int FROM_SELECTED_IMAGES_LIST_SCREEN = 6;
        int FROM_PDF_TO_IMAGES_IMPORT = 7;
        int FROM_ADD_SIGNATURE = 7;
        int FROM_EDIT_SCREEN = 8;
    }

    public interface SaveImageAs {
        int AS_JPG = 1;
        int AS_PDF = 2;
    }

    public interface PdfConstants {
        String DEFAULT_PDF_PAGE_SIZE = "A4";
        int DEFAULT_PDF_PAGE_COLOR = Color.WHITE;
        int DEFAULT_PDF_QUALITY = 30;
        int DEFAULT_BORDER_WIDTH = 0;
    }

    public static boolean FEATURE_BRIGHTNESS_CONTRAST = false;

    public interface FirebaseClickEvents {
        String FILTER_ORIGINAL = "FILTER_ORIGINAL";
        String FILTER_MAGIC_COLOR = "FILTER_MAGIC_COLOR";
        String FILTER_GRAY_COLOR = "FILTER_GRAY_COLOR";
        String B_AND_W_1 = "B_AND_W_1";
        String B_AND_W_2 = "B_AND_W_2";
    }

    public interface SHOW_INTERSTITIAL_ADS {
        boolean FOR_SCAN_RESULT_ACTIVITY = false;
    }

    public static final String GET_BITMAP_FROM = "get_bitmap_from";
    public static final int BITMAP_FROM_FILE_PATH = 1;
    public static final int BITMAP_FROM_URI = 2;

    public interface FileOperations {

        int ACTION_COPY = 4;
    }

    public static final int FROM_MEDIA_FILES = 1 ;
    public static final int FROM_CAMERA_FILES = 2 ;
}
