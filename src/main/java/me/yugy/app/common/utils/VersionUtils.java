package me.yugy.app.common.utils;

import android.os.Build;

@SuppressWarnings("unused")
public class VersionUtils {

    /**
     * Return {@code true} if api level >= 9.
     */
    public static boolean gingerbreadOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Return {@code true} if api level >= 11.
     */
    public static boolean honeyCombOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Return {@code true} if api level >= 14.
     */
    public static boolean iceCreamOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * Return {@code true} if api level >= 16.
     */
    public static boolean jellyBeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Return {@code true} if api level >= 19.
     */
    public static boolean kitKatOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * Return {@code true} if api level >= 21.
     */
    public static boolean lollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Return {@code true} if api level >= 23.
     */
    public static boolean marshMallowOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Return {@code true} if api level >= 24.
     */
    public static boolean nougatOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}
