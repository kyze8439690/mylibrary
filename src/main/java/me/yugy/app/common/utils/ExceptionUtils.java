package me.yugy.app.common.utils;

import com.android.volley.VolleyError;

import java.net.BindException;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;

import javax.net.ssl.SSLException;

public class ExceptionUtils {

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isNetworkException(Exception e) {
        if (e == null) {
            return false;
        }
        if (       e instanceof BindException
                || e instanceof ConnectException
                || e instanceof HttpRetryException
                || e instanceof MalformedURLException
                || e instanceof NoRouteToHostException
                || e instanceof PortUnreachableException
                || e instanceof ProtocolException
                || e instanceof SocketException
                || e instanceof SocketTimeoutException
                || e instanceof SSLException
                || e instanceof UnknownHostException
                || e instanceof UnknownServiceException
                || e instanceof URISyntaxException
                || e instanceof VolleyError
                ) {
            return true;
        } else {
            return false;
        }
    }
}
