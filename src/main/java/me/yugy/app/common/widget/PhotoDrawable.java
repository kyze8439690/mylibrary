package me.yugy.app.common.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

class PhotoDrawable extends BitmapDrawable {

    private int mOriginWidth, mOriginHeight;

    public PhotoDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public void setOriginSize(int width, int height) {
        mOriginWidth = width;
        mOriginHeight = height;
    }

    public int getOriginWidth() {
        return mOriginWidth;
    }

    public int getOriginHeight() {
        return mOriginHeight;
    }
}
