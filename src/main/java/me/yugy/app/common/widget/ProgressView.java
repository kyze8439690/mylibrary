package me.yugy.app.common.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.yugy.app.common.R;

public class ProgressView extends View {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Orientation.HORIZONTAL, Orientation.VERTICAL})
    public @interface Orientation {
        int HORIZONTAL = 0;
        int VERTICAL = 1;
    }

    @Orientation private int mOrientation = Orientation.HORIZONTAL;
    private float mProgress = 0.0f;
    private Paint mPaint;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        final int orientation = a.getInt(R.styleable.ProgressView_pgv_orientation, Orientation.HORIZONTAL);
        final int color = a.getColor(R.styleable.ProgressView_pgv_color, Color.BLUE);
        a.recycle();

        //noinspection WrongConstant
        setOrientation(orientation);
        setColor(color);
    }

    public void setOrientation(@Orientation int orientation) {
        mOrientation = orientation;
        invalidate();
    }

    public void setProgress(@FloatRange(from = 0.0f, to = 1.0f) float progress) {
        mProgress = progress;
        invalidate();
    }

    public void setColor(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mOrientation) {
            case Orientation.HORIZONTAL:
                canvas.drawRect(0, 0, mProgress * getWidth(), getHeight(), mPaint);
                break;
            case Orientation.VERTICAL:
                canvas.drawRect(0, (1 - mProgress) * getHeight(), getWidth(), getHeight(), mPaint);
                break;
        }
    }
}
