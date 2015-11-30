package me.yugy.app.common.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class MeizuUtils {

    public static void hideSmartBar(Activity activity) {
        if (!hasSmartBar())
            return;

        try {
            @SuppressWarnings("rawtypes")
            Class[] arrayOfClass = new Class[1];
            arrayOfClass[0] = Integer.TYPE;
            Method localMethod = View.class.getMethod("setSystemUiVisibility",
                    arrayOfClass);
            Field localField = View.class
                    .getField("SYSTEM_UI_FLAG_HIDE_NAVIGATION");
            Object[] arrayOfObject = new Object[1];
            try {
                arrayOfObject[0] = localField.get(null);
            } catch (Exception ignored) {

            }
            localMethod.invoke(activity.getWindow().getDecorView(), arrayOfObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void transparentStatusBar(Activity activity){
        WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
        Class<?> mzLpClass = localLayoutParams.getClass();
        Field[] fields = mzLpClass.getFields();
        //noinspection TryWithIdenticalCatches
        try {
            Field meizuFlagsField = mzLpClass.getField("meizuFlags");
            int meizuFlags = (Integer) meizuFlagsField.get(localLayoutParams);
            //default meizuFlags is 1.
            meizuFlags |= 0x40;
            meizuFlagsField.set(localLayoutParams, meizuFlags);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (IllegalArgumentException ignored) {
        }
        activity.getWindow().setAttributes(localLayoutParams);
    }

    public static boolean hasSmartBar() {
        try {
            Method method = Class.forName("android.os.Build").getMethod(
                    "hasSmartBar");
            return (Boolean) method.invoke(null);
        } catch (Exception ignored) {
        }

        if (Build.DEVICE.equals("mx2")) {
            return true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            return false;
        }
        return false;
    }

    public static boolean isMeizu(){
        return Build.MANUFACTURER.equalsIgnoreCase("meizu");
    }

}
