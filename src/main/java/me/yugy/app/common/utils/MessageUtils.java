package me.yugy.app.common.utils;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.ViewGroup;
import android.widget.Toast;

@SuppressWarnings("unused")
public class MessageUtils {

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toastOnNonUIThread(Context context, String text) {
        Looper.prepare();
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        Looper.loop();
        Looper.myLooper().quit();
    }

    public static void snack(@Nullable ViewGroup container, String text) {
        if (container == null) {
            return;
        }
        Snackbar.make(container, text, Snackbar.LENGTH_SHORT).show();
    }
}
