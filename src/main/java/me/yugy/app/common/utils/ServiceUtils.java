package me.yugy.app.common.utils;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;

public class ServiceUtils {

    public static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceInfo.service.getClassName().equals(serviceClass.getName())) {
                return true;
            }
        }
        return false;
    }
}
