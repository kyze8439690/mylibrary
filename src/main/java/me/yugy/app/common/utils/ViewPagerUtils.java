package me.yugy.app.common.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class ViewPagerUtils {

    @Nullable
    public static Fragment getFragmentFromPager(
            @NonNull FragmentManager fm, @NonNull ViewPager pager, int index) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager doesn't have a adapter.");
        }
        if (!(pager.getAdapter() instanceof FragmentPagerAdapter)) {
            throw new IllegalStateException("Error adapter type, should be FragmentPagerAdapter.");
        }
        return fm.findFragmentByTag(makeFragmentName(pager.getId(), index));
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
