package me.yugy.app.common.utils;

@SuppressWarnings("unused")
public class MathUtils {

    public static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, min), max);
    }

}
