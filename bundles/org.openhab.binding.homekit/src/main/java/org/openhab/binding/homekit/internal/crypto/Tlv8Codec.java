/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homekit.internal.crypto;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for encoding and decoding TLV8 (Type-Length-Value) data.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Tlv8Codec {

    public static final int MAX_TLV_LENGTH = 255;

    /**
     * Encodes a map of TLV8 key-value pairs into a byte array.
     */
    public static byte[] encode(Map<Integer, byte[]> tlvMap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (Map.Entry<Integer, byte[]> entry : tlvMap.entrySet()) {
            int type = entry.getKey();
            byte[] value = entry.getValue();

            int offset = 0;
            while (offset < value.length) {
                int chunkLength = Math.min(MAX_TLV_LENGTH, value.length - offset);
                out.write(type);
                out.write(chunkLength);
                out.write(value, offset, chunkLength);
                offset += chunkLength;
            }
        }

        return out.toByteArray();
    }

    /**
     * Decodes a TLV8 byte array into a map of key-value pairs.
     */
    public static Map<Integer, byte[]> decode(byte[] data) {
        Map<Integer, ByteArrayOutputStream> tempMap = new LinkedHashMap<>();
        int index = 0;

        while (index + 2 <= data.length) {
            int type = data[index++] & 0xFF;
            int length = data[index++] & 0xFF;

            if (index + length > data.length) {
                throw new IllegalArgumentException("Invalid TLV8 length");
            }

            byte[] chunk = Arrays.copyOfRange(data, index, index + length);
            index += length;

            ByteArrayOutputStream stream = tempMap.computeIfAbsent(type, k -> new ByteArrayOutputStream());
            if (stream != null) {
                stream.writeBytes(chunk);
            }
        }

        Map<Integer, byte[]> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, ByteArrayOutputStream> entry : tempMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toByteArray());
        }

        return result;
    }

    /**
     * Convenience method to encode a single TLV8 pair.
     */
    public static byte[] encode(int type, byte[] value) {
        return encode(Map.of(type, value));
    }
}
