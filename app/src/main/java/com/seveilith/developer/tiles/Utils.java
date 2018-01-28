package com.seveilith.developer.tiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class Utils {

    public static String LOG_TAG = "Developer Tiles";

    public static SharedPreferences getDefaultSharedPreference(Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    }

    public static boolean isSystemApp(Context context) {
        boolean isSystemApp = false;

        try {
            isSystemApp = ((context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return isSystemApp;
    }

    public static String getStringResource(Context context, int identifier) {
        return context.getResources().getString(identifier);
    }

    public static String getPackageSourcePath(Context context) {
        String packageSourcePath = "";

        try {
            packageSourcePath = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageSourcePath;
    }

    public static String getVersionedPackageName(String packageSourcePath) {

        // Substring the packageSourcePath in order to only show the full package name (including its package ID - hyphen and number)
        return packageSourcePath.substring(packageSourcePath.lastIndexOf("com."), packageSourcePath.indexOf("base.apk") -1);
    }
}