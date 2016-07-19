package me.yugy.app.common.pager;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import me.yugy.app.common.R;
import me.yugy.app.common.widget.ScaleInfo;

public abstract class PhotoViewPagerActivity<T extends Photo> extends AppCompatActivity
        implements PhotoViewPager.OnInterceptTouchListener, ViewPager.OnPageChangeListener,
        PhotoViewCallbacks<T> {

    private static final String SAVE_INSTANCE_FULLSCREEN = "fullscreen";
    private static final String SAVE_INSTANCE_ENTER_ANIMATION_FINISHED = "enter_animation_finished";
    public static final String INTENT_PARAM_GALLERY = "gallery";
    public static final String INTENT_PARAM_FRAGMENT_CLASS = "fragment_class";
    public static final String INTENT_PARAM_SCALE_INFO = "scale_info";

    public static final long ANIMATION_DURATION = 400;

    private View mRoot;
    private View mBackground;
    private PhotoViewPager mPager;
    private ImageView mTempImage;

    private ImagePagerAdapter mAdapter;

    private boolean mFullScreen;
    private final Map<Integer, OnScreenListener> mScreenListeners = new HashMap<>();

    private int mLastFlags;

    private boolean mEnterAnimationFinished;

    @Nullable
    private ScaleInfo mScaleInfo;
    private Gallery mGallery;
    private Class mFragmentClass;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        mScaleInfo = getIntent().getParcelableExtra(INTENT_PARAM_SCALE_INFO);
        mGallery = getIntent().getParcelableExtra(INTENT_PARAM_GALLERY);
        if (mGallery == null || !mGallery.isValid()) {
            Toast.makeText(this, "Gallery data error", Toast.LENGTH_SHORT).show();
        }
        mFragmentClass = (Class) getIntent().getSerializableExtra(INTENT_PARAM_FRAGMENT_CLASS);
        if (mFragmentClass == null) {
            mFragmentClass = PhotoViewFragment.class;
        }

        mBackground = findViewById(R.id.background);

        mRoot = findViewById(R.id.root);
        mPager = (PhotoViewPager) findViewById(R.id.pager);
        mAdapter = new ImagePagerAdapter();
        mPager.setAdapter(mAdapter);
        mPager.setOnInterceptTouchListener(this);
        mPager.addOnPageChangeListener(this);
        mTempImage = (ImageView) findViewById(R.id.temp_image);

        if (savedInstanceState != null) {
            mFullScreen = savedInstanceState.getBoolean(SAVE_INSTANCE_FULLSCREEN);
            mEnterAnimationFinished = savedInstanceState.getBoolean(SAVE_INSTANCE_ENTER_ANIMATION_FINISHED);
        } else {
            mPager.setCurrentItem(mGallery.initPosition);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mRoot.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                            visibility == 0 && mLastFlags == 3846) {
                        setFullScreen(false /* fullscreen */, true /* setDelayedRunnable */);
                    }
                }
            });
        }
        if (isScaleAnimationEnabled()) {
            // Keep lights out mode as false. This is to prevent jank cause by concurrent
            // animations during the enter animation.
            setImmersiveMode(false);
            if (mTempImage.getMeasuredWidth() != 0 && mTempImage.getMeasuredHeight() != 0) {
                runEnterAnimation();
            } else {
                mTempImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mTempImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        runEnterAnimation();
                    }
                });
            }
        } else {
            setImmersiveMode(mFullScreen);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_INSTANCE_FULLSCREEN, mFullScreen);
        outState.putBoolean(SAVE_INSTANCE_ENTER_ANIMATION_FINISHED, mEnterAnimationFinished);
    }

    @Override
    public int onTouchIntercept(float origX, float origY) {
        boolean interceptLeft = false;
        boolean interceptRight = false;

        for (OnScreenListener listener : mScreenListeners.values()) {
            if (!interceptLeft) {
                interceptLeft = listener.onInterceptMoveLeft(origX, origY);
            }
            if (!interceptRight) {
                interceptRight = listener.onInterceptMoveRight(origX, origY);
            }
        }

        if (interceptLeft) {
            if (interceptRight) {
                return PhotoViewPager.INTERCEPT_TYPE_BOTH;
            }
            return PhotoViewPager.INTERCEPT_TYPE_LEFT;
        } else if (interceptRight) {
            return PhotoViewPager.INTERCEPT_TYPE_RIGHT;
        }
        return PhotoViewPager.INTERCEPT_TYPE_NONE;
    }

    public PhotoViewPager getPager() {
        return mPager;
    }

    @CallSuper
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset < 0.0001) {
            OnScreenListener before = mScreenListeners.get(position - 1);
            if (before != null) {
                before.onViewInactivated();
            }
            OnScreenListener after = mScreenListeners.get(position + 1);
            if (after != null) {
                after.onViewInactivated();
            }
        }
    }

    @CallSuper
    @Override
    public void onPageSelected(int position) {
        // Restart the timer to return to fullscreen.
//        cancelEnterFullScreenRunnable();
//        postEnterFullScreenRunnableWithDelay();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        OnScreenListener listener = mScreenListeners.get(mPager.getCurrentItem());
        if (listener != null) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                listener.onViewActivated();
            } else {
                listener.onViewInactivated();
            }
        }
    }

    @Override
    public void addScreenListener(int position, OnScreenListener listener) {
        mScreenListeners.put(position, listener);
    }

    @Override
    public void removeScreenListener(int position) {
        mScreenListeners.remove(position);
    }

    @Override
    public boolean isFragmentActive(int position) {
        //noinspection SimplifiableIfStatement
        if (mPager == null || mAdapter == null) {
            return false;
        }
        return mPager.getCurrentItem() == position;
    }

    @Override
    public void toggleFullScreen() {
        setFullScreen(!mFullScreen, true);
    }

    public class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ImagePagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putParcelable(PhotoViewFragment.ARG_IMAGE, (T) mGallery.images.get(position));
            args.putInt(PhotoViewFragment.ARG_POSITION, position);
            if (position == mGallery.initPosition && !isEnterAnimationFinished() && isScaleAnimationEnabled()) {
                // TODO: 6/6/16
            }
            return Fragment.instantiate(PhotoViewPagerActivity.this, mFragmentClass.getName(), args);
        }

        @Override
        public int getCount() {
            return mGallery.images.size();
        }
    }

    protected void setFullScreen(boolean fullScreen, boolean setDelayedRunnable) {

        final boolean fullScreenChanged = (fullScreen != mFullScreen);
        mFullScreen = fullScreen;

        if (mFullScreen) {
            setImmersiveMode(true);
//            cancelEnterFullScreenRunnable();
        } else {
            setImmersiveMode(false);
            if (setDelayedRunnable) {
//                postEnterFullScreenRunnableWithDelay();
            }
        }

        if (fullScreenChanged) {
            for (OnScreenListener listener : mScreenListeners.values()) {
                listener.onFullScreenChanged(mFullScreen);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setImmersiveMode(boolean enabled) {
        int flags = 0;
        final int version = Build.VERSION.SDK_INT;
        final boolean manuallyUpdateActionBar = version < Build.VERSION_CODES.JELLY_BEAN;
        if (enabled &&
                (!isScaleAnimationEnabled() || isEnterAnimationFinished())) {
            // Turning on immersive mode causes an animation. If the scale animation is enabled and
            // the enter animation isn't yet complete, then an immersive mode animation should not
            // occur, since two concurrent animations are very janky.

            // Disable immersive mode for secondary users to prevent b/12015090 (freezing crash)
            // This is fixed in KK_MR2 but there is no way to differentiate between  KK and KK_MR2.
            if (version > Build.VERSION_CODES.KITKAT ||
                    version == Build.VERSION_CODES.KITKAT && !kitkatIsSecondaryUser()) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
            } else if (version >= Build.VERSION_CODES.JELLY_BEAN) {
                // Clients that use the scale animation should set the following system UI flags to
                // prevent janky animations on exit when the status bar is hidden:
                //     View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_STABLE
                // As well, client should ensure `android:fitsSystemWindows` is set on the root
                // content view.
                flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            } else if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                flags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
                //noinspection deprecation
                flags = View.STATUS_BAR_HIDDEN;
            }

            if (manuallyUpdateActionBar) {
                hideActionBar();
            }
        } else {
            if (version >= Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else if (version >= Build.VERSION_CODES.JELLY_BEAN) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                flags = View.SYSTEM_UI_FLAG_VISIBLE;
            } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
                //noinspection deprecation
                flags = View.STATUS_BAR_VISIBLE;
            }

            if (manuallyUpdateActionBar) {
                showActionBar();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mLastFlags = flags;
            mRoot.setSystemUiVisibility(flags);
        }
    }

    private void runEnterAnimation() {
        if (isEnterAnimationFinished() || mScaleInfo == null) {
            return;
        }
        int targetWidth = mTempImage.getMeasuredWidth();
        int targetHeight = mTempImage.getMeasuredHeight();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mTempImage.getLayoutParams();
        lp.leftMargin = mScaleInfo.x;
        lp.topMargin = mScaleInfo.y;
        mTempImage.setLayoutParams(lp);
        mTempImage.setVisibility(View.VISIBLE);

        AlphaAnimation alphaAnim = new AlphaAnimation(0f, 1f);
        alphaAnim.setDuration(ANIMATION_DURATION);
        alphaAnim.setFillBefore(true);
        alphaAnim.setFillAfter(true);
        mBackground.startAnimation(alphaAnim);

        final float scaleW = (float) mScaleInfo.width / targetWidth;
        final float scaleY = (float) mScaleInfo.height / targetHeight;
        final float scale = Math.max(scaleW, scaleY);
        ScaleAnimation scaleAnim = new ScaleAnimation(scale, 1, scale, 1);
        scaleAnim.setDuration(ANIMATION_DURATION);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillAfter(true);

        final int translateX = calculateTranslate(mScaleInfo.x, mScaleInfo.width, targetWidth, scale);
        final int translateY = calculateTranslate(mScaleInfo.y, mScaleInfo.height, targetHeight, scale);
        TranslateAnimation translateAnim = new TranslateAnimation(translateX, 0, translateY, 0);
        translateAnim.setDuration(ANIMATION_DURATION);
        translateAnim.setFillBefore(true);
        translateAnim.setFillAfter(true);

        AnimationSet animSet = new AnimationSet(true);
        animSet.addAnimation(scaleAnim);
        animSet.addAnimation(translateAnim);
        animSet.setDuration(ANIMATION_DURATION);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mEnterAnimationFinished = true;
                mTempImage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBackground.startAnimation(animSet);
    }

    private int calculateTranslate(int start, int startSize, int totalSize, float scale) {
        // Translation takes precedence over scale.  What this means is that if
        // we want an view's upper left corner to be a particular spot on screen,
        // but that view is scaled to something other than 1, we need to take into
        // account the pixels lost to scaling.
        // So if we have a view that is 200x300, and we want it's upper left corner
        // to be at 50x50, but it's scaled by 50%, we can't just translate it to 50x50.
        // If we were to do that, the view's *visible* upper left corner would be at
        // 100x200.  We need to take into account the difference between the outside
        // size of the view (i.e. the size prior to scaling) and the scaled size.
        // scaleFromEdge is the difference between the visible left edge and the
        // actual left edge, due to scaling.
        // scaleFromTop is the difference between the visible top edge, and the
        // actual top edge, due to scaling.
        int scaleFromEdge = Math.round((totalSize - totalSize * scale) / 2);

        // The imageView is fullscreen, regardless of the aspect ratio of the actual image.
        // This means that some portion of the imageView will be blank.  We need to
        // take into account the size of the blank area so that the actual image
        // lines up with the starting image.
        int blankSize = Math.round((totalSize * scale - startSize) / 2);

        return start - scaleFromEdge - blankSize;
    }

    public boolean isScaleAnimationEnabled() {
        return mScaleInfo != null;
    }

    public boolean isEnterAnimationFinished() {
        return mEnterAnimationFinished;
    }

    /**
     * Return true iff the app is being run as a secondary user on kitkat.
     * <p/>
     * This is a hack which we only know to work on kitkat.
     */
    private boolean kitkatIsSecondaryUser() {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
            throw new IllegalStateException("kitkatIsSecondary user is only callable on KitKat");
        }
        return Process.myUid() > 100000;
    }

    public void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
