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
package org.openhab.binding.matter.internal.util;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A utility class for encoding and decoding TLV (Type-Length-Value) data.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TlvCodec {

    private final Map<Integer, byte[]> tlvs;

    public TlvCodec() {
        this(new LinkedHashMap<>());
    }

    public TlvCodec(Map<Integer, byte[]> existingMap) {
        this.tlvs = existingMap;
    }

    /**
     * Get the bytes for a given TLV type.
     * 
     * @param type the TLV type
     * @return the bytes for the TLV type, or empty if the type is not present
     */
    public Optional<byte[]> getBytes(int type) {
        return Optional.ofNullable(tlvs.get(type));
    }

    /**
     * Get the unsigned 16-bit integer value for a given TLV type.
     * 
     * @param type the TLV type
     * @return the unsigned 16-bit integer value, or empty if the type is not present
     */
    public Optional<Integer> getUint16(int type) {
        return getBytes(type).map(b -> Short.toUnsignedInt(ByteBuffer.wrap(b).getShort()));
    }

    /**
     * Get the unsigned 32-bit integer value for a given TLV type.
     * 
     * @param type the TLV type
     * @return the unsigned 32-bit integer value, or empty if the type is not present
     */
    public Optional<Long> getUint32(int type) {
        return getBytes(type).map(b -> Integer.toUnsignedLong(ByteBuffer.wrap(b).getInt()));
    }

    /**
     * Get the unsigned 64-bit integer value for a given TLV type.
     * 
     * @param type the TLV type
     * @return the unsigned 64-bit integer value, or empty if the type is not present
     */
    public Optional<Long> getUint64(int type) {
        return getBytes(type).map(b -> ByteBuffer.wrap(b).getLong());
    }

    /**
     * Put the unsigned 16-bit integer value for a given TLV type.
     * 
     * @param type the TLV type
     * @param value the unsigned 16-bit integer value
     */
    public void putUint16(int type, int value) {
        tlvs.put(type, ByteBuffer.allocate(2).putShort((short) value).array());
    }

    /**
     * Put the unsigned 32-bit integer value for a given TLV type.
     * 
     * @param type the TLV type
     * @param value the unsigned 32-bit integer value
     */
    public void putUint32(int type, long value) {
        tlvs.put(type, ByteBuffer.allocate(4).putInt((int) value).array());
    }

    /**
     * Put the unsigned 64-bit integer value for a given TLV type.
     * 
     * @param type the TLV type
     * @param value the unsigned 64-bit integer value
     */
    public void putUint64(int type, long value) {
        tlvs.put(type, ByteBuffer.allocate(8).putLong(value).array());
    }

    /**
     * Put the bytes for a given TLV type.
     * 
     * @param type the TLV type
     * @param value the bytes
     */
    public void putBytes(int type, byte[] value) {
        tlvs.put(type, value.clone());
    }

    /**
     * Put the bytes for a given TLV type with an expected length.
     * 
     * @param type the TLV type
     * @param value the bytes
     * @param expectedLength the expected length of the bytes
     * @throws IllegalArgumentException if the length of the bytes is not equal to the expected length
     */
    public void putBytes(int type, byte[] value, int expectedLength) {
        if (value.length != expectedLength) {
            throw new IllegalArgumentException("TLV " + type + " expects " + expectedLength + " bytes");
        }
        putBytes(type, value);
    }

    /**
     * Get a read-only view of the underlying map.
     * 
     * @return a read-only view of the underlying map
     */
    public Map<Integer, byte[]> asMap() {
        return Collections.unmodifiableMap(tlvs);
    }

    /**
     * Strip the 0x prefix from a hex string.
     * 
     * @param hex the hex string
     * @return the hex string without the 0x prefix
     */
    public static String strip0x(String hex) {
        return hex.replaceFirst("(?i)^0x", "");
    }

    /**
     * Convert a hex string to a byte array.
     * 
     * @param hex the hex string
     * @return the byte array
     */
    public static byte[] hexStringToBytes(String hex) {
        hex = strip0x(hex);
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }

    /**
     * Convert a byte array to a hex string.
     * 
     * @param bytes the byte array
     * @return the hex string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
