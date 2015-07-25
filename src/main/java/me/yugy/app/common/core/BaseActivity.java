package me.yugy.app.common.core;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;

import me.yugy.app.common.network.RequestManager;

public class BaseActivity extends AppCompatActivity {

    private boolean mIsFirstResume = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsFirstResume) {
            mIsFirstResume = true;
        } else {
            onRealResume();
        }
    }

    protected void onRealResume() {

    }

    public Activity getActivity() {
        return this;
    }

    public void addRequest(Request request) {
        addRequest(request, this);
    }

    public void addRequest(Request request, Object tag) {
        request.setTag(tag);
        RequestManager.getInstance(this).addRequest(request);
    }

    public void cancelRequest(Request<?> request) {
        RequestManager.getInstance(this).cancel(request);
    }

    @Override
    protected void onDestroy() {
        RequestManager.getInstance(this).cancelAll(this);
        super.onDestroy();
    }
}
