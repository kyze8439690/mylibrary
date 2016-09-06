package me.yugy.app.common.database;

import android.support.annotation.Nullable;
import android.util.Base64;

import me.yugy.app.common.utils.AESUtils;

public class AESCursorCrypto implements CursorCrypto {

    private byte[] mKey;

    public AESCursorCrypto(byte[] key) {
        mKey = key;
    }

    @Override
    public String encrypt(String original) {
        byte[] encrypted = AESUtils.AES_CFB_ENCRYPT(original.getBytes(), mKey);
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    @Nullable
    @Override
    public String decrypt(String encrypted) {
        byte[] source = Base64.decode(encrypted, Base64.DEFAULT);
        byte[] decrypted = AESUtils.AES_CFB_DECRYPT(source, mKey);
        return decrypted != null ? new String(decrypted) : null;
    }
}
