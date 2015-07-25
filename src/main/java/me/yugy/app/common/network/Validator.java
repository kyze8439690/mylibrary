package me.yugy.app.common.network;

public class Validator {

    public static boolean isValid(Validate data) {
        return data != null && data.checkValidate();
    }

    public interface Validate {
        boolean checkValidate();
    }
}
