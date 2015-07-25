package me.yugy.app.common.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Patterns;

import me.yugy.app.common.R;

public class TextUtils {

    public static CharSequence getRelativeTimeDisplayString(Context context, long referenceTime) {
        long now = System.currentTimeMillis();
        long difference = now - referenceTime;
        return (difference >= 0 &&  difference <= DateUtils.MINUTE_IN_MILLIS) ?
                context.getResources().getString(R.string.just_now):
                DateUtils.getRelativeTimeSpanString(
                        referenceTime,
                        now,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
    }

    public static boolean isGifLink(String url){
        return url.endsWith(".gif");
    }

    public static long calculateTextLength(CharSequence charSequence){
        double len = 0;
        for (int i = 0; i < charSequence.length(); i++) {
            int tmp = (int) charSequence.charAt(i);
            if (tmp > 0 && tmp < 127) {
                len += 0.5;
            } else {
                len++;
            }
        }
        return Math.round(len);
    }

    public static boolean isUrl(String urlString){
        return Patterns.WEB_URL.matcher(urlString).find();
    }

    private static String strSeparator = "__,__";

    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            // Do not append comma at the end of last element
            if(i<array.length-1){
                str = str+strSeparator;
            }
        }
        return str;
    }
    public static String[] convertStringToArray(String str){
        return str.split(strSeparator);
    }

}
