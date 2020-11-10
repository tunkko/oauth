package org.tunkko.tools.codec;

/**
 * base64工具类
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class Base64Utils {

    private static final char[] ASCII = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    public static String encode(String string) {
        return encode(string.getBytes());
    }

    public static String encode(byte[] bytes) {
        char[] chars = new char[(bytes.length + 2) / 3 * 4];

        int index = 0;
        for (int i = 0; i < bytes.length; i += 3) {
            boolean quad = false;
            boolean trip = false;
            int val = 255 & bytes[i];

            val <<= 8;
            if (i + 1 < bytes.length) {
                val |= 255 & bytes[i + 1];
                trip = true;
            }
            val <<= 8;
            if (i + 2 < bytes.length) {
                val |= 255 & bytes[i + 2];
                quad = true;
            }

            chars[index + 3] = ASCII[quad ? val & 63 : 64];
            val >>= 6;
            chars[index + 2] = ASCII[trip ? val & 63 : 64];
            val >>= 6;
            chars[index + 1] = ASCII[val & 63];
            val >>= 6;
            chars[index] = ASCII[val & 63];
            index += 4;
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
        for (int i = 65; i <= 90; i++) {
            codes[i] = (byte) (i - 65);
        }
        for (int i = 97; i <= 122; i++) {
            codes[i] = (byte) (i - 71);
        }
        for (int i = 48; i <= 57; i++) {
            codes[i] = (byte) (4 + i);
        }
        codes[43] = 62;
        codes[47] = 63;

        int length = chars.length;
        for (char c : chars) {
            if (c > 255 || codes[c] < 0) {
                length--;
            }
        }

        int len = (length / 4) * 3;
        if (length % 4 == 3) {
            len += 2;
        }
        if (length % 4 == 2) {
            len++;
        }

        byte[] bytes = new byte[len];
        int shift = 0, accum = 0, index = 0;
        for (char c : chars) {
            int value = c > 255 ? -1 : codes[c];
            if (value >= 0) {
                accum <<= 6;
                shift += 6;
                accum |= value;
                if (shift >= 8) {
                    shift -= 8;
                    bytes[index++] = (byte) (accum >> shift & 255);
                }
            }
        }
        return bytes;
    }
}
