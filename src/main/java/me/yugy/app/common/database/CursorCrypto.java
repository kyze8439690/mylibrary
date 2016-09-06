package me.yugy.app.common.database;

public interface CursorCrypto {

    String encrypt(String original);
    String decrypt(String encrypted);

}
