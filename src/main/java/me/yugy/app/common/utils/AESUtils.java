package me.yugy.app.common.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

//    String key = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
//    byte[] encrypt = AESUtils.AES_CFB_ENCRYPT(
//            "fuckyoufuckyoufuckyoufuckyoufuckyoufuckyou".getBytes(),
//            key, new IvParameterSpec("b1d15254f0f0417d".getBytes()));
//    String decrypt = Base64.encodeToString(encrypt, Base64.DEFAULT);
//    DebugUtils.log(decrypt);

    public static byte[] AES_CFB_ENCRYPT(byte[] source, String key, IvParameterSpec ivParam) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), ivParam);
            return cipher.doFinal(source);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] AES_CFB_DECRYPT(byte[] source, String key, IvParameterSpec ivParam) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), ivParam);
            return cipher.doFinal(source);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
