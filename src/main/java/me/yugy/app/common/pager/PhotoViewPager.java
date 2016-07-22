package me.yugy.app.common.pager;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * View pager for photo view fragments. Define our own class so we can specify the
 * view pager in XML.
 */
public class PhotoViewPager extends ViewPager {
    /**
     * A type of intercept that should be performed
     */
    public static final int INTERCEPT_TYPE_NONE = 0;
    public static final int INTERCEPT_TYPE_LEFT = 1;
    public static final int INTERCEPT_TYPE_RIGHT = 2;
    public static final int INTERCEPT_TYPE_BOTH = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INTERCEPT_TYPE_NONE, INTERCEPT_TYPE_LEFT, INTERCEPT_TYPE_RIGHT, INTERCEPT_TYPE_BOTH})
    public @interface InterceptType {}

    /**
     * Provides an ability to intercept touch events.
     * <p>
     * {@link ViewPager} intercepts all touch events and we need to be able to override this
     * behavior. Instead, we could perform a similar function by declaring a custom
     * {@link android.view.ViewGroup} to contain the pager and intercept touch events at a higher
     * level.
     */
    public interface OnInterceptTouchListener {
        /**
         * Called when a touch intercept is about to occur.
         *
         * @param origX the raw x coordinate of the initial touch
         * @param origY the raw y coordinate of the initial touch
         * @return Which type of touch, if any, should should be intercepted.
         */
        @InterceptType
        int onTouchIntercept(float origX, float origY);
    }

    private static final int INVALID_POINTER = -1;

    private float mLastMotionX;
    private int mActivePointerId;
    /** The x coordinate where the touch originated */
    private float mActivatedX;
    /** The y coordinate where the touch originated */
    private float mActivatedY;
    private OnInterceptTouchListener mListener;

    public PhotoViewPager(Context context) {
        this(context, null);
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPageTransformer(true, new DeepPageTransformer());
    }

    /**
     * {@inheritDoc}
     * <p>
     * We intercept touch event intercepts so we can prevent switching views when the
     * current view is internally scrollable.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int intercept = (mListener != null)
                ? mListener.onTouchIntercept(mActivatedX, mActivatedY)
                : INTERCEPT_TYPE_NONE;
        final boolean ignoreScrollLeft =
                (intercept == INTERCEPT_TYPE_BOTH || intercept == INTERCEPT_TYPE_LEFT);
        final boolean ignoreScrollRight =
                (intercept == INTERCEPT_TYPE_BOTH || intercept == INTERCEPT_TYPE_RIGHT);

        // Only check ability to page if we can't scroll in one / both directions
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mActivePointerId = INVALID_POINTER;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (ignoreScrollLeft || ignoreScrollRight) {
                    final int activePointerId = mActivePointerId;
                    if (activePointerId == INVALID_POINTER) {
                        // If we don't have a valid id, the touch down wasn't on content.
                        break;
                    }

                    final int pointerIndex =
                            MotionEventCompat.findPointerIndex(ev, activePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);

                    if (ignoreScrollLeft && ignoreScrollRight) {
                        mLastMotionX = x;
                        return false;
                    } else if (ignoreScrollLeft && (x > mLastMotionX)) {
                        mLastMotionX = x;
                        return false;
                    } else if (ignoreScrollRight && (x < mLastMotionX)) {
                        mLastMotionX = x;
                        return false;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                // Use the raw x/y as the children can be located anywhere and there isn't a
                // single offset that would be meaningful
                mActivatedX = ev.getRawX();
                mActivatedY = ev.getRawY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == mActivePointerId) {
                    // Our active pointer going up; select a new active pointer
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * sets the intercept touch listener.
     */
    public void setOnInterceptTouchListener(OnInterceptTouchListener l) {
        mListener = l;
    }
}
