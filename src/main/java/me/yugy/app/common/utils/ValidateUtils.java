package me.yugy.app.common.utils;

import java.util.List;

import me.yugy.app.common.network.Validator;

@SuppressWarnings("unused")
public class ValidateUtils {

    public static boolean check(Validator.Validatable data) {
        return data != null && data.validate();
    }

    public static boolean check(Validator.Validatable[] array) {
        if (array == null) {
            return false;
        }
        for (Validator.Validatable item : array) {
            if (!check(item)) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Validator.Validatable> boolean check(List<T> list) {
        if (list == null) {
            return false;
        }
        for (T item : list) {
            if (!check(item)) {
                return false;
            }
        }
        return true;
    }
}
