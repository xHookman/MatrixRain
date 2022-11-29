package com.chacha.matrixrain;

import static com.chacha.matrixrain.Utils.MY_PACKAGE_NAME;

import android.app.AndroidAppHelper;
import android.widget.FrameLayout;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Module implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {
    static String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    boolean mKeyguardShowing;
    XposedPreferences preferences;
    FrameLayout notification_panel;
    com.chacha.matrixrain.MatrixRain matrixRain;

    @Override
    public void initZygote(StartupParam startupParam) {
        preferences = new XposedPreferences();
        preferences.loadPreferences();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(MY_PACKAGE_NAME)) {
            findAndHookMethod(MY_PACKAGE_NAME + ".Utils", lpparam.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }

        preferences.loadPreferences();
        preferences.reloadPrefs();
        preferences.loadMatrixRainPrefs();

        if (lpparam.packageName.equals(SYSTEMUI_PACKAGE_NAME)) {
            XposedBridge.log("Hooked SystemUI !");

            try {
               findAndHookMethod(SYSTEMUI_PACKAGE_NAME + ".statusbar.phone.NotificationPanelViewController", lpparam.classLoader,
                        "onHeightUpdated", float.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                mKeyguardShowing = XposedHelpers.getBooleanField(param.thisObject, "mKeyguardShowing");
                                refreshMatrixIfNeeded();
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
                                refreshMatrixIfNeeded();
                                setMatrixAlpha(((float) param.args[0]) / ((int) callMethod(param.thisObject, "getMaxPanelHeight")));
                            }
                        });
            }
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
                                matrixRain = new com.chacha.matrixrain.MatrixRain(AndroidAppHelper.currentApplication(), preferences);
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

    private void setMatrixAlpha(float alpha){
        if (!mKeyguardShowing) {
            matrixRain.setAlpha((float) preferences.opacityBg / 100 * alpha);
        } else {
            matrixRain.setAlpha(0); //Without this line the matrix will randomly be showed on lockscreen
        }
    }

    private void refreshMatrix(){
        preferences.loadPreferences();
        preferences.loadMatrixRainPrefs();
        notification_panel.removeView(matrixRain);
        matrixRain = new com.chacha.matrixrain.MatrixRain(AndroidAppHelper.currentApplication(), preferences);
        setMatrixView();
    }

    private void setMatrixView() {
        matrixRain.refreshFont();
        try {
            notification_panel.addView(matrixRain, preferences.position);
        } catch (IndexOutOfBoundsException e) {
            notification_panel.addView(matrixRain, 1);
        }
    }

    private void refreshMatrixIfNeeded(){
        if(preferences.hasPrefsChanged()){
            refreshMatrix();
        }
        matrixRain.refreshFont();
    }
}