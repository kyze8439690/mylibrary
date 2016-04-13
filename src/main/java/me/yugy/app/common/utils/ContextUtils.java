package me.yugy.app.common.utils;

import android.content.Context;
import android.content.Intent;

public class ContextUtils {

    /**
     * use this method to avoid crash when calling {@link Context#startService(Intent)} on Oppo devices.
     * @see <a href="http://bbs.coloros.com/thread-174655-1-1.html">http://bbs.coloros.com/thread-174655-1-1.html</a>
     */
    public static void startService(Context context, Intent intent) {
        try {
            context.startService(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
}
