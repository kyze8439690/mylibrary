package me.yugy.app.common.widget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.yugy.app.common.R;
import me.yugy.app.common.utils.PermissionUtil;

public class ScreenShotLayout extends FrameLayout {

    private static final String TAG = ScreenShotLayout.class.getSimpleName();
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static final int TRIGGER_DISTANCE = 150;    //dp
    private static final int HOVER_COLOR = 0xDD000000;
    private static final int RING_COLOR = 0xDDFFFFFF;
    private static final int SCREENSHOT_COLOR = 0xCCFFFFFF;

    public static ScreenShotLayout attachToActivity(Activity activity) {
        return attachToActivity(activity, null);
    }

    public static ScreenShotLayout attachToActivity(Activity activity, @Nullable OnAttachListener listener) {
        ScreenShotLayout layout = new ScreenShotLayout(activity);
        layout.attach(activity, listener);
        return layout;
    }

    private static final int INVALID = -1;
    private float mInitialDownY = INVALID;
    private int mActivePointIndex = INVALID;
    private int mTouchSlop;
    private int mTriggerDistance;
    private boolean mIsDragging = false;
    private float mProgress = 0f;

    private Paint mRingPaint;
    private RectF mRingRectF;

    private Bitmap mScreenShotBitmap;

    private boolean mIsInScreenShotAnimation = false;
    private boolean mIsInScreenShot = false;
    private int mScreenShotColor;
    @Nullable private OnAttachListener mOnAttachListener;

    public ScreenShotLayout(Context context) {
        this(context, null);
    }

    public ScreenShotLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScreenShotLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        float density = getResources().getDisplayMetrics().density;
        mTriggerDistance = (int) (TRIGGER_DISTANCE * density + 0.5f);

        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setColor(RING_COLOR);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeCap(Paint.Cap.ROUND);
        mRingPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.ring_stroke_size));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.layout(l, t, l + child.getMeasuredWidth(), t + child.getMeasuredHeight());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int ringSize = getResources().getDimensionPixelSize(R.dimen.ring_size);
        int left = (w - ringSize) / 2;
        int top = (h - ringSize) / 2;
        mRingRectF = new RectF(left, top, left + ringSize, top + ringSize);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mIsInScreenShot) return;
        if (mIsInScreenShotAnimation) {
            canvas.drawColor(mScreenShotColor);
        } else {
            int alpha = Color.alpha(HOVER_COLOR);
            int red = Color.red(HOVER_COLOR);
            int green = Color.green(HOVER_COLOR);
            int blue = Color.blue(HOVER_COLOR);
            int hoverColor = Color.argb((int) (alpha * mProgress), red, green, blue);
//        Log.d(TAG, Integer.toHexString(hoverColor));
            canvas.drawColor(hoverColor);
            canvas.drawArc(mRingRectF, 0, 360 * mProgress, false, mRingPaint);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent ev) {
        if (mIsInScreenShotAnimation) {
            return super.onInterceptTouchEvent(ev);
        }
//        Log.d(TAG, MotionEvent.actionToString(ev.getActionMasked()));
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                int count = ev.getPointerCount();
                if (count == 3) {
                    mActivePointIndex = ev.getActionIndex();
                    mInitialDownY = ev.getY(mActivePointIndex);
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (mInitialDownY != INVALID && mActivePointIndex != INVALID) {
                    if (mIsDragging) {
                        float y = ev.getY(mActivePointIndex);
                        mProgress = (y - mInitialDownY) / mTriggerDistance;
                        if (mProgress > 1f) mProgress = 1f;
                        if (mProgress < 0f) mProgress = 0f;
                        dispatchProgress();
                    } else {
                        float y = ev.getY(mActivePointIndex);
                        if (y - mInitialDownY >= mTouchSlop) {
                            mIsDragging = true;
                            return true;
                        }
                    }
                    return true;
                }
                return false;
            case MotionEvent.ACTION_POINTER_UP:
                if (ev.getActionIndex() == 2) {
                    mIsDragging = false;
                    mInitialDownY = INVALID;
                    mActivePointIndex = INVALID;
                    mProgress = 0f;
                    dispatchProgress();
                }
                return true;
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                mInitialDownY = INVALID;
                mActivePointIndex = INVALID;
                mProgress = 0f;
                dispatchProgress();
                return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (mIsInScreenShotAnimation) {
            return super.onTouchEvent(event);
        }
//        Log.d(TAG, MotionEvent.actionToString(event.getActionMasked()));
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (mInitialDownY != INVALID && mActivePointIndex != INVALID) {
                    if (mIsDragging) {
                        float y = event.getY(event.getActionIndex());
                        mProgress = (y - mInitialDownY) / mTriggerDistance;
                        if (mProgress > 1f) mProgress = 1f;
                        if (mProgress < 0f) mProgress = 0f;
                        dispatchProgress();
                    } else {
                        float y = event.getY(event.getActionIndex());
                        if (y - mInitialDownY >= mTouchSlop) {
                            mIsDragging = true;
                            return true;
                        }
                    }
                    return true;
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                mInitialDownY = INVALID;
                mActivePointIndex = INVALID;

                if (mProgress == 1f) {
                    dispatchFire();
                }

                mProgress = 0f;
                dispatchProgress();
                return false;
        }
        return super.onTouchEvent(event);
    }

    private void dispatchProgress() {
        invalidate();
    }

    private void dispatchFire() {
        mIsInScreenShotAnimation = true;

        makeScreenShot();

        if (mScreenShotBitmap == null) {
            Toast.makeText(getContext(), "screenshot failed", Toast.LENGTH_SHORT).show();
            mIsInScreenShotAnimation = false;
        }

        Animation flashAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, @NonNull Transformation t) {
                int alpha = Color.alpha(SCREENSHOT_COLOR);
                int red = Color.red(SCREENSHOT_COLOR);
                int green = Color.green(SCREENSHOT_COLOR);
                int blue = Color.blue(SCREENSHOT_COLOR);
                mScreenShotColor =
                        Color.argb((int) (alpha * (1 - interpolatedTime)), red, green, blue);
                invalidate();
            }
        };
        flashAnimation.setDuration(200);
        flashAnimation.setInterpolator(new LinearInterpolator());
        flashAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                boolean allowWriteExternal = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (allowWriteExternal) {
                    sendEmail();
                } else {
                    PermissionUtil.request((Activity) getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            new PermissionUtil.SimplePermissionCallback() {
                        @Override
                        public void onSuccess() {
                            sendEmail();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(getContext(), "截图失败, 无法保存图片到本地", Toast.LENGTH_SHORT).show();
                            mIsInScreenShotAnimation = false;
                        }
                    }, "保存截图需要获取sdcard权限", "授权", "取消");
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(flashAnimation);
    }

    private void sendEmail() {
        final String title = getContext().getPackageName() + "_" + DATE_FORMAT.format(new Date());
        String screenshotSavePath = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), mScreenShotBitmap, title, "screenshot");
        if (screenshotSavePath == null) {
            Toast.makeText(
                    getContext(), "save screenshot failed.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, screenshotSavePath);

            //noinspection StringBufferReplaceableByString
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder
                    .append("From Context: ").append(getContext().toString()).append("\n")
                    .append("Device: ").append(Build.DEVICE).append("\n")
                    .append("Brand: ").append(Build.BRAND).append("\n")
                    .append("Manufacturer: ").append(Build.MANUFACTURER).append("\n")
                    .append("Api Level: ").append(Build.VERSION.SDK_INT).append("\n");
            if (mOnAttachListener != null) {
                contentBuilder.append(mOnAttachListener.onAttach(getContext())).append("\n");
            }
            contentBuilder.append("\n")
                    .append("Question Description: ")
                    .append("\n");


            ShareCompat.IntentBuilder.from((Activity) getContext())
                    .setChooserTitle("发送反馈邮件")
                    .setEmailTo(new String[]{"me@yanghui.name"})
                    .setText(contentBuilder.toString())
                    .addStream(Uri.parse(screenshotSavePath))
                    .setType("message/rfc822")
                    .setSubject(title).startChooser();
        }
        mIsInScreenShotAnimation = false;
    }

    private void attach(Activity activity, @Nullable OnAttachListener listener) {
        if (getParent() != null) {
            throw new IllegalStateException("This layout has been added into a ViewGroup.");
        }

        mOnAttachListener = listener;

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);

        decor.removeView(decorChild);
        addView(decorChild);
        decor.addView(this);
    }

    private void makeScreenShot() {
        mIsInScreenShot = true;
        Window window = ((Activity) getContext()).getWindow();
        Drawable background = window.getDecorView().getBackground();
        mScreenShotBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mScreenShotBitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
        Canvas canvas = new Canvas(mScreenShotBitmap);
        canvas.translate(-getScrollX(), -getScrollY());
        background.draw(canvas);
        draw(canvas);
        mIsInScreenShot = false;
    }

    private boolean checkPermission(String permission)
    {
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public interface OnAttachListener {
        String onAttach(Context context);
    }

}