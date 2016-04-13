package me.yugy.app.common.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;

import butterknife.ButterKnife;
import me.yugy.app.common.network.RequestManager;

@SuppressWarnings("unused")
public class BaseFragment extends Fragment {

    private boolean mIsFirstResume = false;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getLayoutId() == 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        } else {
            View rootView = inflater.inflate(getLayoutId(), container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }
    }

    public int getLayoutId() {
        return 0;
    }

    public void addRequest(Request request) {
        addRequest(request, this);
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
        RequestManager.getInstance(getActivity()).cancelAll(this);
        super.onDestroy();
    }
}
