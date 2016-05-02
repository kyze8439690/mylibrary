package me.yugy.app.common.utils;

@SuppressWarnings("unused")
public class MathUtils {

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

}
