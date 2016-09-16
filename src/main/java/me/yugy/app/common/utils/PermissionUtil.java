package me.yugy.app.common.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import java.util.Map;

import me.yugy.app.common.activity.RequestPermissionActivity;

public class PermissionUtil {

    public static void request(Activity activity, final String permission, final SinglePermissionCallback listener,
                               @StringRes int requestRationaleId, @StringRes int okTextId, @StringRes int cancelTextId) {
        request(activity, permission, listener, activity.getString(requestRationaleId), activity.getString(okTextId),
                activity.getString(cancelTextId));
    }

    public static void request(Activity activity, final String permission, final SinglePermissionCallback listener,
                               @NonNull String requestRationale, @Nullable String okText, @Nullable String cancelText) {
        request(activity, new String[]{permission}, new PermissionCallback() {
            @Override
            public void onCancel() {
                if (listener != null) listener.onCancel();
            }
            @Override
            public void onResult(Map<String, Integer> result) {
                Integer resultCode = result.get(permission);
                if (resultCode == null || resultCode == PackageManager.PERMISSION_DENIED) {
                    if (listener != null) listener.onDenied();
                } else {
                    if (listener != null) listener.onGranted();
                }
            }
        }, requestRationale, okText, cancelText);
    }

    public static void request(Activity activity, String[] permissions, final PermissionCallback listener,
                               @StringRes int requestRationaleId, @StringRes int okTextId, @StringRes int cancelTextId) {
        request(activity, permissions, listener, activity.getString(requestRationaleId), activity.getString(okTextId),
                activity.getString(cancelTextId));
    }

    public static void request(final Activity activity, final String[] permissions, final PermissionCallback listener,
                               @NonNull String requestRationale, @Nullable String okText, @Nullable String cancelText) {
        boolean haveShowRationaleAndRequest = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                new AlertDialog.Builder(activity)
                        .setMessage(requestRationale)
                        .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestPermissionActivity.launch(activity, permissions, listener);
                            }
                        })
                        .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onCancel();
                            }
                        }).show();
                haveShowRationaleAndRequest = true;
                break;
            }
        }
        if (!haveShowRationaleAndRequest) {
            RequestPermissionActivity.launch(activity, permissions, listener);
        }
    }

    public interface PermissionCallback extends RequestPermissionActivity.PermissionsResultListener {
        void onCancel();
    }

    public interface SinglePermissionCallback {
        void onGranted();
        void onDenied();
        void onCancel();
    }

    public static abstract class SimplePermissionCallback implements SinglePermissionCallback {
        public abstract void onSuccess();
        public abstract void onFailure();
        @Override
        public void onGranted() {
            onSuccess();
        }
        @Override
        public void onDenied() {
            onFailure();
        }
        @Override
        public void onCancel() {
            onFailure();
        }
    }

}
