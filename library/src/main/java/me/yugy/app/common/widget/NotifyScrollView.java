package me.yugy.app.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

@SuppressWarnings("unused")
public class NotifyScrollView extends ScrollView{

    private static final String LOG_TAG = NotifyScrollView.class.getName();

    private OnScrollListener mOnScrollListener;

    public NotifyScrollView(Context context) {
        this(context, null);
    }

    public NotifyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotifyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        Log.d(LOG_TAG, l + ", " + t + ", " + oldl + ", " + oldt);

        if(t <= 0 || oldt <= 0 || mOnScrollListener == null){
            return;
        }

        if(t > oldt){
            mOnScrollListener.onScroll(OnScrollListener.DIRECTION_UP, l, t, oldl, oldt);
        }else if(t < oldt){
            mOnScrollListener.onScroll(OnScrollListener.DIRECTION_DOWN, l, t, oldl, oldt);
        }
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    public interface OnScrollListener{

        int DIRECTION_UP = 0;
        int DIRECTION_DOWN = 1;

        void onScroll(int direction, int l, int t, int oldl, int oldt);
    }
}
