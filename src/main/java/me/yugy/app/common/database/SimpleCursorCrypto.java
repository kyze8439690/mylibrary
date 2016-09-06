package me.yugy.app.common.database;

public class SimpleCursorCrypto implements CursorCrypto {
    @Override
    public String encrypt(String original) {
        return original;
    }

    @Override
    public String decrypt(String encrypted) {
        return encrypted;
    }
}
