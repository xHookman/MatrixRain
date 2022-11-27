package com.chacha.matrixrain;

import android.content.Context;
import android.content.SharedPreferences;

import com.coniy.fileprefs.FileSharedPreferences;

public class Preference {
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    protected static SharedPreferences loadPreferences(Context context){
        try {
            //noinspection deprecation
            pref = context.getSharedPreferences("user_settings", Context.MODE_WORLD_READABLE);
        } catch (SecurityException ignored) {
            pref = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        }
        FileSharedPreferences.makeWorldReadable(context.getPackageName(), "user_settings");
        editor = pref.edit();
        return pref;
    }
}
