package com.itl.commonres.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPref {

    public static final String FCM_TOKEN = "fcm_token";

    public static final String APP_KEY = "Mobibuz";


    public static String getString(Context context, String str) {
        return context.getSharedPreferences(APP_KEY, 0).getString(str, "");
    }

    public static void setString(Context context, String str, String str2) {
        SharedPreferences.Editor edit = context.getSharedPreferences(APP_KEY, 0).edit();
        edit.putString(str, str2);
        edit.apply();
    }

    public static int getInt(Context context, String str) {
        return context.getSharedPreferences(APP_KEY, 0).getInt(str, 0);
    }

    public static int getFreeMin(Context context, String str) {
        return context.getSharedPreferences(APP_KEY, 0).getInt(str, 2);
    }

    public static int getPaidMin(Context context, String str) {
        return context.getSharedPreferences(APP_KEY, 0).getInt(str, 5);
    }

    public static void setInt(Context context, String str, int i) {
        SharedPreferences.Editor edit = context.getSharedPreferences(APP_KEY, 0).edit();
        edit.putInt(str, i);
        edit.apply();
    }

    public static boolean getBoolean(Context context, String str) {
        return context.getSharedPreferences(APP_KEY, 0).getBoolean(str, false);
    }

    public static void setBoolean(Context context, String str, boolean z) {
        SharedPreferences.Editor edit = context.getSharedPreferences(APP_KEY, 0).edit();
        edit.putBoolean(str, z);
        edit.apply();
    }

    public static void setLong(Context context, String str, long l) {
        try {
            SharedPreferences.Editor edit = context.getSharedPreferences(APP_KEY, 0).edit();
            edit.putLong(str, l);
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Long getLong(Context context, String str) {
        return context.getSharedPreferences(APP_KEY, 0).getLong(str, 0);
    }

    public static void setFloat(Context context, String str, float l) {
        try {
            SharedPreferences.Editor edit = context.getSharedPreferences(APP_KEY, 0).edit();
            edit.putFloat(str, l);
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Float getFloat(Context context, String str) {
        return context.getSharedPreferences(APP_KEY, 0).getFloat(str, 0);
    }

    public static void clearAll(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(APP_KEY, 0).edit();
        editor.clear();
        editor.apply();
    }

}
