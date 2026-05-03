package com.cam.scanner.scantopdf.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.models.GoogleDriveChildFileModel;
import com.cam.scanner.scantopdf.android.models.GoogleDriveFolderModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openpdf.text.PageSize;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PrefManager {

    private static final String SHARED_PREF = "shared_preference";
    private static final String IS_FIRST_TIME_LAUNCH = "is_first_time_launch";
    private static final String SORTING_ORDER = "sorting_order";
    private static final String FILE_SORTING_ORDER = "file_sorting_order";
    private static final String FOLDER_SORTING_ORDER = "folder_sorting_order";
    private static final String APP_SORTING_ORDER = "app_sorting_order";
    private static final String IS_APP_AD_FREE = "is_app_ad_free";
    private static final String IS_PREMIUM_YEARLY = "is_premium_yearly";
    private static final String IS_PREMIUM_QUARTERLY = "is_premium_quarterly";
    private static final String IS_OCR_MONTHLY = "is_ocr_monthly";
    private static final String IS_APP_WATERMARK_FREE = "is_app_watermark_free";
    public static final String PREF_TRACKING_DONE = "tracking_done";
    public static final String PREF_TRACKING_URL = "tracking_url";
    public static final String PREF_COUNTRY = "pref_country";
    public static final String PREF_TRACKING_CAMP = "tracking_camp";
    public static final String PREF_TRACKING_SOURCE = "tracking_source";
    public static final String PREF_TRACKING_PUB = "tracking_pub";
    public static final String PREF_TRACKING_MEDIUM = "tracking_medium";
    public static final String PREF_TRACKING_CONTENT = "tracking_content";
    public static final String PREF_TRACKING_TERM = "tracking_term";
    public static final String PREF_TRACKING_TIME = "tracking_time";
    public static final String PREF_DOWNLOAD_IP = "download_ip";
    public static final String PREF_X_AT = "x_at";
    private static final String MODIFIED_FILE_NAME = "modified_file_name";
    private static final String MASTER_PWD = "master_password";
    private static final String APP_SELECTED_PDF_PAGE_SIZE = "app_selected_pdf_page_size";
    private static final String ANTI_COUNTERFEIT_TXT = "anti_counterfeit_txt";
    private static final String ANTI_COUNTERFEIT_TXT_SIZE = "anti_counterfeit_txt_size";
    private static final String ANTI_COUNTERFEIT_TXT_COLOR = "anti_counterfeit_txt_color";
    private static final String IS_PDF_EDITOR_TUT_WATCHED = "is_pdf_editor_tut_watched";
    private static final String IS_STRONG_SHADOWS_ENABLED = "is_strong_shadows_enabled";
    private static final String IS_AUTO_CROP_ENABLED = "is_auto_crop_enabled";
    private static final String IS_IMAGE_CROP_TUT_WATCHED = "is_image_crop_tut_watched";
    private static final String IS_SCAN_RESULT_TUT_WATCHED = "is_scan_result_tut_watched";
    private static final String IS_ADD_SIGNATURE_TUT_WATCHED = "is_add_signature_tut_watched";
    private static final String IS_AFTER_ADD_SIGNATURE_TUT_WATCHED = "is_after_add_signature_tut_watched";
    private static final String IS_OCR_LANG_WATCHED = "is_ocr_lang_watched";
    private static final String PREMIUM_COUNT = "premium_count";
    private static final String OCR_MONTHLY_COUNT = "ocr_monthly_count";
    private static final String ORDER_ID_PREMIUM_YEARLY = "order_id_premium_yearly";
    private static final String ORDER_ID_PREMIUM_QUARTERLY = "order_id_premium_quarterly";
    private static final String ORDER_ID_OCR_MONTHLY = "order_id_ocr_monthly";
    private static final String DEVICE_ID_OF_INSTALL_TIME = "device_id_of_install_time";
    private static final String SECURE_ANDROID_ID = "secure_android_id";
    private static final String PLAN_ID_FOR_API = "plan_id_for_api";
    private static final String IS_UNSUBSCRIBE_FROM_FREE = "is_unsubscribe_from_free";
    private static final String IS_SPLASH_DONE = "is_splash_done";
    public static final String FIREBASE_DEVICE_TOKEN = "firebase_device_token";
    public static final String OFFER_URL_SERVER = "offer_url_server";
    public static final String SHOW_APP_OPEN_AD = "show_app_open_ad";
    public static final String SHOW_SPLASH_INETSTITIAL_AD = "show_splash_interstitial_ad";
    public static final String QUARTERLY_PLAN_PRICE = "quaterly_plan_price";
    public static final String YEARLY_PLAN_PRICE = "yearly_plan_price";
    public static final String PURCHASED_PLAN_NAME = "purchased_plan_name";
    private Context context;
    public static SharedPreferences sharedPreferences;
    private static final String PIXEL_NETICA_LICENSE_KEY = "pixel_netica_license_key";
    private static final String KEY_GOOGLE_DRIVE_DATA_LIST = "google_drive_data_list";

    public PrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PrefManager.SHARED_PREF, Context.MODE_PRIVATE);
    }

    public boolean isFirstTimeLaunched() {
        boolean isFirstTimeLaunch;
        isFirstTimeLaunch = sharedPreferences.getBoolean(PrefManager.IS_FIRST_TIME_LAUNCH, false);
        return isFirstTimeLaunch;
    }

    public void setFirstTimeLaunch(boolean isFirstTimeLaunch) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PrefManager.IS_FIRST_TIME_LAUNCH, isFirstTimeLaunch);
        editor.apply();
    }

    public void saveFileSortingOrder(int sortBy) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PrefManager.FILE_SORTING_ORDER, sortBy);
        editor.apply();
    }

    public int getFileSortingOrder() {
        return sharedPreferences.getInt(PrefManager.FILE_SORTING_ORDER, getAppSortingOrder());
    }

    public int getFoldersSortingOrder() {
        return sharedPreferences.getInt(PrefManager.FOLDER_SORTING_ORDER, getAppSortingOrder());
    }

    public void saveFoldersSortingOrder(int sortBy) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PrefManager.FOLDER_SORTING_ORDER, sortBy);
        editor.apply();
    }

    public void saveAppSortingOrder(int sortBy) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PrefManager.APP_SORTING_ORDER, sortBy);
        editor.apply();
    }

    public void saveSelectedPdfPageSizeForWholeApp(String pdfPageSize) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(APP_SELECTED_PDF_PAGE_SIZE, pdfPageSize);
        editor.apply();
    }

    public String getSelectedPdfSizeForWholeApp() {
        return sharedPreferences.getString(APP_SELECTED_PDF_PAGE_SIZE, String.valueOf(PageSize.A4)); // by default A4
    }

    public int getAppSortingOrder() {
        return sharedPreferences.getInt(PrefManager.APP_SORTING_ORDER, Constants.SORT_BY.defaultOrder);
    }

    public boolean isAppAdFree() {  // true if payment done - don't show any ad in app
        boolean isAdFree = sharedPreferences.getBoolean(PrefManager.IS_APP_AD_FREE, false);
        boolean isPremium = sharedPreferences.getBoolean(PrefManager.IS_PREMIUM_YEARLY, false);
        // boolean isPremiumQuarterly = sharedPreferences.getBoolean(PrefManager.IS_PREMIUM_QUARTERLY, false);
        if (isPremium /*|| isPremiumQuarterly*/) {
            isAdFree = true;
        }
        return isAdFree;
    }


    public void setAppAdFree(boolean isAppAdFree) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_APP_AD_FREE, isAppAdFree).apply();
        editor.apply();
    }

    public boolean isPremiumYearly() {  // true if payment done - don't show any ad in app
        return sharedPreferences.getBoolean(PrefManager.IS_PREMIUM_YEARLY, false);
    }


    public void setPremiumYearly(boolean isPremium) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_PREMIUM_YEARLY, isPremium).apply();
        editor.apply();
    }

    /*public boolean isPremiumQuarterly() {
        return sharedPreferences.getBoolean(PrefManager.IS_PREMIUM_QUARTERLY, false);
    }*/

   /* public void setPremiumQuarterly(boolean isPremiumQuarterly) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_PREMIUM_QUARTERLY, isPremiumQuarterly).apply();
        editor.apply();
    }*/

    public boolean isOcrMonthly() {  // true if payment done - don't show any ad in app
        return sharedPreferences.getBoolean(PrefManager.IS_OCR_MONTHLY, false);
    }


    public void setOcrMonthly(boolean isOcrMonthly) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_OCR_MONTHLY, isOcrMonthly).apply();
        editor.apply();
    }

    public boolean isAppWatermarkFree() {  // true if payment done - don't show Reward ad in app and make pdf without watermark
        return sharedPreferences.getBoolean(PrefManager.IS_APP_WATERMARK_FREE, false);
    }


    public void setAppWatermarkFree(boolean isWatermarkFree) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_APP_WATERMARK_FREE, isWatermarkFree).apply();
        editor.apply();
    }

    public void setTrackingDone(boolean isTrackingDone) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_TRACKING_DONE, isTrackingDone).apply();
        editor.apply();
    }

    public boolean isTrackingDone() {
        boolean isTrackingDone = sharedPreferences.getBoolean(PREF_TRACKING_DONE, false);
        return isTrackingDone;
    }

    public void setTrackingUrl(String trackingUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_TRACKING_URL, trackingUrl).apply();
        editor.apply();
    }


    public String getTrackingUrl() {
        String trackingUrl = sharedPreferences.getString(PREF_TRACKING_URL, "");
        return trackingUrl;
    }

    public void setCountry(String strCountry) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_COUNTRY, strCountry).apply();
        editor.apply();
    }

    public String getCountry() {
        return sharedPreferences.getString(PREF_COUNTRY, "");
    }

    public void setTrackingCamp(String trackingCamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_TRACKING_CAMP, trackingCamp).apply();
        editor.apply();
    }

    public String getTrackingCamp() {
        String trackingCamp = sharedPreferences.getString(PREF_TRACKING_CAMP, "");
        return trackingCamp;
    }

    public void setTrackingSource(String trackingSource) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_TRACKING_SOURCE, trackingSource).apply();
        editor.apply();
    }

    public String getTrackingSource() {
        String trackingSource = sharedPreferences.getString(PREF_TRACKING_SOURCE, "");
        return trackingSource;
    }

    public void setTrackingPub(String trackingSource) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_TRACKING_PUB, trackingSource).apply();
        editor.apply();
    }

    public String getTrackingPub() {
        String trackingSource = sharedPreferences.getString(PREF_TRACKING_PUB, "");
        return trackingSource;
    }

    public void setTrackingMedium(String trackingMedium) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_TRACKING_MEDIUM, trackingMedium).apply();
        editor.apply();
    }

    public String getTrackingMedium() {
        String trackingSource = sharedPreferences.getString(PREF_TRACKING_MEDIUM, "");
        return trackingSource;
    }

    public void setTrackingContent(String trackingContent) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_TRACKING_CONTENT, trackingContent).apply();
        editor.apply();
    }

    public String getTrackingContent() {
        String trackingSource = sharedPreferences.getString(PREF_TRACKING_CONTENT, "");
        return trackingSource;
    }

    public void setTrackingTerm(String trackingTerm) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_TRACKING_TERM, trackingTerm).apply();
        editor.apply();
    }

    public String getTrackingTerm() {
        String trackingSource = sharedPreferences.getString(PREF_TRACKING_TERM, "");
        return trackingSource;
    }

    public void setInstallTime(long trackingTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_TRACKING_TIME, trackingTime).apply();
        editor.apply();
    }

    public long getTrackingTime() {
        long trackingTime = sharedPreferences.getLong(PREF_TRACKING_TIME, 0);
        return trackingTime;
    }

    public void setDownloadIp(String strIp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_DOWNLOAD_IP, strIp).apply();
        editor.apply();
    }

    public String getDownloadIp() {
        return sharedPreferences.getString(PREF_DOWNLOAD_IP, "");
    }

    public void setX_At(String x_at) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_X_AT, x_at).apply();
        editor.apply();
    }

    public String getX_At() {
        return sharedPreferences.getString(PREF_X_AT, "");
    }

    public void saveModifiedFileName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefManager.MODIFIED_FILE_NAME, name);
        editor.apply();
    }

    public String getMasterPassword() {
        return sharedPreferences.getString(MASTER_PWD, context.getString(R.string.module_name));
    }

    public void setMasterPassword(String masterPwd) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MASTER_PWD, masterPwd);
        editor.apply();
    }

    public void saveSignatureBitmap(List<String> bitmapList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bitmapList);
        editor.putString("sign_bitmap", json);
        editor.apply();
    }

    public List<String> retrieveSignatureBitmap() {
        List<String> signatureList = null;
        Gson gson = new Gson();
        String json = sharedPreferences.getString("sign_bitmap", null);
        if (json != null) {
            if (json.isEmpty()) {
                signatureList = new ArrayList<>();
            } else {
                Type type = new TypeToken<List<String>>() {
                }.getType();
                signatureList = gson.fromJson(json, type);
            }
        }
        return signatureList;
    }

    public void saveAntiCounterFeitText(String waterMarkTxt) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ANTI_COUNTERFEIT_TXT, waterMarkTxt);
        editor.apply();
    }

    public String getAntiCounterfeitTxt() {
        return sharedPreferences.getString(ANTI_COUNTERFEIT_TXT, "");
    }

    public void saveAntiCounterFeitTextSize(int watermarkFontSize) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ANTI_COUNTERFEIT_TXT_SIZE, watermarkFontSize);
        editor.apply();
    }

    public int getAntiCounterFeitTextSize() {
        return sharedPreferences.getInt(ANTI_COUNTERFEIT_TXT_SIZE, 100); // default
    }

    public void saveAntiConterFeitTextColor(String watermarkTextColor) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ANTI_COUNTERFEIT_TXT_COLOR, watermarkTextColor);
        editor.apply();
    }

    public String getAntiCounterfeitTxtColor() {
        return sharedPreferences.getString(ANTI_COUNTERFEIT_TXT_COLOR, "#FF0000");
    }

    public boolean isPdfEditorTutorialWatched() {
        return sharedPreferences.getBoolean(IS_PDF_EDITOR_TUT_WATCHED, false);
    }

    public void setPdfEditorTutorialWatched(boolean isTutorialWatched) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_PDF_EDITOR_TUT_WATCHED, isTutorialWatched);
        editor.apply();
    }

    public void setStrongShadowEnabled(boolean isStrongShadowEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_STRONG_SHADOWS_ENABLED, isStrongShadowEnabled);
        editor.apply();
    }

    public boolean isStrongShadowEnabled() {
        return sharedPreferences.getBoolean(IS_STRONG_SHADOWS_ENABLED, false);
    }

    public void setAutoCropEnabled(boolean isStrongShadowEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_AUTO_CROP_ENABLED, isStrongShadowEnabled);
        editor.apply();
    }

    public boolean isAutoCropEnabled() {
        return sharedPreferences.getBoolean(IS_AUTO_CROP_ENABLED, false);
    }

    public void setImageCropTutorialWatched(boolean isTutorialWatched) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_IMAGE_CROP_TUT_WATCHED, isTutorialWatched);
        editor.apply();
    }

    public boolean isImageCropTutorialWatched() {
        return sharedPreferences.getBoolean(IS_IMAGE_CROP_TUT_WATCHED, false);
    }

    public void setScanResultTutWatched(boolean isTutorialWatched) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_SCAN_RESULT_TUT_WATCHED, isTutorialWatched);
        editor.apply();
    }

    public boolean isScanResultTutWatched() {
        return sharedPreferences.getBoolean(IS_SCAN_RESULT_TUT_WATCHED, false);
    }

    public void setAddSignatureTutWatched(boolean isTutorialWatched) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_ADD_SIGNATURE_TUT_WATCHED, isTutorialWatched);
        editor.apply();
    }

    public boolean isAddSignatureTutWatched() {
        return sharedPreferences.getBoolean(IS_ADD_SIGNATURE_TUT_WATCHED, false);
    }

    public void setAfterAddSignatureTutWatched(boolean isTutorialWatched) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_AFTER_ADD_SIGNATURE_TUT_WATCHED, isTutorialWatched);
        editor.apply();
    }

    public boolean isAfterAddSignatureTutWatched() {
        return sharedPreferences.getBoolean(IS_AFTER_ADD_SIGNATURE_TUT_WATCHED, false);
    }

    public boolean isOCRLangWatched() {
        return sharedPreferences.getBoolean(IS_OCR_LANG_WATCHED, false);
    }

    public void setOCRLangWatched(boolean isWatched) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_OCR_LANG_WATCHED, isWatched);
        editor.apply();
    }

    public int getPremiumCount() {
        return sharedPreferences.getInt(PREMIUM_COUNT, 0);
    }

    public void setPremiumCount(int count) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREMIUM_COUNT, count);
        editor.apply();
    }

    public int getOcrMonthlyCount() {
        return sharedPreferences.getInt(OCR_MONTHLY_COUNT, 0);
    }

    public void setOcrMonthlyCount(int count) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(OCR_MONTHLY_COUNT, count);
        editor.apply();
    }

    public void setOrderIdPremiumYearly(String orderId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ORDER_ID_PREMIUM_YEARLY, orderId);
        editor.apply();
    }

    public void setOrderIdPremiumQuarterly(String orderId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ORDER_ID_PREMIUM_QUARTERLY, orderId);
        editor.apply();
    }

    public String getOrderIdPremiumYearly() {
        return sharedPreferences.getString(ORDER_ID_PREMIUM_YEARLY, null);
    }

    public String getOrderIdPremiumQuarterly() {
        return sharedPreferences.getString(ORDER_ID_PREMIUM_QUARTERLY, null);
    }

    public void setOrderIdOcrMonthly(String orderId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ORDER_ID_OCR_MONTHLY, orderId);
        editor.apply();
    }

    public String getOrderIdOcrMonthly() {
        return sharedPreferences.getString(ORDER_ID_OCR_MONTHLY, null);
    }

    public void setDeviceIdOfInstallTime(String selfAndroidId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_ID_OF_INSTALL_TIME, selfAndroidId);
        editor.apply();
    }

    public String getDeviceIdOfInstallTime() {
        return sharedPreferences.getString(DEVICE_ID_OF_INSTALL_TIME, null);
    }

    public void setSecureAndroidId(String selfAndroidId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SECURE_ANDROID_ID, selfAndroidId);
        editor.apply();
    }

    public void setPlanIdForApi(int planId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PLAN_ID_FOR_API, planId);
        editor.apply();
    }

    public int getPlanIdForApi() {
        return sharedPreferences.getInt(PLAN_ID_FOR_API, Constants.PLAN_FREE);
    }

    public boolean isUnsubscribedFromFree() {
        boolean isUnsubscribed;
        isUnsubscribed = sharedPreferences.getBoolean(PrefManager.IS_UNSUBSCRIBE_FROM_FREE, false);
        return isUnsubscribed;
    }

    public void setUnsubscribeFromFree(boolean isUnsubscribed) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PrefManager.IS_UNSUBSCRIBE_FROM_FREE, isUnsubscribed);
        editor.apply();
    }

    public void setFirebaseDeviceToken(String firebaseDeviceToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FIREBASE_DEVICE_TOKEN, firebaseDeviceToken);
        editor.apply();
    }

    public String getFirebaseDeviceToken() {
        return sharedPreferences.getString(FIREBASE_DEVICE_TOKEN, null);
    }

    public void setOfferUrlServer(String offerUrlServer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(OFFER_URL_SERVER, offerUrlServer);
        editor.apply();
    }

    public String getOfferUrlServer() {
        return sharedPreferences.getString(OFFER_URL_SERVER, null);
    }

    public boolean isSplashDone() {
        boolean isSplashDone;
        isSplashDone = sharedPreferences.getBoolean(PrefManager.IS_SPLASH_DONE, false);
        return isSplashDone;
    }

    public void setSplashDone(boolean isSplashDone) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PrefManager.IS_SPLASH_DONE, isSplashDone);
        editor.apply();
    }

    public boolean showAppOpenAd() {
        boolean showAppOpenAd;
        showAppOpenAd = sharedPreferences.getBoolean(PrefManager.SHOW_APP_OPEN_AD, true);
        return showAppOpenAd;
    }

    public void setshowAppOpenAd(boolean showAppOpenAd) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PrefManager.SHOW_APP_OPEN_AD, showAppOpenAd);
        editor.apply();
    }

    public boolean showSplashInterstitialAd() {
        boolean showSplashInterstitialAd;
        showSplashInterstitialAd = sharedPreferences.getBoolean(PrefManager.SHOW_SPLASH_INETSTITIAL_AD, false);
        return showSplashInterstitialAd;
    }

    public void setSplashInterstitialAd(boolean showSplashInterstitialAd) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PrefManager.SHOW_SPLASH_INETSTITIAL_AD, showSplashInterstitialAd);
        editor.apply();
    }

    public void setQuarterlyPlanPrice(String quarterlyPlanPrice) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(QUARTERLY_PLAN_PRICE, quarterlyPlanPrice).apply();
        editor.apply();
    }

    public String getQuarterlyPlanPrice() {
        String quarterlyPlanPrice = sharedPreferences.getString(QUARTERLY_PLAN_PRICE, "");
        return quarterlyPlanPrice;
    }

    public void setYearlyPlanPrice(String yearlyPlanPrice) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(YEARLY_PLAN_PRICE, yearlyPlanPrice).apply();
        editor.apply();
    }

    public String getYearlyPlanPrice() {
        String yearlyPlanPrice = sharedPreferences.getString(YEARLY_PLAN_PRICE, "");
        return yearlyPlanPrice;
    }

    public void setPurchasedPlanName(int purchasePlanName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PURCHASED_PLAN_NAME, purchasePlanName).apply();
        editor.apply();
    }

    public int getPurchasedPlanName() {
        int purchasePlanName = sharedPreferences.getInt(PURCHASED_PLAN_NAME, 0);
        return purchasePlanName;
    }


    public void setPixelNeticaLicenseKey(String licenseKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PIXEL_NETICA_LICENSE_KEY, licenseKey);
        editor.apply();
    }

    public String getPixelNeticaLicenseKey() {
        String licenseKey;
        licenseKey = sharedPreferences.getString(PrefManager.PIXEL_NETICA_LICENSE_KEY, "");
        return licenseKey;
    }

    public void saveGoogleDriveDataList(List<GoogleDriveFolderModel> googleDriveFolderModelList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<List<GoogleDriveFolderModel>>() {
        }.getType();
        String json = gson.toJson(googleDriveFolderModelList, type);
        editor.putString(KEY_GOOGLE_DRIVE_DATA_LIST, json);
        editor.apply();
        Log.d("Pref", "saveGoogleDriveDataList: " + json);
    }

    public List<GoogleDriveFolderModel> getGoogleDriveDataList() {
        List<GoogleDriveFolderModel> googleDriveFolderModelList = null;
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_GOOGLE_DRIVE_DATA_LIST, "");
        if (json.isEmpty()) {
            googleDriveFolderModelList = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<GoogleDriveFolderModel>>() {
            }.getType();
            googleDriveFolderModelList = gson.fromJson(json, type);
        }
        return googleDriveFolderModelList;
    }


    public void addFolderToGoogleDriveDataList(String folderId, String folderName, List<GoogleDriveChildFileModel> googleDriveChildFileModelList) {
        List<GoogleDriveFolderModel> googleDriveFolderModelList = getGoogleDriveDataList();
        googleDriveFolderModelList.add(new GoogleDriveFolderModel(folderId, folderName, googleDriveChildFileModelList));
        saveGoogleDriveDataList(googleDriveFolderModelList);
    }

    public void deleteFolderFromGoogleDriveDataList(String folderId) {
        List<GoogleDriveFolderModel> googleDriveFolderModelList = getGoogleDriveDataList();
        ListIterator<GoogleDriveFolderModel> iter = googleDriveFolderModelList.listIterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(folderId)) {
                iter.remove();
                break;
            }
        }
        saveGoogleDriveDataList(googleDriveFolderModelList);
    }

    public GoogleDriveFolderModel isFolderExistOnGoogleDrive(String folderName) {
        for (GoogleDriveFolderModel googleDriveFolderModel : getGoogleDriveDataList()) {
            if (googleDriveFolderModel.getFolderName().equals(folderName)) {
                return googleDriveFolderModel;
            }
        }
        return null;
    }

    public void updateChildOfGoogleDriveFolder(String folderId, List<GoogleDriveChildFileModel> googleDriveChildFileModelList) {
        List<GoogleDriveFolderModel> googleDriveFolderModelList = getGoogleDriveDataList();
        GoogleDriveFolderModel updateDriveFolderModel = null;
        for (GoogleDriveFolderModel driveFolderModel : googleDriveFolderModelList) {
            if (driveFolderModel.getId().equals(folderId)) {
                updateDriveFolderModel = driveFolderModel;
                break;
            }
        }
        if (updateDriveFolderModel != null) {
            updateDriveFolderModel.setGoogleDriveChildFileModelList(googleDriveChildFileModelList);
        }
        saveGoogleDriveDataList(googleDriveFolderModelList);
    }
}
