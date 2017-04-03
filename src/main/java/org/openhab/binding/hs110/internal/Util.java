package org.openhab.binding.hs110.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    private static Logger log = LoggerFactory.getLogger(Util.class);

    private static final int DECRYPTION_KEY = 0x2B;
    private static final int ENCRYPTION_KEY = DECRYPTION_KEY + 0x80;

    public static String decrypt(InputStream inputStream, boolean broadcast) throws IOException {

        int in;
        int key = DECRYPTION_KEY;
        int nextKey;
        StringBuilder sb = new StringBuilder();
        while ((in = inputStream.read()) != -1) {

            nextKey = in;
            in = in ^ key;
            key = nextKey;
            sb.append((char) in);
        }
        log.trace("Decrypted string with length: {}", sb.length());
        if (broadcast) {
            return "{" + sb.toString().substring(1, sb.length() - 1) + "}";
        } else {
            return "{" + sb.toString().substring(5, sb.length() - 1) + "}";
        }
    }

    public static int[] encrypt(String command) {

        int[] buffer = new int[command.length()];
        int key = 0xAB;
        for (int i = 0; i < command.length(); i++) {

            buffer[i] = command.charAt(i) ^ key;
            key = buffer[i];
        }
        return buffer;
    }

    public static byte[] encryptWithHeader(String command) {

        int[] data = encrypt(command);
        byte[] bufferHeader = ByteBuffer.allocate(4).putInt(command.length()).array();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferHeader.length + data.length).put(bufferHeader);
        for (int in : data) {

            byteBuffer.put((byte) in);
        }
        return byteBuffer.array();
    }

    public static byte[] encryptBytes(String command) {

        byte[] buffer = new byte[command.length()];
        byte key = (byte) ENCRYPTION_KEY;
        for (int i = 0; i < command.length(); i++) {

            buffer[i] = (byte) (command.charAt(i) ^ key);
            key = buffer[i];
        }
        return buffer;
    }

}
