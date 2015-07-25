package me.yugy.app.common.compat;

import android.os.Build;
import android.support.annotation.Nullable;
import android.view.ViewTreeObserver;

public class ViewTreeObserverCompat {

    public static void removeOnGlobalLayoutListener(ViewTreeObserver observer, @Nullable ViewTreeObserver.OnGlobalLayoutListener victim) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            observer.removeGlobalOnLayoutListener(victim);
        } else {
            observer.removeOnGlobalLayoutListener(victim);
        }
    }

}
