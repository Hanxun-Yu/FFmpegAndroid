package com.kedacom.demo.appcameratoh264.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by xunxun on 2015/7/6.
 */
public class ShareReferenceUtil {
    public static String get(Context con, String name, String key) {
        return get( con, name,  key,"");
    }

    public static String get(Context con, String name, String key, String defaultVal) {
        SharedPreferences settings = con.getSharedPreferences(name, 0);
        return settings.getString(key,defaultVal);
    }
    public static int get(Context con, String name, String key, int defaultVal) {
        SharedPreferences settings = con.getSharedPreferences(name, 0);
        return settings.getInt(key,defaultVal);
    }
    public static void set(Context con, String name, String key, String value) {
        SharedPreferences settings = con.getSharedPreferences(name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public static void set(Context con, String name, String key, int value) {
        SharedPreferences settings = con.getSharedPreferences(name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key,value);
        editor.commit();
    }


    public static void set(Context con, String name, String key, boolean value) {
        SharedPreferences settings = con.getSharedPreferences(name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }
    public static boolean get(Context con, String name, String key, boolean defaultVal) {
        SharedPreferences settings = con.getSharedPreferences(name, 0);
        return settings.getBoolean(key,defaultVal);
    }
}
