package me.yugy.app.common.network;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public class GsonHelper {

    private static Gson sInstance;

    public GsonHelper() {
        if (sInstance == null) {
            synchronized (GsonHelper.class) {
                if (sInstance == null) {
                    sInstance = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                }
            }
        }
    }

    @Nullable
    public <T> T loads(String json, Class<T> clazz) {
        try {
            return sInstance.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public <T> T loads(String json, Type type) {
        try {
            return sInstance.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String dump(Object src) {
        return sInstance.toJson(src);
    }
}
