package org.tunkko.tools.codec;

/**
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class Base64Utils {

    private static final char[] DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    public static String encode(String string) {
        return encode(string.getBytes());
    }

    public static String encode(byte[] bytes) {
        char[] chars = new char[((bytes.length + 2) / 3) * 4];
        for (int i = 0, index = 0; i < bytes.length; i += 3, index += 4) {
            boolean quad = false, trip = false;

            int val = (0xFF & (int) bytes[i]);
            val <<= 8;
            if ((i + 1) < bytes.length) {
                val |= (0xFF & (int) bytes[i + 1]);
                trip = true;
            }
            val <<= 8;
            if ((i + 2) < bytes.length) {
                val |= (0xFF & (int) bytes[i + 2]);
                quad = true;
            }
            chars[index + 3] = DIGITS[(quad ? (val & 0x3F) : 64)];
            val >>= 6;
            chars[index + 2] = DIGITS[(trip ? (val & 0x3F) : 64)];
            val >>= 6;
            chars[index + 1] = DIGITS[val & 0x3F];
            val >>= 6;
            chars[index] = DIGITS[val & 0x3F];
        }
        return new String(chars);
    }

    public static String decode(String string) {
        return new String(decode0(string));
    }

    public static byte[] decode0(String string) {
        char[] chars = string.toCharArray();

        byte[] codes = new byte[256];
        for (int i = 0; i < 256; i++) {
            codes[i] = -1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            codes[i] = (byte) (i - 'A');
        }
        for (int i = 'a'; i <= 'z'; i++) {
            codes[i] = (byte) (26 + i - 'a');
        }
        for (int i = '0'; i <= '9'; i++) {
            codes[i] = (byte) (52 + i - '0');
        }
        codes['+'] = 62;
        codes['/'] = 63;

        int length = chars.length;
        for (char c : chars) {
            if ((c > 255) || codes[c] < 0) {
                --length;
            }
        }

        int len = (length / 4) * 3;
        if ((length % 4) == 3) {
            len += 2;
        }
        if ((length % 4) == 2) {
            len += 1;

        }

        byte[] bytes = new byte[len];
        int shift = 0, accum = 0, index = 0;
        for (char c : chars) {
            int value = (c > 255) ? -1 : codes[c];
            if (value >= 0) {
                accum <<= 6;
                shift += 6;
                accum |= value;
                if (shift >= 8) {
                    shift -= 8;
                    bytes[index++] = (byte) ((accum >> shift) & 0xff);
                }
            }
        }
        return bytes;
    }
}
