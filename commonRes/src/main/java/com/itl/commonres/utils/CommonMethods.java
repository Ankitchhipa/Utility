package com.itl.commonres.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itl.commonres.R;
import com.itl.commonres.appinterface.OnAdDismissInterface;
import com.itl.commonres.firebaseUtils.FirebaseConstants;

import java.util.ArrayList;

public class CommonMethods {

    Context context;
    public static long mLastClickTime;

    public static FirebaseAnalytics mFirebaseAnalytics;

    private static String TAG = CommonMethods.class.getSimpleName();

    public static OnAdDismissInterface onAdDismissInterface;

    public CommonMethods(Context context) {
        this.context = context;
        mLastClickTime = 0;
    }

    public AlertDialog showAlertDialog(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnClick
            , String negativeLabel, DialogInterface.OnClickListener negativeOnClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        if (!TextUtils.isEmpty(msg)) {
            builder.setMessage(msg);
        }
        builder.setCancelable(false);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        builder.setNeutralButton(negativeLabel, negativeOnClick);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    public void showExitDialog(final OnExitClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_exit);

        // Make dialog background transparent to show CardView corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView btnExit = dialog.findViewById(R.id.btnExit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onExit();
            }
        });

        dialog.setCancelable(true);
        dialog.show();
    }

    public interface OnExitClickListener {
        void onExit();
    }

    public void showUniScanPermissionDialog(PermissionInterface permissionInterface, Boolean showNotNow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.uniscan_permission_dialog, null);
        TextView desc = view.findViewById(R.id.desc);
        desc.setText(context.getString(R.string.all_file_access_permission_desc, "Mobibuz"));
        TextView tvNotNow = view.findViewById(R.id.tv_not_now);
        if (showNotNow) tvNotNow.setVisibility(View.VISIBLE);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.btn_uniscan).setOnClickListener(v -> {
            permissionInterface.onPermissionClickOkay(true, context);
            alertDialog.cancel();
        });

        tvNotNow.setOnClickListener(v -> {
            permissionInterface.onPermissionClickNotNow(context);
            alertDialog.cancel();
        });

        view.findViewById(R.id.ic_close).setOnClickListener(v -> {
            alertDialog.cancel();
        });
        alertDialog.show();
    }

    public void showPermissionDialog(String permissionName, PermissionInterface permissionInterface, Boolean showNotNow) {
        String message;
        boolean isAllFilesAccess = false;
        switch (permissionName) {
            case "Storage_and_Camera":
                message = context.getString(R.string.permission_storage_camera);
                break;
            case "Storage":
                message = context.getString(R.string.permission_storage);
                break;
            case "Camera":
                message = context.getString(R.string.permission_setting_camera);
                break;
            case "All_Files_Access":
                isAllFilesAccess = true;
                message = context.getString(R.string.permission_setting_all_files);
                break;
            default:
                message = context.getString(R.string.permission_setting_screen);
                break;
        }

        boolean finalIsAllFilesAccess = isAllFilesAccess;
        if (isAllFilesAccess) {
            new CommonMethods(context).showUniScanPermissionDialog(permissionInterface, showNotNow);
        } else {
            new CommonMethods(context).showAlertDialog(context.getString(R.string.all_file_access_permission_title), message, context.getString(R.string.go_to_settings), (dialog, which) -> {
                dialog.dismiss();
                permissionInterface.onPermissionClickOkay(finalIsAllFilesAccess, context);
            }, context.getString(R.string.cancel), (dialog, which) -> {
                dialog.dismiss();
            });
        }
    }

    public void askUserToRequestPermissionExplicitly(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public void askUserToRequestNotificationPermissionExplicitly(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        context.startActivity(intent);
    }

    public void askUserToRequestAllFilesAccess(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", context.getPackageName())));
            context.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            context.startActivity(intent);
        }
    }

    public void processPermission(Boolean isAllFilesAccess, Context context) {
        if (isAllFilesAccess) {
            askUserToRequestAllFilesAccess(context);
        } else {
            askUserToRequestPermissionExplicitly(context);
        }
    }

    public void redirectToBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static boolean multipleClicked() {
        long l = SystemClock.elapsedRealtime() - mLastClickTime;
        mLastClickTime = SystemClock.elapsedRealtime();
        return l < 200;
    }

    public static void logCustomFireBaseEvents(String screenName, String actionName) {
        Bundle params = new Bundle();
        params.putString("device_name", Build.MODEL.toUpperCase());
        params.putString("screen_name", screenName);
        params.putString("action", actionName);

        //Log.i(TAG, "logCustomFireBaseEvents:" + params);
        //mFirebaseAnalytics.logEvent(Constants.USER_ACTION, params);
    }

    public static void logCustomFireBaseEvents(String screenName, String actionName, ArrayList<String> list) {
        Bundle params = new Bundle();
        params.putString("device_name", Build.MODEL.toUpperCase());
        params.putString("screen_name", screenName);
        params.putString("action", actionName);
        params.putStringArrayList("fileTypes", list);
        //Log.i(TAG, "logCustomFireBaseEvents:" + params);
       // mFirebaseAnalytics.logEvent(Constants.USER_ACTION, params);
    }

    public void setAppName(TextView tvAppName, Context context) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(context.getString(R.string.app_name));

        int firstColorInt = Color.parseColor("#000000");
        int secondColorInt = Color.parseColor("#087DFF");

        spannableStringBuilder.setSpan(
                new ForegroundColorSpan(firstColorInt),
                0,
                5,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannableStringBuilder.setSpan(
                new ForegroundColorSpan(secondColorInt),
                5,
                spannableStringBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvAppName.setText(spannableStringBuilder);
    }

    public static void generateFCMTokenIfEmpty(Context context) {
        if (isConnectingToInternet(context)) {
            String savedFCMToken = AppPref.getString(context, AppPref.FCM_TOKEN);
            Log.e("FCM Token =====", "generateFCMTokenIfEmpty savedFCMToken: " + savedFCMToken);
            if (TextUtils.isEmpty(savedFCMToken)) {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !TextUtils.isEmpty(task.getResult())) {
                        AppPref.setString(context, AppPref.FCM_TOKEN, task.getResult());
                    }
                });
            }
        }
    }

    public static void loadInterstitialAd(Activity mContext, Boolean isLoadingShow, String interstitialAdUnitId) {
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.e(TAG, "Interstitial unit id > " + interstitialAdUnitId);

        if (isLoadingShow) {
            showLoading(mContext);
        }

        InterstitialAd.load(mContext, interstitialAdUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                        Constants.interstitialAd = interstitialAd;

                        Constants.interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d(TAG, "The inter ad was dismiss.");
                                Constants.interstitialAd = null;
                                onAdDismissInterface.onAdDismiss();
                                //loadInterstitialAd(mContext, false);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                Log.d(TAG, "The inter ad failed to show.");
                                Log.d(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                                hideLoading();
                                if (Constants.interstitialAd == null) {
                                    onAdDismissInterface.onAdDismiss();
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                hideLoading();
                                Log.d(TAG, "The inter ad was shown.");
                            }
                        });

                        if (isLoadingShow && Constants.interstitialAd != null && !isAppInBackground()) {
                            Constants.interstitialAd.show(mContext);
                        } else {
                            hideLoading();
                        }

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        hideLoading();
                        Log.i("onAdFailedToLoad", loadAdError.getMessage());
                        onAdDismissInterface.onAdDismiss();
                    }
                });
    }

    /*public static void loadAppOpenAd(Context context) {
        String adUnitId = BuildConfig.AD_UNIT_ID_APP_OPEN_AD;
        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(
                context, adUnitId, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i("appOpenAdFailedToLoad", loadAdError.getMessage());

                    }

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {

                        Constants.appOpenAd = appOpenAd;

                        Constants.appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                Log.e(TAG, "onAdClicked: app open ad");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "The app open ad was dismiss.");
                                onAdDismissInterface.onAdDismiss();
                                loadAppOpenAd(context);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                Log.e(TAG, "onAdFailedToShowFullScreenContent: App open ad error === " + adError);
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                Log.d(TAG, "The app open ad was shown.");

                                Constants.appOpenAd = null;
                            }
                        });

                    }
                });
    }*/

    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        //Toast.makeText(_context, "Internet true", Toast.LENGTH_SHORT).show();
                        return true;
                    }
        }
        return false;
    }


    public static void loadRewardedAd(Activity activity, RewardedAdInterface rewardedAdInterface, String rewardedAdUnitId) {
        if (isConnectingToInternet(activity) && Constants.isAdShow) {
            final Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.loading_ads);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            AdRequest adRequest = new AdRequest.Builder().build();
            Log.e(TAG, "Rewarded unit id > " + rewardedAdUnitId);
            RewardedAd.load(activity, rewardedAdUnitId, adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    Log.d(TAG, adError.getMessage());
                    if (dialog.isShowing()) {
                        dismissWithTryCatch(dialog);
                    }
                    rewardedAdInterface.proceed();
                }

                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                    Log.d(TAG, "Ad was loaded.");
                    Constants.rewardedAd = rewardedAd;
                    Constants.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            if (dialog.isShowing()) {
                                dismissWithTryCatch(dialog);
                            }
                            Log.d(TAG, "Ad was shown.");
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Log.d(TAG, "Ad failed to show.");
                            Log.d(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                            Log.d(TAG, "onAdFailedToShowFullScreenContent: " + adError.getCode());

                            if (dialog.isShowing()) {
                                dismissWithTryCatch(dialog);
                            }

                            if (Constants.rewardedAd == null) {
                                rewardedAdInterface.proceed();
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad was dismissed.");
                            Constants.rewardedAd = null;
                            rewardedAdInterface.proceed();
                        }
                    });

                    if (Constants.rewardedAd != null && !isAppInBackground()) {
                        Constants.rewardedAd.show(activity, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                Log.e(TAG, "REWARD_CALLED");
                            }
                        });
                    } else {
                        if (dialog.isShowing()) {
                            dismissWithTryCatch(dialog);
                        }
                    }
                }
            });

        } else {
            Log.e("Mobibuz : ", "Ad Not Showing");
            rewardedAdInterface.proceed();
//            goToNext(activity, intent, finish);
        }
    }

    public static void dismissWithTryCatch(Dialog dialog) {
        try {
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadBannerAd(Context context, AdSize adSize, AdStatusInterface adStatusInterface, ViewGroup adContainer, String bannerAdUnitId) {
        if (isConnectingToInternet(context)) {

            AdView adView = new AdView(context);
            // Step 4 - Set the adaptive ad size on the ad view.
            adView.setAdSize(adSize);
            adView.setAdUnitId(bannerAdUnitId);

            Log.e(TAG, "Banner unit id > " + bannerAdUnitId);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e(TAG, "onAdFailedToLoad: " + loadAdError);
                    adStatusInterface.onAdFailed();
                }

                @Override
                public void onAdLoaded() {
                    adStatusInterface.onAdLoaded();
                    if (adContainer != null) {
                        // Create layout parameters for the adView
                        adContainer.addView(adView);
                    }
                }
            });

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adStatusInterface.onAdFailed();
        }
    }

    /*public static void loadBannerAdNew(AdView adView) {
        Log.e(TAG, "Banner unit id > " + BuildConfig.AD_UNIT_ID_BANNER_AD);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }*/

    public static AdSize getAdSize(Activity activity) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    private static AlertDialog dialog;

    public static void hideLoading() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public static void showLoading(Context context) {
        dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_ads, null);
        dialog.setView(view);
        dialog.show();
    }

    public static Boolean isAppInBackground() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState() == Lifecycle.State.CREATED;
    }

    public static void hexToBinary() {
        String hexString = FirebaseConstants.Companion.getHEX_SHOW_AD();
        Log.i(TAG, "hexToBinary:hexString::" + hexString);
        if (!TextUtils.isEmpty(hexString)) {
            // Remove the "0x" prefix if present and convert to uppercase
            String cleanHexString = hexString.replaceFirst("0x", "").toUpperCase();

            // Check if the string is a valid hexadecimal number
            if (!cleanHexString.matches("[0-9A-F]+")) {
                throw new IllegalArgumentException("Invalid hexadecimal string: " + hexString);
            }

            StringBuilder binaryStringBuilder = new StringBuilder();

            for (char hexChar : cleanHexString.toCharArray()) {
                int decimalValue;
                if (hexChar >= '0' && hexChar <= '9') {
                    decimalValue = hexChar - '0';
                } else if (hexChar >= 'A' && hexChar <= 'F') {
                    decimalValue = hexChar - 'A' + 10;
                } else {
                    throw new IllegalArgumentException("Invalid hexadecimal character: " + hexChar);
                }
                // Convert the decimal value to a 4-bit binary string
                String binaryString = String.format("%4s", Integer.toBinaryString(decimalValue)).replace(' ', '0');
                binaryStringBuilder.append(binaryString);
            }

            Constants.binaryFirebase = binaryStringBuilder.toString();
        }
    }

    public static void createInterstitialAdArrayList() {
        Constants.AdInterstitialConfigArrayList.clear();
        Constants.AdInterstitialCappingArrayList.clear();

        long num = FirebaseConstants.Companion.getINTERSTITIAL_SHOW_AD();
        if (num != 0L) {

            while (num > 0) {
                int digit = (int) (num % 10);
                Constants.AdInterstitialConfigArrayList.add(0, digit);
                num /= 10;
                Constants.AdInterstitialCappingArrayList.add(0);
            }
        }
    }

    public static boolean isAdActive(int position) {
        String binary = Constants.binaryFirebase;
        int length = binary.length();
        if (!TextUtils.isEmpty(binary) && position < length) {
            int value = binary.charAt(position);
            Log.i(TAG, "isAdActive:binary::" + binary);
            Log.i(TAG, "isAdActive:value::" + value + "::position::" + position);
            return value != 0 && value != 48; //0:false, 48: ASCII value of 0 , 49: ASCII value of 1
        }
        return false;
    }

    public static boolean isShowInterstitialAdDashboard(int module, int index) {
        if (isInterstitialCappingValid(index)) {
            if (module == OnClickEnum.ScanHub.getValue()) {
                if (Constants.isFirstLaunch) {
                    return FirebaseConstants.Companion.getScanHubFirstLaunch();
                } else {
                    return FirebaseConstants.Companion.getScanHubSecondLaunch();
                }
            } else if (module == OnClickEnum.BoostX.getValue()) {
                if (Constants.isFirstLaunch) {
                    return FirebaseConstants.Companion.getBoostXFirstLaunch();
                } else {
                    return FirebaseConstants.Companion.getBoostXSecondLaunch();
                }
            }
        }
        return false;
    }

    public static boolean isInterstitialCappingValid(int index) {
        if (!Constants.AdInterstitialConfigArrayList.isEmpty() && !Constants.AdInterstitialCappingArrayList.isEmpty()) {
            return Constants.AdInterstitialCappingArrayList.get(index) < Constants.AdInterstitialConfigArrayList.get(index);
        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        boolean isInternetOn = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities == null) {
                return false;
            }
            isInternetOn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        } else {
            isInternetOn = isOnline(context);
        }
        return isInternetOn;
    }

    private static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }
}
