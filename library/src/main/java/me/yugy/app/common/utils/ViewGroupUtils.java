package me.yugy.app.common.utils;

import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.yugy.app.common.compat.ViewCompat;

@SuppressWarnings("unused")
public class ViewGroupUtils {

    public static void offsetChildrenTopAndBottom(ViewGroup viewGroup, int offset) {
        if (!offsetChildrenTopAndBottomReflect(viewGroup, offset)) {
            final int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = viewGroup.getChildAt(i);
                ViewCompat.offsetTopAndBottom(child, offset);
            }
        }
    }

    public static void offsetChildrenLeftAndRight(ViewGroup viewGroup, int offset) {
        if (!offsetChildrenLeftAndRightReflect(viewGroup, offset)) {
            final int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = viewGroup.getChildAt(i);
                ViewCompat.offsetLeftAndRight(child, offset);
            }
        }
    }

    private static boolean offsetChildrenTopAndBottomReflect(ViewGroup viewGroup, int offset) {
        try {
            Method method = ViewGroup.class.getMethod("offsetChildrenTopAndBottom", int.class);
            method.invoke(viewGroup, offset);
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean offsetChildrenLeftAndRightReflect(ViewGroup viewGroup, int offset) {
        try {
            Method method = ViewGroup.class.getMethod("offsetChildrenLeftAndRight", int.class);
            method.invoke(viewGroup, offset);
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

}
