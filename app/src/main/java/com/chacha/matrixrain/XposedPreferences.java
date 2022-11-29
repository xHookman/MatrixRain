package com.chacha.matrixrain;

import static com.chacha.matrixrain.Utils.MY_PACKAGE_NAME;

import android.graphics.Color;
import android.os.Environment;
import java.io.File;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class XposedPreferences extends Preferences {
    private XSharedPreferences pref;

    private XSharedPreferences getPref() {
        XSharedPreferences pref = new XSharedPreferences(MY_PACKAGE_NAME, "Settings");
        return pref.getFile().canRead() ? pref : null;
    }

    private XSharedPreferences getLegacyPrefs() {
        File f = new File(Environment.getDataDirectory(), "data/" + MY_PACKAGE_NAME + "/shared_prefs/Settings.xml");
        return new XSharedPreferences(f);
    }

    public XSharedPreferences loadPreferences() {
        if (XposedBridge.getXposedVersion() < 93) {
            pref = getLegacyPrefs();
        } else {
            pref = getPref();
        }

        if (pref != null) {
            pref.reload();
        } else {
            XposedBridge.log("Can't load preference in the module");
        }

        return pref;
    }

    public void loadMatrixRainPrefs(){
        choosedColor1 = pref.getInt("color1", Color.GREEN);
        choosedColor2 = pref.getInt("color2", Color.GREEN);
        choosedColorBg = pref.getInt("colorBg", Color.BLACK);
        trailSize = pref.getInt("trailSize", 10);
        text = pref.getString("rndText", "ABCDEFGHIJKLMNOPQRSTUVWSYZabcdefghijklmnopqrstuvwyz").toCharArray();
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

    public boolean hasPrefsChanged(){
        return pref.hasFileChanged();
    }
    public void reloadPrefs(){
        pref.reload();
    }
}
