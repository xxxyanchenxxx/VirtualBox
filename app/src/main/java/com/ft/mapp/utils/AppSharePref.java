package com.ft.mapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ft.mapp.VApp;

public class AppSharePref {

    private SharedPreferences mSharedPreferences = null;
    private static volatile AppSharePref sSnapSharePref;

    public static AppSharePref getInstance(Context context) {
        if (sSnapSharePref == null) {
            synchronized (AppSharePref.class) {
                if (sSnapSharePref == null) {
                    sSnapSharePref = new AppSharePref(context);
                }
            }
        }
        return sSnapSharePref;
    }

    private AppSharePref(Context context) {
        if (context == null) {
            context = VApp.getApp();
        }
        mSharedPreferences = context.getSharedPreferences("v_app", Context.MODE_MULTI_PROCESS);
    }

    /**************************基本方法定义************************************/
    public void putBoolean(String keyString, boolean value) {
        mSharedPreferences.edit().putBoolean(keyString, value).apply();
    }

    public boolean getBoolean(String keyString) {
        return mSharedPreferences.getBoolean(keyString, false);
    }

    public boolean getBoolean(String keyString, boolean defValue) {
        return mSharedPreferences.getBoolean(keyString, defValue);
    }

    public void putString(String keyString, String value) {
        mSharedPreferences.edit().putString(keyString, value).apply();
    }

    public String getString(String keyString) {
        return mSharedPreferences.getString(keyString, "");
    }

    public void putInt(String keyString, int value) {
        mSharedPreferences.edit().putInt(keyString, value).apply();
    }

    public int getInt(String keyString) {
        return mSharedPreferences.getInt(keyString, -1);
    }

    public int getInt(String keyString, int defaultValue) {
        return mSharedPreferences.getInt(keyString, defaultValue);
    }

    public void putLong(String keyString, long value) {
        mSharedPreferences.edit().putLong(keyString, value).apply();
    }

    public long getLong(String keyString) {
        return mSharedPreferences.getLong(keyString, -1);
    }

    public long getLong(String keyString, long defValue) {
        return mSharedPreferences.getLong(keyString, defValue);
    }
}
