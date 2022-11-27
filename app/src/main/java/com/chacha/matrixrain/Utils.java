package com.chacha.matrixrain;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import java.io.DataOutputStream;

import eu.chainfire.libsuperuser.Shell;

public class Utils {
    private static boolean isModuleActive(){
        return false;
    }

    protected static void checkXposed(Context context) {
        if (!isModuleActive()) {
            AlertDialog alertDialog = new AlertDialog.Builder(context, 4).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Make sure you have Xposed installed or that you enable this module. Root is not obliged but is usefull to automatically restart SystemUI after changes.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "EXIT",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        }
    }

    protected static void unrooted(Context context) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, 4).create();
        alertDialog.setTitle("Pleeeeaseee read !");
        alertDialog.setMessage("You must grant root permission to kill SystemUI to update the icon ;)");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok I understand :)",
                (dialog, which) -> alertDialog.cancel());
        alertDialog.show();
    }

    protected static void killSystemUi(Context context) {
        if (Shell.SU.available()) {
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(su.getOutputStream());
                os.writeBytes("adb shell" + "\n");
                os.flush();
                os.writeBytes("killall com.android.systemui" + "\n");
                os.flush();
                /*os.writeBytes("adb shell" + "\n");
                os.flush();
                os.writeBytes("killall com.sec.android.app.launcher" + "\n");
                os.flush();*/

            } catch (Exception e) {
                Toast.makeText(context, "Root not granted !", Toast.LENGTH_SHORT).show();
            }
        } else {
            unrooted(context);
        }
    }
}
