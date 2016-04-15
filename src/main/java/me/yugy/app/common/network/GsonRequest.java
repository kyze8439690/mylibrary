package me.yugy.app.common.network;

import android.support.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class GsonRequest<T extends BaseResponse> extends BaseRequest<T> {

    public GsonRequest(int method, String url, @Nullable Param[] params,
                       @Nullable Response.Listener<T> listener,
                       @Nullable Response.ErrorListener errorListener) {
        super(method, url, params, listener, errorListener);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            try {
                return Response.success(parseJson(json),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (ClassCastException e) {
                throw new ClassCastException("Response data class should extends BaseResponse");
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    public T parseJson(String json) {
        Type type = ((ParameterizedType) ((Object)this).getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return new GsonHelper().loads(json, type);
    }
}
