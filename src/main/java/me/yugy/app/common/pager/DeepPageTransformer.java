package me.yugy.app.common.pager;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

public class DeepPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        // The >= 1 is needed so that the page
        // (page A) that transitions behind the newly visible
        // page (page B) that comes in from the left does not
        // get the touch events because it is still on screen
        // (page A is still technically on screen despite being
        // invisible). This makes sure that when the transition
        // has completely finished, we revert it to its default
        // behavior and move it off of the screen.
        if (position < 0 || position >= 1.f) {
            ViewCompat.setTranslationX(page, 0);
            ViewCompat.setAlpha(page, 1.f);
            ViewCompat.setScaleX(page, 1);
            ViewCompat.setScaleY(page, 1);
        } else {
            ViewCompat.setTranslationX(page, -position * page.getWidth());
            ViewCompat.setAlpha(page, Math.max(0,1.f - position));
            final float scale = Math.max(0, 1.f - position * 0.3f);
            ViewCompat.setScaleX(page, scale);
            ViewCompat.setScaleY(page, scale);
        }
    }
}
