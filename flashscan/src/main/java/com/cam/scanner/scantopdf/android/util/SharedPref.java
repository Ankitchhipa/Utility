package com.cam.scanner.scantopdf.android.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private static final String PREF = "shared_preference";
    private static final String MODIFIED_FILE_NAME = "modified_file_name";
    private static final String IS_APP_AD_FREE = "is_app_ad_free";
    private Context context;
    private SharedPreferences sharedPreferences;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SharedPref.PREF, Context.MODE_PRIVATE);
    }


    public void saveModifiedFileName(String fileName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref.MODIFIED_FILE_NAME, fileName);
        editor.apply();
    }

    public String getModifiedFileName() {
        return sharedPreferences.getString(SharedPref.MODIFIED_FILE_NAME, null);
    }

    public boolean isAppAdFree() {  // true if payment done - don't show any ad in app
        return sharedPreferences.getBoolean(SharedPref.IS_APP_AD_FREE, false);
    }
}
