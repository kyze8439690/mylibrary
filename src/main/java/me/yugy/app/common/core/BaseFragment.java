package me.yugy.app.common.core;

import android.support.v4.app.Fragment;

import com.android.volley.Request;

import me.yugy.app.common.network.RequestManager;

public class BaseFragment extends Fragment {

    private boolean mIsFirstResume = false;
    private Object mTag;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = new Object();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsFirstResume) {
            mIsFirstResume = true;
        } else {
            onRealResume();
        }
    }

    protected void onRealResume() {

    }

    public void addRequest(Request request) {
        addRequest(request, mTag);
    }

    public void addRequest(Request request, Object tag) {
        request.setTag(tag);
        RequestManager.getInstance(getActivity()).addRequest(request);
    }

    public void cancelRequest(Request<?> request) {
        RequestManager.getInstance(getActivity()).cancel(request);
    }

    @Override
    public void onDestroy() {
        RequestManager.getInstance(getActivity()).cancelAll(mTag);
        super.onDestroy();
    }
}
