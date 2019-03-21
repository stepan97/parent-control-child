package com.example.userasef.parentcontrolappchild.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {
    public static void putString(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_TAG, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defValue);
    }
}
