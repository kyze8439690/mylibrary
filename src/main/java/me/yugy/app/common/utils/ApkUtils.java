package me.yugy.app.common.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by yugy on 2014/7/4.
 */
public class ApkUtils {

    public static void installApplication(Context context, String apkPath){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void uninstallApplication(Context context, String pacakgeName){
        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", pacakgeName, null));
        context.startActivity(intent);
    }

}
