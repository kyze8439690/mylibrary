package me.yugy.app.common.utils;

import android.view.View;
import android.view.ViewGroup;

@SuppressWarnings("unused")
public class ViewUtils {

    public static void dispatchStartTemporaryDetach(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            final int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = viewGroup.getChildAt(i);
                dispatchStartTemporaryDetach(child);
            }
        } else {
            view.onStartTemporaryDetach();
        }
    }

    public static void dispatchFinishTemporaryDetach(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            final int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = viewGroup.getChildAt(i);
                dispatchFinishTemporaryDetach(child);
            }
        } else {
            view.onFinishTemporaryDetach();
        }
    }
}
