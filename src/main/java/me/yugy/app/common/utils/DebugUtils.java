package me.yugy.app.common.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by yugy on 14-1-29. See <a href="https://github.com/MustafaFerhan/DebugLog">https://github.com/MustafaFerhan/DebugLog</a>
 */
public class DebugUtils {

    private static boolean DEBUG = false;

    static String className;
    static String methodName;
    static int lineNumber;

    private static String createLog( String log ) {
        return "[" + methodName + ":" + lineNumber + "]" + log;
    }

    private static void getMethodNames(StackTraceElement[] sElements){
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void log(Object log){
        if(DEBUG){
            getMethodNames(new Throwable().getStackTrace());

            if(log == null){ //log null
                Log.e(className, createLog("log content is null"));
            }else if(log instanceof Integer || log instanceof Float || log instanceof Double){ //log num
                Log.d(className, createLog(String.valueOf(log)));
            }else if(log instanceof String){ //log string
                Log.d(className, createLog((String) log));
            }else if(log instanceof JSONObject || log instanceof JSONArray){ //log json
                Log.d(className, createLog(log.toString()));
            }else if(log instanceof byte[]){ //log byte array
                Log.d(className, createLog(new String((byte[]) log)));
            } else {
                Log.d(className, createLog(log.toString()));
            }
        }
    }

    public static void setLogEnable(boolean DEBUG) {
        DebugUtils.DEBUG = DEBUG;
    }
}
