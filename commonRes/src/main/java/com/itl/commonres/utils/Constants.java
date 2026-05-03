package com.itl.commonres.utils;

import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.itl.commonres.BuildConfig;

import java.util.ArrayList;

public class Constants {
    public static String Storage_and_Camera = "Storage_and_Camera";
    public static String Storage = "Storage";
    public static String Camera = "Camera";
    public static String All_Files_Access = "All_Files_Access";

    public static String privacyPolicyUrl = BuildConfig.PRIVACY_POLICY_URL;
    public static String termsOfUseUrl = BuildConfig.TERMS_OF_USE_URL;
    public static String helpUrl = BuildConfig.HELP_URL;

    //Event Name
    public static String USER_ACTION = "user_action";
    public static String APP_FIRST_LAUNCH = "app_first_launch";
    public static String APP_OPEN = "app_open";

    //Mobibuz Events
    public static String OPEN_SCAN_HUB = "Click_ScanHub";
    public static String OPEN_BOOSTX = "Click_BoostX";
    public static String CLICK_MENU = "Click_Menu";
    public static String CLICK_REFER_A_FRIEND = "Click_refer_a_friend";
    public static String CLICK_ABOUT_US = "Click_about_us";
    public static String CLICK_USEFUL_LINKS = "Click_useful_links";
    public static String CLICK_SHARE = "Click_Share";
    public static String CLICK_FEEDBACK = "Click_Feedback";
    public static String CLICK_SUBMIT = "Click_Submit";
    public static String CLICK_HELP_CENTER = "Click_HelpCenter";
    public static String CLICK_PRIVACY_POLICY = "Click_Privacy_Policy";
    public static String CLICK_TERMS_OF_USE = "Click_terms_of_use";
    public static String Launch_Tutorial_Screen = "Launch_tutorial_screen";
    public static String CLICK_ANYTIME_ASTRO_AD = "Click_Anytime_Astro_Ad";
    public static String CLICK_TAROT_LIFE_AD = "Click_Tarot_Life_Ad";

    //ScanHub Events
    public static String OPEN_DOC_SCANNER = "Click_Doc_Scanner";
    public static String OPEN_QR_CODE_SCANNER = "Click_QR_Code_Scanner";
    public static String OPEN_OCR_SCANNER = "Click_OCR_Scanner";
    public static String OPEN_MY_FAVORITES = "Click_My_Favorites";
    public static String CLICK_SETTINGS_ICON = "Click_Settings_icon";
    public static String CLICK_DEFAULT_SHARING = "Click_Default_sharing";
    public static String CLICK_PDF_PAGE_SIZE = "Click_PDF_Page_Size";
    public static String CLICK_PDF_ICON_CREATE_PDF = "Click_PDF_icon_create_PDF";
    public static String CLICK_PDF_SHARE_ICON = "Click_share_icon";
    public static String CLICK_ANTI_COUNTERFEIT = "Click_Anti_counterfeit";
    public static String CLICK_PDF_SIGNATURE = "Click_PDF_signature";
    public static String CLICK_PDF_PASSWORD = "Click_PDF_password";
    public static String CLICK_FILE_COMPRESSION = "Click_File_Compression";
    public static String CLICK_PDF_ICON = "Click_PDF_icon";
    public static String CLICK_SHARE_ICON = "Click_share_icon";
    public static String CLICK_GOOGLE_DRIVE_ICON = "Click_Google_Drive_icon";
    public static String CLICK_DELETE_ICON = "Click_delete_icon";

    //BoostX Events
    public static String CLICK_JUNKCLEANER = "Click_JunkCleaner";
    public static String CLICK_JUNK_MEDIA_CLEANER = "Click_Junk_Media_Cleaner";
    public static String CLICK_ANTIMALWARE = "Click_AntiMalware";
    public static String CLICK_SOCIAL_CLEANER = "Click_SocialCleaner";
    public static String CLICK_DUPLICATES_CLEANER = "Click_DuplicatesCleaner";
    public static String CLICK_APPLICATION_MANAGER = "Click_ApplicationManager";
    public static String CLICK_FILE_MANAGER = "Click_FileManager";
    public static String CLICK_STOP_SCANNING = "Click_Stop_Scanning";
    public static String WATCH_AD_TO_CLEAN_JUNK = "Watch_Ad_to_Clean_Junk";
    public static String WATCH_AD_TO_CLEAN_ANTIMALWARE = "Watch_Ad_to_CleanMalware";
    public static String WATCH_AD_TO_CLEAN_SOCIAL = "Watch_Ad_to_CleanUp";
    public static String WATCH_AD_TO_DELETE_PHOTOS = "Watch_Ad_to_Delete_Photos";
    public static String CLICK_CLEAN_JUNK = "Click_Junk_Clean_Button";
    public static String CLICK_CLEAN_ANTIMALWARE = "Click_AntiMalware_Clean_Button";
    public static String CLICK_CLEAN_SOCIAL = "Click_Socical_Clean_Button";
    public static String CLICK_DUPLICATES_DELETE_PHOTOS = "Click_Duplicates_Clean_Button";
    public static String DELETE_DUPLICATE_PHOTOS = "Delete_Duplicate_Photos";
    public static String FIND_DUPLICATES = "Find_Duplicates";
    public static String CLICK_SECURITY = "Click_Security";
    public static String CLICK_SPACE_SAVER = "Click_Space_Saver";
    public static String CLICK_CLEAN_BUTTON = "Click_Clean_Button";
    public static String CLICK_INFO = "Click_Info";
    public static String CLICK_SECURE_BROWSER = "Click_Secure_Browser";
    public static String CLICK_NOTIFICATION_MANAGER = "Click_Notification_Manager";

    public static InterstitialAd interstitialAd = null;
    public static AppOpenAd appOpenAd = null;

    public static RewardedAd rewardedAd = null;
    public static Boolean isAdShow = false;

    public static String playStoreUrl = "https://play.google.com/store/apps/details?id=com.utilify.boost.cleaner";

    public static boolean isFirstLaunch = false;

    public static String binaryFirebase = "";
    public static ArrayList<Integer> AdInterstitialConfigArrayList = new ArrayList<>();
    public static ArrayList<Integer> AdInterstitialCappingArrayList = new ArrayList<>();

    public static String FROM_USEFUL_SCREEN = "fromUsefulScreen";

}
