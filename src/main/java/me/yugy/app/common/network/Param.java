package me.yugy.app.common.network;

@SuppressWarnings("unused")
public class Param {

    private String mKey;
    private String mValue;

    public Param(String key, String value) {
        mKey = key;
        mValue = value;
    }

    public Param(String key, int value) {
        mKey = key;
        mValue = String.valueOf(value);
    }

    public Param(String key, long value) {
        mKey = key;
        mValue = String.valueOf(value);
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
