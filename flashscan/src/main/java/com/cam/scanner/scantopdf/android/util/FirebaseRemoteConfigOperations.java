package com.cam.scanner.scantopdf.android.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseRemoteConfigOperations {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String TAG = FirebaseRemoteConfigOperations.class.getSimpleName();
    private Context _ctx;
    private Activity _activity;
    private DBHandler dbHandler;
    private PrefManager prefManager;

    public FirebaseRemoteConfigOperations(Context ctx, Activity activity) {
//        this.mFirebaseRemoteConfig = mFirebaseRemoteConfig;
        this._ctx = ctx;
        this._activity = activity;
        dbHandler = AppController.getINSTANCE().dbHandler;
        prefManager = new PrefManager(ctx);
    }

    public void firebaseRemoteConfig() {

        Log.i(TAG, "activity: " + _activity);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                //.setDeveloperModeEnabled(true)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        //Set default values
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        //Fetch and activate remote config values
        fetchAndActivateRemoteConfigValues();
    }

    public String getPackageName() {
        String packageNameRemoteConfig = mFirebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_PACKAGE_NAME);
        Log.i(TAG, "packageName: " + packageNameRemoteConfig);
        return packageNameRemoteConfig;
    }

    private void fetchAndActivateRemoteConfigValues() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(_activity, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            Log.i(TAG, "Fetch and activate succeeded");
                            /*Toast.makeText(_ctx, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();*/

                        } else {
                            Log.i(TAG, "Fetch failed");
                            /*Toast.makeText(_ctx, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();*/
                        }
                        operationOnRemoteConfigData();
                    }
                });
    }

    private void operationOnRemoteConfigData() {
        Log.i(TAG, "Starting necessary operations on remote config data");
        putInSharedPref();
        putInDbRules();

        if (!Constants.IS_REMOTE_CONFIG_FROM_OWN_API) {
            putInDbOCRRules();
        }

        String dataJsonStr = mFirebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_DATA);
        Log.i(TAG, "data: " + dataJsonStr);

        try {

            JSONObject dataJson = new JSONObject(dataJsonStr);

            Log.d(TAG, dataJson.toString());

            JSONArray jsonArrDevices = dataJson.getJSONArray(Constants.JSON_NODE_DEVICES);

            putInDbDevices(jsonArrDevices);

        } catch (JSONException je) {
            Log.e(TAG, "jsonexception;" + je.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "exception" + e.getMessage());
        }

        getValuesFromDb();
    }

    private void putInSharedPref() {
        //OpenAd flag override in pref
        boolean showAppOpenAd = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_APP_OPEN_AD);
        Log.i(TAG, "show app open ad: " + showAppOpenAd);
        prefManager.setshowAppOpenAd(showAppOpenAd);

        boolean showSplashInterstitialAd = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_INTERS_SPLASH);
        Log.i(TAG, "splashInterstitialAd " + showSplashInterstitialAd);
        prefManager.setSplashInterstitialAd(showSplashInterstitialAd);

        String pixelNeticaLicenseKey = mFirebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_PIXEL_NETICA_LICENSE_KEY);
        Log.i(TAG, "pixelNeticaLicenseKey " + pixelNeticaLicenseKey);
        prefManager.setPixelNeticaLicenseKey(pixelNeticaLicenseKey);
    }

    private void putInDbOCRRules() {
        String ocrJsonStr = mFirebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_OCR_RULES);
        Log.i(TAG, "OCR json str: " + ocrJsonStr);

        try {

            JSONObject ocrJson = new JSONObject(ocrJsonStr);

            Log.d(TAG, ocrJson.toString());

            int freeOcr = ocrJson.getInt(Constants.JSON_NODE_FREE);
            int premiumYearly = ocrJson.getInt(Constants.JSON_NODE_PREMIUM_YEARLY);
            int ocrMonthly = ocrJson.getInt(Constants.JSON_NODE_OCR_MONTHLY);

            int testFreeOcr = ocrJson.getInt(Constants.JSON_NODE_TEST_FREE);
            int testPremiumYearly = ocrJson.getInt(Constants.JSON_NODE_TEST_PREMIUM_YEARLY);
            int testOcrMonthly = ocrJson.getInt(Constants.JSON_NODE_TEST_OCR_MONTHLY);

            Log.d(TAG, "freeOcr: " + freeOcr);
            Log.d(TAG, "premiumYearlyOcr: " + premiumYearly);
            Log.d(TAG, "ocrMonthly: " + ocrMonthly);

            Log.d(TAG, "testFreeOcr: " + testFreeOcr);
            Log.d(TAG, "testPremiumYearlyOcr: " + testPremiumYearly);
            Log.d(TAG, "testOcrMonthly: " + testOcrMonthly);

            if (dbHandler.existOcrRules()) {
                dbHandler.updateOcrRules(freeOcr, premiumYearly, ocrMonthly, testFreeOcr, testPremiumYearly, testOcrMonthly);
            } else {
                dbHandler.insertOcrRules(freeOcr, premiumYearly, ocrMonthly, testFreeOcr, testPremiumYearly, testOcrMonthly);
            }

        } catch (JSONException je) {
            Log.e(TAG, "jsonexception;" + je.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "exception" + e.getMessage());
        }
    }

    private void putInDbDevices(JSONArray jsonArrDevices) {
        if (dbHandler.existDevicesAllowed()) {
            dbHandler.deleteDeviceAllowed();
            Log.i(TAG, "previous devices deleted");
        }

        for (int i = 0; i < jsonArrDevices.length(); i++) {
            try {
                String androidId = (String) jsonArrDevices.get(i);
                Log.i(TAG, "android_id: " + androidId);

                if (!dbHandler.existDevicesAllowed(androidId)) {
                    dbHandler.insertDevicesAllowed(androidId);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void putInDbRules() {
        boolean showIntersSplash = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_INTERS_SPLASH);
        boolean showIntersExit = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_INTERS_EXIT);
        boolean showIntersCreation = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_INTERS_CREATION);
        boolean showNative = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_NATIVE);
        int intersSplashAfter = (int) mFirebaseRemoteConfig.getLong(Constants.REMOTE_CONFIG_INTERS_SPLASH_AFTER);
        int intersCreateFreqInSession = (int) mFirebaseRemoteConfig.getLong(Constants.REMOTE_CONFIG_INTERS_CREATE_FREQ_IN_SESSION);

        int showIntersSplashInt = showIntersSplash ? 1 : 0;
        int showIntersExitInt = showIntersExit ? 1 : 0;
        int showIntersCreationInt = showIntersCreation ? 1 : 0;
        int showNativeInt = showNative ? 1 : 0;

        Log.i(TAG, "show_inters_splash: " + showIntersSplash + " : " + showIntersSplashInt);
        Log.i(TAG, "show_inters_exit: " + showIntersExit + " : " + showIntersExitInt);
        Log.i(TAG, "show_inters_creation: " + showIntersCreation + " : " + showIntersCreationInt);
        Log.i(TAG, "show_native: " + showNative + " : " + showNativeInt);
        Log.i(TAG, "inters_splash_after: " + intersSplashAfter);
        Log.i(TAG, "inters_create_freq_in_session: " + intersCreateFreqInSession);

        if (dbHandler.existAdsRules()) {
            dbHandler.updateAdsRules(showIntersSplashInt, showIntersExitInt, showIntersCreationInt,
                    showNativeInt,
                    intersSplashAfter, intersCreateFreqInSession);
        } else {
            dbHandler.insertAdsRules(showIntersSplashInt, showIntersExitInt, showIntersCreationInt,
                    showNativeInt,
                    intersSplashAfter, intersCreateFreqInSession);
        }

    }

    private void getValuesFromDb() {
        boolean showIntersSplash = dbHandler.showIntesSplash();
        boolean showIntersExit = dbHandler.showIntersExit();
        boolean showIntersCreation = dbHandler.showIntersCreation();
        boolean showNative = dbHandler.showNative();
        int intersSplashAfter = dbHandler.intersSplashAfter();
        int intersCreateFreqInSession = dbHandler.intersCreateFreqInSession();

        Log.i(TAG, "from DB: show_inters_splash: " + showIntersSplash);
        Log.i(TAG, "from DB: show_inters_exit: " + showIntersExit);
        Log.i(TAG, "from DB: show_inters_creation: " + showIntersCreation);
        Log.i(TAG, "from DB: show_native: " + showNative);
        Log.i(TAG, "from DB: inters_splash_after: " + intersSplashAfter);
        Log.i(TAG, "from DB: inters_create_freq_in_session: " + intersCreateFreqInSession);
    }
}
