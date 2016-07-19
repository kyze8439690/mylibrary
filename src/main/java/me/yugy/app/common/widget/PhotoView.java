package me.yugy.app.common.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.yugy.app.common.R;
import me.yugy.app.common.utils.MarkableFileInputStream;
import me.yugy.app.common.utils.UIUtils;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.LibraryLoader;

public class PhotoView extends View implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

    private static final int TRANSLATE_NONE = 0;
    private static final int TRANSLATE_X_ONLY = 1;
    private static final int TRANSLATE_Y_ONLY = 2;
    private static final int TRANSLATE_BOTH = 3;

    private static final int CROP_SHAPE_RECT = 0;
    private static final int CROP_SHAPE_CIRCLE = 1;

    /** Zoom animation duration; in milliseconds */
    private static final long ZOOM_ANIMATION_DURATION = 200L;
    /** Amount of time to wait after over-zooming before the zoom out animation; in milliseconds */
    private static final long ZOOM_CORRECTION_DELAY = 100L;
    /** Snap animation duration; in milliseconds */
    private static final long SNAP_DURATION = 100L;
    /** Amount of time to wait before starting snap animation; in milliseconds */
    private static final long SNAP_DELAY = 250L;
    /** By how much to scale the image when double tap occurs */
    private static final float DOUBLE_TAP_SCALE_FACTOR = 2.0f;
    /** Amount which can be zoomed in past the maximum scale, and then scaled back */
    private static final float SCALE_OVERZOOM_FACTOR = 1.5f;

    /** If {@code true}, the static values have been initialized, which can be used across instance. */
    private static boolean sInitialized = false;

    /** Touch slop used to determine if this double tap is valid for starting a scale or should be
     * ignored. */
    private static int sTouchSlipSquare;

    /**
     * Detect current runtime support gif display or not. If gif support is needed, please compile
     * gif drawable library in build.gradle file.
     * <br/>
     * See <a href="https://github.com/koral--/android-gif-drawable">https://github.com/koral--/android-gif-drawable</a>
     */
    private static boolean sSupportGif = false;

    // Paints
    /** Paint to partially dim the photo during crop */
    private static Paint sCropDimPaint;
    /** Paint to highlight the cropped portion of the photo */
    private static Paint sCropBorderPaint;

    /** Whether or not crop is allowed */
    private boolean mAllowCrop;
    /** The crop region */
    private Rect mCropRect = new Rect();
    /** Actual crop display size; may be smaller according to screen size */
    private int mCropWidth, mCropHeight;
    /** Crop output bitmap size; must be the same aspect ratio with display crop rect */
    private int mCropOutputWidth, mCropOutputHeight;
    private int mCropBorderColor;
    private float mCropBorderSize;
    private int mCropShape;
    private Path mCropDimPath;
    private boolean mAllowCropOverscroll;

    /** The maximum amount of scaling to apply to images */
    private float mMaxInitialScaleFactor = 1f;

    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    /** Whether the QuickSale gesture (double tap and drag to scale) is enabled. */
    private boolean mQuickScaleEnabled;

    // To support translation [i.e. panning]
    /** Runnable that can move the image */
    private TranslateRunnable mTranslateRunnable;
    private SnapRunnable mSnapRunnable;

    // To support zooming
    /** Runnable that scales the image */
    private ScaleRunnable mScaleRunnable;
    /** When {@code true}, allows gestures to scale / pan the image */
    private boolean mTransformsEnabled = true;
    /** When {@code true}, a double tap scales the image by {@link #DOUBLE_TAP_SCALE_FACTOR} */
    private boolean mDoubleTapToZoomEnabled = true;
    /** When {@code false}, event is a scale gesture. Otherwise, event is a double touch. */
    private boolean mIsDoubleTouch;
    /** Minimum scale the image can have. */
    private float mMinScale;
    /** Maximum scale to limit scaling to, 0 means no limit. */
    private float mMaxScale;
    /** Track whether a double tap event occurred. */
    private boolean mDoubleTapOccurred;
    /** When {@code true}, prevents scale end gesture from falsely triggering a double click. */
    private boolean mDoubleTapDebounce;

    /** The photo to display */
    @Nullable private Drawable mDrawable;
    /** The matrix used for drawing; this may be {@code null} */
    @Nullable private Matrix mDrawMatrix;
    /** A matrix to apply the scaling of the photo */
    private Matrix mMatrix = new Matrix();
    /** The original matrix for this image; used to reset any transformations applied by the user */
    private Matrix mOriginalMatrix = new Matrix();

    /** When {@code true}, the view has been laid out */
    private boolean mHaveLayout;

    // Convenience fields
    // These are declared here not because they are important properties of the view. Rather, we
    // declare them here to avoid object allocation during critical graphics operatinos; such as
    // layout or drawing.
    /** Source (i.e. the photo size) bounds */
    private RectF mTempSrc = new RectF();
    /** Destination (i.e. the display) bounds. The image is scaled to this size. */
    private RectF mTempDst = new RectF();
    /** Rectangle to handle translations */
    private RectF mTranslateRect = new RectF();
    /** Array to store a copy of the matrix values */
    private float[] mValues = new float[9];

    /**
     * X and Y coordinates for the current down event. Since mDoubleTapOccurred only contains the
     * information that there was a double tap event, use these to get the secondary tap information
     * to determine if a user has moved beyond touch slop.
     */
    private float mDownFocusX, mDownFocusY;

    private OnClickListener mOnClickListener;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mGestureDetector = new GestureDetectorCompat(context, this, null);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mQuickScaleEnabled = ScaleGestureDetectorCompat.isQuickScaleEnabled(mScaleGestureDetector);
        mTranslateRunnable = new TranslateRunnable(this);
        mSnapRunnable = new SnapRunnable(this);
        mScaleRunnable = new ScaleRunnable(this);

        int cropDimColor = ContextCompat.getColor(context, R.color.default_crop_dim_color);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PhotoView,
                defStyleAttr, 0);
        try {
            mCropWidth = a.getDimensionPixelSize(R.styleable.PhotoView_pv_crop_width, -1);
            if (mCropWidth == -1) {
                mCropWidth = getResources().getDimensionPixelSize(R.dimen.default_crop_size);
            }
            mCropHeight = a.getDimensionPixelSize(R.styleable.PhotoView_pv_crop_height, -1);
            if (mCropHeight == -1) {
                mCropHeight = getResources().getDimensionPixelSize(R.dimen.default_crop_size);
            }
            mCropOutputWidth = a.getDimensionPixelSize(
                    R.styleable.PhotoView_pv_crop_output_width, mCropWidth);
            mCropOutputHeight = a.getDimensionPixelSize(
                    R.styleable.PhotoView_pv_crop_output_height, mCropHeight);
            checkCropSize();

            cropDimColor = a.getColor(R.styleable.PhotoView_pv_crop_dim_color, cropDimColor);
            boolean cropEnabled = a.getBoolean(R.styleable.PhotoView_pv_crop_enabled, false);
            enableAllowCrop(cropEnabled);
            mCropBorderColor = a.getColor(R.styleable.PhotoView_pv_crop_border_color, Color.WHITE);
            mCropBorderSize = a.getDimension(R.styleable.PhotoView_pv_crop_border_size, -1f);
            if (mCropBorderSize == -1f) {
                mCropBorderSize = getResources().getDimension(R.dimen.default_crop_border_size);
            }
            mCropShape = a.getInt(R.styleable.PhotoView_pv_crop_shape, CROP_SHAPE_CIRCLE);
            mAllowCropOverscroll = a.getBoolean(R.styleable.PhotoView_pv_crop_allow_overscroll, false);
            mCropDimPath = new Path();
        } finally {
            a.recycle();
        }

        if (!sInitialized) {
            sInitialized = true;

            sCropDimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sCropDimPaint.setColor(cropDimColor);
            sCropDimPaint.setStyle(Paint.Style.FILL);

            sCropBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sCropBorderPaint.setStyle(Paint.Style.STROKE);
            sCropBorderPaint.setStrokeWidth(mCropBorderSize);
            sCropBorderPaint.setColor(mCropBorderColor);

            int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            sTouchSlipSquare = touchSlop * touchSlop;

            try {
                Class.forName("pl.droidsonroids.gif.GifDrawable");
                Class.forName("pl.droidsonroids.gif.LibraryLoader");
                sSupportGif = true;
            } catch (ClassNotFoundException ignored) {
                sSupportGif = false;
            }
        }
    }

    public void setCropOutputSize(int width, int height) {
        mCropOutputWidth = width;
        mCropOutputHeight = height;
        checkCropSize();
    }

    private void checkCropSize() {
        if (Math.abs((float) mCropWidth / mCropHeight - (float) mCropOutputWidth / mCropOutputHeight) >= 1) {
            throw new IllegalArgumentException(
                    "crop output aspect ratio should be the same with crop rect.");
        }
    }

    /**
     * if {@link #mAllowCrop} is {@code false}, return null.See {@link #enableAllowCrop(boolean)}.
     */
    @Nullable
    public Bitmap getCroppedBitmap() {
        if (!mAllowCrop) {
            return null;
        }

        // translate to the origin & scale
        final Matrix matrix = new Matrix(mDrawMatrix);
        matrix.postTranslate(-mCropRect.left, -mCropRect.top);

        //check available size
        matrix.getValues(mValues);
        float scale = mValues[Matrix.MSCALE_X];
        int availableWidth = (int) (mCropRect.width() / scale);
        int availableHeight = (int) (mCropRect.height() / scale);

        int targetWidth = Math.min(availableWidth, mCropOutputWidth);
        int targetHeight = Math.min(availableHeight, mCropOutputHeight);

        matrix.postScale(
                (float) targetWidth / mCropWidth,
                (float) targetHeight / mCropHeight);

        final Bitmap croppedBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
                Bitmap.Config.ARGB_8888);
        final Canvas croppedCanvas = new Canvas(croppedBitmap);

        // draw the photo
        if (mDrawable != null) {
            croppedCanvas.concat(matrix);
            mDrawable.draw(croppedCanvas);
        }
        return croppedBitmap;
    }

//    /**
//     * Use {@link BitmapRegionDecoder BitmapRegionDecoder} to cropped image, which can avoid image
//     * quality loss but slower. Be aware that this method can only be used on
//     * {@link android.os.Build.VERSION_CODES#GINGERBREAD_MR1 GINGERBREAD_MR1} or newer devices.
//     * @param imageUri source image uri
//     * @return if {@link #mAllowCrop} is {@code false} or decode failed, return null.
//     * See {@link #enableAllowCrop(boolean)}.
//     */
//    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
//    @Nullable
//    public Bitmap getPreciseCroppedBitmap(Uri imageUri) throws IOException {
//        if (!mAllowCrop) {
//            return null;
//        }
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
//            throw new IllegalStateException(
//                    "This method can only be used in api level 10 + devices, " +
//                            "please use getCroppedBitmap instead.");
//        }
//        int exifRotation = Utils.getOrientation(getContext().getContentResolver(), imageUri);
//        InputStream in = null;
//        try {
//            in = getContext().getContentResolver().openInputStream(imageUri);
//            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(in, false);
//            final int originWidth = decoder.getWidth();
//            final int originHeight = decoder.getHeight();
//            float targetWidth, targetHeight;
//            mMatrix.getValues(mValues);
//            float currentScale = mValues[Matrix.MSCALE_X];
//            if (originWidth > originHeight) {
//                targetHeight = originHeight / currentScale;
//                targetWidth = targetHeight * mCropOutputWidth / mCropOutputHeight;
//            } else {
//                targetWidth = originWidth / currentScale;
//                targetHeight = targetWidth * mCropOutputHeight / mCropOutputWidth;
//            }
//            Rect rect = new Rect();
//            RectF displayRect = getDisplayRect();
//            float scaledWidth = displayRect.right - displayRect.left;
//            float scaledHeight = displayRect.bottom - displayRect.top;
//            switch (exifRotation) {
//                case 0:
//                    rect.left = (int) (-displayRect.left / scaledWidth * originWidth);
//                    rect.top = (int) (-displayRect.top / scaledHeight * originHeight);
//                    rect.right = (int) (rect.left + targetWidth);
//                    rect.bottom = (int) (rect.top + targetHeight);
//                    break;
//                case 90:
//                    rect.left = (int) (-displayRect.top / scaledHeight * originWidth);
//                    rect.bottom = (int) (originHeight + displayRect.left / scaledWidth * originHeight);
//                    rect.right = (int) (rect.left + targetWidth);
//                    rect.top = (int) (rect.bottom - targetHeight);
//                    break;
//                case 180:
//                    rect.right = (int) (originWidth + displayRect.left / scaledWidth * originWidth);
//                    rect.bottom = (int) (originHeight + displayRect.top / scaledHeight * originHeight);
//                    rect.left = (int) (rect.right - targetWidth);
//                    rect.top = (int) (rect.bottom - targetHeight);
//                    break;
//                case 270:
//                    rect.right = (int) (originWidth + displayRect.top / scaledHeight * originWidth);
//                    rect.top = (int) (-displayRect.left / scaledWidth * originHeight);
//                    rect.left = (int) (rect.right - targetWidth);
//                    rect.bottom = (int) (rect.top + targetHeight);
//                    break;
//            }
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = Utils.calculateInSampleSize(originWidth, originHeight,
//                    mCropOutputWidth, mCropOutputHeight);
//            Bitmap bitmap = decoder.decodeRegion(rect, options);
//            if (bitmap != null) {
//                Matrix matrix = new Matrix();
//                matrix.postRotate(exifRotation);
//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                return bitmap;
//            }
//        } finally {
//            if (in != null) {
//                in.close();
//            }
//        }
//        return null;
//    }
//
//    private RectF getDisplayRect() {
//        if (mMatrix != null && mDrawable != null) {
//            RectF displayRect = new RectF(0, 0, mDrawable.getIntrinsicWidth(),
//                    mDrawable.getIntrinsicHeight());
//            mMatrix.mapRect(displayRect);
//            return displayRect;
//        } else {
//            return new RectF();
//        }
//    }

    /**
     * Enable or disable cropping of the displayed image. Cropping can only be enabled
     * <em>before</em> the view has been laid out. Additionally, once cropping has been
     * enabled, it cannot be disabled.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void enableAllowCrop(boolean allowCrop) {
        if (allowCrop && mHaveLayout) {
            throw new IllegalArgumentException("Cannot set crop after view has been laid out");
        }
        if (!allowCrop && mAllowCrop) {
            throw new IllegalArgumentException("Cannot unset crop mode");
        }
        mAllowCrop = allowCrop;
        if (mAllowCrop && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * Set the size of cropping. Cropping size can only be modified
     * <em>before</em> the view has been laid out.
     */
    public void setCropSize(int cropWidth, int cropHeight) {
        if (mHaveLayout) {
            throw new IllegalArgumentException("Cannot set crop size after view has been laid out");
        }
        mCropWidth = cropWidth;
        mCropHeight = cropHeight;
    }

    public void setImageSource(Uri uri) throws IOException {
        InputStream input = getContext().getContentResolver().openInputStream(uri);
        if (input == null) {
            throw new IOException("open uri failed.");
        }
        byte[] header = new byte[6];
        int length = input.read(header);
        if (length != 6) {
            throw new IOException("File length error.");
        }
        String headerString = new String(header);
        if (sSupportGif && (headerString.equals("GIF89a") || headerString.equals("GIF87a"))) {
            //Gif
            input = getContext().getContentResolver().openInputStream(uri);
            if (input == null) {
                throw new IOException("open uri failed.");
            }
            LibraryLoader.initialize(getContext());
            GifDrawable drawable = new GifDrawable(input);
            bindDrawable(drawable);
            return;
        }

        // JPG, PNG, ...
        try {
            input = getContext().getContentResolver().openInputStream(uri);
            if (input == null) {
                throw new IOException("open uri failed.");
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            int originWidth = options.outWidth;
            int originHeight = options.outHeight;
            final int[] size = tryGetViewSize();
            options.inSampleSize = Math.max(originWidth / size[0], originHeight / size[1]);
            options.inJustDecodeBounds = false;
            input = getContext().getContentResolver().openInputStream(uri);
            if (input == null) {
                throw new IOException("open uri failed.");
            }
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            PhotoDrawable drawable = new PhotoDrawable(getResources(), bitmap);
            drawable.setOriginSize(originWidth, originHeight);
            bindDrawable(new PhotoDrawable(getResources(), bitmap));
        } catch (OutOfMemoryError e) {
            throw new IOException(e);
        }
    }

    public void setImageSource(File file) throws IOException {
        setImageSource(new MarkableFileInputStream(file));
    }

    public void setImageSource(InputStream input) throws IOException {
        byte[] header = new byte[6];
        int length = input.read(header);
        if (length != 6) {
            throw new IOException("File length error.");
        }
        String headerString = new String(header);
        if (sSupportGif && (headerString.equals("GIF89a") || headerString.equals("GIF87a"))) {
            //Gif
            input.reset();
            LibraryLoader.initialize(getContext());
            GifDrawable drawable = new GifDrawable(input);
            bindDrawable(drawable);
            return;
        }

        // JPG, PNG, ...
        try {
            input.reset();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            final int[] size = tryGetViewSize();
            options.inSampleSize = Math.max(options.outWidth / size[0], options.outHeight / size[1]);
            options.inJustDecodeBounds = false;
            input.reset();
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            bindDrawable(new PhotoDrawable(getResources(), bitmap));
        } catch (OutOfMemoryError e) {
            throw new IOException(e);
        }
    }

    public int[] tryGetViewSize() {
        int width, height;
        ViewGroup.LayoutParams lp = getLayoutParams();
        if ((width = getMeasuredWidth()) != 0 && ((height = getMeasuredHeight()) != 0)) {
            return new int[] {width, height};
        } else if (lp != null && (width = lp.width) > 0 && ((height = lp.height) > 0)) {
            return new int[] {width, height};
        } else if ((width = getWidth()) != 0 && (height = getHeight()) != 0) {
            return new int[] {width, height};
        } else {
            Context context = getContext();
            return new int[] {UIUtils.getDisplayWidth(context), UIUtils.getDisplayHeight(context)};
        }
    }

    public void bindDrawable(Drawable drawable) {
        boolean changed = false;
        if (drawable != null && drawable != mDrawable) {
            // Clear previous state.
            if (mDrawable != null) {
                mDrawable.setCallback(null);
            }

            mDrawable = drawable;

            // Reset mMinScale to ensure the bounds / matrix are recalculated
            mMinScale = 0f;

            // Set a callback?
            mDrawable.setCallback(this);

            changed = true;
        }

        configureBounds(changed);
        invalidate();
    }

    /**
     * Free all resources held by this view.
     * The view is on its way to be collected and will not be reused.
     */
    public void clear() {
        mGestureDetector = null;
        mScaleGestureDetector = null;
        mTranslateRunnable.stop();
        mTranslateRunnable = null;
        mSnapRunnable.stop();
        mSnapRunnable = null;
        setOnClickListener(null);

        if (mDrawable == null) {
            return;
        }
        if (mDrawable instanceof PhotoDrawable) {
            ((PhotoDrawable) mDrawable).getBitmap().recycle();
        } else if (sSupportGif && mDrawable instanceof GifDrawable) {
            ((GifDrawable) mDrawable).recycle();
        }
        mDrawable = null;
    }

    public boolean interceptMoveLeft() {
        if (!mTransformsEnabled) {
            // Allow intercept if we're not in transform mode
            return false;
        } else if (mTranslateRunnable.mRunning) {
            // Don't allow touch intercept until we've stopped flinging
            return true;
        } else {
            mMatrix.getValues(mValues);
            mTranslateRect.set(mTempSrc);
            mMatrix.mapRect(mTranslateRect);

            final float viewWidth = getWidth();
            final float transX = mValues[Matrix.MTRANS_X];
            final float drawWidth = mTranslateRect.right - mTranslateRect.left;

            if (!mTransformsEnabled || drawWidth <= viewWidth) {
                // Allow intercept if not in transform mode or the image is smaller than the view
                return false;
            } else if (transX == 0) {
                // We're at the left-side of the image; allow intercepting movements to the right
                return false;
            } else if (viewWidth >= drawWidth + transX) {
                // We're at the right-side of the image; allow intercepting movements to the left
                return true;
            } else {
                // We're in the middle of the image; don't allow touch intercept
                return true;
            }
        }
    }

    public boolean interceptMoveRight() {
        if (!mTransformsEnabled) {
            // Allow intercept if we're not in transform mode
            return false;
        } else if (mTranslateRunnable.mRunning) {
            // Don't allow touch intercept until we've stopped flinging
            return true;
        } else {
            mMatrix.getValues(mValues);
            mTranslateRect.set(mTempSrc);
            mMatrix.mapRect(mTranslateRect);

            final float viewWidth = getWidth();
            final float transX = mValues[Matrix.MTRANS_X];
            final float drawWidth = mTranslateRect.right - mTranslateRect.left;

            if (!mTransformsEnabled || drawWidth <= viewWidth) {
                // Allow intercept if not in transform mode or the image is smaller than the view
                return false;
            } else if (transX == 0) {
                // We're at the left-side of the image; allow intercepting movements to the right
                return true;
            } else if (viewWidth >= drawWidth + transX) {
                // We're at the right-side of the image; allow intercepting movements to the left
                return false;
            } else {
                // We're in the middle of the image; don't allow touch intercept
                return true;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHaveLayout = true;
        final int layoutWidth = getWidth();
        final int layoutHeight = getHeight();

        if (mAllowCrop) {
            mCropWidth = Math.min(mCropWidth, layoutWidth);
            mCropHeight = Math.min(mCropHeight, layoutHeight);
            final int cropLeft = (layoutWidth - mCropWidth) / 2;
            final int cropTop = (layoutHeight - mCropHeight) / 2;
            final int cropRight = cropLeft + mCropWidth;
            final int cropBottom = cropTop + mCropHeight;

            // Create a crop region overlay. We need to separate canvas to be able to "punch a hole"
            // through to the underlying image.
            mCropRect.set(cropLeft, cropTop, cropRight, cropBottom);
            mCropDimPath.reset();
            if (mCropShape == CROP_SHAPE_CIRCLE) {
                mCropDimPath.addCircle(getWidth() / 2, getHeight() / 2, mCropRect.width() / 2,
                        Path.Direction.CW);
            } else {
                mCropDimPath.addRect(mCropRect.left, mCropRect.top, mCropRect.right,
                        mCropRect.bottom, Path.Direction.CW);
            }
        }
        configureBounds(changed);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw the photo
        if (mDrawable != null) {
            int saveCount = canvas.getSaveCount();
            canvas.save();

            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);

            canvas.restoreToCount(saveCount);

            // Extract the drawable's bounds (in our own copy, to not alter the image)
            mTranslateRect.set(mDrawable.getBounds());
            if (mDrawMatrix != null) {
                mDrawMatrix.mapRect(mTranslateRect);
            }
        }

        // draw the crop overlay
        if (mAllowCrop) {
            int saveCount = canvas.save();
            canvas.clipPath(mCropDimPath, Region.Op.DIFFERENCE);

            canvas.drawRect(0, 0, getWidth(), getHeight(), sCropDimPaint);
            canvas.restoreToCount(saveCount);
            if (mCropShape == CROP_SHAPE_CIRCLE) {
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, mCropRect.width() / 2, sCropBorderPaint);
            } else {
                canvas.drawRect(mCropRect, sCropBorderPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScaleGestureDetector == null || mGestureDetector == null) {
            // We're being destroyed; ignore any touch events
            return true;
        }

        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!mTranslateRunnable.mRunning) {
                    snap();
                }
                break;
        }

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mOnClickListener != null && !mIsDoubleTouch) {
            mOnClickListener.onClick(this);
        }
        mIsDoubleTouch = false;
        return true;
    }

    @Override public void onLongPress(MotionEvent e) {}
    @Override public void onShowPress(MotionEvent e) {}
    @Override public boolean onSingleTapUp(MotionEvent e) {return false;}

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        mDoubleTapOccurred = true;
        //noinspection SimplifiableIfStatement
        if (!mQuickScaleEnabled) {
            return scale(e);
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        final int action = e.getAction();
        boolean handled = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mQuickScaleEnabled) {
                    mDownFocusX = e.getX();
                    mDownFocusY = e.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mQuickScaleEnabled) {
                    handled = scale(e);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mQuickScaleEnabled && mDoubleTapOccurred) {
                    final int deltaX = (int) (e.getX() - mDownFocusX);
                    final int deltaY = (int) (e.getY() - mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    if (distance > sTouchSlipSquare) {
                        mDoubleTapOccurred = false;
                    }
                }
                break;
        }

        return handled;
    }

    private boolean scale(MotionEvent e) {
        boolean handled = false;
        if (mDoubleTapToZoomEnabled && mTransformsEnabled && mDoubleTapOccurred) {
            if (!mDoubleTapDebounce) {
                float currentScale = getScale();
                float targetScale;
                float centerX, centerY;

                // Zoom out if not default scale, otherwise zoom in
                if (currentScale > mMinScale) {
                    targetScale = mMinScale;
                    float relativeScale = targetScale / currentScale;
                    //Find the apparent origin for scaling that equals this scale and translate
                    centerX = (getWidth() / 2 - relativeScale * mTranslateRect.centerX()) /
                            (1 - relativeScale);
                    centerY = (getHeight() / 2 - relativeScale * mTranslateRect.centerY()) /
                            (1 - relativeScale);
                } else {
                    targetScale = currentScale * DOUBLE_TAP_SCALE_FACTOR;
                    // Ensure the target scale is within our bounds
                    targetScale = Math.max(mMinScale, targetScale);
                    targetScale = Math.min(mMaxScale, targetScale);
                    float relativeScale = targetScale / currentScale;
                    float widthBuffer = (getWidth() - mTranslateRect.width()) / relativeScale;
                    float heightBuffer = (getHeight() - mTranslateRect.height()) / relativeScale;
                    // Clamp the center if it would result in uneven borders
                    if (mTranslateRect.width() <= widthBuffer * 2) {
                        centerX = mTranslateRect.centerX();
                    } else {
                        centerX = Math.min(
                                Math.max(mTranslateRect.left + widthBuffer, e.getX()),
                                mTranslateRect.right - widthBuffer);
                    }
                    if (mTranslateRect.height() <= heightBuffer * 2) {
                        centerY = mTranslateRect.centerY();
                    } else {
                        centerY = Math.min(
                                Math.max(mTranslateRect.top + heightBuffer, e.getY()),
                                mTranslateRect.bottom - heightBuffer);
                    }
                }

                mScaleRunnable.start(currentScale, targetScale, centerX, centerY);
                handled = true;
            }
            mDoubleTapDebounce = false;
        }
        mDoubleTapOccurred = false;
        return handled;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (mTransformsEnabled) {
            mTranslateRunnable.stop();
            mSnapRunnable.stop();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mTransformsEnabled && !mScaleRunnable.mRunning) {
            translate(-distanceX, -distanceY);
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mTransformsEnabled && !mScaleRunnable.mRunning) {
            mTranslateRunnable.start(velocityX, velocityY);
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mTransformsEnabled && !mScaleRunnable.mRunning) {
            mIsDoubleTouch = false;
            float currentScale = getScale();
            float newScale = currentScale * detector.getScaleFactor();
            scale(newScale, detector.getFocusX(), detector.getFocusY());
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (mTransformsEnabled && mScaleRunnable.mRunning) {
            mScaleRunnable.stop();
            mIsDoubleTouch = true;
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (mTransformsEnabled && mIsDoubleTouch) {
            mDoubleTapDebounce = true;
            resetTransformations();
        }
        checkZoomBack();
    }

    /**
     * Resets the image transformation to its original value.
     */
    public void resetTransformations() {
        // snap transformations; we don't animate
        mMatrix.set(mOriginalMatrix);

        // Invalidate the view because if you move off this PhotoView
        // to another one and come back, you want it to draw from scratch
        // in case you were zoomed in or translated (since those settings
        // are not preserved and probably shouldn't be).
        invalidate();
    }

    /**
     * Configures the bounds of the photo. The photo will always be scaled to fit center.
     * @param changed whether the view size has been changed
     */
    private void configureBounds(boolean changed) {
        if (mDrawable == null || !mHaveLayout) {
            return;
        }
        final int dwidth = mDrawable.getIntrinsicWidth();
        final int dheight = mDrawable.getIntrinsicHeight();
        final int vwidth = getWidth();
        final int vheight = getHeight();

        final boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);
        // We need to do the scaling ourself, so have the drawable use its native size.
        mDrawable.setBounds(0, 0, dwidth, dheight);
        // Create a matrix with the proper transforms
        if (changed || (mMinScale == 0 && mDrawable != null && mHaveLayout)) {
            generateMatrix();
            generateScale();
        }

        if (fits || mMatrix.isIdentity()) {
            // The bitmap fits exactly, no transform needed.
            mDrawMatrix = null;
        } else {
            mDrawMatrix = mMatrix;
        }
    }

    private void generateScale() {
        if (mDrawable == null) {
            return;
        }
        final int dwidth = mDrawable.getIntrinsicWidth();
        final int dheight = mDrawable.getIntrinsicHeight();

        final int vwidth = mAllowCrop ? mCropWidth : getWidth();
        final int vheight = mAllowCrop ? mCropHeight : getHeight();

        if (dwidth < vwidth && dheight < vheight && !mAllowCrop) {
            mMinScale = 1.0f;
        } else {
            mMinScale = getScale();
        }
        mMaxScale = Math.max(mMinScale * 4, 4);
    }

    /**
     * Returns the currently applied scale factor for the image.
     * <P>
     * NOTE: This method overwrites any values stored in {@link #mValues}.
     * </P>
     */
    private float getScale() {
        mMatrix.getValues(mValues);
        return mValues[Matrix.MSCALE_X];
    }

    /**
     * Generates the initial transformation matrix for drawing. Additionally, it sets the minimum
     * and maximum scale values.
     */
    private void generateMatrix() {
        if (mDrawable == null) {
            return;
        }
        final int dwidth = mDrawable.getIntrinsicWidth();
        final int dheight = mDrawable.getIntrinsicHeight();

        final int vwidth = mAllowCrop ? mCropWidth : getWidth();
        final int vheight = mAllowCrop ? mCropHeight :getHeight();

        final boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);

        if (fits && !mAllowCrop) {
            mMatrix.reset();
        } else {
            // Generate the required transforms for the photo
            mTempSrc.set(0, 0, dwidth, dheight);
            if (mAllowCrop) {
                RectF scale = new RectF();
                int width, height;
                if ((float) vwidth / vheight > (float) dwidth / dheight) {
                    width = vwidth;
                    height = dheight * width / dwidth;
                } else {
                    height = vheight;
                    width = dwidth * height / dheight;
                }
                scale.set(
                        getWidth() / 2 - width / 2,
                        getHeight() / 2 - height / 2,
                        getWidth() / 2 + width / 2,
                        getHeight() / 2 + height / 2);
                mTempDst.set(scale);
            } else {
                mTempDst.set(0, 0, vwidth, vheight);
            }
            mMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
        }
        mOriginalMatrix.set(mMatrix);
    }

    /**
     * Translates the image.
     *
     * This method will not allow the image to be translated outside of the visible area.
     *
     * @param tx how many pixels to translate horizontally
     * @param ty how many pixels to translate vertically
     * @return result of the translation, represented as either {@link #TRANSLATE_NONE},
     * {@link #TRANSLATE_X_ONLY}, {@link #TRANSLATE_Y_ONLY}, or {@link #TRANSLATE_BOTH}
     */
    private int translate(float tx, float ty) {
        mTranslateRect.set(mTempSrc);
        mMatrix.mapRect(mTranslateRect);

        final float maxLeft = mAllowCrop ? mCropRect.left : 0.0f;
        final float maxRight = mAllowCrop ? mCropRect.right :getWidth();
        final float l = mTranslateRect.left;
        final float r = mTranslateRect.right;

        final float translateX;
        if (mAllowCrop && mAllowCropOverscroll) {
            // If we're cropping, allow the image to scroll off the edge of the screen
            translateX = Math.max(maxLeft - mTranslateRect.right,
                    Math.min(maxRight - mTranslateRect.left, tx));
        } else {
            // Otherwise, ensure the image never leaves the screen
            if (r - l < maxRight - maxLeft) {
                translateX = maxLeft + ((maxRight - maxLeft) - (r + l)) / 2;
            } else {
                translateX = Math.max(maxRight - r, Math.min(maxLeft - l, tx));
            }
        }

        final float maxTop = mAllowCrop ? mCropRect.top : 0.0f;
        final float maxBottom = mAllowCrop ? mCropRect.bottom : getHeight();
        final float t = mTranslateRect.top;
        final float b = mTranslateRect.bottom;

        final float translateY;
        if (mAllowCrop && mAllowCropOverscroll) {
            // If we're cropping, allow the image to scroll off the edge of the screen
            translateY = Math.max(maxTop - mTranslateRect.bottom,
                    Math.min(maxBottom - mTranslateRect.top, ty));
        } else {
            // Otherwise, ensure the image never leaves the screen
            if (b - t < maxBottom - maxTop) {
                translateY = maxTop + ((maxBottom - maxTop) - (b + t)) / 2;
            } else {
                translateY = Math.max(maxBottom - b, Math.min(maxTop - t, ty));
            }
        }

        // Do the translation
        mMatrix.postTranslate(translateX, translateY);
        invalidate();

        boolean didTranslateX = translateX == tx;
        boolean didTranslateY = translateY == ty;
        if (didTranslateX && didTranslateY) {
            return TRANSLATE_BOTH;
        } else if (didTranslateX) {
            return TRANSLATE_X_ONLY;
        } else if (didTranslateY) {
            return TRANSLATE_Y_ONLY;
        } else {
            return TRANSLATE_NONE;
        }
    }

    /**
     * Snaps the image so it touches all edges of the view.
     */
    private void snap() {
        mTranslateRect.set(mTempSrc);
        mMatrix.mapRect(mTranslateRect);

        // Determine how much to snap in the horizontal direction [if any]
        float maxLeft = mAllowCrop ? mCropRect.left : 0.0f;
        float maxRight = mAllowCrop ? mCropRect.right : getWidth();
        float l = mTranslateRect.left;
        float r = mTranslateRect.right;

        final float translateX;
        if (r - l < maxRight - maxLeft) {
            // Image is narrower than view; translate to the center of the view
            translateX = maxLeft + ((maxRight - maxLeft) - (r + l)) / 2;
        } else if (l > maxLeft) {
            // Image is off right-edge of screen; bring it into view
            translateX = maxLeft - l;
        } else if (r < maxRight) {
            // Image is off left-edge of screen; bring it into view
            translateX = maxRight - r;
        } else {
            translateX = 0.0f;
        }

        // Determine how much to snap in the vertical direction [if any]
        float maxTop = mAllowCrop ? mCropRect.top : 0.0f;
        float maxBottom = mAllowCrop ? mCropRect.bottom : getHeight();
        float t = mTranslateRect.top;
        float b = mTranslateRect.bottom;

        final float translateY;
        if (b - t < maxBottom - maxTop) {
            // Image is shorter than view; translate to the bottom edge of the view
            translateY = maxTop + ((maxBottom - maxTop) - (b + t)) / 2;
        } else if (t > maxTop) {
            // Image is off bottom-edge of screen; bring it into view
            translateY = maxTop - t;
        } else if (b < maxBottom) {
            // Image is off top-edge of screen; bring it into view
            translateY = maxBottom - b;
        } else {
            translateY = 0.0f;
        }

//        if (Math.abs(translateX) > SNAP_THRESHOLD || Math.abs(translateY) > SNAP_THRESHOLD) {
            mSnapRunnable.start(translateX, translateY);
//        } else {
//            mMatrix.postTranslate(translateX, translateY);
//            invalidate();
//        }
    }

    /**
     * Scales the image while keeping the aspect ratio.
     *
     * The given scale is capped so that the resulting scale fo the image always remains between
     * {@link #mMinScale} and {@link #mMaxScale}.
     *
     * If the image is smaller than the viewable area, it will be centered.
     *
     * @param newScale the new scale
     * @param centerX the center horizontal point around which to scale
     * @param centerY the center vertical point around which to scale
     */
    private void scale(float newScale, float centerX, float centerY) {
        // Ensure that mMinScale <= newScale <= mMaxScale
        newScale = Math.max(newScale, mMinScale);
        newScale = Math.min(newScale, mMaxScale * SCALE_OVERZOOM_FACTOR);

        float currentScale = getScale();

        float factor = newScale / currentScale;

        // adjust centerX and centerY to make sure no empty space appear in crop area
        if (mAllowCrop && factor < 1 && mAllowCropOverscroll) {
            mTranslateRect.set(mTempSrc);
            mMatrix.mapRect(mTranslateRect);
            float minCenterX = mTranslateRect.right - (mTranslateRect.right - mCropRect.right) / (1 - factor);
            float maxCenterX = mTranslateRect.left + (mCropRect.left - mTranslateRect.left) / (1 - factor);
            float minCenterY = mTranslateRect.bottom - (mTranslateRect.bottom - mCropRect.bottom) / (1 - factor);
            float maxCenterY = mTranslateRect.top + (mCropRect.top - mTranslateRect.top) / (1 - factor);
            centerX = Math.max(minCenterX, Math.min(maxCenterX, centerX));
            centerY = Math.max(minCenterY, Math.min(maxCenterY, centerY));
        }

        // Apply the scale factor
        mMatrix.postScale(factor, factor, centerX, centerY);

        invalidate();
    }

    /**
     * Check scale value when scale gesture ends. If current scale is larger than {@link #mMaxScale},
     * image need to be zoomed back.
     * @return If {@code true}, means image need to be zoomed back.
     */
    private boolean checkZoomBack() {
        float currentScale = getScale();
        // Prepare to animate zoom out if over-zooming
        if (currentScale > mMaxScale) {
            Runnable zoomBackRunnable = new Runnable() {
                @Override
                public void run() {
                    // Scale back to the maximum if over-zoomed
                    float currentScale = getScale();
                    if (currentScale > mMaxScale) {
                        // The number of times the crop amount pulled in can fit on the screen
                        float marginFit = 1 / (1 - mMaxScale / currentScale);
                        // The (negative) relative maximum distance from an image edge such that
                        // when scaled this far from the edge, all of the image off-screen in that
                        // direction is pulled in
                        float relativeDistance = 1 - marginFit;
                        float finalCenterX = getWidth() / 2;
                        float finalCenterY = getHeight() / 2;
                        // This center will pull all of the margin from the lesser side, over will
                        // expose trim
                        float maxX = mTranslateRect.left * relativeDistance;
                        float maxY = mTranslateRect.top * relativeDistance;
                        // This center will pull all of the margin from the greater side, over will
                        // expose trim
                        float minX = getWidth() * marginFit + mTranslateRect.right *
                                relativeDistance;
                        float minY = getHeight() * marginFit + mTranslateRect.bottom *
                                relativeDistance;
                        // Adjust center according to bounds to avoid bad crop
                        if (minX > maxX) {
                            // Border is inevitable due to small image size, so we split the crop
                            finalCenterX = (minX + maxX) / 2;
                        } else {
                            finalCenterX = Math.min(Math.max(minX, finalCenterX), maxX);
                        }
                        if (minY > maxY) {
                            // Border is inevitable due to small image size, so we split the crop
                            finalCenterY = (minY + maxY) / 2;
                        } else {
                            finalCenterY = Math.min(Math.max(minY, finalCenterY), maxY);
                        }
                        mScaleRunnable.start(currentScale, mMaxScale, finalCenterX, finalCenterY);
                    }
                }
            };
            postDelayed(zoomBackRunnable, ZOOM_CORRECTION_DELAY);
            return true;
        }
        return false;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void onViewActivated() {
        if (mDrawable != null && sSupportGif && mDrawable instanceof GifDrawable) {
            GifDrawable drawable = (GifDrawable) mDrawable;
            if (!drawable.isAnimationCompleted() && !drawable.isPlaying() && !drawable.isRecycled()) {
                drawable.start();
            }
        }
    }

    public void onViewInactivated() {
        if (mDrawable != null && sSupportGif && mDrawable instanceof GifDrawable) {
            ((GifDrawable) mDrawable).pause();
        }
    }

    private static abstract class BaseImageRunnable implements Runnable {

        protected static final long NEVER = -1L;

        protected final PhotoView mView;

        protected long mLastRunTime;
        protected long mStartRunTime;

        protected boolean mRunning;
        protected boolean mStop;

        private BaseImageRunnable(PhotoView view) {
            mView = view;
        }

        public void stop() {
            mRunning = false;
            mStop = true;
        }
    }

    /**
     * Runnable that animates an image translation operation.
     */
    private static class TranslateRunnable extends BaseImageRunnable {

        private static final float DECELERATION_RATE = 20000f;

        private float mVelocityX, mVelocityY;
        private float mDecelerationX, mDecelerationY;

        private TranslateRunnable(PhotoView view) {
            super(view);
            mLastRunTime = NEVER;
        }

        /**
         * Starts the animation.
         */
        public boolean start(float velocityX, float velocityY) {
            if (mRunning) {
                return false;
            }
            mLastRunTime = NEVER;
            mVelocityX = velocityX;
            mVelocityY = velocityY;

            float angle = (float) Math.atan2(mVelocityY, mVelocityX);
            mDecelerationX = (float) (DECELERATION_RATE * Math.cos(angle));
            mDecelerationY = (float) (DECELERATION_RATE * Math.sin(angle));

            mStop = false;
            mRunning = true;
            mView.post(this);
            return true;
        }

        @Override
        public void run() {
            // See if we were told to stop:
            if (mStop) {
                return;
            }

            // Translate according to current velocities and time delta:
            long now = System.currentTimeMillis();
            float delta = (mLastRunTime != NEVER) ? (now - mLastRunTime) / 1000f : 0f;
            final int translateResult = mView.translate(mVelocityX * delta, mVelocityY * delta);
            mLastRunTime = now;
            // Slow down:
            float slowDownX = mDecelerationX * delta;
            if (Math.abs(mVelocityX) > Math.abs(slowDownX)) {
                mVelocityX -= slowDownX;
            } else {
                mVelocityX = 0f;
            }
            float slowDownY = mDecelerationY * delta;
            if (Math.abs(mVelocityY) > Math.abs(slowDownY)) {
                mVelocityY -= slowDownY;
            } else {
                mVelocityY = 0f;
            }

            // Stop when done
            if ((mVelocityX == 0f && mVelocityY == 0f) || translateResult == TRANSLATE_NONE) {
                stop();
                mView.snap();
            } else if (translateResult == TRANSLATE_X_ONLY) {
                mDecelerationX = (mVelocityX > 0) ? DECELERATION_RATE : -DECELERATION_RATE;
                mDecelerationY = 0f;
                mVelocityY = 0f;
            } else if (translateResult == TRANSLATE_Y_ONLY) {
                mDecelerationX = 0f;
                mVelocityX = 0f;
                mDecelerationY = (mVelocityY > 0) ? DECELERATION_RATE : -DECELERATION_RATE;
            }

            // See if we need to continue flinging:
            if (mStop) {
                return;
            }
            mView.post(this);
        }
    }

    /**
     * Runnable that animates an image translation operation.
     */
    private static class SnapRunnable extends BaseImageRunnable {

        private float mTranslateX, mTranslateY;

        private SnapRunnable(PhotoView view) {
            super(view);
            mStartRunTime = NEVER;
        }

        /**
         * Starts the animation
         */
        public boolean start(float translateX, float translateY) {
            if (mRunning) {
                return false;
            }
            mStartRunTime = NEVER;
            mTranslateX = translateX;
            mTranslateY = translateY;
            mStop = false;
            mRunning = true;
            mView.postDelayed(this, SNAP_DELAY);
            return true;
        }

        @Override
        public void run() {
            // See if we were told to stop:
            if (mStop) {
                return;
            }

            // Translate according to current velocities and time delta:
            long now = System.currentTimeMillis();
            float delta = (mStartRunTime != NEVER) ? (now - mStartRunTime) : 0f;

            if (mStartRunTime == NEVER) {
                mStartRunTime = now;
            }

            float transX, transY;
            if (delta >= SNAP_DURATION) {
                transX = mTranslateX;
                transY = mTranslateY;
            } else {
                transX = (mTranslateX / (SNAP_DURATION - delta)) * 10f;
                transY = (mTranslateY / (SNAP_DURATION - delta)) * 10f;
                if (Math.abs(transX) > Math.abs(mTranslateX) || Float.isNaN(transX)) {
                    transX = mTranslateX;
                }
                if (Math.abs(transY) > Math.abs(mTranslateY) || Float.isNaN(transY)) {
                    transY = mTranslateY;
                }
            }

            mView.translate(transX, transY);
            mTranslateX -= transX;
            mTranslateY -= transY;

            if (mTranslateX == 0 && mTranslateY == 0) {
                stop();
            }

            // See if we need to continue flinging:
            if (mStop) {
                return;
            }
            mView.post(this);
        }
    }

    /**
     * Runnable that animates an image scale operation.
     */
    private static class ScaleRunnable extends BaseImageRunnable {

        private float mCenterX, mCenterY;

        private boolean mZoomingIn;

        private float mTargetScale;
        private float mStartScale;
        private float mVelocity;

        private ScaleRunnable(PhotoView view) {
            super(view);
        }

        public boolean start(float startScale, float targetScale, float centerX, float centerY) {
            if (mRunning) {
                return false;
            }

            mCenterX = centerX;
            mCenterY = centerY;

            // Ensure the target scale is within the min/max bounds
            mTargetScale = targetScale;
            mStartRunTime = System.currentTimeMillis();
            mStartScale = startScale;
            mZoomingIn = mTargetScale > mStartScale;
            mVelocity = (mTargetScale - mStartScale) / ZOOM_ANIMATION_DURATION;
            mRunning = true;
            mStop = false;
            mView.post(this);
            return true;
        }

        @Override
        public void run() {
            if (mStop) {
                return;
            }

            // Scale
            long now = System.currentTimeMillis();
            long ellapsed = now - mStartRunTime;
            float newScale = (mStartScale + mVelocity * ellapsed);
            mView.scale(newScale, mCenterX, mCenterY);

            // Stop when done
            if (newScale == mTargetScale || (mZoomingIn == (newScale > mTargetScale))) {
                mView.scale(mTargetScale, mCenterX, mCenterY);
                stop();
            }

            if (!mStop) {
                mView.post(this);
            }
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || mDrawable == who;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        if (mDrawable == drawable) {
            invalidate();
        } else {
            super.invalidateDrawable(drawable);
        }
    }
}
