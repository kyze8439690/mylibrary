package me.yugy.app.common.network;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import me.yugy.app.common.utils.DebugUtils;

public abstract class BaseRequest<T> extends Request<T>{

    @Nullable private final Response.Listener<T> mListener;

    public BaseRequest(int method, String url, @Nullable Param[] params,
                       @Nullable Response.Listener<T> listener,
                       @Nullable Response.ErrorListener errorListener) {
        super(method, buildUrl(url, params), errorListener);
        mListener = listener;
    }

    public static String buildUrl(String url, @Nullable  Param... params) {
        if (params == null) {
            return url;
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Param param : params) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
        }
        return builder.toString();
    }

    @Override
    protected void deliverResponse(T response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        try {
            String response = new String(error.networkResponse.data);
            DebugUtils.log("DeliverError: " + response);
        } catch (NullPointerException ignored) {}
        super.deliverError(error);
    }
}
