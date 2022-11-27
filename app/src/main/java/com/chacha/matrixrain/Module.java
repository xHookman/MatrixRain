package com.chacha.matrixrain;

import android.app.AndroidAppHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.widget.FrameLayout;
import java.io.File;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Module implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {
    static String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    static String MY_PACKAGE_NAME = "com.chacha.matrixrain";
    boolean mKeyguardShowing;
    XSharedPreferences pref;
    FrameLayout notification_panel;
    com.chacha.matrixrain.MatrixRain matrixRain;

    public void prefLoad() {
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
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        prefLoad();
    }

    public static XSharedPreferences getPref() {
        XSharedPreferences pref = new XSharedPreferences(MY_PACKAGE_NAME, "Settings");
        return pref.getFile().canRead() ? pref : null;
    }

    private XSharedPreferences getLegacyPrefs() {
        File f = new File(Environment.getDataDirectory(), "data/" + MY_PACKAGE_NAME + "/shared_prefs/Settings.xml");
        return new XSharedPreferences(f);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(MY_PACKAGE_NAME)) {
            findAndHookMethod(MY_PACKAGE_NAME + ".Utils", lpparam.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }

        prefLoad();
        pref.reload();

        if (lpparam.packageName.equals(SYSTEMUI_PACKAGE_NAME)) {
            XposedBridge.log("Hooked SystemUI !");

            try {
               findAndHookMethod(SYSTEMUI_PACKAGE_NAME + ".statusbar.phone.NotificationPanelViewController", lpparam.classLoader,
                        "onHeightUpdated", float.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                mKeyguardShowing = XposedHelpers.getBooleanField(param.thisObject, "mKeyguardShowing");
                                if (pref.hasFileChanged()) //To reload without restarting systemui
                                    refreshMatrix();
                                    matrixRain.refreshFont();

                                setMatrixAlpha(((float) param.args[0]) / ((int) callMethod(param.thisObject, "getMaxPanelHeight")));
                            }
                        });
            } catch (XposedHelpers.ClassNotFoundError e) {
                findAndHookMethod(SYSTEMUI_PACKAGE_NAME + ".statusbar.phone.NotificationPanelView", lpparam.classLoader, //Android Pie
                        "onHeightUpdated", float.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                mKeyguardShowing = XposedHelpers.getBooleanField(param.thisObject, "mKeyguardShowing");
                                if (pref.hasFileChanged()) //To reload without restarting systemui
                                    refreshMatrix();
                                    matrixRain.refreshFont();

                                setMatrixAlpha(((float) param.args[0]) / ((int) callMethod(param.thisObject, "getMaxPanelHeight")));
                            }
                        });
            }

            findAndHookMethod(SYSTEMUI_PACKAGE_NAME + ".statusbar.phone.NotificationPanelViewController", lpparam.classLoader,
                    "initPanelBackground", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            XposedBridge.log("SystemUI : initPanelBackground !!!!");
                            matrixRain = new com.chacha.matrixrain.MatrixRain(AndroidAppHelper.currentApplication(), pref.getString("rndText", "ABCDEFGHIJKLMNOPQRSTUVWSYZabcdefghijklmnopqrstuvwyz"), pref.getInt("color1", Color.GREEN), pref.getInt("color2", Color.GREEN), pref.getInt("colorBg", Color.BLACK), pref.getInt("trailSize", 10), pref.getString("fontPath", ""), pref.getInt("speed", 20), pref.getInt("size", 20), pref.getInt("columnSize", 1), pref.getInt("vertLetterSpace", 1), pref.getInt("horLetterSpace", 1), pref.getInt("offsetEndLines", 9), pref.getBoolean("isGradient", false), pref.getBoolean("isRandomColors", false), pref.getBoolean("isInvert", false), true);
                            if(notification_panel!=null)
                                setMatrixView();
                        }
                    });
        }
       /* if(lpparam.packageName == "com.sec.android.app.launcher") {
            findAndHookMethod("com.android.quickstep.views.ShelfScrimView", lpparam.classLoader,
                    "onAttachedToWindow", float.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                           matrixRain.setAlpha(1);
                           XposedBridge.log("matrixRain.setAlpha(1);\n");
                        }
                    });
            findAndHookMethod("com.android.quickstep.views.ShelfScrimView", lpparam.classLoader,
                    "onDetachedFromWindow", float.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            matrixRain.setAlpha(0);
                            XposedBridge.log("matrixRain.setAlpha(0);\n");
                        }
                    });
        }*/
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        if (resparam.packageName.equals(SYSTEMUI_PACKAGE_NAME)) {
            XposedBridge.log("OKKKKK\n");
            resparam.res.hookLayout("com.android.systemui", "layout", "status_bar_expanded", new XC_LayoutInflated() {

                            @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                                notification_panel = liparam.view.findViewById(liparam.res.getIdentifier("notification_panel", "id", SYSTEMUI_PACKAGE_NAME));
                                matrixRain = new com.chacha.matrixrain.MatrixRain(AndroidAppHelper.currentApplication(), pref.getString("rndText", "ABCDEFGHIJKLMNOPQRSTUVWSYZabcdefghijklmnopqrstuvwyz"), pref.getInt("color1", Color.GREEN), pref.getInt("color2", Color.GREEN), pref.getInt("colorBg", Color.BLACK), pref.getInt("trailSize", 10), pref.getString("fontPath", ""), pref.getInt("speed", 20), pref.getInt("size", 20), pref.getInt("columnSize", 1), pref.getInt("vertLetterSpace", 1), pref.getInt("horLetterSpace", 1), pref.getInt("offsetEndLines", 9), pref.getBoolean("isGradient", false), pref.getBoolean("isRandomColors", false), pref.getBoolean("isInvert", false), true);
                                setMatrixView();
                }
            });
        }
       /* if (resparam.packageName.equals("com.sec.android.app.launcher")) {
            resparam.res.hookLayout("com.sec.android.app.launcher", "layout", "launcher", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    XposedBridge.log("Matrix Rain : View added to recent panel !");
                    FrameLayout recent_panel = liparam.view.findViewById(liparam.res.getIdentifier("drag_layer", "id", "com.sec.android.app.launcher"));
                   // matrixRain = new com.chacha.matrixrain.MatrixRain(AndroidAppHelper.currentApplication(), pref.getString("rndText", "ABCDEFGHIJKLMNOPQRSTUVWSYZabcdefghijklmnopqrstuvwyz"), pref.getString("color1", "#ff00ff00"), pref.getString("color2", "#ff00ff00"), pref.getString("fontPath", ""), pref.getInt("speed", 20), pref.getInt("size", 20), pref.getInt("columnSize", 1), pref.getInt("vertLetterSpace", 1), pref.getInt("horLetterSpace", 1), pref.getInt("offsetEndLines", 9), pref.getBoolean("isGradient", false), pref.getBoolean("isRandomColors", false), pref.getBoolean("isInvert", false), true);
                    recent_panel.addView(matrixRain, pref.getInt("position", 8));
                }
            });
        }*/
    }

    public void setMatrixAlpha(float alpha){
        if (!mKeyguardShowing) {
            matrixRain.setAlpha((float) pref.getInt("opacityBg", 100) / 100 * alpha);
        } else {
            matrixRain.setAlpha(0); //Without this line the matrix will randomly be showed on lockscreen
        }
    }

    public void refreshMatrix(){
        prefLoad();
        notification_panel.removeView(matrixRain);
        matrixRain = new com.chacha.matrixrain.MatrixRain(AndroidAppHelper.currentApplication(), pref.getString("rndText", "ABCDEFGHIJKLMNOPQRSTUVWSYZabcdefghijklmnopqrstuvwyz"), pref.getInt("color1", Color.GREEN), pref.getInt("color2", Color.GREEN), pref.getInt("colorBg", Color.BLACK), pref.getInt("trailSize", 10), pref.getString("fontPath", ""), pref.getInt("speed", 20), pref.getInt("size", 20), pref.getInt("columnSize", 1), pref.getInt("vertLetterSpace", 1), pref.getInt("horLetterSpace", 1), pref.getInt("offsetEndLines", 9), pref.getBoolean("isGradient", false), pref.getBoolean("isRandomColors", false), pref.getBoolean("isInvert", false), true);
        setMatrixView();
    }

    public void setMatrixView() {
        try {
            notification_panel.addView(matrixRain, pref.getInt("position", 8));
        } catch (IndexOutOfBoundsException e) {
            notification_panel.addView(matrixRain, 1);
        }
    }
}