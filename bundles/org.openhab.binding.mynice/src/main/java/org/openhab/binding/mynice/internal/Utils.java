package org.openhab.binding.mynice.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] invertArray(byte[] data) {
        byte[] result = new byte[data.length];
        int i = data.length - 1;
        int c = 0;
        while (i >= 0) {
            int c2 = c + 1;
            result[c] = data[i];
            i--;
            c = c2;
        }
        return result;
    }

    public static String intToHexString(int value) {
        return String.format("%08x", new Object[] { Integer.valueOf(value) }).toUpperCase();
    }

    public static byte[] sha256(byte[]... values) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (byte[] data : values) {
            digest.update(data);
        }
        return digest.digest();
    }
}
