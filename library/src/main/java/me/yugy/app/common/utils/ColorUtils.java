package me.yugy.app.common.utils;

import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

@SuppressWarnings("unused")
public class ColorUtils {

    public static int getProgressiveColor(@ColorInt int startColor, @ColorInt int endColor,
                                          @FloatRange(from=0.0, to=1.0) float progress) {
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        return (startA + (int)(progress * (endA - startA))) << 24 |
                (startR + (int)(progress * (endR - startR))) << 16 |
                (startG + (int)(progress * (endG - startG))) << 8 |
                (startB + (int)(progress * (endB - startB)));
    }

    public static int changeAlpha(@IntRange(from=0, to=255) int alpha, @ColorInt int color) {
        return  (color & 0xffffff) + (alpha << 24);
    }

}
