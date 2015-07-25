package me.yugy.app.common.network;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GsonRequest<T> extends BaseRequest<T> {

    private static final Gson gson = new Gson();

    public GsonRequest(int method, String url, Param[] params,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
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
        //noinspection unchecked
        return (T) gson.fromJson(json, type);
    }
}
