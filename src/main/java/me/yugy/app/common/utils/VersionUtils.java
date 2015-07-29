package me.yugy.app.common.utils;

import android.os.Build;

/**
 * Created by yugy on 2015/7/29.
 */
public class VersionUtils {

    public static boolean lollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean kitKatOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean JellyBeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean gingerbreadOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }
}
