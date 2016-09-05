package me.yugy.app.common.network;

import android.support.annotation.Nullable;

import com.android.volley.Response;

public abstract class RequestListener<T> implements Response.Listener<T>, Response.ErrorListener {
    @Override
    public void onResponse(T response) {
        onSuccess(response);
    }

    public abstract void onSuccess(@Nullable T response);
}
