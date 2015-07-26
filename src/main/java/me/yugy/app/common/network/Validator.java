package me.yugy.app.common.network;

public class Validator {

    public static boolean isValid(Validate data) {
        return data != null && data.checkValidate();
    }

    public static boolean isValid(Validate[] data) {
        if (data == null) {
            return false;
        }
        for (Validate item : data) {
            if (!isValid(item)) {
                return false;
            }
        }
        return true;
    }

    public interface Validate {
        boolean checkValidate();
    }
}
