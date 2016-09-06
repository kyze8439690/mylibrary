package me.yugy.app.common.utils;

import android.support.annotation.Nullable;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("unused")
public class AESUtils {

    @Nullable
    public static byte[] generateKey(String password) {
        try {
            /* Store these things on disk used to derive key later: */
            int iterationCount = 1000;
            int keyLength = 256; // 256-bits for AES-256, 128-bits for AES-128, etc
            int saltLength = keyLength / 8; // bytes; should be the same size as the output (256 / 8 = 32)

            /* When first creating the key, obtain a salt with this: */
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[saltLength];
            random.nextBytes(salt);

            /* Use this to derive the key from the password: */
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                    iterationCount, keyLength);
            SecretKeyFactory keyFactory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static byte[] AES_CFB_ENCRYPT(byte[] source, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(
                    new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,});
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), iv);
            return cipher.doFinal(source);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static byte[] AES_CFB_DECRYPT(byte[] source, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(
                    new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,});
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), iv);
            return cipher.doFinal(source);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
