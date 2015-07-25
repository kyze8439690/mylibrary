package me.yugy.app.common.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestManager {

    private static RequestManager sInstance;

    public static RequestManager getInstance(Context context) {
            if (sInstance == null) {
                synchronized (RequestManager.class) {
                    if (sInstance == null) {
                        sInstance = new RequestManager(context);
                    }
                }
            }
            return sInstance;
    }

    private RequestQueue mRequestQueue;

    private RequestManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public void addRequest(Request request) {
        mRequestQueue.add(request);
    }

    public void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    public void cancel(final Request<?> request) {
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> r) {
                return r.equals(request);
            }
        });
    }
}
