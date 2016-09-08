package me.yugy.app.common.network;

import java.util.List;

@SuppressWarnings("unused")
public class Validator {

    public interface Validatable {
        boolean validate();
    }

    public static boolean isValid(Validatable data) {
        return data != null && data.validate();
    }

    public static boolean isValid(Validatable[] array) {
        if (array == null) {
            return false;
        }
        for (Validatable item : array) {
            if (!isValid(item)) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Validatable> boolean isValid(List<T> list) {
        if (list == null) {
            return false;
        }
        for (T item : list) {
            if (!isValid(item)) {
                return false;
            }
        }
        return true;
    }
}
