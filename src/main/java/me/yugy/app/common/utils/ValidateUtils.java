package me.yugy.app.common.utils;

import java.util.List;

import me.yugy.app.common.network.Validator;

@SuppressWarnings("unused")
public class ValidateUtils {

    public static boolean check(Validator.Validate data) {
        return data != null && data.checkValidate();
    }

    public static boolean check(Validator.Validate[] array) {
        if (array == null) {
            return false;
        }
        for (Validator.Validate item : array) {
            if (!check(item)) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Validator.Validate> boolean check(List<T> list) {
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
