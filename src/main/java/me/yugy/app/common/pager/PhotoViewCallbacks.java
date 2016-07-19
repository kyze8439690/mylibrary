package me.yugy.app.common.pager;

import me.yugy.app.common.widget.PhotoView;

public interface PhotoViewCallbacks<T extends Photo> {

    void addScreenListener(int position, OnScreenListener listener);

    void removeScreenListener(int position);

    boolean isFragmentActive(int position);

    void toggleFullScreen();

    void onRequestDisplay(T image, PhotoView photoView);
}
