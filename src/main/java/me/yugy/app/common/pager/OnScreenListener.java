package me.yugy.app.common.pager;

public interface OnScreenListener {
    /**
     * The full screen state has changed.
     */
    void onFullScreenChanged(boolean fullScreen);

    /**
     * A new view has been activated and the previous view de-activated.
     */
    void onViewActivated();

    /**
     * This view is a candidate for being the next view.
     *
     * This will be called when the view is focused completely on the view immediately before
     * or after this one, so that this view can reset itself if nessecary.
     */
    void onViewInactivated();

    /**
     * Called when a right-to-left touch move intercept is about to occur.
     *
     * @param origX the raw x coordinate of the initial touch
     * @param origY the raw y coordinate of the initial touch
     * @return {@code true} if the touch should be intercepted.
     */
    boolean onInterceptMoveLeft(float origX, float origY);

    /**
     * Called when a left-to-right touch move intercept is about to occur.
     *
     * @param origX the raw x coordinate of the initial touch
     * @param origY the raw y coordinate of the initial touch
     * @return {@code true} if the touch should be intercepted.
     */
    boolean onInterceptMoveRight(float origX, float origY);
}
