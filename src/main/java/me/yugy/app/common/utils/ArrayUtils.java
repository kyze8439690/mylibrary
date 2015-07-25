package me.yugy.app.common.utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;

/**
 * Created by yugy on 2014/4/22.
 */
public class ArrayUtils {

    public static String[] StringJsonArray2StringArray(JSONArray jsonArray) throws JSONException {
        String[] strings = new String[jsonArray.length()];
        int jsonSize = jsonArray.length();
        for(int i = 0; i < jsonSize; i++){
            strings[i] = jsonArray.getString(i);
        }
        return strings;
    }

    public static String[] getWeiboPicArray(JSONArray jsonArray) throws JSONException {
        String[] picArray = new String[jsonArray.length()];
        int jsonSize = picArray.length;
        for(int i = 0; i < jsonSize; i++){
            picArray[i] = jsonArray.getJSONObject(i).getString("thumbnail_pic");
        }
        return picArray;
    }

    public static final String strSeparator = "__,__";

    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0; i < array.length; i++) {
            str = str + array[i];
            // Do not append comma at the end of last element
            if(i < array.length - 1){
                str = str + strSeparator;
            }
        }
        return str;
    }
    public static String[] convertStringToArray(String str){
        if(str.equals("")){
            return new String[0];
        }else{
            return str.split(strSeparator);
        }
    }

    public static <T> T[] concatenate (T[] A, T[] B) {
        int aLen = A.length;
        int bLen = B.length;

        @SuppressWarnings("unchecked")
        T[] C = (T[]) Array.newInstance(A.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);

        return C;
    }

}
