package me.yugy.app.common.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by yugy on 14-1-6.
 */
public class MessageUtils {

    public static void toast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toastOnNonUIThread(Context context, String text){
        Looper.prepare();
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        Looper.loop();
        Looper.myLooper().quit();
    }
}
