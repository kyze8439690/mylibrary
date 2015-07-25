package me.yugy.app.common.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by yugy on 14/11/19.
 */
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
            } catch (Exception e) {

            }
            localMethod.invoke(activity.getWindow().getDecorView(), arrayOfObject);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void transparentStatusBar(Activity activity){
        WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
        Class<?> mzLpClass = localLayoutParams.getClass();
        Field[] fields = mzLpClass.getFields();
        try {
            Field meizuFlagsField = mzLpClass.getField("meizuFlags");
            int meizuFlags = (Integer) meizuFlagsField.get(localLayoutParams);
            //default meizuFlags is 1.
            meizuFlags |= 0x40;
            meizuFlagsField.set(localLayoutParams, meizuFlags);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        }
        activity.getWindow().setAttributes(localLayoutParams);
    }

    public static boolean hasSmartBar() {
        try {
            Method method = Class.forName("android.os.Build").getMethod(
                    "hasSmartBar");
            return ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {
        }

        if (Build.DEVICE.equals("mx2")) {
            return true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            return false;
        }
        return false;
    }

    public static final boolean isMeizu(){
        return Build.MANUFACTURER.equalsIgnoreCase("meizu");
    }

}
