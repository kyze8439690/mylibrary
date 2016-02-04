package me.yugy.app.common.compat;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import me.yugy.app.common.utils.VersionUtils;

@SuppressWarnings("unused")
public class ViewCompat extends android.support.v4.view.ViewCompat {

    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }

    public static boolean isHardwareAccelerated(View view) {
        //noinspection SimplifiableIfStatement
        if (VersionUtils.honeyCombOrLater()) {
            return view.isHardwareAccelerated();
        } else {
            return false;
        }
    }

}
