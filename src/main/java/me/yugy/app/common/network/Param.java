package me.yugy.app.common.network;

public class Param {

    private String mKey;
    private String mValue;

    public Param(String key, String value) {
        mKey = key;
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }
}
