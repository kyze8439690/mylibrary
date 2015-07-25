package me.yugy.app.common.network;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public abstract class BaseRequest<T> extends Request<T>{

    private final Response.Listener<T> mListener;

    public BaseRequest(int method, String url, Param[] params, Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        super(method, buildUrl(url, params), errorListener);
        mListener = listener;
    }

    private static String buildUrl(String url, Param... params) {
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
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        try {
            String response = new String(error.networkResponse.data);
            Log.e("Volley", response);
        } catch (NullPointerException ignored) {}
        super.deliverError(error);
    }
}
