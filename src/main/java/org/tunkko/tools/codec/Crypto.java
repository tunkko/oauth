package org.tunkko.tools.codec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 对称算法加解密类
 *
 * @author tunkko
 */
public class Crypto {

    public static String encrypt(String string, String salt) {
        return encrypt(Algorithm.AES, string, salt);
    }

    public static String encrypt(Algorithm algorithm, String string, String salt) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.cipher);
            byte[] bytes = string.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, getKey(algorithm.name, salt));
            byte[] result = cipher.doFinal(bytes);
            return Base64.encode(result);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String string, String salt) {
        return decrypt(Algorithm.AES, string, salt);
    }

    public static String decrypt(Algorithm algorithm, String string, String salt) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.cipher);
            cipher.init(Cipher.DECRYPT_MODE, getKey(algorithm.name, salt));
            byte[] bytes = cipher.doFinal(Base64.decode0(string));
            return new String(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static SecretKeySpec getKey(String algorithm, String salt) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(algorithm);
            generator.init(new SecureRandom(salt.getBytes()));
            SecretKey secretKey = generator.generateKey();
            return new SecretKeySpec(secretKey.getEncoded(), algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public enum Algorithm {

        /**
         * 算法
         */
        AES("AES", "AES/ECB/PKCS5Padding"),
        DES("DES", "DES"),
        DES3("DESede", "DESede/ECB/PKCS5Padding");

        private String name;
        private String cipher;

        Algorithm(String name, String cipher) {
            this.name = name;
            this.cipher = cipher;
        }
    }
}
