package com.chacha.matrixrain;

import static com.chacha.matrixrain.Utils.MY_PACKAGE_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

import com.coniy.fileprefs.FileSharedPreferences;

import java.io.File;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class Preferences {
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    private static final String prefFileName = "Settings";
    public final Context context;

    public int fontSize, columnSize, speed;
    public int vertLetterSpace, horLetterSpace;
    public float offsetEndLines;
    public char[] text;
    public int choosedColor1, choosedColor2, choosedColorBg, trailSize;
    public int opacityBg;
    public int position;
    public String fontPath;
    public boolean isGradient, isRandomColors, isInvert;

    public Preferences(Context context) {
      this.context = context;
    }

    protected static SharedPreferences loadPreferences(Context context){
        try {
            //noinspection deprecation
            pref = context.getSharedPreferences(prefFileName, Context.MODE_WORLD_READABLE);
        } catch (SecurityException ignored) {
            pref = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
        }
        FileSharedPreferences.makeWorldReadable(context.getPackageName(), prefFileName);
        editor = pref.edit();
        return pref;
    }

    public void loadMatrixRainPrefs(){
        choosedColor1 = pref.getInt("color1", Color.GREEN);
        choosedColor2 = pref.getInt("color2", Color.GREEN);
        choosedColorBg = pref.getInt("colorBg", Color.BLACK);
        trailSize = pref.getInt("trailSize", 10);
        text = pref.getString("rndText", context.getResources().getString(R.string.default_rndtext)).toCharArray();
        fontPath = pref.getString("fontPath", "");
        fontSize = pref.getInt("size", 20);
        speed = pref.getInt("speed", 20);
        isGradient = pref.getBoolean("isGradient", false);
        isRandomColors = pref.getBoolean("isRandomColors", false);
        isInvert = pref.getBoolean("isInvert", false);
        columnSize = pref.getInt("columnSize", 1);
        vertLetterSpace = pref.getInt("vertLetterSpace", 1);
        horLetterSpace = pref.getInt("horLetterSpace", 1);
        offsetEndLines = pref.getInt("offsetEndLines", 9);
        opacityBg = pref.getInt("opacityBg", 100);
        position = pref.getInt("position", 8);

        if(offsetEndLines==9)
            offsetEndLines=0.965f;
        else if(offsetEndLines==10)
            offsetEndLines=0.995f;
        else
            offsetEndLines/=10;
    }
}
