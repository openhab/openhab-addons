/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hs110.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Util} en- and decrypts data to be sent over the network.
 *
 * Contains Source from
 * agent4788 https://gist.github.com/agent4788/81beb25cdcdbf7e9371361ca87d3b04a
 * Insxnity https://github.com/Insxnity/hs-100
 *
 */
public class Util {
    private static Logger logger = LoggerFactory.getLogger(Util.class);

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
        logger.trace("Decrypted string with length: {}", sb.length());
        if (broadcast) {
            return "{" + sb.toString().substring(1, sb.length() - 1) + "}";
        } else {
            return "{" + sb.toString().substring(5, sb.length() - 1) + "}";
        }
    }

    public static int[] encrypt(String command) {

        int[] buffer = new int[command.length()];
        int key = ENCRYPTION_KEY;
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
