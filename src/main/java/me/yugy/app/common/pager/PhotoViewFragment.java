package me.yugy.app.common.pager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import me.yugy.app.common.widget.PhotoView;

public class PhotoViewFragment<T extends Photo> extends Fragment implements OnScreenListener {

    public static final String ARG_IMAGE = "image";
    public static final String ARG_POSITION = "position";

    private PhotoView mPhotoView;
    private FrameLayout mOverlayContainer;

    private T mImage;
    private int mPosition;
    @Nullable private PhotoViewCallbacks mCallback;

    public int getPosition() {
        return mPosition;
    }

    public PhotoView getPhotoView() {
        return mPhotoView;
    }

    public FrameLayout getOverlayContainer() {
        return mOverlayContainer;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (PhotoViewCallbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() + " should implement "
                    + PhotoViewCallbacks.class.getName() + " interface.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImage = getArguments().getParcelable(ARG_IMAGE);
        mPosition = getArguments().getInt(ARG_POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(getContext());
        mPhotoView = new PhotoView(getContext());
        frameLayout.addView(mPhotoView);
        mOverlayContainer = new FrameLayout(getContext());
        frameLayout.addView(mOverlayContainer);
        return frameLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mCallback != null) {
            mCallback.onRequestDisplay(mImage, mPhotoView);
        }
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.toggleFullScreen();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.addScreenListener(mPosition, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCallback != null) {
            mCallback.removeScreenListener(mPosition);
        }
    }

    @Override
    public void onDestroyView() {
        mPhotoView.clear();
        super.onDestroyView();
    }

    @Override
    public void onFullScreenChanged(boolean fullScreen) {

    }

    @Override
    public void onViewActivated() {
        mPhotoView.onViewActivated();
    }

    @Override
    public void onViewInactivated() {
        mPhotoView.onViewInactivated();
    }

    @Override
    public boolean onInterceptMoveLeft(float origX, float origY) {
        if (mCallback == null || !mCallback.isFragmentActive(mPosition)) {
            // we're not in the foreground; don't intercept any touches
            return false;
        }

        return (mPhotoView != null && mPhotoView.interceptMoveLeft());
    }

    @Override
    public boolean onInterceptMoveRight(float origX, float origY) {
        if (mCallback == null || !mCallback.isFragmentActive(mPosition)) {
            // we're not in the foreground; don't intercept any touches
            return false;
        }

        return (mPhotoView != null && mPhotoView.interceptMoveRight());
    }
}
