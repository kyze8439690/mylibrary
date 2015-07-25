package me.yugy.app.common.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SelectorImageView
        extends ImageView {

    private Drawable mSelectorDrawable;

    public SelectorImageView(Context context) {
        this(context, null);
    }

    public SelectorImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectorImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSelectorDrawable = context.getResources().getDrawable(android.R.drawable.list_selector_background, context.getTheme());
        } else {
            //noinspection deprecation
            mSelectorDrawable = context.getResources().getDrawable(android.R.drawable.list_selector_background);
        }
        if (mSelectorDrawable != null) {
            mSelectorDrawable.setCallback(this);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mSelectorDrawable.setState(getDrawableState());
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        mSelectorDrawable.jumpToCurrentState();
    }

    @Override
    protected boolean verifyDrawable(Drawable dr) {
        return dr == mSelectorDrawable || super.verifyDrawable(dr);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable dr) {
        if (dr == mSelectorDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mSelectorDrawable != null) {
            mSelectorDrawable.setHotspot(x, y);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSelectorDrawable.setBounds(0, 0, w, h);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        mSelectorDrawable.draw(canvas);
    }

    public void setSelectorDrawable(Drawable selectorDrawable) {
        mSelectorDrawable = selectorDrawable;
        if (mSelectorDrawable != null) {
            mSelectorDrawable.setBounds(0, 0, getWidth(), getHeight());
            mSelectorDrawable.setCallback(this);
        }
    }

    public void setSelectorDrawable(@DrawableRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSelectorDrawable(getResources().getDrawable(resId, getContext().getTheme()));
        } else {
            //noinspection deprecation
            setSelectorDrawable(getResources().getDrawable(resId));
        }
    }

}