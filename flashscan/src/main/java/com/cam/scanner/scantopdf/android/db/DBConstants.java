package com.cam.scanner.scantopdf.android.db;

public class DBConstants {
    static final String DATABASE_NAME = "flash_scan.db";

    //    static final int DB_VERSION = 3; //version was 2 up to build 4.5
//    static final int DB_VERSION = 4; //version was 3 up to build 4.9
    static final int DB_VERSION = 6; //version was 4 up to build 4.9

    static final String COL_ID = "_id";

    public static final int APPLY_ALL_FILTER_VALUE = 1;

    //Doc Editing
    //Table Name
    static final String TABLE_DOC_EDITING = "doc_editing";
    static final String TABLE_APPLY_FILTER_ALL = "apply_filter_all";
    public final static String TABLE_ADS_RULES = "ads_rules";
    public final static String TABLE_ADS_STATUS = "ads_status";
    public final static String TABLE_DEVICES_ALLOWED = "devices_allowed";
    public final static String TABLE_OCR_RULES = "ocr_rules";
    public final static String TABLE_OCR_STATUS = "ocr_status";

    //After build version 4.9 (in db v 4)
    public final static String TABLE_OCR_CREDITS_API = "ocr_credits_api";

    //Fields
    static final String COL_FOLDER_NAME = "folder_name";
    static final String COL_FILE_NAME = "file_name";
    static final String COL_FILTER_TYPE = "filter_type";
    static final String COL_POINT_X = "point_x";
    static final String COL_POINT_Y = "point_y";
    static final String COL_ROTATION = "rotation";
    static final String COL_RECT = "rect";

    static final String COL_IS_APPLY_FILTER_ALL = "is_apply_filter_all";

    public static final String COL_SPLASH_ATTEMPTED = "splash_attempt";
    public static final String COL_AD_INTERS_CREATE_ATTEMPTED_IN_SESSION = "ad_inters_create_attempted_in_session";

    public static final String COL_SHOW_INTERS_SPLASH = "show_inters_splash";
    public static final String COL_SHOW_INTERS_EXIT = "show_inters_exit";
    public static final String COL_SHOW_INTERS_CREATION = "show_inters_creation";
    public static final String COL_SHOW_NATIVE = "show_native";
    public static final String COL_INTERS_SPLASH_AFTER = "inters_splash_after";
    public static final String COL_INTERS_CREATE_FREQ_IN_SESSION = "inters_create_freq_in_session";

    public static final String COL_ANDROID_ID = "android_d";

    public static final String COL_FREE = "free";
    public static final String COL_PREMIUM_YARLY = "premium_yarly";
    public static final String COL_OCR_MONTHLY = "ocr_monthly";

    public static final String COL_TEST_FREE = "test_free";
    public static final String COL_TEST_PREMIUM_YARLY = "test_premium_yarly";
    public static final String COL_TEST_OCR_MONTHLY = "test_ocr_monthly";

    public static final String COL_OCR_FREE_ATTEMPTED = "ocr_free_attempted";
    public static final String COL_OCR_MONTHLY_ATTEMPTED = "ocr_monthly_attempted";
    public static final String COL_OCR_PREMIUM_YEARLY_ATTEMPTED = "ocr_premium_yearly_attempted";

    public static final String COL_CREDITS_FROM_API = "credits_from_api";
    public static final String COL_CREDITS_OFFLINE_ATTEMPTED = "credits_offline_attempted";

    //After build version 5.0 (in db v 5)
    public static final String COL_DEVICE_ID = "device_id";
    public static final String COL_SUBSCRIPTION_ID = "subscription_id";
    public static final String COL_EMAIL = "email";
    public static final String COL_IS_OCR = "is_ocr";
    public static final String COL_IS_PREMIUM = "is_premium";
    public static final String COL_PLAN_ID = "plan_id";

    public static final String COL_CREATED_ON = "created_on";
    public static final String COL_UPDATED_ON = "updated_on";

    static final String QUERY_CREATE_TABLE_DOC_FILTERS = "CREATE TABLE "
            + TABLE_DOC_EDITING + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBConstants.COL_FOLDER_NAME + " TEXT, " +
            DBConstants.COL_FILE_NAME + " TEXT, " +
            DBConstants.COL_ROTATION + " INTEGER, " +
            DBConstants.COL_POINT_X + " TEXT, " +
            DBConstants.COL_POINT_Y + " TEXT, " +
            DBConstants.COL_FILTER_TYPE + " INTEGER, " +
            DBConstants.COL_RECT + " TEXT" +
            ")";

    static final String QUERY_CREATE_TABLE_IS_APPLY_FILTER_ALL = "CREATE TABLE "
            + TABLE_APPLY_FILTER_ALL + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBConstants.COL_FOLDER_NAME + " TEXT, " +
            DBConstants.COL_IS_APPLY_FILTER_ALL + " INTEGER DEFAULT 0" +
            ")";

    public static final String QUERY_CREATE_TABLE_ADS_RULES = "CREATE TABLE "
            + TABLE_ADS_RULES + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DBConstants.COL_SHOW_INTERS_SPLASH + " INTEGER DEFAULT 0, " +
            DBConstants.COL_SHOW_INTERS_EXIT + " INTEGER DEFAULT 0, " +
            DBConstants.COL_SHOW_INTERS_CREATION + " INTEGER DEFAULT 0, " +
            DBConstants.COL_SHOW_NATIVE + " INTEGER DEFAULT 0, " +
            DBConstants.COL_INTERS_SPLASH_AFTER + " INTEGER DEFAULT 1, " +
            DBConstants.COL_INTERS_CREATE_FREQ_IN_SESSION + " INTEGER DEFAULT 3, " +
            DBConstants.COL_CREATED_ON + " TEXT, " +
            DBConstants.COL_UPDATED_ON + " TEXT" +
            ")";

    public static final String QUERY_CREATE_TABLE_ADS_STATUS = "CREATE TABLE "
            + TABLE_ADS_STATUS + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DBConstants.COL_SPLASH_ATTEMPTED + " INTEGER DEFAULT 0, " +
            DBConstants.COL_AD_INTERS_CREATE_ATTEMPTED_IN_SESSION + " INTEGER DEFAULT 0, " +
            DBConstants.COL_CREATED_ON + " TEXT, " +
            DBConstants.COL_UPDATED_ON + " TEXT" +
            ")";

    public static final String QUERY_CREATE_TABLE_DEVICES_ALLOWED = "CREATE TABLE "
            + TABLE_DEVICES_ALLOWED + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DBConstants.COL_ANDROID_ID + " TEXT, " +
            DBConstants.COL_CREATED_ON + " TEXT, " +
            DBConstants.COL_UPDATED_ON + " TEXT" +
            ")";

    public static final String QUERY_CREATE_TABLE_OCR_RULES = "CREATE TABLE "
            + TABLE_OCR_RULES + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DBConstants.COL_FREE + " INTEGER, " +
            DBConstants.COL_PREMIUM_YARLY + " INTEGER, " +
            DBConstants.COL_OCR_MONTHLY + " INTEGER, " +
            DBConstants.COL_TEST_FREE + " INTEGER, " +
            DBConstants.COL_TEST_PREMIUM_YARLY + " INTEGER, " +
            DBConstants.COL_TEST_OCR_MONTHLY + " INTEGER, " +
            DBConstants.COL_CREATED_ON + " TEXT, " +
            DBConstants.COL_UPDATED_ON + " TEXT" +
            ")";

    public static final String QUERY_CREATE_TABLE_OCR_STATUS = "CREATE TABLE "
            + TABLE_OCR_STATUS + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DBConstants.COL_OCR_FREE_ATTEMPTED + " INTEGER, " +
            DBConstants.COL_OCR_MONTHLY_ATTEMPTED + " INTEGER, " +
            DBConstants.COL_OCR_PREMIUM_YEARLY_ATTEMPTED + " INTEGER, " +
            DBConstants.COL_CREATED_ON + " TEXT, " +
            DBConstants.COL_UPDATED_ON + " TEXT" +
            ")";

    //After build version 4.9 (in db v 4)
    public static final String QUERY_CREATE_TABLE_OCR_CREDITS_API = "CREATE TABLE "
            + TABLE_OCR_CREDITS_API + "(" +
            DBConstants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DBConstants.COL_DEVICE_ID + " TEXT, " +
            DBConstants.COL_SUBSCRIPTION_ID + " TEXT, " +
            DBConstants.COL_EMAIL + " TEXT, " +
            DBConstants.COL_CREDITS_FROM_API + " INTEGER DEFAULT -1, " +
            DBConstants.COL_IS_OCR + " INTEGER DEFAULT -1, " +
            DBConstants.COL_PLAN_ID + " INTEGER DEFAULT -1, " +
            DBConstants.COL_IS_PREMIUM + " INTEGER DEFAULT -1, " +
            DBConstants.COL_CREDITS_OFFLINE_ATTEMPTED + " INTEGER DEFAULT -1, " +
            DBConstants.COL_CREATED_ON + " TEXT, " +
            DBConstants.COL_UPDATED_ON + " TEXT" +
            ")";

    // drop table queries
    public static final String QUERY_DROP_TABLE_ADS_STATUS = "drop table if exists " +
            TABLE_ADS_STATUS;

    //in db v 5
    public static final String QUERY_ALTER_TABLE_OCR_CREDITS_API = "ALTER TABLE "
            + DBConstants.TABLE_OCR_CREDITS_API
            + " ADD COLUMN "
            + DBConstants.COL_DEVICE_ID + " TEXT";

    public static final String QUERY_ALTER_TABLE_OCR_CREDITS_API_2 = "ALTER TABLE "
            + DBConstants.TABLE_OCR_CREDITS_API
            + " ADD COLUMN "
            + DBConstants.COL_SUBSCRIPTION_ID + " TEXT";

    public static final String QUERY_ALTER_TABLE_OCR_CREDITS_API_3 = "ALTER TABLE "
            + DBConstants.TABLE_OCR_CREDITS_API
            + " ADD COLUMN "
            + DBConstants.COL_EMAIL + " TEXT";

    public static final String QUERY_ALTER_TABLE_OCR_CREDITS_API_4 = "ALTER TABLE "
            + DBConstants.TABLE_OCR_CREDITS_API
            + " ADD COLUMN "
            + DBConstants.COL_IS_OCR + " INTEGER DEFAULT -1";

    public static final String QUERY_ALTER_TABLE_OCR_CREDITS_API_5 = "ALTER TABLE "
            + DBConstants.TABLE_OCR_CREDITS_API
            + " ADD COLUMN "
            + DBConstants.COL_IS_PREMIUM + " INTEGER DEFAULT -1";

    public static final String QUERY_ALTER_TABLE_OCR_CREDITS_API_6 = "ALTER TABLE "
            + DBConstants.TABLE_OCR_CREDITS_API
            + " ADD COLUMN "
            + DBConstants.COL_PLAN_ID + " INTEGER DEFAULT -1";

    //Alter table example - adding column
    /*public static final String QUERY_ALTER_TABLE_KID_FEATURES = "ALTER TABLE "
            + DBConstants.TABLE_KID_FEATURES
            + " ADD COLUMN "
            + DBConstants.COL_SETTING_TIMESTAMP + " TEXT DEFAULT 0";*/
    ////

}
